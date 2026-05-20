# ⚡ SwiftLinkAI

> **Production-grade SaaS AI-powered URL shortener**  
> Java 21 · Spring Boot 3 · React 19 · Redis · MySQL · Resilience4j · Spring AI · EKS

---

## Architecture Overview

```
┌────────────────────────────────────────────────────────────────┐
│  React PWA (Vite + TypeScript + Tailwind)                      │
│  Zustand · React Query · Zod · PWA/Service Worker              │
└──────────────────────────┬─────────────────────────────────────┘
                           │ HTTPS / JWT Bearer
┌──────────────────────────▼─────────────────────────────────────┐
│  Spring Boot 3 Backend (Java 21)                               │
│                                                                │
│  UrlShortenerController  ──►  UrlShortenerService              │
│         │                            │                         │
│  AuthController           GenAIService (Circuit Breaker)       │
│                                 │                              │
│                         OpenAiMetadataProvider                 │
│                         (Strategy Pattern)                     │
│         │                                                      │
│  RateLimitService (Redis sliding window)                       │
│  AnalyticsService (async, fire-and-forget)                     │
└────────┬───────────────────────┬────────────────────────────── ┘
         │                       │
    ┌────▼────┐           ┌──────▼──────┐
    │  MySQL  │           │    Redis    │
    │ (Flyway)│           │ (redirect   │
    │         │           │  cache +    │
    └─────────┘           │  rate limit)│
                          └─────────────┘
```

---

## Project Structure

```
swiftlinkai/
├── backend/                         # Spring Boot 3 / Java 21
│   ├── src/main/java/com/swiftlinkai/
│   │   ├── SwiftLinkAIApplication.java
│   │   ├── config/                  # Security, Redis, OpenAPI, AI
│   │   ├── controller/              # UrlShortenerController, AuthController
│   │   ├── service/                 # UrlShortenerService interface + impl
│   │   ├── ai/                      # GenAIService, AiMetadataProvider strategy
│   │   ├── entity/                  # User, ShortUrl, UrlAnalytics
│   │   ├── repository/              # JPA repositories
│   │   ├── dto/                     # Request / Response DTOs
│   │   ├── security/                # JWT, TenantContext, Filter
│   │   ├── cache/                   # RedirectCacheService
│   │   ├── rate_limit/              # RateLimitService (Redis)
│   │   ├── analytics/               # AnalyticsService (async)
│   │   ├── mapper/                  # MapStruct
│   │   ├── exception/               # GlobalExceptionHandler + custom exceptions
│   │   └── scheduler/               # Expired URL cleanup
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/            # Flyway V1__initial_schema.sql
│   ├── src/test/                    # Unit + Integration (Testcontainers)
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                        # React 19 + Vite + TypeScript
│   ├── src/
│   │   ├── api/                     # Axios client, typed API services
│   │   ├── auth/                    # Zustand store, ProtectedRoute
│   │   ├── pages/                   # Login, Register, Dashboard, Shorten, Analytics
│   │   └── App.tsx                  # Router setup, QueryClient
│   ├── vite.config.ts               # PWA plugin, proxy, code splitting
│   ├── Dockerfile
│   └── nginx.conf
│
├── k8s/
│   └── base/
│       ├── backend-deployment.yaml  # Deployment, resource limits, probes
│       ├── services.yaml            # Services, HPA, Ingress, NetworkPolicy
│       └── secrets.yaml.template   # Secret template (never commit real values)
│
├── jenkins/
│   └── Jenkinsfile                  # 9-stage CI/CD: test → sonar → docker → EKS
│
├── docker-compose.yml               # Local dev: MySQL + Redis + backend + frontend
└── README.md
```

---

## Quick Start (Local)

```bash
# 1. Clone
git clone https://github.com/your-org/swiftlinkai.git && cd swiftlinkai

# 2. Set secrets
cp .env.example .env
# Edit OPENAI_API_KEY and JWT_SECRET

# 3. Start all services
docker-compose up --build

# Backend:  http://localhost:8080
# Frontend: http://localhost:3000
# Swagger:  http://localhost:8080/swagger-ui.html
```

---

## Backend: Build & Run Standalone

```bash
cd backend
mvn clean package -DskipTests
java -jar target/swiftlinkai-backend-1.0.0-SNAPSHOT.jar
```

---

## Frontend: Dev Server

```bash
cd frontend
npm install
npm run dev       # http://localhost:5173
```

---

## Environment Variables

| Variable | Description | Required |
|---|---|---|
| `DB_URL` | JDBC MySQL connection URL | ✅ |
| `DB_USERNAME` | Database username | ✅ |
| `DB_PASSWORD` | Database password | ✅ |
| `REDIS_HOST` | Redis hostname | ✅ |
| `OPENAI_API_KEY` | OpenAI API key | ✅ |
| `JWT_SECRET` | Base64 JWT signing secret (≥256 bits) | ✅ |
| `APP_BASE_URL` | Public base URL e.g. `https://swiftlink.ai` | ✅ |

---

## Key APIs

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | None | Register user |
| POST | `/api/v1/auth/login` | None | Login → JWT |
| POST | `/api/v1/shorten` | Bearer | Shorten URL with AI |
| GET | `/api/v1/{alias}` | None | Redirect |
| GET | `/api/v1/urls` | Bearer | List URLs |
| DELETE | `/api/v1/urls/{alias}` | Bearer | Deactivate URL |

---

## Execution Order for Implementation

1. Database schema (`V1__initial_schema.sql`) + Flyway config  
2. Entities (`User`, `ShortUrl`, `UrlAnalytics`)  
3. Repositories  
4. DTOs + Mapper  
5. JWT security (`JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`)  
6. Cache service (`RedirectCacheService`) + Rate limiting  
7. AI pipeline (`AiMetadataProvider` → `OpenAiMetadataProvider` → `GenAIService`)  
8. Service layer (`UrlShortenerServiceImpl`)  
9. Controllers (`AuthController`, `UrlShortenerController`)  
10. Analytics service + Scheduler  
11. Global exception handler  
12. Unit tests → Integration tests (Testcontainers)  
13. Frontend: auth → API layer → pages → PWA  
14. Docker → Compose → K8s manifests → Jenkinsfile  

---

## Quality & Observability

- **Code coverage**: JaCoCo enforced at ≥ 80%  
- **Quality gate**: SonarQube (0 critical issues)  
- **Security scans**: OWASP Dependency Check + Trivy  
- **Metrics**: Prometheus `/actuator/prometheus`  
- **Tracing**: Correlation IDs via `X-Trace-Id` header  
- **Logging**: Structured JSON with `traceId` + `tenantId` in MDC  
- **Health**: `/actuator/health`, `/actuator/readiness`, `/actuator/liveness`
