# Advertiser Growth CRM Backend Extension

## Interview-sized architecture

The extension adds a revenue-platform workflow to the Spring PetClinic REST baseline:

1. A manager creates an advertiser account and an opportunity through validated REST APIs.
2. An opportunity advances through a guarded lifecycle (`PROSPECTING` to `WON`/`LOST`).
3. `Idempotency-Key` prevents duplicate stage transitions.
4. The database transaction writes both the transition and an `AuditEvent` outbox record.
5. A scheduled publisher sends pending events to Kafka; failures remain visible and retry up to five times.
6. Account reads use Redis under the `infra` profile; cache failures degrade to PostgreSQL.
7. Spring Security roles separate read, management, and destructive operations.
8. Actuator and Prometheus expose health and workflow counters.

## Public API

- `POST /api/growth/accounts`
- `GET|PUT|DELETE /api/growth/accounts/{id}`
- `POST /api/growth/accounts/{accountId}/opportunities`
- `GET /api/growth/opportunities?accountId=&stage=&page=&size=`
- `GET /api/growth/opportunities/{id}`
- `POST /api/growth/opportunities/{id}/transitions` with `Idempotency-Key`
- `GET /api/growth/audit-events`
- `/actuator/health`, `/actuator/prometheus`, `/swagger-ui.html`

## Local paths

Fast H2 path:

```powershell
./mvnw.cmd spring-boot:run
```

Full infrastructure path:

```powershell
docker compose up --build
```

The full path uses PostgreSQL, Redis, Kafka in KRaft mode, and HTTP Basic RBAC. Seed users use the
upstream demo password hash and are for local demonstration only.

## Explicit limits

This is a local engineering demonstration. It does not claim production traffic, multi-region
deployment, exactly-once end-to-end delivery, or general performance at TikTok scale.

