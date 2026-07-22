# Document 02: Service Decomposition

## Document Name
Service Decomposition

## Purpose
Define the principles that determine when functionality becomes its own independently deployable service versus remaining part of another service, and apply those principles to enumerate the 9 backend services required to implement the Phase 3 PRDs. This document sets the granularity philosophy — not an org chart, and not a final microservice count carved in stone.

## Why It Exists
Without a documented decomposition philosophy, services get split or merged ad hoc — by whichever team happens to build a feature first — producing either a monolith that cannot scale or deploy independently at 100M+ users, or an over-fragmented mesh where every PRD spawns a new service and cross-service calls dominate latency budgets. Because the product is experienced as one assistant across three pillars, service boundaries also directly determine which teams can ship independently without blocking each other and where the "one assistant" illusion is stitched together at runtime; getting this wrong either recreates pillar silos the product is explicitly trying to avoid or creates a distributed monolith with all of a monolith's coupling and none of its simplicity.

## Approximate Page Count
10-12 pages.

## Sections
1. **Decomposition Philosophy** — the guiding principles used (bounded-context alignment, independent deployability, single-team ownership, data ownership, blast-radius containment) and explicitly rejected approaches (e.g., decomposing by technical layer or by PRD).
2. **Granularity Trade-off Analysis** — the cost/benefit reasoning for coarser vs. finer service boundaries at 100M+ user scale, including operational overhead, deployment velocity, and failure isolation.
3. **Service Inventory** — the enumerated list of the 9 backend services (e.g., User/Identity, Calendar, Task, Finance, Health, Notification, Search, Media, Analytics/AI-Orchestration) with a one-paragraph mandate per service.
4. **Service-to-Domain Mapping** — how each service maps to the bounded contexts defined in Document 03 (Domain Boundaries), flagging any service that spans multiple domains and why.
5. **Service-to-PRD Traceability** — a mapping showing which of the 47 Phase 3 PRDs each service is responsible for implementing, ensuring no PRD is orphaned or duplicated across services.
6. **Inter-Service Dependency Map** — which services call or are called by which others, distinguishing hard dependencies from soft/optional ones, feeding Document 05 (API Architecture) and Document 04 (Event Architecture).
7. **Ownership & Team Alignment** — the intended mapping from services to owning engineering teams, and the process for when a service outgrows single-team ownership.
8. **Splitting & Merging Criteria** — concrete, measurable triggers (e.g., deploy frequency conflicts, data contention, team size) for when a service should later be split further or merged, so decomposition remains a living decision.
9. **Anti-Patterns to Avoid** — documented decomposition mistakes the org commits to avoiding (shared databases across services, chatty synchronous chains, god services).

## Deliverables
* Approved Service Decomposition document with the 9-service inventory.
* Service-to-domain and service-to-PRD traceability matrices.
* Inter-service dependency diagram.

## Dependencies
Requires Overall System Architecture. Informs and is refined alongside Domain Boundaries. Requires Phase 3 Product Specifications (all 47 PRDs) as the traceability source.

## Teams
Engineering, Platform/Infrastructure, Data Engineering, AI/ML.

## Completion Criteria
- [ ] All 9 services have a documented single-sentence mandate with no overlapping responsibility.
- [ ] 100% of the 47 Phase 3 PRDs are traceable to exactly one primary owning service.
- [ ] Inter-service dependency map reviewed for cyclic hard dependencies, with zero unresolved cycles.
- [ ] Splitting/merging criteria validated against at least one projected 100M-user hotspot (e.g., Notification or Search).
- [ ] Signed off by: Principal Architect (required), VP Engineering (required), owning Engineering Leads for all 9 services (required).
