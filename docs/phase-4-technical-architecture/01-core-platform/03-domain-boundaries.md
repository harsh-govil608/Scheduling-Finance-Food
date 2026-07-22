# Document 03: Domain Boundaries

## Document Name
Domain Boundaries

## Purpose
Define the bounded contexts for each domain in the system — User, Calendar, Task, Finance, Health, Notification, Search, Media, and Analytics — specifying exactly what data and business logic each domain owns exclusively, and what it must never directly access from another domain. This document is the domain-driven-design counterpart to Document 02's service inventory.

## Why It Exists
When domains are not given explicit, exclusive ownership of their data and logic, services quietly reach into each other's data stores "just this once," and within a few quarters no team can say with confidence which service is the source of truth for a given fact about the user. In an AI Life Operating System where a single proactive suggestion may touch Calendar, Finance, and Health data simultaneously, ambiguous ownership is especially dangerous: it produces contradictory AI recommendations (e.g., Finance and Health both silently mutating the same "available time" concept), makes consent scoping (central to the Proactivity Ladder) impossible to enforce consistently, and turns any future domain split or re-org into a multi-quarter untangling project instead of a boundary move.

## Approximate Page Count
10-12 pages.

## Sections
1. **Bounded Context Method** — the DDD-derived method used to identify domain boundaries (ubiquitous language, entity ownership, ubiquitous language conflicts) and how it was applied to this product's pillars.
2. **User Domain** — the exclusive owner of identity, profile, preferences, and consent state; explicitly the source of truth the Authorization model (Document 08) checks against.
3. **Calendar Domain** — ownership of events, availability, scheduling logic; its relationship to Task domain time-blocking.
4. **Task Domain** — ownership of tasks, projects, reminders; boundary with Calendar over due dates and scheduled work.
5. **Finance Domain** — ownership of accounts, transactions, budgets, financial goals; strict boundary around what other domains may read (e.g., "affordability" signals) versus never access (raw transaction detail).
6. **Health Domain** — ownership of health metrics, routines, wearable data; boundary reflecting elevated sensitivity/consent requirements relative to other domains.
7. **Notification Domain** — ownership of delivery, channel preference, and timing logic; explicitly a consumer of events from all other domains rather than an owner of pillar data.
8. **Search & Media & Analytics Domains** — ownership boundaries for cross-cutting/derived domains: Search as an index over other domains' data (not a source of truth), Media as attachment/asset storage, Analytics as a read-only aggregator.
9. **Cross-Domain Data Sharing Rules** — the only sanctioned ways one domain may use another's data (published events, defined read APIs, explicitly shared reference data), with direct database access declared out of bounds.
10. **Domain Boundary Violation Detection** — how the architecture will detect and flag boundary violations (e.g., architectural fitness functions, dependency linting) before they reach production.

## Deliverables
* Approved Domain Boundaries document with a bounded-context diagram for all nine domains.
* Domain ownership matrix (domain -> owned data entities -> owned business logic -> sanctioned external access methods).
* Cross-domain data sharing rulebook.

## Dependencies
Requires Overall System Architecture and Service Decomposition. Informs Event Architecture, API Architecture, and Authorization. Requires Phase 2 Product Definition docs describing cross-pillar experience and Phase 3 PRDs per pillar.

## Teams
Engineering, Data Engineering, Security, AI/ML.

## Completion Criteria
- [ ] Every one of the 9 domains has an explicit, non-overlapping list of exclusively owned entities.
- [ ] Cross-domain data sharing rules validated against at least two real cross-pillar PRD scenarios (one Finance-Health, one Productivity-Finance).
- [ ] No domain boundary requires direct cross-database access in the documented design.
- [ ] Boundary violation detection mechanism agreed upon and assigned an owning team.
- [ ] Signed off by: Principal Architect (required), Data Engineering Lead (required), Security Lead (required).
