# Document 37: Notes PRD

## Document Name
Notes PRD

## Purpose
Define structured note-taking as the product's generic, tag/link-organized capture surface — titled notes with structure, linking, and search — distinct from the Journal's reflective free-text stream and from a task's descriptive fields. It defines the note data model, organizational mechanics (tags, folders, linking to other entities), and how notes surface through search and cross-pillar context, not the emotional/reflective capture pattern owned by the Journal PRD.

## Why It Exists
An AI Life Operating System still needs one place for a user to deliberately write and structure information they'll want to find again — a packing checklist template, meeting notes, a running list of gift ideas — and without a dedicated, clearly-bounded home for that, this content either gets crammed into Journal entries (polluting the mood/reflection signal Journal is meant to produce) or into Task descriptions (polluting the action-tracking signal Tasks are meant to produce). This PRD exists specifically to draw that boundary precisely enough that engineering and the AI's context model both know, unambiguously, which of the three capture surfaces — Notes, Journal, Knowledge Vault — a given piece of content belongs to, preventing Notes from becoming an undifferentiated catch-all that undermines the product's Information Architecture.

## Approximate Page Count
6-8 pages

## Sections
1. **Feature Scope** — In scope: structured note creation (title, body, tags, optional folder), linking a note to other entities (a task, a trip, a contact), full-text search integration, and note-to-note linking/backlinks. Out of scope: free-text reflective/mood-tagged entries (owned by the Journal PRD), long-form saved reference material and external documents/articles (owned by the Knowledge Vault PRD), and task-specific description fields or subtask notes (owned by the Task Management PRD).
2. **User Stories** — As a user in a meeting, I want to quickly create a structured note with a title and tag it to a project; as a user planning a gift, I want a running note I can keep adding ideas to over weeks and find again by searching "gift"; as a user, I want to link a note directly to a task so opening the task shows the related note; as a user, I want to organize notes into folders that mirror how I think about my life areas; as a user, I want the AI to suggest linking a new note to a related existing note or task when the content clearly overlaps.
3. **Functional Requirements** — Define the note schema (title, body, tags, folder, linked-entity references, created/modified timestamps), the linking mechanism between notes and other entities (tasks, trips, contacts), the search-indexing requirements for full-text and tag-based retrieval, and the AI-suggested-link logic (content-similarity threshold for proposing a link).
4. **Non-Functional Requirements** — Define search-latency ceilings for note retrieval, sync-consistency requirements across devices for concurrent note edits, offline-editing and conflict-resolution behavior, and the privacy boundary on which note content can be used as AI context versus requiring explicit per-note opt-out.
5. **UX Requirements** — This feature must conform to the Information Architecture (Phase 2) for where Notes lives in navigation relative to Journal and Knowledge Vault, and to the Search Experience (Phase 2) for how notes appear in unified search results; feature-specific UX rules must cover how linked-entity references are displayed inline within a note and how AI-suggested links are presented for accept/reject rather than applied silently.
6. **States & Flows** — Enumerate the note lifecycle: draft (unsaved/in-progress) → saved → linked (has at least one entity reference) → archived → deleted (soft-delete with recovery window), plus the flow for converting an AI-detected pattern (e.g., a recurring note topic) into a suggested folder or tag.
7. **Edge Cases** — Cover a note linked to an entity that is later deleted (dangling-reference handling), a note edited simultaneously on two devices, an extremely long note approaching size/performance limits, and note tags that conflict with or duplicate an existing folder structure.
8. **Failure Scenarios** — Define behavior when the core assumption — that notes sync reliably and links stay valid — breaks: a sync failure that risks data loss on concurrent edits, a broken link to a deleted task/trip left unresolved in the UI, and search-index staleness that causes a note to be temporarily unfindable after creation.
9. **AI Behaviors** — Detail how the AI's role in Notes is bounded to organizational assistance rather than content generation by default — suggesting tags, links, or folder placement — and how these suggestions are gated by the Proactivity Ladder from silent indexing up to auto-applied tagging once trust is established.
10. **Notification Behaviors** — Define the deliberately minimal set of note-related events that warrant a notification, such as a suggested link to a newly relevant note, versus the default of no notifications for routine note-taking, and how this integrates with the Notification System's arbitration to avoid over-notifying for a low-urgency capture feature.
11. **Success Criteria** — A user should be able to find any note they wrote within seconds via search, and should never feel unsure whether a given piece of content belongs in Notes, Journal, or Knowledge Vault.
12. **Metrics** — Define targets such as note search-success rate (query results in the intended note being opened), AI-suggested-link acceptance rate, and percentage of notes with at least one entity link (a proxy for cross-pillar integration depth).
13. **Open Questions** — Capture unresolved questions such as whether collaborative/shared notes are in scope for v1, and how much structure (e.g., templates) should be imposed by default versus left fully freeform.

## Deliverables
- Full Notes PRD document following the 13-section structure above.
- Note data model and lifecycle-state diagram.
- Scope-boundary comparison table (Notes vs. Journal vs. Knowledge Vault vs. Task descriptions).
- AI-suggested-linking decision-flow diagram.

## Dependencies
Phase 3: Journal PRD, Knowledge Vault PRD, Task Management PRD. Phase 2: Information Architecture, Search Experience, Automation Philosophy, Notification System. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Backend, Search), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Note lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] Scope boundary against Journal PRD, Knowledge Vault PRD, and Task Management PRD confirmed with no functional overlap.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
