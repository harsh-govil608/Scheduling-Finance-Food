# Document 17: Analytics Service

## Document Name
Analytics Service

## Purpose
Define the architecture of the internal product and usage analytics pipeline — event ingestion, aggregation, and reporting used by Product, Growth, and Engineering to understand feature usage and system health — as distinct from the user-facing AI Memory and Context Timeline features, which consume similarly shaped event data for a different purpose.

## Why It Exists
A 100M+ user, AI-first, event-driven product generates enormous volumes of behavioral signal, and Phase 3 already defines two user-facing features — AI Memory and Context Timeline — that consume "what did the user do" data to serve the user back their own life. Without an explicit architecture document, engineers risk either building internal product analytics as a side effect of those user-facing pipelines, coupling internal reporting concerns to a feature governed by strict per-user consent and memory-management rules, or building analytics with looser privacy handling than the user-facing pipeline, undermining the consent model defined in Phase 3's Permissions & Consent PRD. This document exists to give internal analytics its own architecture while explicitly requiring it to inherit the same privacy principles as user-facing data: aggregation-first thinking, consent-respecting collection, and no analytics use that circumvents user-facing controls.

## Approximate Page Count
7-9 pages.

## Sections
1. **Service Boundary & Responsibility** — what Analytics Service owns (internal event ingestion, aggregation, dashboards and reporting feeds) versus what it explicitly does not own — it is not the AI Memory or Context Timeline data store.
2. **Relationship to AI Memory & Context Timeline** — an explicit boundary statement distinguishing internal product analytics from the user-facing memory/timeline pipelines, including where the two may share an underlying event stream without sharing storage or purpose.
3. **Event Ingestion Architecture** — how client and backend events reach the analytics pipeline, cross-referencing Document 04 Event Architecture, and the sampling/volume strategy required at 100M+ user scale.
4. **Aggregation & Storage Model** — the architecture-level shape of raw-event versus aggregated-metric storage, without prescribing a final analytical data warehouse vendor.
5. **Privacy-Respecting Collection Architecture** — how consent state, sourced from Document 08 Authorization and the Permissions & Consent PRD, gates what is collected; how personally identifiable information is minimized or pseudonymized at ingestion; and how the same user-facing deletion and export rights defined in the Data Export & Portability PRD apply to internal analytics data.
6. **Access Model for Internal Consumers** — how Product, Growth, and Engineering query aggregated analytics without direct access to raw per-user event streams.
7. **Multi-Region Considerations** — where analytics events are collected and aggregated given multi-region deployment, and how regional data-residency rules constrain cross-region aggregation.
8. **Scaling Characteristics at 100M+ Users** — ingestion throughput assumptions and aggregation latency, distinguishing real-time dashboard needs from batch reporting needs.
9. **Retention & Deletion Architecture** — retention windows for raw versus aggregated analytics data, and the propagation path by which a user's deletion request reaches this service.

## Deliverables
* Approved Analytics Service architecture with ingestion and aggregation diagrams.
* An explicit written boundary statement distinguishing this service from AI Memory and Context Timeline.
* Privacy-respecting collection architecture reviewed against the Permissions & Consent PRD.
* A documented deletion-propagation path validated against the Data Export & Portability PRD.

## Dependencies
Requires Document 01 Overall System Architecture, Document 02 Service Decomposition, Document 03 Domain Boundaries, Document 04 Event Architecture, Document 05 API Architecture, Document 07 Authentication, and Document 08 Authorization (Core Platform group). Requires Document 09 User Service through Document 13 Health Service, and Document 14 Notification Service through Document 16 Media Service, as event sources. Cross-references Phase 3 AI Memory PRD and Context Timeline PRD for boundary definition, and Permissions & Consent PRD and Data Export & Portability PRD for privacy alignment.

## Teams
Data Engineering, Backend Engineering, Product Analytics, Privacy/Legal, Security, SRE.

## Completion Criteria
- [ ] Boundary between Analytics Service and AI Memory/Context Timeline reviewed and confirmed unambiguous by the Head of AI/ML and the Head of Data.
- [ ] Privacy-respecting collection architecture reviewed and approved by Privacy/DPO.
- [ ] Deletion-propagation path tested end-to-end against a sample user deletion request.
- [ ] Internal access model reviewed by Security for least-privilege compliance.
- [ ] Signed off by: VP Engineering (required), Head of Data/Analytics (required), Head of Privacy/DPO (required).
