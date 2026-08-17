import os


EMBEDDING_MODEL = os.getenv(
    "EMBEDDING_MODEL",
    "sentence-transformers/all-MiniLM-L6-v2",
)
EMBEDDING_DIMENSION = int(os.getenv("EMBEDDING_DIMENSION", "384"))
MAX_BATCH_SIZE = int(os.getenv("MAX_BATCH_SIZE", "64"))
GENERATION_PROVIDER = os.getenv("GENERATION_PROVIDER", "openai").strip().lower()
OPENAI_GENERATION_MODEL = os.getenv("GENERATION_MODEL", "gpt-5.6-luna")
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://host.docker.internal:11434").rstrip("/")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "llama3.2")
GENERATION_MODEL = OLLAMA_MODEL if GENERATION_PROVIDER == "ollama" else OPENAI_GENERATION_MODEL
