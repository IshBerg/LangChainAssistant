"""
ingest.py
LangChainAssistant/ingest.py
Version: 2.0.0

КОМИТЕТ - Document Ingestion for Multi-Language Projects
=======================================================

Обновленная версия для индексации YAKKI проекта (Kotlin, XML, Gradle файлы).
Поддерживает множественные file extensions и рекурсивное сканирование.

Авторы: Барух, Claude (Anthropic)
Проект: LangChainAssistant / КОМИТЕТ
Дата создания: Июль 2025
"""

import os
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.embeddings import SentenceTransformerEmbeddings
from langchain_community.vectorstores import Chroma

SOURCE_DIRECTORY = "source_code"
PERSIST_DIRECTORY = "db"

# Расширенный список файлов для YAKKI проекта
SUPPORTED_EXTENSIONS = [
    "**/*.py",  # Python файлы (старые)
    "**/*.kt",  # Kotlin файлы
    "**/*.kts",  # Kotlin script файлы (build.gradle.kts)
    "**/*.xml",  # Android manifest, layouts
    "**/*.gradle",  # Gradle файлы
    "**/*.java",  # Java файлы (если есть)
    "**/*.json",  # Конфигурационные файлы
    "**/*.properties",  # Properties файлы
    "**/*.txt",  # Text файлы
    "**/*.md"  # Markdown документация
]

print("--- Начинаем процесс индексации ---")
print(f"Поддерживаемые типы файлов: {', '.join([ext.replace('**/*.', '.') for ext in SUPPORTED_EXTENSIONS])}")

# 1. Загрузка документов
print(f"\n1. Загружаю документы из папки '{SOURCE_DIRECTORY}'...")

all_documents = []
loader_kwargs = {'encoding': 'utf-8'}

# Загружаем файлы по каждому расширению
for extension in SUPPORTED_EXTENSIONS:
    try:
        loader = DirectoryLoader(
            SOURCE_DIRECTORY,
            glob=extension,
            loader_cls=TextLoader,
            loader_kwargs=loader_kwargs,
            show_progress=True
        )
        documents = loader.load()

        if documents:
            print(f"  ✅ {extension}: найдено {len(documents)} файлов")
            all_documents.extend(documents)
        else:
            print(f"  ⚪ {extension}: файлы не найдены")

    except Exception as e:
        print(f"  ❌ {extension}: ошибка загрузки - {e}")

if not all_documents:
    print("!!! ОШИБКА: Не найдено ни одного документа для загрузки. Проверьте папку 'source_code'.")
    exit()

print(f"\n📊 Общий итог: успешно загружено {len(all_documents)} документов.")

# Показываем статистику по типам файлов
file_stats = {}
for doc in all_documents:
    ext = os.path.splitext(doc.metadata.get('source', ''))[-1]
    file_stats[ext] = file_stats.get(ext, 0) + 1

print("📈 Статистика по типам файлов:")
for ext, count in sorted(file_stats.items()):
    print(f"  {ext or 'без расширения'}: {count} файлов")

# 2. Разбиение на фрагменты
print("\n2. Разбиваю документы на фрагменты...")
text_splitter = RecursiveCharacterTextSplitter(
    chunk_size=800,  # Увеличено для кода
    chunk_overlap=100  # Больше overlap для лучшего контекста
)
texts = text_splitter.split_documents(all_documents)
print(f"Документы разделены на {len(texts)} фрагментов.")

# 3. Создание эмбеддингов
print("\n3. Инициализирую модель для создания эмбеддингов (векторов)...")
embeddings = SentenceTransformerEmbeddings(model_name="all-MiniLM-L6-v2")
print("Модель эмбеддингов готова.")

# 4. Создание и сохранение векторной базы данных
print("\n4. Создаю и сохраняю векторную базу данных. Это может занять некоторое время...")

# Удаляем старую базу если есть
if os.path.exists(PERSIST_DIRECTORY):
    import shutil

    shutil.rmtree(PERSIST_DIRECTORY)
    print("🗑️  Старая база данных удалена.")

db = Chroma.from_documents(texts, embeddings, persist_directory=PERSIST_DIRECTORY)
print(f"✅ База данных успешно создана и содержит {db._collection.count()} документов.")

print("\n--- Индексация успешно завершена! ---")
print("🎯 КОМИТЕТ теперь знает весь код YAKKI проекта!")
print("🚀 Можете задавать вопросы через: python ask_assistant.py --question 'ваш вопрос'")