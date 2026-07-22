# Document 04: Event Architecture

## Document Name
Event Architecture

## Purpose
Define the system-wide approach to asynchronous, event-driven communication between services — what qualifies as a domain event, how events are named/versioned, and which services publish vs. consume them. This document establishes the event conventions that Document 02's service inventory and Document 03's domain boundaries communicate through.

## Why It Exists
An AI Life Operating System's core value — cross-pillar coordination and proactive suggestions — depends on services reacting to what happens in other domains without tight synchronous coupling. Without a documented event architecture, teams invent incompatible pub/sub conventions per service, and cross-pillar features become brittle point-to-point integrations that cannot survive a single service's redeploy, let alone a multi-region failover. At 100M+ users, an undisciplined event architecture also becomes the single biggest source of untraceable production incidents — a message silently dropped, duplicated, or replayed out of order looks identical to an AI "hallucinating" a life event that never happened, which is unacceptable in a system users trust to manage their finances and health.

## Approximate Page Count
10-12 pages.

## Sections
1. **Event Taxonomy** — domain event vs. integration event vs. system event, naming convention (subject-verb-object past tense, e.g., `finance.transaction.categorized`), and how the taxonomy maps to the 9 domains.
2. **Event Bus/Broker Choice Criteria** — evaluation criteria (not final vendor selection) for the underlying message broker, covering throughput at 100M+ user scale, multi-region replication support, ordering guarantees, and operational maturity.
3. **Schema & Versioning** — event schema registry approach, backward/forward-compatibility rules, and the deprecation process for retiring an event version.
4. **Publish/Subscribe Ownership** — which service owns publishing which event (tying back to Document 03's domain ownership), the consumer registration pattern, and rules against consumers inferring undocumented event contracts.
5. **Delivery Guarantees** — at-least-once vs. exactly-once requirements per event class, and the idempotency requirements placed on consumers as a result.
6. **Event Replay & Auditability** — how events support debugging and the AI Memory feature's need for a durable, replayable activity log of everything that happened in a user's life stream.
7. **Failure & Dead-Letter Handling** — what happens when a consumer fails to process an event, retry/backoff policy, and alerting thresholds tied to Document 04 of the Infrastructure & Observability group.
8. **Ordering & Consistency Guarantees** — where strict per-user ordering is required (e.g., financial transaction sequences) versus where eventual consistency across regions is acceptable.
9. **Cross-Service Event Catalog Governance** — how new event types get proposed, reviewed, and approved, including the review body and required documentation per event.
10. **Consent-Aware Event Propagation** — how events carry or reference the Proactivity Ladder consent context so downstream consumers never act on data the user has not authorized for that use.

## Deliverables
* Approved Event Architecture document.
* Event naming/versioning convention reference.
* Initial event catalog template and the first populated catalog entries for cross-pillar scenarios.
* Dead-letter and replay runbook template.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Domain Boundaries. Feeds API Architecture (for sync/async decision boundaries) and Authorization (for consent propagation).

## Teams
Engineering, Platform/Infrastructure, Data Engineering, AI/ML, SRE.

## Completion Criteria
- [ ] Event taxonomy validated against at least one cross-pillar scenario per pillar pair (Productivity-Finance, Finance-Health, Health-Productivity).
- [ ] Schema registry and versioning rules ratified with a documented backward-compatibility guarantee window.
- [ ] Dead-letter handling and replay procedures tested against at least one simulated consumer failure.
- [ ] Consent-aware propagation rule confirmed compatible with the Authorization document's trust-tier model.
- [ ] Signed off by: CTO/VP Engineering (required), Principal Architect (required), SRE Lead (required).
