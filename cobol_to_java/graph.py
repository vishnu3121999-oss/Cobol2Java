from __future__ import annotations

import re
from typing import TypedDict

from langchain_core.messages import HumanMessage, SystemMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langgraph.graph import END, START, StateGraph


class ConversionState(TypedDict):
    cobol_code: str
    java_code: str
    reviewed_java_code: str


class ProjectConversionState(TypedDict):
    project_sources: str
    generated_project_text: str
    reviewed_project_text: str


def _get_model(model_name: str, temperature: float) -> ChatGoogleGenerativeAI:
    return ChatGoogleGenerativeAI(
        model=model_name,
        temperature=temperature,
    )


def _convert_node(state: ConversionState, model_name: str, temperature: float) -> ConversionState:
    llm = _get_model(model_name=model_name, temperature=temperature)
    messages = [
        SystemMessage(
            content=(
                "You are a senior modernization engineer. Convert COBOL to idiomatic Java. "
                "Preserve business logic, data handling, and control flow. "
                "If assumptions are required, include them in code comments. "
                "Return only Java code."
            )
        ),
        HumanMessage(
            content=(
                "Convert this COBOL code to Java:\n\n"
                f"{state['cobol_code']}"
            )
        ),
    ]
    response = llm.invoke(messages)
    return {
        **state,
        "java_code": response.content if isinstance(response.content, str) else str(response.content),
    }


def _review_node(state: ConversionState, model_name: str, temperature: float) -> ConversionState:
    llm = _get_model(model_name=model_name, temperature=temperature)
    messages = [
        SystemMessage(
            content=(
                "You are a Java code reviewer for legacy migration projects. "
                "Improve correctness, naming, null-safety, and readability while preserving behavior. "
                "Return only final Java code."
            )
        ),
        HumanMessage(
            content=(
                "Review and improve this Java code generated from COBOL:\n\n"
                f"{state['java_code']}"
            )
        ),
    ]
    response = llm.invoke(messages)
    return {
        **state,
        "reviewed_java_code": response.content if isinstance(response.content, str) else str(response.content),
    }


def _finalize_without_review(state: ConversionState) -> ConversionState:
    return {
        **state,
        "reviewed_java_code": state["java_code"],
    }


def build_graph(model_name: str = "gemini-2.5-flash-lite", temperature: float = 0.1, include_review: bool = True):
    workflow = StateGraph(ConversionState)
    workflow.add_node(
        "convert_cobol_to_java",
        lambda state: _convert_node(state, model_name=model_name, temperature=temperature),
    )
    if include_review:
        workflow.add_node(
            "review_java",
            lambda state: _review_node(state, model_name=model_name, temperature=temperature),
        )
    else:
        workflow.add_node("finalize", _finalize_without_review)
    workflow.add_edge(START, "convert_cobol_to_java")
    if include_review:
        workflow.add_edge("convert_cobol_to_java", "review_java")
        workflow.add_edge("review_java", END)
    else:
        workflow.add_edge("convert_cobol_to_java", "finalize")
        workflow.add_edge("finalize", END)
    return workflow.compile()


def convert_cobol(
    cobol_code: str,
    model_name: str = "gemini-2.5-flash-lite",
    temperature: float = 0.1,
    include_review: bool = True,
) -> str:
    app = build_graph(model_name=model_name, temperature=temperature, include_review=include_review)
    result = app.invoke(
        {
            "cobol_code": cobol_code,
            "java_code": "",
            "reviewed_java_code": "",
        }
    )
    return result["reviewed_java_code"]


def _format_sources_for_prompt(cobol_files: dict[str, str]) -> str:
    parts: list[str] = []
    for path in sorted(cobol_files.keys()):
        parts.append(f"### FILE: {path}\n{cobol_files[path]}")
    return "\n\n".join(parts)


def _convert_project_node(
    state: ProjectConversionState,
    model_name: str,
    temperature: float,
) -> ProjectConversionState:
    llm = _get_model(model_name=model_name, temperature=temperature)
    messages = [
        SystemMessage(
            content=(
                "You are a principal modernization architect. Convert a COBOL multi-file CRUD system to a Spring Boot project "
                "with MongoDB. Generate production-style code with layers: controller, service, repository, model, dto, "
                "exception handling, and configuration. Include pom.xml and application.properties. "
                "Output only files in this strict format:\n"
                "FILE: <relative/path>\n```<language>\n<content>\n```\n"
                "Do not include explanations."
            )
        ),
        HumanMessage(
            content=(
                "Convert this COBOL project to Spring Boot + MongoDB. "
                "Map CRUD behavior and preserve business rules.\n\n"
                f"{state['project_sources']}"
            )
        ),
    ]
    response = llm.invoke(messages)
    return {
        **state,
        "generated_project_text": response.content if isinstance(response.content, str) else str(response.content),
    }


def _review_project_node(
    state: ProjectConversionState,
    model_name: str,
    temperature: float,
) -> ProjectConversionState:
    llm = _get_model(model_name=model_name, temperature=temperature)
    messages = [
        SystemMessage(
            content=(
                "You are a senior Spring Boot reviewer. Improve correctness, package structure, null-safety, "
                "dependency alignment, and MongoDB mapping. Preserve the same FILE block output format exactly."
            )
        ),
        HumanMessage(content=state["generated_project_text"]),
    ]
    response = llm.invoke(messages)
    return {
        **state,
        "reviewed_project_text": response.content if isinstance(response.content, str) else str(response.content),
    }


def _finalize_project_without_review(state: ProjectConversionState) -> ProjectConversionState:
    return {
        **state,
        "reviewed_project_text": state["generated_project_text"],
    }


def build_project_graph(
    model_name: str = "gemini-2.5-flash-lite",
    temperature: float = 0.1,
    include_review: bool = True,
):
    workflow = StateGraph(ProjectConversionState)
    workflow.add_node(
        "convert_cobol_project_to_springboot",
        lambda state: _convert_project_node(state, model_name=model_name, temperature=temperature),
    )
    if include_review:
        workflow.add_node(
            "review_springboot_project",
            lambda state: _review_project_node(state, model_name=model_name, temperature=temperature),
        )
    else:
        workflow.add_node("finalize_project", _finalize_project_without_review)

    workflow.add_edge(START, "convert_cobol_project_to_springboot")
    if include_review:
        workflow.add_edge("convert_cobol_project_to_springboot", "review_springboot_project")
        workflow.add_edge("review_springboot_project", END)
    else:
        workflow.add_edge("convert_cobol_project_to_springboot", "finalize_project")
        workflow.add_edge("finalize_project", END)
    return workflow.compile()


def parse_generated_project_files(generated_text: str) -> dict[str, str]:
    pattern = re.compile(
        r"FILE:\s*(?P<path>[^\r\n]+)\r?\n(?:```[^\r\n]*\r?\n)?(?P<body>.*?)(?:\r?\n```|\Z)",
        re.DOTALL,
    )
    files: dict[str, str] = {}
    for match in pattern.finditer(generated_text):
        rel_path = match.group("path").strip().replace("\\", "/")
        body = match.group("body").strip() + "\n"
        if rel_path:
            files[rel_path] = body
    return files


def convert_cobol_project_to_springboot(
    cobol_files: dict[str, str],
    model_name: str = "gemini-2.5-flash-lite",
    temperature: float = 0.1,
    include_review: bool = True,
) -> dict[str, str]:
    if not cobol_files:
        raise ValueError("No COBOL files found to convert.")
    app = build_project_graph(model_name=model_name, temperature=temperature, include_review=include_review)
    result = app.invoke(
        {
            "project_sources": _format_sources_for_prompt(cobol_files),
            "generated_project_text": "",
            "reviewed_project_text": "",
        }
    )
    parsed = parse_generated_project_files(result["reviewed_project_text"])
    if not parsed:
        raise ValueError("Model output did not contain parsable FILE blocks.")
    return parsed
