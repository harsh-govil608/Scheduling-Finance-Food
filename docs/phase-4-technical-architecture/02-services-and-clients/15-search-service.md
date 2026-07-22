# Document 15: Search Service

## Document Name
Search Service

## Purpose
Define the architecture of the backend service that builds and serves the unified, cross-pillar search index underlying Phase 3's Search PRD — indexing tasks, transactions, meal/health entries, notes and journal and knowledge-vault content, and AI memory — and that aggregates results into one ranked list regardless of which pillar owns the underlying data.

## Why It Exists
Phase 2's Search Experience and Phase 3's Search PRD promise that a user can search once and find anything — a task, a transaction, a meal log, a past conversation — without knowing which pillar owns it. But each pillar's data lives in a separately-owned service per Document 03 Domain Boundaries, so nothing in any individual service's architecture explains how a single query fans out across all of them and returns a coherent, ranked result within an acceptable latency budget, or how a newly created entity becomes searchable within a bounded delay. Without this document, search either becomes a set of ad hoc per-service query calls stitched together on the client — multiplying latency and breaking the "one assistant" promise — or unbounded scope creep inside a service that was never meant to own indexing.

## Approximate Page Count
8-10 pages.

## Sections
1. **Service Boundary & Responsibility** — what the Search Service owns (index, query, ranking) versus what source-of-truth services continue to own (the underlying entity data itself).
2. **Indexed Entity Model** — the cross-pillar catalog of what is indexed — tasks, transactions, meal/health entries, notes, journal, knowledge vault, AI memory summaries — and at what granularity each is represented.
3. **Index Update Pipeline** — how source services propagate creates, updates, and deletes into the index, cross-referencing Document 04 Event Architecture, and the target freshness/staleness bound per entity type.
4. **Query & Ranking Architecture** — the shape of query fan-out, relevance ranking, and cross-entity-type result blending, without prescribing the ranking algorithm itself, which is deferred to Phase 5 wherever AI-driven ranking is involved.
5. **Permission-Aware Search** — how search results respect the same access and consent boundaries as direct service access, cross-referencing Document 08 Authorization and the Permissions & Consent PRD.
6. **Multi-Region Considerations** — where the index is built and served given multi-region deployment, and how index freshness is maintained across regions.
7. **Scaling Characteristics at 100M+ Users** — read (query) versus write (index-update) pattern assumptions and index sharding/partitioning strategy.
8. **Search as a Retrieval Platform for AI Memory & Context Timeline** — the boundary at which Search Service supplies retrieval capability to Phase 3's AI Memory PRD and Context Timeline PRD without owning their semantics or ranking logic.
9. **Latency & Availability Targets** — the service-level objectives required because search sits on the interactive path of the product.
10. **Observability & Query Analytics** — tracking what is searched for, zero-result queries, and result-click feedback, feeding Document 17 Analytics Service and ongoing product iteration.

## Deliverables
* Approved Search Service architecture with an indexed-entity catalog and update-pipeline diagram.
* Documented freshness/staleness SLO for each indexed entity type.
* Permission-aware query architecture reviewed against the Authorization model.
* Latency and availability SLOs for the query path.

## Dependencies
Requires Document 01 Overall System Architecture, Document 02 Service Decomposition, Document 03 Domain Boundaries, Document 04 Event Architecture, Document 05 API Architecture, Document 06 Gateway, Document 07 Authentication, and Document 08 Authorization (Core Platform group). Requires Document 09 User Service, Document 10 Calendar Service, Document 11 Task Service, Document 12 Finance Service, Document 13 Health Service, and Document 16 Media Service as source-of-truth data owners. Implements Phase 2 Search Experience and Phase 3 Search PRD; cross-references Phase 3 AI Memory PRD, Context Timeline PRD, and Permissions & Consent PRD.

## Teams
Backend Engineering, Search/Data Infrastructure, AI/ML (boundary only), Product, SRE, Security.

## Completion Criteria
- [ ] Indexed-entity catalog cross-checked against the Search PRD's "what must be searchable" requirements with no gaps.
- [ ] Index freshness SLOs agreed with each source-of-truth service owner (Documents 09-13, 16).
- [ ] Permission-aware query model reviewed and approved by Security against Document 08 Authorization.
- [ ] Query latency SLOs validated as achievable at 100M+ user scale by the Principal Architect.
- [ ] Signed off by: VP Engineering (required), Principal Architect (required), Head of AI/ML (required, for the retrieval boundary).
