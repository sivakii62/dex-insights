# Dex Insights

A store operations API and UI over a snapshot of store, transaction and incident data, with a
grounded question-answering endpoint that cites the dataset records its answers come from.

- `backend/` — Java 21, Spring Boot 3.5, Maven
- `ui/` — Angular 21 (zoneless, signals), TypeScript

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21 |
| Maven | 3.9+ (or use `./mvnw`) |
| Node.js | 20+ |
| npm | 10+ |
| Docker (optional) | for the containerized run |

## Running locally

**Backend** — from `backend/`:

```bash
mvn spring-boot:run
```

Listens on `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui`, the raw
OpenAPI document at `http://localhost:8080/v3/api-docs`, and health at
`http://localhost:8080/actuator/health`.

**UI** — from `ui/`:

```bash
npm install
npm start
```

Serves on `http://localhost:4200` and proxies `/v1/*` to the backend (see `ui/proxy.conf.json`), so
start the backend first.

## Running with Docker

```bash
docker compose up --build
```

Builds both images and starts them together — UI at `http://localhost:4200`, API at
`http://localhost:8080`. The UI container's nginx proxies `/v1/*` to the backend container over the
compose network, the same relative-path arrangement as the dev proxy above. Copy `.env.example` to
`.env` to override any of the settings in [Configuration](#configuration); every value has a working
default, so `docker compose up` needs no `.env` file at all.

## Running tests

```bash
cd backend && mvn clean test
cd ui && npm test
```

Backend tests are plain JUnit 5 + AssertJ (service and pipeline logic against in-memory fixtures)
plus one `@SpringBootTest` / MockMvc pass over the real endpoints and the bundled dataset. UI tests
are Vitest specs for the API services, the shared status-badge component's tone logic, the chat
form's validation and submit flow, and the app shell's navigation.

## API overview

All endpoints are under `/v1`; full request/response schemas are in Swagger UI.

| Method | Path | Description |
|---|---|---|
| `GET` | `/v1/stores` | List stores. Filter by `brand`, `status`; sort by `sortBy`/`direction`; paginate with `page`/`size`. |
| `GET` | `/v1/stores/{storeId}` | A single store, including tank readings. 404 if unknown. |
| `GET` | `/v1/insights/overview` | Fleet summary, stores ranked by offline pumps, tank runout risks, incident counts. |
| `POST` | `/v1/chat` | `{ "question": string, "storeId"?: string }` → `{ answer, citations[], retrievedContextSummary }`, grounded in retrieved records. |

```bash
curl -s -X POST localhost:8080/v1/chat \
  -H 'content-type: application/json' \
  -d '{"question":"Which stores have the highest offline pumps and what incidents are associated with them?"}'
```

## Configuration

All backend settings live in `backend/src/main/resources/application.yaml` and are overridable by
environment variable:

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP port |
| `DEX_CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origins the API accepts browser requests from |
| `DEX_CHAT_LLM_ENABLED` | `false` | Turns on LLM-backed chat generation (see below) |
| `DEX_CHAT_LLM_BASE_URL` | `https://api.openai.com/v1` | Any OpenAI-compatible chat completions endpoint |
| `DEX_CHAT_LLM_API_KEY` | *(empty)* | Bearer token for the endpoint above |
| `DEX_CHAT_LLM_MODEL` | `gpt-4o-mini` | Model name passed to the endpoint |

The tank runout threshold (`dex.insights.low-tank-threshold`, default 25%) and retrieval size
(`dex.chat.top-k`, `dex.chat.max-context-chars`) are also configuration, not code, since an
operations team would reasonably want to tune them without a redeploy.

## Design decisions and tradeoffs

**In-memory dataset, no database.** Ten stores, fifteen transactions, eight incidents — loading them
once at startup into indexed `Map`s is simpler, faster, and has fewer moving parts than standing up a
database for data this size, and it matches the "runs with no external dependencies" spirit of the
exercise. `DatasetLoader` fails fast on a missing file, an empty file, or a duplicate id, so bad data
is a startup error, not a 500 three requests later. The tradeoff is explicit: no persistence, no
writes, and everything reloads from the bundled JSON on restart. `StoreRepository`,
`IncidentRepository` and `TransactionRepository` are interfaces specifically so a real datastore can
replace the in-memory implementation without touching services or controllers.

**Domain records doubling as API responses.** `Store`, `Incident` and `Transaction` are Java 21
records returned directly by the controllers rather than mapped into separate DTOs. For a read-only
projection API that's less ceremony for the same result; a DTO layer is the first thing I'd add the
moment the API needed to diverge from the domain shape (versioning, a write path, or a field the
domain doesn't need). The one place this bit us: the source feed's `STOREID`/`BRAND` fields needed
`@JsonAlias` rather than `@JsonProperty`, so those *read* the raw feed's names while the API still
*writes* clean camelCase.

**Grounding is structural, not prompted.** Retrieval is BM25 (not plain TF-IDF) over documents
rendered from the domain records into natural language at startup — BM25's length normalization and
term-frequency saturation matter here because store documents are much longer than transaction
documents and would otherwise dominate every query. A bare number in a question (e.g. "store 10001")
gets an explicit boost, since lexical overlap alone is a weak signal for an id. Retrieved stores pull
in their linked incidents (`ChatService.expandWithLinkedIncidents`) so "which stores have offline
pumps and what incidents are associated" can actually answer the second half. Citations are emitted
from the same loop that assembles the context, so an answer can never cite a record that wasn't
actually retrieved. The default answer generator is deterministic — the question only selects which
retrieved facts to report and how to rank them, and every number in the answer is read from the
domain record behind a citation — which makes it reproducible, testable, and dependency-free.
`AnswerGenerator` is the seam for an LLM: `LlmAnswerGenerator` is `@ConditionalOnProperty`-gated,
targets any OpenAI-compatible endpoint (so it also works against a local Ollama model, not just a
paid API), and falls back to the deterministic generator on any failure — the app never has a request
path that requires credentials.

**Java 21 features used where they earn their keep.** Records for every immutable domain and DTO
type, with compact constructors doing validation instead of a mapper or builder. Virtual threads are
turned on (`spring.threads.virtual.enabled`) since request handling here is dominated by cheap
in-memory lookups rather than long blocking I/O, so the win is modest but free at this scale — it's
the right default for a web tier and costs nothing to enable.

**RFC 9457 `ProblemDetail` everywhere**, including a request id (`X-Request-Id`) threaded through the
MDC, the log pattern, and every response header, so a specific failure can be traced from a support
ticket back to a log line without guessing.

**Angular 21, zoneless, with `rxResource` instead of a state library.** Four screens' worth of
server-driven state doesn't justify NgRx; each feature's loading/error/value state comes off one
signal-based resource tied to its query params, which is less code and less indirection than actions
and reducers would be here. Zoneless was the scaffold's default (no `zone.js` dependency), so change
detection runs on signal writes rather than on every browser event — kept as-is rather than added
back.

**A hand-rolled SCSS token layer instead of Angular Material.** A small `_tokens.scss` plus a handful
of shared primitives (`.card`, `.btn`, `.table`) covers four screens without Material's bundle size or
default look. The tradeoff is that accessibility contrast and focus states had to be set explicitly
rather than inherited from a library — addressed with one visible focus ring site-wide and a
deliberate light/dark color pairing rather than an automated audit.

**In-memory pagination.** `PageResponse.of` slices an already-filtered, already-sorted `List` in
memory. Correct and simple for ten records; the first thing to change if the dataset grew is pushing
the filter/sort/page operation down to wherever the data actually lives.

**Test scope.** Backend and UI tests are deliberately unit-level plus one integration pass each
(MockMvc against real endpoints; Vitest specs with `HttpTestingController`), not a browser-driven E2E
suite — proportionate to a four-screen app backed by eighteen source files of fixed data. Playwright
or Cypress would be the natural next investment if the UI grew past this size.

## AI assistance disclosure

This codebase was built with Claude as an AI pair-programmer (via Claude Code / Cowork) for:
scaffolding Spring Boot and Angular configuration, implementing the domain model, REST layer, BM25
retrieval pipeline and RAG endpoint, writing the Angular components and shared services, drafting unit
and integration tests, authoring the Dockerfiles and this README, and fixing a real bug found while
running the backend test suite (a `@JsonProperty` on the `Store` record was overriding both the read
*and* write field names, leaking the source feed's `STOREID`/`BRAND` into API responses — fixed by
switching to `@JsonAlias`, see "Design decisions" above). Every change was reviewed, and the backend
test suite, the Angular build (dev and production) and its Vitest suite were run and passed before
being treated as done. No proprietary or private code or data was used — the dataset is the synthetic
one provided with the exercise.

## Productionizing on AWS

- **Images**: both Dockerfiles are already multi-stage, non-root, and healthcheck-equipped. Push them
  to **ECR**, built and tagged by CI on merge.
- **Compute**: **ECS Fargate**, two services. The backend runs in a private subnet, reachable only from
  the UI service (ECS Service Connect or an internal ALB) — it never needs a public IP. Alternative to
  a UI container entirely: build the Angular app in CI and serve it from **S3 + CloudFront**, with
  CloudFront routing `/v1/*` to the backend's ALB origin and everything else to the S3 origin; cheaper
  at scale and removes a container, at the cost of losing the single-nginx-image simplicity this repo
  ships with.
- **Data**: the in-memory repositories are the one piece sized for a take-home dataset, not production
  data — swap them for RDS/Aurora Postgres (or DynamoDB, if access stays purely key-based) behind the
  same repository interfaces; no service or controller code changes.
- **RAG at scale**: the in-memory BM25 index is fine for thousands of records; past that, move it to
  **OpenSearch** (BM25 built in) or add a vector store (OpenSearch k-NN, Aurora pgvector, or Bedrock
  Knowledge Bases) for semantic retrieval, keeping `ContextAssembler`'s citation contract unchanged so
  nothing downstream (including the UI) needs to know retrieval changed.
- **LLM**: enable `dex.chat.llm.enabled` against **Amazon Bedrock** (via a Bedrock-specific
  `AnswerGenerator`, or an OpenAI-compatible gateway in front of it) with the API key/config in
  **Secrets Manager**, injected as an ECS task secret rather than a plain environment variable.
- **Observability**: ship logs to **CloudWatch Logs** (switch the console log pattern to a JSON
  encoder for structured queries), enable **Container Insights**, and correlate traces using the
  existing `X-Request-Id` header with **X-Ray** or OpenTelemetry. Alarm on the actuator health check
  and on ALB 5xx/latency.
- **Security**: ACM-issued TLS on the ALB/CloudFront, **WAF** in front of the public edge, least-
  privilege ECS task roles, VPC endpoints for ECR/Secrets Manager/CloudWatch to avoid NAT gateway
  costs.
- **Delivery**: CI runs `mvn verify` and `npm test && npm run build` as required checks; CD deploys via
  ECS rolling or blue/green (CodeDeploy) once they're green, with the backend's actuator health check
  gating traffic shift.
