# Document 18: Data Deletion

## Document Name
Data Deletion

## Purpose
Define the mechanics of how a deletion request — user-initiated, consent-withdrawal-triggered, retention-expiry-triggered, or legally required — is executed end-to-end, with the hard technical requirement that deletion propagates to every derived and downstream copy of the data, including vector embeddings and AI-formed memory, not just the originating source record.

## Why It Exists
"Delete my data" only means something if deletion is actually complete, and this product has more places for a deleted fact to hide than most: a primary database record, a read replica, a cache entry, a search index, a backup, an analytics warehouse row, and — uniquely load-bearing here — a vector embedding and an AI memory entry derived from it. Embedding & Vector Store Strategy (Phase 5 Doc 10) and Memory System Architecture (Phase 5 Doc 07) already flagged, from the ML engineering side, that a forgotten memory's vector must be purged from the index rather than merely excluded by a query-time filter; this document is where that requirement stops being an aspiration inside an ML architecture document and becomes an enforced, verifiable, auditable cross-system deletion contract that every service owner is held to.

## Approximate Page Count
9-11 pages

## Sections
1. **Deletion Request Types & Triggers** — user-initiated deletion (single item, category, or full account), consent-withdrawal-triggered deletion, retention-expiry-triggered deletion, and legally required deletion.
2. **Deletion Scope Definition** — what "delete this" precisely means per request type, removing ambiguity about whether derived or related data is included by default versus requiring a separate, explicit request.
3. **Cascading Deletion Map** — the traversal from a source record to every place it has been copied, cached, derived, or embedded (primary database, read replicas, caches, search index, vector store, AI memory, logs, backups, analytics warehouse, third-party sub-processors), built on the ownership map in Data Ownership (Doc 15) and the entity model in the Canonical Data Model (Phase 4 Doc 56).
4. **AI Memory & Vector Store Deletion Contract** — the non-negotiable requirement, extending the forgetting mechanics of Memory System Architecture (Phase 5 Doc 07) and the purge requirement of Embedding & Vector Store Strategy (Phase 5 Doc 10), that a deleted source record's derived memory entries and vector embeddings are physically purged, not soft-filtered, within a defined time bound.
5. **Deletion Propagation SLA & Verification** — the maximum time bound for a deletion to reach every system in the cascading deletion map, and the verification mechanism that proves completion rather than assuming every downstream consumer complied.
6. **Backup & Archival Deletion Handling** — how deletion is honored in backup and archive copies that cannot always be edited in place, including the maximum acceptable lag before a backup is naturally aged out or actively scrubbed.
7. **Partial Failure & Retry Handling** — what happens when a deletion cascade fails partway through one downstream system, how that failure is detected, retried, and escalated rather than silently reported as complete.
8. **Deletion Audit Trail** — the record kept to prove a deletion occurred (what, when, and scope), designed so the audit record itself does not reintroduce the deleted personal data.
9. **Irreversibility & Grace Period Policy** — whether a deletion request carries a cancellable grace period before becoming irreversible, and how that window is communicated so it does not undermine the user's confidence that deletion is real and final.
10. **Third-Party & Sub-Processor Deletion Propagation** — how a deletion request is forwarded to, and confirmed by, third parties or sub-processors holding a copy of the data on the company's behalf.

## Deliverables
- Cascading deletion map per data class, covering every store type including vector store and backups.
- AI Memory & vector store deletion technical contract.
- Deletion propagation SLA table with a verification method per downstream system.
- Partial-failure detection and retry runbook.
- Deletion audit log specification.

## Dependencies
Phase 6 Data Lifecycle (Doc 16); Phase 6 Data Ownership (Doc 15); Phase 5 Memory System Architecture (Doc 07); Phase 5 Embedding & Vector Store Strategy (Doc 10); Phase 4 Data Architecture & Canonical Data Model (Doc 56); Phase 4 Backups (Doc 36); Phase 3 Permissions & Consent PRD (Doc 41).

## Teams
Engineering, AI/ML, Data Platform, Security, Privacy/DPO, Legal, Compliance

## Completion Criteria
- [ ] Cascading deletion map validated against every store type a data class can live in, including the vector store and backups.
- [ ] AI Memory and vector store purge verified with an actual test deletion and confirmed absence from the index, not merely absence from query results.
- [ ] Propagation SLA agreed by every downstream system owner named in the cascading deletion map.
- [ ] Partial-failure handling tested against at least one simulated downstream failure.
- [ ] Signed off by: Head of Privacy/DPO (required), CISO (required), Head of AI/ML (required), Head of Engineering (required).
