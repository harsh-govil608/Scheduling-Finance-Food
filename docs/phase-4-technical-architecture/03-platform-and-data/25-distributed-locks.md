# Document 25: Distributed Locks

## Document Name
Distributed Locks

## Purpose
Define the concurrency control requirements for operations across the platform that must not race with themselves or with each other — for example duplicate transaction detection in Finance, concurrent budget recalculation, or double-firing of a scheduled reminder. This document specifies when distributed locking is required, what guarantees it must provide, and where lock-free alternatives are preferred instead.

## Why It Exists
At 100M+ users with an event-driven, multi-instance, multi-region architecture, the same logical operation can be triggered more than once by design — retried events, redundant scheduler instances, replayed messages — and without an explicit concurrency control strategy, that redundancy silently becomes double-charged budgets, duplicate transactions, or corrupted recalculated balances. These are exactly the failure modes a Finance-pillar product cannot tolerate, since incorrect money math destroys user trust immediately and irreversibly. This document exists to give every service a shared, correct mechanism for "only one of these may run at a time" so correctness under concurrency is a platform guarantee, not something each team re-derives under deadline pressure.

## Approximate Page Count
6-8 pages.

## Sections
1. **When Locking Is Required** — the criteria for identifying operations that need distributed mutual exclusion (non-idempotent side effects, read-modify-write races, cross-instance duplicate execution risk) versus operations that should instead be made idempotent and not locked.
2. **Lock Scope & Granularity** — guidance on choosing lock scope (e.g., per-user, per-account, per-resource) to maximize correctness while minimizing contention and throughput loss.
3. **Lock Guarantees Required** — required properties of the locking mechanism (mutual exclusion, automatic expiry/lease timeout, fencing to prevent stale-lock actions, fairness under contention).
4. **Failure Mode Handling** — behavior required when a lock holder crashes or a network partition occurs, ensuring the system fails toward safety (no double execution) rather than availability, for the specific operation classes that require it.
5. **Representative Use Cases** — worked examples across services: duplicate financial transaction detection, budget recalculation, scheduled-reminder double-fire prevention (cross-referencing Scheduling), and any health-data reconciliation job.
6. **Alternatives to Locking** — where idempotency keys, optimistic concurrency (versioned writes), or conflict-free data structures are preferred over locking, and the criteria for choosing them instead.
7. **Performance & Contention Limits** — expected lock contention behavior at 100M-user scale and the requirement that locking never becomes a platform-wide bottleneck for high-frequency operations.
8. **Selection Criteria** — evaluation criteria for the underlying distributed lock implementation (latency, availability guarantees, integration with the chosen data stores), without naming a specific technology.

## Deliverables
* Approved Distributed Locks document defining when and how mutual exclusion is required platform-wide.
* Decision criteria distinguishing "must lock" from "must be idempotent instead."
* Worked examples for at least one Finance, one Health, and one scheduling-related use case.
* Failure mode and fencing requirements.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Databases, Message Queues, Background Jobs, Scheduling.

## Teams
Platform/Infrastructure, Engineering, Finance Engineering, Health Engineering, SRE.

## Completion Criteria
- [ ] Locking-vs-idempotency decision criteria applied to at least one representative operation per pillar (Productivity, Finance, Health).
- [ ] Fencing/stale-lock-holder handling explicitly defined with no unresolved failure paths.
- [ ] Duplicate financial transaction detection scenario traced end-to-end using the defined locking guarantees.
- [ ] Contention/performance requirements validated against a projected 100M-user peak-load estimate.
- [ ] Signed off by: Principal Architect (required), VP Engineering (required), Head of Finance Engineering (required).
