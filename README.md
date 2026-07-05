STUDENT NOTES AI ASSISTANT

A full-stack Retrieval-Augmented Generation (RAG) system built to help students query, summarize, and navigate their uploaded study materials (PDFs). Unlike generic API-wrapper chatbots, this project uses a custom document parsing and vector-search pipeline to deliver context-isolated, citation-backed answers.


Architecture and Flow

[React Frontend] <---> [Spring Boot Backend] <---> [MySQL (Metadata and History)] | +---> [Qdrant Vector DB (Vector Storage and Similarity Search)] | +---> [Gemini API (Embeddings and Chat Generation)]

1 Upload and Parse: The backend extracts text page-by-page from PDFs.
2 Chunk and Embed: Text is chunked into logical passages, converted into 3072-dimensional embeddings via Gemini, and indexed in Qdrant.
3 Retrieve: User queries are vectorized and matched with relevant chunks in Qdrant, using metadata filters to ensure strict session isolation.
4 Generate: The retrieved context, conversation history, and query are sent to Gemini to generate a highly accurate response with citations.


Key Features

1 Multi-Document Support: Upload multiple PDFs and ask questions across all of them or specific selections.
2 Context-Isolated Sessions: Strict separation of user sessions using JWT-based authentication and metadata-level vector database filtering.
3 Citation-Backed Responses: The assistant highlights exactly which page and chunk of the notes were used to generate the answer.
4 Stateful Conversations: Remembers historical chat turns in the session for natural follow-up questions.
5 Fully Dockerized: Easily spin up the entire application stack (MySQL, Qdrant, Backend, Frontend) with a single command.


Tech Stack

1 Frontend: React, TailwindCSS, Vite
2 Backend: Spring Boot, Spring Security (JWT), Spring Data JPA
3 Databases:
      1 MySQL: Relational data, user authentication details, document metadata, and session history.
      2 Qdrant: High-performance vector database for similarity search.
4 AI/LLM: Gemini API (gemini-2.5-flash for generation, gemini-embedding-001 for vector embeddings).
5 DevOps: Docker and Docker Compose.

Getting Started

Prerequisites

Docker and Docker Compose installed.
A Gemini API Key (obtainable from Google AI Studio).


Setup and Running

1 Clone the repository: git clone https://github.com/PriNce0P-Negi/student-notes-ai-assistant.git cd student-notes-ai-assistant

2 Run with Docker Compose: Provide your Gemini API key as an environment variable when spinning up the containers: GEMINI_API_KEY="your-api-key-here" docker-compose up --build

3 Access the application:
       1 Frontend: Open http://localhost in your browser.
       2 Backend API: Running on http://localhost:8080.
       3 Qdrant Console: Running on http://localhost:6335/dashboard.
