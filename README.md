# AI Developer Assistant

A Spring Boot backend that exposes REST endpoints (implemented twice: in **Java** and in **Kotlin**) backed by a **local Ollama** model. No OpenAI key or paid cloud service is needed.

Day 1 scope: health endpoints and a non-streaming chat endpoint. Planned later: code explanation, review, bug detection, refactoring, JUnit generation, quality analysis, conversation history (PostgreSQL), Docker.

## Architecture

```
Client ──► Controller (Java or Kotlin) ──► OllamaService ──► RestClient ──► Ollama (localhost:11434)
              │                                  │
              └── Bean Validation                └── OllamaException / OllamaUnavailableException
                          └────────── ApiExceptionHandler (@RestControllerAdvice) ──► JSON errors
```

Layers: controller, service, DTO (records / data classes), configuration (`OllamaProperties`, `OllamaConfiguration`), exception handling, tests. Both controllers reuse the **same** `OllamaService` bean.

## Technology stack

Java 21 · Kotlin 2.2.20 (overrides Spring Boot's 1.9.25 so the build also runs on JDK 25) · Spring Boot 3.5.7 · Spring MVC · Spring Validation · Spring `RestClient` · Lombok (Java only) · Maven · JUnit 5 · Mockito · Ollama.

Mixed Java/Kotlin build: the Kotlin compiler runs first (`process-sources`), then `javac` (with Lombok). Kotlin code can therefore use Java classes, but **not** Lombok-generated members, which is why classes used by Kotlin (`OllamaService`, `AiRequest`, `AiResponse`) are plain Java without Lombok-generated API.

## Prerequisites

- JDK 21 or newer (code targets Java 21; JDK 25 also works with the pinned Kotlin 2.2.20). In IntelliJ set both Project SDK and Settings → Build Tools → Maven → Runner JRE
- Maven 3.9+ (or the one bundled in IntelliJ IDEA / Android Studio)
- [Ollama](https://ollama.com) (only needed to run the app against a real model, not for tests)
- IntelliJ IDEA: enable the Lombok plugin and *Settings → Build → Compiler → Annotation Processors → Enable annotation processing*

## Ollama setup

```bash
ollama pull qwen2.5-coder:3b
ollama serve            # skip if Ollama already runs as a service
curl http://localhost:11434/api/tags
```

## Build, test, run

```bash
mvn clean test          # tests mock Ollama; no model required
mvn spring-boot:run     # starts on http://localhost:8080
mvn clean package       # builds target/ai-developer-assistant-0.1.0-SNAPSHOT.jar
java -jar target/ai-developer-assistant-0.1.0-SNAPSHOT.jar
```

### Configuration (`src/main/resources/application.yml`)

| Env variable | Default | Meaning |
|---|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Ollama server |
| `OLLAMA_MODEL` | `qwen2.5-coder:3b` | Model name |
| `OLLAMA_TIMEOUT_SECONDS` | `120` | Read timeout |
| `SERVER_PORT` | `8080` | App port |

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/java/health` | Health (Java controller) |
| POST | `/api/v1/java/ai/chat` | Chat (Java controller) |
| GET | `/api/v1/kotlin/health` | Health (Kotlin controller) |
| POST | `/api/v1/kotlin/ai/chat` | Chat (Kotlin controller) |

Chat request body: `{ "message": "required, max 10000 chars", "systemPrompt": "optional, max 2000 chars" }`
Chat response: `{ "model": "qwen2.5-coder:3b", "answer": "..." }`

Errors (JSON `{timestamp,status,error,message,details}`): `400` validation / malformed JSON, `502` Ollama returned an error, `503` Ollama unreachable, `500` unexpected.

### curl examples

```bash
curl http://localhost:8080/api/v1/java/health
curl http://localhost:8080/api/v1/kotlin/health

curl -X POST http://localhost:8080/api/v1/java/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"Explain what a Java record is.","systemPrompt":"You are a concise senior Java engineer."}'

curl -X POST http://localhost:8080/api/v1/kotlin/ai/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"Explain what a Kotlin data class is."}'

# validation error -> 400
curl -i -X POST http://localhost:8080/api/v1/java/ai/chat \
  -H 'Content-Type: application/json' -d '{"message":""}'
```

More examples (IntelliJ HTTP client format): `docs/requests.http`.

## Testing

`mvn clean test` runs:

- `JavaHealthControllerTest`, `KotlinHealthControllerTest` – health logic
- `JavaAiControllerTest`, `KotlinAiControllerTest` – MockMvc slice tests with a mocked `OllamaService` (success, blank/missing message, malformed JSON, Ollama 503/502)
- `OllamaServiceTest` – `MockRestServiceServer`: request shape, success, connection failure, HTTP 500, empty body
- `OllamaPropertiesTest` – defaults
- `ApplicationIntegrationTest` – full context; both controllers share the service

No test needs a running Ollama.

## Project structure

```
ai-developer-assistant/
├── pom.xml
├── README.md
├── .gitignore
├── docs/requests.http
└── src
    ├── main
    │   ├── java/com/example/ai
    │   │   ├── AiDeveloperAssistantApplication.java
    │   │   ├── ai/      OllamaService, OllamaConfiguration, OllamaProperties,
    │   │   │            AiRequest, AiResponse, OllamaException, OllamaUnavailableException
    │   │   ├── java/    JavaHealthController, JavaAiController
    │   │   └── common/  ApiExceptionHandler, ApiError, HealthResponse
    │   ├── kotlin/com/example/ai/kotlin/
    │   │   └── KotlinHealthController, KotlinAiController, KotlinAiRequest
    │   └── resources/application.yml
    └── test
        ├── java/com/example/ai/...
        └── kotlin/com/example/ai/kotlin/...
```
