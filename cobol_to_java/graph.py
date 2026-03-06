from __future__ import annotations

from typing import TypedDict

from langchain_core.messages import HumanMessage, SystemMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langgraph.graph import END, START, StateGraph


class ConversionState(TypedDict):
    cobol_code: str
    java_code: str
    reviewed_java_code: str


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
