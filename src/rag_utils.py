# src/rag_utils.py
from langchain_community.vectorstores import Chroma
from langchain_community.embeddings import SentenceTransformerEmbeddings
from langchain.prompts import ChatPromptTemplate
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
def get_rag_chain(llm):
    embeddings = SentenceTransformerEmbeddings(model_name="all-MiniLM-L6-v2")
    db = Chroma(persist_directory="db", embedding_function=embeddings)
    retriever = db.as_retriever()
    template = "Context: {context}\n\nQuestion: {question}\n\nAnswer:"
    prompt = ChatPromptTemplate.from_template(template)
    rag_chain = (
        {"context": retriever, "question": RunnablePassthrough()} | prompt | llm | StrOutputParser()
    )
    return rag_chain