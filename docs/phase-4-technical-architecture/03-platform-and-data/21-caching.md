# Document 21: Caching

## Document Name
Caching

## Purpose
Define the caching strategy across services — what gets cached, at which layer (CDN, gateway, service, database), invalidation rules, and the consistency tradeoffs accepted at each layer. This document establishes shared caching principles so every service applies the same reasoning to a cache-or-not decision rather than inventing one ad hoc per feature.

## Why It Exists
An AI system that proactively manages a user's life is only trustworthy if the data it acts on is current — a stale cached budget balance or stale cached calendar slot leads directly to a wrong proactive suggestion, which is a much costlier failure than a slow page load. At the same time, at 100M+ users, refusing to cache anything is not viable: origin databases and AI orchestration paths would collapse under redundant reads for data that rarely changes, like a user's timezone or notification preferences. This document exists to draw an explicit, auditable line between what may be cached and how staleness is bounded, versus what must always be read fresh, so caching accelerates the product without silently undermining the proactive-correctness the whole system is built to deliver.

## Approximate Page Count
7-9 pages.

## Sections
1. **Cache Layers** — CDN, API gateway, service-local, and database query cache — what belongs at each layer and why.
2. **Cacheability Classification** — per-domain guidance across all 9 backend services (e.g., user profile: cacheable with short TTL; live budget balance: not cacheable; calendar availability: cacheable with event-driven invalidation) establishing a repeatable classification method, not just examples.
3. **Invalidation Strategy** — event-driven invalidation versus TTL-based expiry, which mechanism applies to which cacheability class, and how invalidation events tie into the Event Architecture (Phase 4 Document 04).
4. **Consistency Tradeoffs** — where eventual consistency is acceptable given the product's proactive-suggestion use cases, and where it is explicitly disallowed (financial balances, health safety-relevant data).
5. **Multi-Region Cache Coherence** — cache behavior across regions given multi-region deployment, including whether caches are region-local only or require cross-region invalidation propagation.
6. **Cache Stampede & Cold-Start Handling** — protection patterns for cache-miss storms (request coalescing, jittered TTLs, pre-warming) expected at 100M-user scale.
7. **Cache Key Design & Namespacing** — conventions for cache key structure per service to avoid collisions and enable targeted bulk invalidation.
8. **Observability & Cache Health Metrics** — the hit-rate, staleness, and invalidation-lag metrics every cached path must expose, cross-referencing the Infrastructure & Observability document group.

## Deliverables
* Approved Caching document with the layered cache model and cacheability classification method.
* Cacheability classification table covering at least one representative entity per backend service.
* Invalidation strategy mapped to each cacheability class.
* Cache observability metric requirements.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Storage, Databases, Message Queues.

## Teams
Platform/Infrastructure, Engineering, SRE, AI/ML.

## Completion Criteria
- [ ] Cacheability classification covers at least one entity per backend service.
- [ ] Every "not cacheable" classification includes a stated reason tied to correctness or safety risk.
- [ ] Invalidation strategy for each cacheability class is either event-driven, TTL-based, or explicitly both, with no unspecified classes.
- [ ] Multi-region cache coherence behavior validated against at least one cross-region user scenario.
- [ ] Signed off by: CTO/VP Engineering (required), Principal Architect (required).
