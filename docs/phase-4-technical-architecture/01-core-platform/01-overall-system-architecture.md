# Document 01: Overall System Architecture

## Document Name
Overall System Architecture

## Purpose
Define the single top-level architectural map of the AI Life Operating System — how client applications, the API gateway layer, backend services, the event bus, data stores, and the AI platform boundary relate to one another. This document establishes the canonical system diagram and vocabulary that every other Phase 4 document zooms into.

## Why It Exists
Without one authoritative system map, every engineering team, PRD reviewer, and new hire reconstructs their own mental model of how the platform fits together, and those mental models diverge — one team assumes synchronous calls where another assumes events, one assumes a single global deployment where another assumes regional isolation. At 100M+ users across multiple regions, with three pillars (Productivity, Finance, Health) that must feel like one proactive assistant, that divergence produces incompatible service contracts, duplicated infrastructure, and integration failures discovered only in production. This document exists to be the single reference that all 47 PRDs and all subsequent Phase 4 documents are consistent with, so "the architecture" means one specific thing across the company.

## Approximate Page Count
10-14 pages.

## Sections
1. **System Context Diagram** — the C4-style "level 1" view: users, client apps, external integrations (calendars, banks, wearables), and the system as a single box, showing what is inside vs. outside the system boundary.
2. **Container-Level Architecture Diagram** — the "level 2" view: client apps, API gateway, backend services grouping, event bus, data stores, AI platform, and observability/infra as distinct containers with labeled relationships.
3. **Client Application Layer Overview** — how mobile-first apps, web, and any companion surfaces (widgets, wearable companions) consume the backend, at a level that Document 06 (Gateway) will expand on.
4. **Backend Services Layer Overview** — a summary map of the 9 backend services this product needs, cross-referencing Document 02 (Service Decomposition) rather than duplicating it.
5. **Communication Patterns Overview** — where synchronous request/response is used vs. asynchronous event-driven communication, cross-referencing Document 04 (Event Architecture) and Document 05 (API Architecture) for detail.
6. **Data Layer Overview** — the categories of data stores in play (operational, analytical, cache, search, media) without prescribing final per-service storage choices, cross-referencing Document 03's sibling document group (Platform & Data).
7. **AI Platform Boundary** — where the AI/ML decisioning and orchestration layer sits relative to backend services and events, explicitly scoped as a boundary/interface definition only (internals deferred to Phase 5).
8. **Multi-Region & Deployment Topology (Summary)** — a high-level statement of the multi-region, highly-available deployment posture, cross-referencing the Infrastructure & Observability and Scalability document groups for depth.
9. **Cross-Pillar Coordination Flow** — a concrete end-to-end trace of one cross-pillar scenario (e.g., a calendar change triggering a finance and health re-plan) through every layer of the diagram, to prove the architecture actually supports the "one assistant" mission.
10. **Architectural Principles & Non-Goals** — the standing principles (event-driven by default, consent-first, mobile-first, AI-first) and explicit non-goals (e.g., this document does not select vendors or define AI model internals) that constrain every downstream Phase 4 document.

## Deliverables
* Approved Overall System Architecture document with system context and container-level diagrams.
* A shared architectural vocabulary/glossary (service, domain, event, gateway, AI platform boundary) referenced by all subsequent Phase 4 documents.
* A traced end-to-end cross-pillar scenario diagram.

## Dependencies
Requires Phase 1 Company Foundation (mission, Proactivity Ladder), Phase 2 Product Definition (42 UX/behavior docs, especially cross-pillar experience docs), and Phase 3 Product Specifications (all 47 PRDs, as the source of required capabilities). No Phase 4 dependencies — this is the root document all others depend on.

## Teams
Engineering, Platform/Infrastructure, Security, Data Engineering, AI/ML, SRE, Product.

## Completion Criteria
- [ ] System context and container-level diagrams reviewed and approved by all Phase 4 document owners.
- [ ] At least one full cross-pillar scenario traced end-to-end through the diagram without gaps.
- [ ] Every subsequent Phase 4 document's scope cross-checked against this document with no contradictions.
- [ ] Explicit non-goals section confirms no AI/ML internals or final vendor selections are present.
- [ ] Signed off by: CTO/VP Engineering (required), Principal Architect (required), Head of AI/ML (required).
