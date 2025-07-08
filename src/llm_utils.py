# src/llm_utils.py
from langchain_openai import ChatOpenAI
from langchain_google_genai import ChatGoogleGenerativeAI
def get_llm(model_name: str):
    if "gpt" in model_name:
        return ChatOpenAI(model=model_name, temperature=0.7)
    elif "gemini" in model_name:
        return ChatGoogleGenerativeAI(model="gemini-1.5-pro-latest", temperature=0.3)
    else:
        return ChatOpenAI(model="gpt-4o", temperature=0.7)