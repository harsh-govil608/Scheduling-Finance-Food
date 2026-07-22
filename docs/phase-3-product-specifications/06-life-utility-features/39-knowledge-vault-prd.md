# Document 39: Knowledge Vault PRD

## Document Name
Knowledge Vault PRD

## Purpose
Define the personal reference and document-storage feature — saved articles, uploaded documents, clipped web content — as the AI's durable external-memory store that Search and cross-pillar context can retrieve from, rather than a general cloud-storage or file-manager product. It defines the storage/ingestion model, metadata/tagging for retrievability, and how Vault content is surfaced through the Search Experience and AI context, not authored notes (owned by the Notes PRD) or reflective entries (owned by the Journal PRD).

## Why It Exists
The "Remember" pillar of the product's philosophy breaks down the moment a user has to leave the app to find something the AI should already know — a lease document, a saved article about a health condition, a passport expiry date buried in a PDF — so the Vault exists specifically to give the AI a durable, retrievable memory of externally-sourced material it did not generate and the user did not author as a note. Without a designed retrieval path back into Search and AI context, the Vault is indistinguishable from a generic cloud-drive feature; this PRD exists to ensure it earns its place as infrastructure for the AI's proactive recall rather than a bolted-on file manager.

## Approximate Page Count
7-9 pages

## Sections
1. **Feature Scope** — In scope: document/file upload and storage, saved-article/web-content clipping, metadata tagging and categorization, expiry/renewal-date extraction for eligible document types (e.g., passports, licenses), and retrieval integration with Search and AI context. Out of scope: authored structured notes (owned by the Notes PRD), reflective journal entries (owned by the Journal PRD), task-specific file attachments (owned by the Task Management PRD), and general-purpose cloud file sync/storage as a standalone product — this is a curated reference store, not a drive replacement.
2. **User Stories** — As a user, I want to save an article I'm reading so the AI can reference it later if I ask a related question; as a user, I want to upload my passport so the AI can proactively remind me before it expires; as a user, I want to ask "what's the WiFi password from the router setup doc I saved" and get a direct answer rather than having to reopen the file; as a user, I want saved documents organized by category (identity, home, health, finance) automatically; as a user, I want confidence that a sensitive uploaded document (e.g., an ID) is stored with stronger protection than a saved recipe article.
3. **Functional Requirements** — Define the vault-item schema (file/content type, source, category, extracted metadata, tags, sensitivity level), the ingestion pipeline for clipped web content versus uploaded files, the metadata/date-extraction logic for eligible document types, and the retrieval API contract that Search and AI context use to query vault content.
4. **Non-Functional Requirements** — Define storage size limits and supported file types, encryption-at-rest requirements scaled to sensitivity level, search/retrieval latency ceilings, and the strict privacy boundary excluding certain vault-content categories (e.g., identity documents) from general AI-context use even when other vault content is included.
5. **UX Requirements** — This feature must conform to the Search Experience (Phase 2) for how vault items appear in unified search results distinctly from notes and journal entries, and to the Information Architecture (Phase 2) for where the Vault lives in navigation; feature-specific UX rules must cover how sensitivity-tiered items are visually flagged and how extracted metadata (e.g., an expiry date) is surfaced without requiring the user to open the underlying file.
6. **States & Flows** — Enumerate the vault-item lifecycle: ingested (uploaded/clipped) → processing (metadata/date extraction running) → indexed (searchable, retrievable) → active → expiring-soon (for date-bound documents) → expired/archived, plus the flow for a user manually correcting mis-extracted metadata.
7. **Edge Cases** — Cover a document with no extractable metadata (falls back to manual tagging), a clipped article whose source page later goes offline, a duplicate upload of the same document, and a document type that spans multiple categories (e.g., a lease that's both a home and a finance document).
8. **Failure Scenarios** — Define behavior when the core assumption — that ingested content can be reliably parsed and indexed — breaks: metadata-extraction failure on an upload, search-index corruption or staleness, and a sensitivity-classification error that risks under-protecting a sensitive document (treated as a critical incident class).
9. **AI Behaviors** — Detail how the Vault feeds AI context and proactive reminders (e.g., expiry-date nudges) gated by the Proactivity Ladder, how confidence in extracted metadata affects whether the AI acts on it automatically versus asks for confirmation, and how retrieval ranking learns from which vault items a user actually opens after a search or AI reference.
10. **Notification Behaviors** — Define which vault events warrant a notification (an extracted expiry date approaching, a failed ingestion needing user attention) versus silent indexing, and how this integrates with the Notification System's arbitration to avoid competing with other pillar reminders for the same underlying deadline (e.g., a passport-renewal task).
11. **Success Criteria** — A user should trust the Vault enough to store genuinely important documents in it, and should be able to retrieve any saved item through natural search or a direct AI question within seconds.
12. **Metrics** — Define targets such as retrieval search-success rate, metadata-extraction accuracy rate, percentage of date-bound documents with a successfully surfaced reminder before expiry, and vault adoption rate (percentage of users storing at least one document).
13. **Open Questions** — Capture unresolved questions such as whether OCR/extraction should run entirely on-device for sensitive document types, and how the Vault's category taxonomy should be allowed to evolve as usage patterns emerge without breaking existing retrieval.

## Deliverables
- Full Knowledge Vault PRD document following the 13-section structure above.
- Vault-item data model and lifecycle-state diagram.
- Sensitivity-tiering and encryption-boundary table.
- Retrieval integration diagram (Vault → Search Experience / AI context).

## Dependencies
Phase 3: Notes PRD (scope boundary), Journal PRD (scope boundary), Task Management PRD. Phase 2: Search Experience, Information Architecture, Automation Philosophy, Notification System, Permissions & Consent UX. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Backend, Search, Security), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Vault-item lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] Scope boundary against Notes PRD and Journal PRD confirmed with no functional overlap.
- [ ] Sensitivity-tiering and encryption requirements reviewed by Security.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Security Lead (required).
