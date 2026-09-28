# COMMITTEE — early multi-LLM review pipeline (July 2025)

My first working "committee" of AI models: instead of trusting one model, a task goes through several models with different roles, and their findings are combined.

- **Coder** (Gemini) analyzes the code and the task
- **Advisor** (GPT-4o) reviews the Coder's analysis and proposes improvements
- **RAG** over the project's own source code (LangChain) gives both models real context

Built in July 2025 on a zero budget. Later the same idea grew into the daily practice behind my main project, YAKKI: every architectural decision is reviewed independently by several models (Claude, GPT, Gemini, Grok) before implementation.

This repository is kept as-is as the historical first version. The `source_code/` folder is sample input (an early Android project) used to test the pipeline.

## Files
- `multiagent_pipeline.py` — the Coder → Advisor committee
- `ask_assistant.py` — RAG question answering over a codebase
- `ingest.py` — indexing source code into the vector store
- `.env.example` — required keys (OpenAI, Google)
