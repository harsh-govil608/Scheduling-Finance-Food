# Document 16: Media Service

## Document Name
Media Service

## Purpose
Define the architecture of the service handling upload, storage, processing-pipeline orchestration, and lifecycle/retention of user-submitted media — meal photos, receipt and transaction images, and documents — including the handoff boundary into Phase 5's AI processing, without covering that processing itself.

## Why It Exists
Phase 3's Expense Capture PRD and Meal Recognition PRD both depend on a user submitting a photo that is eventually turned into structured data by AI, and the Knowledge Vault PRD implies document storage as well — but none of those PRDs, and no Core Platform document, define how a photo gets from a device camera to durable storage, how it becomes reliably available to the AI processing pipeline, or how long it is retained and under what deletion and export rules. Without a dedicated Media Service architecture, every pillar that needs image capture would build its own ad hoc upload path, duplicating storage, CDN, and retention logic, and — more seriously — producing inconsistent privacy handling for what is, across pillars, among the most sensitive classes of user data: photos of receipts containing account numbers, and photos of meals tied to health data.

## Approximate Page Count
8-10 pages.

## Sections
1. **Service Boundary & Responsibility** — what Media Service owns (upload, storage, transformation, lifecycle) versus what consuming services own (the structured data extracted from media, owned by Finance Service for receipts and Health Service for meal photos).
2. **Upload Architecture** — direct-to-storage versus proxied upload patterns and resumable/chunked upload for mobile networks, cross-referencing Document 18 Client & Mobile Application Architecture for offline-queued capture.
3. **Storage & Format Model** — how media is organized and addressed at an architecture level, including per-user and per-pillar namespacing, without prescribing a final storage vendor, cross-referencing the Platform & Data document group's Storage document.
4. **Processing Pipeline Hooks** — the event/handoff boundary at which uploaded media becomes available to Phase 5's AI processing, such as meal recognition or receipt parsing — an interface definition only, explicitly excluding the AI processing itself.
5. **Access Control & Signed Delivery** — how media is served back to clients and to the AI processing pipeline securely, cross-referencing Document 07 Authentication and Document 08 Authorization.
6. **Retention & Deletion Policy Architecture** — how long raw media is retained after processing, how user-initiated deletion propagates, and alignment with Phase 3's Data Export & Portability PRD and Permissions & Consent PRD.
7. **Multi-Region & Data Residency Considerations** — where media is stored and replicated given multi-region deployment, and any regional data-residency constraints that apply to photographed financial or health information.
8. **Scaling Characteristics at 100M+ Users** — upload volume assumptions for daily meal photos and receipts, and CDN/read-path scaling.
9. **Cost & Lifecycle Tiering** — architecture-level hooks for moving processed or aged media to cheaper storage tiers, with final tiering policy owned by the Platform & Data and Scalability document groups.

## Deliverables
* Approved Media Service architecture with upload, storage, and processing-handoff diagrams.
* Defined retention/deletion policy architecture aligned with the Data Export & Portability PRD.
* Access-control model for media reviewed against Authentication/Authorization.
* Documented data-residency posture per deployment region.

## Dependencies
Requires Document 01 Overall System Architecture, Document 02 Service Decomposition, Document 03 Domain Boundaries, Document 04 Event Architecture, Document 05 API Architecture, Document 06 Gateway, Document 07 Authentication, and Document 08 Authorization (Core Platform group). Requires Document 12 Finance Service and Document 13 Health Service as primary consumers, and Document 18 Client & Mobile Application Architecture for capture and offline-queuing behavior. Implements storage needs of Phase 3's Expense Capture PRD, Meal Recognition PRD, and Knowledge Vault PRD; cross-references Data Export & Portability PRD and Permissions & Consent PRD; defers final storage implementation to the Platform & Data document group.

## Teams
Backend Engineering, Platform/Infrastructure, Security, Privacy/Legal, AI/ML (boundary only), Mobile Engineering.

## Completion Criteria
- [ ] Upload path reviewed against Document 18 Client & Mobile Application Architecture's offline-capture behavior.
- [ ] Processing-handoff interface reviewed and accepted as sufficient by AI/ML without exposing AI internals.
- [ ] Retention/deletion policy architecture cross-checked against Data Export & Portability PRD and Permissions & Consent PRD.
- [ ] Data-residency posture reviewed by Legal/Privacy for every deployment region.
- [ ] Signed off by: VP Engineering (required), Principal Architect (required), Head of Privacy/DPO (required), Head of AI/ML (required, for the processing handoff interface).
