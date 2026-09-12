# Design: Extract the Notification Service (Microservices Learning — Phase 2)

**Status:** Approved, implementation in progress
**Date:** 2026-09-12

## Context

Second step of the incremental monolith-to-microservices learning project
described in `solydshop_ecomm`'s
`docs/superpowers/specs/2026-09-09-k3s-monolith-deployment-design.md`.
Phase 1 (deploying the unmodified monolith to a self-hosted k3s cluster) is
done and verified. This phase extracts the first real service out of the
monolith.

**Roadmap recap:**
1. Phase 1 — done: monolith on k3s.
2. **Phase 2 (this doc):** extract a Notification service.
3. Phase 3: swap the REST call for RabbitMQ.
4. Phase 4: migrate from k3s to DOKS.
5. Later: further service extractions.

Notifications was chosen as the first extraction because it's self-contained
today, isn't on the critical path of checkout, and naturally receives events
from many parts of the monolith (orders, product moderation, seller
applications/downgrades, quotes) — a good first lesson in inter-service
communication without touching anything money-related.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Repo | New standalone repo, `solydshop-notifications` | Matches how independent microservices are normally owned/deployed/versioned. |
| Language/stack | Java 17 + Spring Boot, same as the monolith | This phase is about learning service boundaries and deployment, not also learning a new language. |
| Communication | Plain synchronous REST (monolith → service) | Simplest way to learn the extraction itself. RabbitMQ is a deliberate, separate Phase 3 lesson. |
| Data ownership | Own database (`notificationsdb`), own user/credentials, same Postgres install as the monolith's `solydShopdb` | Real data ownership (no cross-service queries) without running a second Postgres server process. |
| Frontend | Unchanged — still talks only to the monolith | Keeps this phase backend-only; the monolith's existing `/api/notifications/**` controller proxies to the new service. |
| Security | Shared-secret header (`X-Internal-Api-Key`) | Service is ClusterIP-only (never public), but any other pod on the same cluster could otherwise reach it. |
| Deployment target | Same k3s cluster as the monolith (ClusterIP Service, not NodePort) | Builds directly on the Phase 1 cluster; practices running multiple services on one cluster. |
| CI/CD | Build + push image to GHCR only; `kubectl apply` is manual for now | Keeps this phase focused on the extraction itself — automated CI→k8s deploys is a separate future lesson. |
| Who writes the code | Claude writes the new service's code, user reviews | This phase's learning value is in the architecture/deployment/debugging, not re-typing Spring Boot boilerplate already present in the monolith. |
| Historical data | Left behind (same as Phase 1) | This is the k3s learning environment only; production is untouched throughout. |

## API contract

Internal-only, under `/internal/notifications`, every request requires
`X-Internal-Api-Key`:

| Method | Path | Purpose |
|---|---|---|
| POST | `/internal/notifications` (body: userId, title, message, type, resourceId?) | create |
| GET | `/internal/notifications?userId=` | list (last 30, newest first) |
| GET | `/internal/notifications/unread-count?userId=` | unread count |
| PUT | `/internal/notifications/{id}/read?userId=` | mark one read |
| PUT | `/internal/notifications/read-all?userId=` | mark all read |
| DELETE | `/internal/notifications/{id}?userId=` | delete one |
| DELETE | `/internal/notifications/all?userId=` | delete all |

## Monolith integration

`NotificationServiceImpl` (in `solydshop_ecomm`) is replaced with an
HTTP-client-backed implementation of the *same* `NotificationService`
interface. Because the interface is unchanged, `NotificationController` and
every caller (`OrderServiceImpl`, `ProductServiceImpl`, `QuoteServiceImpl`,
`SellerApplicationServiceImpl`, `SellerDowngradeServiceImpl`) require no
code changes.

Error handling mirrors the current behavior:
- Writes (`createForUser`) never propagate a failure — a notification
  service outage must not roll back or fail the real action (placing an
  order, approving a seller, etc.), matching today's `REQUIRES_NEW`
  transaction isolation.
- Reads (`getNotifications`/`getUnreadCount`) degrade gracefully — on
  failure, return an empty list / zero count and log, rather than failing
  the frontend's notification bell.

New monolith config (`solydshop_ecomm/k8s/configmap.yaml` /`secret.yaml`):
`NOTIFICATION_SERVICE_URL` (`http://solydshop-notifications:8081`, the
cluster-internal Service DNS name) and `INTERNAL_API_KEY` (shared with this
service).

## Testing

- This repo: unit tests for the service layer (mirroring the monolith's
  Mockito-based test conventions) and the API key filter.
- Monolith: `NotificationServiceImplTest` rewritten to mock the HTTP client
  instead of the repository.
- End-to-end verification: trigger a real event through the monolith (e.g.
  submit a seller application) and confirm the notification appears via
  `GET /api/notifications` — proving the full round trip works.
