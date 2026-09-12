# SolydShop — Notifications Service

The first service extracted out of the [solydshop_ecomm](https://github.com/boatengsamueltuga/solydshop_ecomm)
monolith, as part of an incremental, learning-driven move from a monolith to
microservices. See that repo's
`docs/superpowers/specs/2026-09-09-k3s-monolith-deployment-design.md` for the
full roadmap this fits into.

## What this is

An internal-only REST API for creating and managing user notifications
(new orders, moderation decisions, approvals, etc.). It is never exposed to
the public internet or called by the frontend directly — only the monolith
calls it, over the cluster's internal network, authenticated with a shared
`X-Internal-Api-Key` header. The monolith's own `/api/notifications/**`
endpoints (used by the frontend) proxy to this service internally.

It owns its own Postgres database (`notificationsdb`), separate from the
monolith's `solydShopdb`, with its own credentials — no other service can
query it directly.

## Tech stack

Java 17, Spring Boot 3.5, Spring Data JPA, PostgreSQL — same stack as the
monolith, deliberately, so this extraction is about learning service
boundaries and deployment, not also learning a new language.

## Running locally

1. Create the database: `CREATE DATABASE notificationsdb;`
2. Copy `src/main/resources/application.properties.example` values into
   real env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `INTERNAL_API_KEY`).
3. `./mvnw spring-boot:run`

The service starts on `http://localhost:8081`.

## API

All endpoints are under `/internal/notifications` and require the
`X-Internal-Api-Key` header.

| Method | Path | Purpose |
|---|---|---|
| POST | `/internal/notifications?userId=` | create a notification |
| GET | `/internal/notifications?userId=` | list (last 30, newest first) |
| GET | `/internal/notifications/unread-count?userId=` | unread count |
| PUT | `/internal/notifications/{id}/read?userId=` | mark one read |
| PUT | `/internal/notifications/read-all?userId=` | mark all read |
| DELETE | `/internal/notifications/{id}?userId=` | delete one |
| DELETE | `/internal/notifications/all?userId=` | delete all |

## Deployment

Deployed as a second Deployment + ClusterIP Service on the same k3s cluster
as the monolith (see `k8s/` — `configmap.yaml`/`deployment.yaml`/`service.yaml`
are committed, `secret.yaml` holds real credentials and is gitignored, same
split as the monolith repo). CI builds and pushes the image to GHCR;
deploying to the cluster is a manual `kubectl apply` for now.
