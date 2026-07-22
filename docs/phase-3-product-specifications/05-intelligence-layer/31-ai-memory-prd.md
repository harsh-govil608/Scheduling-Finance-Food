# Document 31: AI Memory PRD

## Document Name
AI Memory PRD

## Purpose
This PRD will define the user-facing feature for what the AI remembers about a user, how a user views, corrects, and forgets that memory, and how a remembered fact is allowed to visibly resurface inside a suggestion, reminder, or coaching moment. It defines the memory *feature surface* — the screens, controls, and disclosure behaviors — not the underlying storage, retrieval, or embedding architecture that produces the memory, which is reserved for Phase 5.

## Why It Exists
The Memory Model — Behavioral Perspective (Phase 2) established that memory must be legible and controllable in principle, but it stopped short of specifying the concrete feature: what screen a user opens to see their memories, what an edit action actually does end-to-end, and what "forgotten" must look like the moment after a user asks. Without this PRD, engineering has no committed spec to build against and each pillar will quietly implement its own half-version of "the AI remembered this," producing a product where memory feels reliable in Finance and untrustworthy in Health. Because memory underwrites every other proactive behavior in the product — a wrong or un-correctable memory poisons every downstream suggestion — this is one of the highest-trust-risk PRDs in the Intelligence Layer and must specify correction and forgetting with zero ambiguity.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: the memory viewing surface, edit/correct/forget controls, and the mechanism by which a memory-sourced suggestion visibly cites its source. Out of scope: the underlying memory storage/retrieval architecture and the ML model that forms or scores memories (Phase 5), and the general context-freshness rules for transient signals (owned by the Context Timeline PRD).
2. **User Stories** — As a user, I want to see why the AI suggested something ("remembered you skip breakfast on Mondays") so I can trust or challenge it; as a user, I want to correct a wrong memory ("I no longer live at that address") and know it won't keep affecting suggestions; as a user, I want to ask the AI to forget an entire category of facts (e.g., everything about a past relationship) in one action; as a user, I want to browse what the AI remembers about me by pillar rather than hunting through settings; as a user who corrected a memory once, I want to trust that the correction actually stuck the next time a related suggestion appears.
3. **Functional Requirements** — Define the memory browsing surface's organization (by pillar, by recency, by category), the citation mechanism that attaches a "based on this memory" reference to a suggestion, the edit flow and its required confirmation step, the forget flow at both single-fact and category granularity, and the propagation requirement that an edited or forgotten fact stops influencing any in-flight or future suggestion within a defined time bound.
4. **Non-Functional Requirements** — Define the latency ceiling between a correction/forget action and its effect being reflected in the next suggestion, the requirement that forgetting be irreversible from the user's perspective once confirmed (no silent retention), and the privacy constraint that memory contents displayed to the user never expose signals the user never consented to sharing.
5. **UX Requirements** — This feature must conform to the Memory Model — Behavioral Perspective and the User Control Model (Phase 2) for how edit/forget map to the Control Primitives (Override, Undo), and to Personalization for how tone of memory citations may vary per user; feature-specific UX rules must cover how a citation is visually distinguished from a plain suggestion and how a "forgetting in progress" state is communicated before completion.
6. **States & Flows** — Enumerate the lifecycle a memory moves through: observed → candidate → confirmed/stored → surfaced (cited in a suggestion) → [edited → re-confirmed] or [forget-requested → forgotten], including the branch where a user disputes a memory that was never shown to them directly (inferred-only memory).
7. **Edge Cases** — Cover a memory that contradicts a more recent one (which wins, and how the conflict is surfaced), a forget request for a fact that is embedded inside another still-valid memory, a correction that itself turns out to be wrong, and a user requesting to see "everything" the AI remembers when the catalogue is large enough to overwhelm a single screen.
8. **Failure Scenarios** — Define behavior when the core assumption — that a correction or forget instruction is reliably and promptly honored — breaks: a correction that fails to propagate to an already-queued suggestion, a forget request made while offline, and a system that cannot determine whether a fact was ever actually forgotten (audit-of-forgetting problem).
9. **AI Behaviors** — Detail how memory correction and forgetting feed the Proactivity Ladder: repeated corrections of memory-sourced suggestions in a category should demote autonomy for that category, while a long run of unchallenged citations should be treated as one input toward earning higher autonomy; define how the AI decides which remembered facts are worth surfacing as a citation versus using silently, and how a corrected fact is weighted differently from an unconfirmed inference going forward.
10. **Notification Behaviors** — Define whether a successful forget/correction action generates a confirmation notification or only an in-app acknowledgment, how a memory-triggered suggestion's citation interacts with the Notification System's arbitration (a citation is never itself a separate interruption), and how conflicting-memory edge cases are escalated to the user if at all.
11. **Success Criteria** — State the qualitative bar: a user should be able to explain, in their own words, why the AI made a given suggestion by pointing at a remembered fact, and should never discover a fact they explicitly forgot still influencing behavior.
12. **Metrics** — Define quantitative targets such as citation-click-through rate, correction rate per suggestion category, time-to-propagation after a correction, forget-request completion rate within the defined latency ceiling, and rate of repeated corrections to the same fact (a proxy for correction not sticking).
13. **Open Questions** — Capture unresolved questions such as how to handle a forget request for a fact that other pillars still functionally depend on, whether memory citations should be shown by default or only on request, and how "forgetting" is reconciled with any legal/compliance retention requirement.

## Deliverables
- Full AI Memory PRD document following the 13-section structure above.
- Memory lifecycle state diagram (observed → stored → surfaced → edited/forgotten).
- Citation UX requirements list for Design.
- Correction/forget propagation SLA table for Engineering.

## Dependencies
Phase 2: Memory Model — Behavioral Perspective, Context Engine — Product Perspective, Personalization, User Control Model, Notification System. Phase 1: Product (Behavioral) Philosophy Document, Guiding Principles Document. Phase 3: Context Timeline PRD (for the distinction between visible activity history and durable memory).

## Teams Using This
Product, Engineering (AI Platform), AI/ML, Design, Trust & Safety, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Memory lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Correction and forget propagation requirements validated against the Memory Model — Behavioral Perspective's trust principles.
- [ ] Scope boundary against the Context Timeline PRD confirmed with no functional overlap.
- [ ] Signed off by: Head of Product (required), Head of AI/ML (required), Head of Trust & Safety (required).
