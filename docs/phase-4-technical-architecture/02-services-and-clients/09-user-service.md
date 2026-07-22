# Document 09: User Service

## Document Name
User Service

## Purpose
Define the architecture of the service owning user identity, profile, preferences, and account lifecycle — the one service every other backend service depends on for "who is this user." This document specifies the service's data ownership boundary, API contract, consistency guarantees, and scaling posture at 100M+ user scale, without prescribing AI/ML internals or final vendor selections.

## Why It Exists
Without a single owning service for user identity, other services duplicate or diverge on user state — profile fields, preference flags, account status — causing consistency bugs at the exact layer (identity, permissions, entitlements) where bugs are most costly and hardest to unwind. Every one of the eight other backend services (Calendar, Task, Finance, Health, Notification, Search, Media, Analytics) and the client application resolve "current user" through this service, either directly or via cached/propagated state, so its boundary and consistency model must be unambiguous before those services can be architected against it.

## Approximate Page Count
8-10 pages

## Sections
1. **Service Boundary** — exactly what data and operations this service owns (profile, preferences, account status, device registry) versus what it delegates (credentials and tokens owned by Authentication; role/permission grants owned by Authorization; pillar-specific data owned by Calendar/Task/Finance/Health).
2. **Data Model** — core entities (user, profile, preference set, linked device, account status/lifecycle state) described at an architecture level, including which fields are mutable, versioned, or soft-deleted.
3. **API Surface** — the read and write operations other services and the Gateway call against this service, distinguishing synchronous lookups (e.g., profile fetch) from asynchronous/event-driven consumption.
4. **Consistency Model & Event Publishing** — how user-state changes (profile edits, account deactivation, preference updates) propagate to dependent services, and which changes require synchronous confirmation versus eventual, event-driven propagation.
5. **Multi-Region Considerations** — where the authoritative user record lives given multi-region deployment, how it is replicated or partitioned, and how the service behaves when a user's home region is unavailable.
6. **Scaling Characteristics** — read/write ratio assumptions at 100M+ users, expected hot paths (login-time profile fetch, preference reads on every app open), and the resulting capacity and caching implications.
7. **Caching & Read-Path Strategy** — what may be cached at the Gateway or client edge, cache invalidation triggers tied to the event model, and staleness tolerances per field category.
8. **Account Lifecycle & Data Lifecycle Hooks** — architectural hooks for account creation (from Onboarding), deactivation, deletion, and data export requests, aligned to the Data Export & Portability PRD.
9. **Failure Modes & Degraded Operation** — what happens to dependent services when User Service is degraded or unreachable, and which user-facing flows must have a defined fallback.
10. **Security & Privacy Boundary** — classification of profile fields as PII, which fields require field-level protection, and how access to user records is itself audited.

## Deliverables
- Service boundary diagram showing ownership handoffs to Authentication, Authorization, and the four pillar services.
- Entity-relationship diagram for user, profile, preference, and device entities.
- API contract summary (operations, callers, sync vs. async).
- Event catalog entries this service publishes (e.g., user.profile.updated, user.account.deactivated).
- Region/residency map for user records.
- Capacity model at 100M+ user scale (peak QPS, storage growth projection).
- Cache topology and invalidation rules.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Domain Boundaries, Event Architecture, API Architecture, Gateway Architecture, Authentication Architecture, and Authorization Architecture. Also informed by the Onboarding PRD, Permissions & Consent PRD, Account & Profile Management PRD, Cross-Device Sync PRD, and Data Export & Portability PRD.

## Teams
Platform Engineering, Identity & Access team, Data Engineering, Site Reliability Engineering, Security & Compliance, Mobile/Client Engineering (as a consuming team)

## Completion Criteria
- [ ] Service boundary reviewed against Domain Boundaries for overlap with Authentication/Authorization and with each pillar service.
- [ ] Event catalog entries cross-checked against Event Architecture for naming and schema conventions.
- [ ] Multi-region data placement reviewed against Overall System Architecture's regional topology.
- [ ] Capacity model validated against the 100M+ user scale assumption with explicit peak-load numbers.
- [ ] Signed off by: CTO/VP Engineering (required), Principal Architect (required), Security Lead (required).
