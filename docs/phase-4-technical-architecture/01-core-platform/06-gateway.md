# Document 06: Gateway

## Document Name
Gateway

## Purpose
Define the API gateway layer that sits between all client applications and the backend services — its responsibilities for request routing, request aggregation for mobile clients, protocol translation, and the boundary of what the gateway is and is not responsible for (e.g., business logic must not live here). This document turns the client-facing entry point implied by Document 01's system diagram into an implementable architecture.

## Why It Exists
Without a well-defined gateway layer, mobile clients are forced to make many direct round-trips to individual backend services to assemble a single screen (e.g., the morning dashboard spanning Calendar, Task, Finance, and Health data), which is untenable on mobile networks at 100M+ users and directly contradicts the mobile-first requirement. An undocumented gateway also becomes an uncontrolled dumping ground for business logic that duplicates or drifts from the services behind it, and inconsistent gateway behavior across regions undermines the multi-region high-availability posture the platform requires.

## Approximate Page Count
8-10 pages.

## Sections
1. **Gateway Responsibilities & Non-Responsibilities** — an explicit boundary: routing, aggregation, protocol translation, rate limiting, and TLS termination belong here; domain business logic and data ownership do not.
2. **Request Routing Architecture** — how incoming client requests are routed to the correct backend service, including service discovery integration.
3. **Backend-for-Frontend (BFF) / Aggregation Pattern** — how the gateway composes multiple service calls into a single mobile-optimized response, and the criteria for when a dedicated BFF layer is warranted versus generic aggregation.
4. **Protocol Translation** — how the gateway bridges external client-facing protocols (e.g., REST/GraphQL for clients) to internal service protocols (e.g., gRPC) defined in Document 05.
5. **Rate Limiting & Traffic Shaping** — per-user and per-endpoint rate limiting strategy, and how it protects downstream services from both abusive and legitimately bursty AI-driven traffic patterns.
6. **Multi-Region Routing & Failover** — how the gateway directs traffic to the nearest healthy region and behaves during regional failover, cross-referencing the Infrastructure & Observability document group.
7. **Caching at the Edge** — what response caching, if any, the gateway performs, and how it avoids serving stale personalized/AI-driven content.
8. **Gateway Resilience Patterns** — circuit breaking, timeout budgets, and graceful degradation behavior when a backend service is unavailable, so one pillar's outage does not take down the whole assistant experience.
9. **Gateway Security Responsibilities** — the gateway's role in the authentication/authorization pipeline (validating tokens before routing) without owning the identity or permission model itself, cross-referencing Documents 07 and 08.
10. **Client Versioning & Backward Compatibility** — how the gateway supports old mobile app versions in the field against a continuously evolving backend.

## Deliverables
* Approved Gateway document with routing and aggregation architecture diagrams.
* Rate limiting and circuit-breaking policy reference.
* Multi-region failover behavior specification for the gateway tier.

## Dependencies
Requires Overall System Architecture, Service Decomposition, API Architecture. Coordinates closely with Authentication and Authorization. Informs and is informed by the Infrastructure & Observability and Scalability document groups.

## Teams
Engineering, Platform/Infrastructure, Security, SRE.

## Completion Criteria
- [ ] Gateway responsibility boundary reviewed to confirm zero business logic is scoped into the gateway layer.
- [ ] Aggregation pattern validated against at least one real mobile dashboard PRD requiring 3+ backend service calls.
- [ ] Multi-region failover behavior tested in a design walkthrough against a simulated single-region outage.
- [ ] Rate limiting policy reviewed against projected 100M+ user peak traffic assumptions.
- [ ] Signed off by: Principal Architect (required), Platform/Infrastructure Lead (required), Security Lead (required).
