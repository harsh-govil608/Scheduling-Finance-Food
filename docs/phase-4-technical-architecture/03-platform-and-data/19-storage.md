# Document 19: Storage

## Document Name
Storage

## Purpose
Define the requirements and selection criteria for object/blob storage of unstructured and semi-structured content — meal photos, receipt scans, uploaded documents, voice memos, exported reports, and other binary artifacts — as distinct from the structured, queryable data covered in Document 20 (Databases). This document specifies what any object storage solution must guarantee, not which vendor or product is chosen.

## Why It Exists
The product's proactive pillars generate binary content constantly and by design — Health captures meal and workout photos, Finance captures receipt and statement scans, Productivity and cross-pillar features capture voice notes and generated PDF summaries — and at 100M+ users this content is the single largest storage volume the system produces, growing continuously and rarely deleted outright. Treating blob storage as an afterthought bolted onto a relational database (storing binaries as BLOBs, or scattering upload logic per service) creates cost, latency, and compliance liabilities: oversized database backups, inconsistent access-control enforcement on sensitive personal photos, and no consistent lifecycle policy for content that ages out of relevance. This document exists so every backend service uploads, retrieves, and retires binary content through one consistently governed storage layer with clear ownership of durability, access control, and cost.

## Approximate Page Count
7-9 pages.

## Sections
1. **Scope & Content Inventory** — the categories of binary content produced across services (meal/workout photos, receipt/document scans, voice memos, generated exports, AI-generated media) and their approximate size/volume/growth profile per user.
2. **Selection Criteria** — the required properties any object storage solution must satisfy (durability SLA, regional availability, cost-per-GB at 100M-user scale, native lifecycle policy support, encryption-at-rest, presigned-access support) without naming a specific vendor.
3. **Access Pattern & API Contract** — the standard upload/download/delete interface backend services use, including presigned URL patterns so clients upload directly without transiting application servers.
4. **Access Control & Consent Enforcement** — how object-level permissions tie back to the user's consent state (Phase 1/2 privacy commitments), ensuring a photo or document is never retrievable outside its owning user's authorization context.
5. **Multi-Region Replication & Data Residency** — replication strategy for binary content across regions, and how regional data-residency requirements constrain where a given user's objects may physically live.
6. **Lifecycle & Retention Policy** — rules for tiering aging content to cheaper storage classes, and deletion/expiry rules aligned with retention commitments and user-initiated data deletion requests.
7. **Media Processing Pipeline Interface** — the boundary between raw object storage and downstream processing (thumbnailing, OCR on receipts, transcription of voice memos), clarifying that processing logic itself lives in owning services, not in the storage layer.
8. **Cost Management** — expected cost drivers at scale (storage volume, egress, request volume) and the controls (lifecycle tiering, compression, dedup) used to keep cost proportional to active usage.
9. **Backup, Durability & Disaster Recovery** — durability guarantees required and cross-region recovery expectations if a region is lost.

## Deliverables
* Approved Storage document defining object storage selection criteria and standard access contract.
* Content inventory mapping binary content types to owning services.
* Lifecycle and retention policy applicable across all content categories.
* Data residency decision matrix by region.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Domain Boundaries.

## Teams
Platform/Infrastructure, Engineering, Security, Data Engineering, Privacy/Legal.

## Completion Criteria
- [ ] Every binary content type produced by a Phase 3 PRD is mapped to a storage category and owning service.
- [ ] Selection criteria are vendor-neutral and cover durability, residency, encryption, and cost.
- [ ] Access control model verified against at least one sensitive-content scenario (e.g., meal photo) with no unauthorized-access path.
- [ ] Lifecycle and retention policy reconciled with user-initiated deletion requirements.
- [ ] Signed off by: Principal Architect (required), Head of Security (required), Privacy/Legal Lead (required).
