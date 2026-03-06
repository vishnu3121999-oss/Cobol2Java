from __future__ import annotations

import argparse
import os
import sys
from pathlib import Path

from cobol_to_java.graph import convert_cobol, convert_cobol_project_to_springboot
from dotenv import load_dotenv


def _load_cobol_text(args: argparse.Namespace) -> str:
    if args.input_file:
        return Path(args.input_file).read_text(encoding="utf-8")
    if args.cobol:
        return args.cobol
    raise ValueError("Provide COBOL code with --cobol or --input-file.")


def _load_cobol_project(input_dir: str) -> dict[str, str]:
    root = Path(input_dir)
    if not root.exists() or not root.is_dir():
        raise ValueError(f"Input directory not found: {input_dir}")

    allowed_suffixes = {".cbl", ".cob", ".cpy", ".copybook", ".txt"}
    sources: dict[str, str] = {}
    for file_path in root.rglob("*"):
        if file_path.is_file() and file_path.suffix.lower() in allowed_suffixes:
            rel_path = file_path.relative_to(root).as_posix()
            sources[rel_path] = file_path.read_text(encoding="utf-8")

    if not sources:
        raise ValueError("No COBOL project files found (.cbl/.cob/.cpy/.copybook/.txt).")
    return sources


def _write_generated_project(files: dict[str, str], output_dir: str) -> None:
    root = Path(output_dir)
    root.mkdir(parents=True, exist_ok=True)
    for rel_path, content in files.items():
        normalized = Path(rel_path.replace("\\", "/"))
        if normalized.is_absolute() or ".." in normalized.parts:
            continue
        target = root / normalized
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")


def _validate_env() -> None:
    if not os.getenv("GOOGLE_API_KEY"):
        raise EnvironmentError(
            "GOOGLE_API_KEY is not set. Add it to your environment or a .env loader."
        )


def _build_model_candidates(primary_model: str) -> list[str]:
    candidates = [
        primary_model,
        "gemini-2.5-flash-lite",
        "gemini-flash-lite-latest",
        "gemini-2.5-flash",
        "gemini-flash-latest",
    ]
    deduped: list[str] = []
    for model in candidates:
        if model and model not in deduped:
            deduped.append(model)
    return deduped


def _is_retryable_model_error(error_text: str) -> bool:
    checks = [
        "RESOURCE_EXHAUSTED",
        "429",
        "NOT_FOUND",
        "404",
        "no longer available",
    ]
    upper = error_text.upper()
    return any(check.upper() in upper for check in checks)


def main() -> None:
    load_dotenv()
    parser = argparse.ArgumentParser(description="Convert COBOL to Java or Spring Boot using LangGraph.")
    parser.add_argument("--target", choices=["java", "springboot"], default="java", help="Conversion target.")
    parser.add_argument("--cobol", type=str, help="COBOL code as a string.")
    parser.add_argument("--input-file", type=str, help="Path to a COBOL source file.")
    parser.add_argument("--input-dir", type=str, help="Path to a multi-file COBOL project directory.")
    parser.add_argument("--output-file", type=str, help="Path to write Java output.")
    parser.add_argument("--output-dir", type=str, default="springboot-out", help="Directory for generated Spring Boot project.")
    parser.add_argument(
        "--model",
        type=str,
        default=os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite"),
        help="Gemini model name.",
    )
    parser.add_argument("--temperature", type=float, default=0.1, help="Model temperature.")
    parser.add_argument(
        "--skip-review",
        action="store_true",
        help="Run only conversion step (single model call, lower quota usage).",
    )
    args = parser.parse_args()

    try:
        _validate_env()
        output_text = ""
        output_project_files: dict[str, str] = {}
        last_exc: Exception | None = None
        selected_model = args.model
        for model_name in _build_model_candidates(args.model):
            selected_model = model_name
            try:
                if args.target == "springboot":
                    if args.input_dir:
                        cobol_sources = _load_cobol_project(args.input_dir)
                    else:
                        cobol_text = _load_cobol_text(args)
                        cobol_sources = {"main.cbl": cobol_text}
                    output_project_files = convert_cobol_project_to_springboot(
                        cobol_files=cobol_sources,
                        model_name=model_name,
                        temperature=args.temperature,
                        include_review=not args.skip_review,
                    )
                else:
                    cobol_code = _load_cobol_text(args)
                    output_text = convert_cobol(
                        cobol_code=cobol_code,
                        model_name=model_name,
                        temperature=args.temperature,
                        include_review=not args.skip_review,
                    )
                break
            except Exception as exc:
                last_exc = exc
                if _is_retryable_model_error(str(exc)):
                    continue
                raise
        else:
            if last_exc:
                raise last_exc

        if args.target == "springboot":
            _write_generated_project(output_project_files, args.output_dir)
            print(
                f"Spring Boot project written to {args.output_dir} "
                f"(files: {len(output_project_files)}, model: {selected_model})"
            )
        else:
            if args.output_file:
                Path(args.output_file).write_text(output_text, encoding="utf-8")
                print(f"Java code written to {args.output_file} (model: {selected_model})")
            else:
                print(output_text)
    except Exception as exc:
        error_text = str(exc)
        if "RESOURCE_EXHAUSTED" in error_text or "429" in error_text:
            print(
                "Error: Quota exceeded for the selected Gemini model/key.\n"
                "Try:\n"
                "1) python main.py --input-file .\\sample.cbl --output-file .\\Converted.java --model gemini-2.5-flash-lite --skip-review\n"
                "2) python main.py --target springboot --input-dir .\\sample_cobol_crud_mongo --output-dir .\\springboot-out --model gemini-2.5-flash-lite --skip-review\n"
                "2) Increase quota/billing for your GCP project key\n"
                "3) Use a different API key/project with available quota",
                file=sys.stderr,
            )
        elif "NOT_FOUND" in error_text or "404" in error_text:
            print(
                "Error: The selected model is unavailable for your project/key.\n"
                "Try:\n"
                "1) python main.py --input-file .\\sample.cbl --output-file .\\Converted.java --model gemini-2.5-flash-lite --skip-review\n"
                "2) Update GEMINI_MODEL in .env to gemini-2.5-flash-lite",
                file=sys.stderr,
            )
        else:
            print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
