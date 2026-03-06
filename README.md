# COBOL to Java Converter (LangGraph + Gemini)

This project provides a LangGraph workflow to convert COBOL code to Java using Gemini.

## 1. Setup

```powershell
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

## 2. Configure API key

Set your GCP/Gemini API key:

```powershell
$env:GOOGLE_API_KEY="YOUR_API_KEY"
```

Or copy `.env.example` to `.env` and load it with your preferred method.

## 3. Run

From `main.py`:

```powershell
python main.py --input-file .\sample.cbl --output-file .\Converted.java
```

Or pass COBOL inline:

```powershell
python main.py --cobol "IDENTIFICATION DIVISION. PROGRAM-ID. HELLO."
```

## 4. Convert COBOL project to Spring Boot (MongoDB)

Use the included sample multi-file COBOL CRUD project:

```powershell
python main.py --target springboot --input-dir .\sample_cobol_crud_mongo --output-dir .\springboot-out --skip-review
```

The generated Spring Boot project files are written under `springboot-out`.

## 5. Optional model override

```powershell
python main.py --input-file .\sample.cbl --model gemini-2.5-flash-lite --temperature 0.1
```

For lower quota usage (single model call), skip review:

```powershell
python main.py --input-file .\sample.cbl --output-file .\Converted.java --skip-review
```
