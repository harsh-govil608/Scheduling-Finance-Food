# Document 40: Multi-tenancy

## Document Name
Multi-tenancy

## Purpose
Define the data isolation architecture that guarantees one user's data is never accessible to another absent explicit consent, establishing the individual user account as the default tenancy unit across all 9 backend services. This document also specifies how the Phase 3 Shared Family Mode PRD's multi-user households are modeled as an explicit, opt-in overlay on top of that default, so a household member's shared access is a bounded, auditable exception rather than a redefinition of the platform's baseline isolation guarantee for the overwhelming majority of users who never opt into sharing.

## Why It Exists
Every architectural document up to this point assumes a single-user relationship between one person and their AI, and that assumption is a trust commitment, not just a technical default — a user who discovers their financial or health data was reachable by another account, even accidentally, experiences a severe breach of the product's core promise. Shared Family Mode introduces the platform's first legitimate second class of data relationship, and if it is implemented by loosening the underlying tenancy model rather than by adding a scoped, opt-in overlay, it risks quietly weakening the isolation guarantee for every user, including the large majority who never join a shared space. This document exists to make the isolation guarantee an enforced architectural property — verified at the data-access layer across all services — rather than a per-feature convention that a single service can silently violate.

## Approximate Page Count
7-9 pages.

## Sections
1. **Tenancy Model Definition** — the individual user account as the default tenancy unit (not an organization/company model), consistent with the single-user architecture assumption established across Phase 1-3.
2. **Data Isolation Guarantee** — the enforced boundary ensuring one user's data across all 9 services is unreachable by another user absent explicit consent, and the requirement that this is enforced at the data-access layer (e.g., mandatory partition key or row-level security checks), not solely by application-layer logic.
3. **Shared Family Mode Tenancy Extension** — how a shared household from the Phase 3 Shared Family Mode PRD is modeled as its own addressable entity referencing member user IDs, with per-item visibility rules, rather than as a merged multi-user tenant — preserving default single-user isolation for every account not explicitly part of a shared space.
4. **Cross-Service Tenancy Enforcement Consistency** — the requirement that all 9 backend services enforce the identical tenancy and isolation model, cross-referencing Databases, so no service can introduce its own ad hoc sharing shortcut.
5. **Authorization & Consent Binding** — how tenancy boundaries bind to consent state defined in the Permissions & Consent PRD, including how a per-item "keep private within a shared space" override is enforced at the data-access layer, not only hidden in the UI.
6. **Isolation Failure Blast Radius** — the containment requirement that any isolation defect affects the smallest possible population — bounded to, at most, members of a single shared space rather than the broader user base.
7. **Tenancy-Aware Caching & Search Indexing** — the requirement that caches and search indexes, cross-referencing Storage and Databases, partition by tenant so a secondary system cannot become an unintended cross-tenant leak path.
8. **Auditability of Cross-User Access** — the logging and audit-trail requirement for any access to another user's data, including authorized access within a consented shared space, feeding the Infrastructure & Observability document group.
9. **Testing & Verification Requirements** — the required isolation-boundary testing approach (e.g., automated tenancy-boundary fuzz or adversarial testing) that must pass before any release touching a multi-tenant-adjacent code path.

## Deliverables
* Approved Multi-tenancy document with the tenancy model definition and enforcement pattern.
* Mandatory data-access-layer isolation pattern (e.g., partition key / row-level security requirement) applied consistently across all 9 services.
* Shared Family Mode data-access boundary diagram distinguishing shared-space entities from individual user data.
* Isolation test plan, including required adversarial/fuzz testing coverage.

## Dependencies
Requires Databases, Storage, Overall System Architecture, Service Decomposition, API Architecture (Phase 4). Requires the Phase 3 Shared Family Mode PRD and Permissions & Consent PRD for the shared-space model and consent binding; governed by the Phase 2 User Control Model and Permissions & Consent UX for how sharing-scope changes are surfaced and reversed.

## Teams
Engineering, Security, Platform/Infrastructure, Data Engineering, Privacy/Legal, QA.

## Completion Criteria
- [ ] Data isolation guarantee is enforced at the data-access layer, not application logic alone, for all 9 backend services.
- [ ] Shared Family Mode is modeled as a scoped, opt-in overlay with no change to the default isolation guarantee for non-family users.
- [ ] Per-item "private within a shared space" overrides are verified enforced at the data-access layer.
- [ ] Audit logging covers every instance of cross-user data access, including consented shared-space access.
- [ ] Isolation-boundary testing is defined as a required release gate for any multi-tenant-adjacent change.
- [ ] Signed off by: Head of Security (required), Principal Architect (required), Privacy/Legal Lead (required).
