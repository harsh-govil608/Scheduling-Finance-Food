# Document 38: Journal PRD

## Document Name
Journal PRD

## Purpose
Define a lightweight free-text journaling feature that feeds the AI's cross-pillar context — mood/energy signals informing scheduling suggestions and health check-ins — rather than existing as an isolated notes feature. It defines the entry data model, optional mood/energy tagging, and precisely how, and under what consent, journal content is allowed to influence AI behavior in other pillars, not the structured note-taking patterns owned by the Notes PRD.

## Why It Exists
Without a designed connection back into the AI's context model, a journal is just a notes app bolted onto the product — it only belongs here if the AI actually uses it to serve the mission, for instance noticing a multi-day pattern of low-energy entries and gently adjusting how aggressively it proposes new commitments, or correlating a stressful entry with a spike in discretionary spending. This PRD exists to force an explicit, consent-gated answer to exactly which signals a journal entry is allowed to produce and where they're allowed to travel, so the feature earns its place as part of an AI Life Operating System rather than existing as a disconnected, low-engagement side feature.

## Approximate Page Count
6-9 pages

## Sections
1. **Feature Scope** — In scope: free-text entry creation, optional mood/energy tagging per entry, opt-in AI analysis of entries for cross-pillar context signals, and entry search/history browsing. Out of scope: rich-media journaling such as photo or voice entries (deferred to a future phase), structured note-taking (owned by the Notes PRD), and any therapeutic/clinical mental-health functionality (explicitly out of scope for the whole product, not just this PRD).
2. **User Stories** — As a user winding down at night, I want to quickly write a free-text entry without being forced to categorize or structure it; as a user who journals about feeling burnt out several days running, I want the AI to notice the pattern and gently ease off proactive scheduling suggestions rather than pile on more; as a user, I want to tag an entry's mood without writing anything else, on days I don't want to write; as a user, I want to review past entries from a specific week; as a privacy-conscious user, I want to journal with total confidence that entries are never used as AI context unless I've explicitly opted in.
3. **Functional Requirements** — Define the entry schema (free-text body, optional mood/energy tag, timestamp), the opt-in mechanism and its granularity (all entries vs. per-entry consent for AI analysis), the pattern-detection logic that derives cross-pillar signals from consented entries (e.g., a multi-day negative-mood streak), and how derived signals are handed off to other pillars — as an abstracted signal only, never as raw entry text.
4. **Non-Functional Requirements** — Define the encryption/storage requirements for entry content given its sensitivity, the latency ceiling for entry save (must feel instant even offline), the strict privacy boundary that raw entry text never leaves on-device analysis or is exposed to any pillar beyond the derived signal, and data-deletion guarantees (immediate, irreversible on user request).
5. **UX Requirements** — This feature must conform to the Information Architecture (Phase 2) for where Journal lives relative to Notes and Knowledge Vault, and to the Automation Philosophy (Phase 2) for how consent to cross-pillar use is requested and revocable; feature-specific UX rules must cover the entry-writing surface being maximally frictionless (no mandatory fields beyond text) and mood tagging being optional and quick (single tap).
6. **States & Flows** — Enumerate the entry lifecycle: draft (in-progress, autosaved) → saved → analyzed (if opted in; signal extracted) → archived, plus the consent flow where a user changes their opt-in setting and must be told clearly whether that affects past entries or only future ones.
7. **Edge Cases** — Cover a user who writes an entry then immediately deletes it before autosave completes, an entry containing content that triggers a self-harm/crisis-language safety flag, a user who toggles AI-analysis consent mid-history (retroactive vs. forward-only application), and an extremely long single entry.
8. **Failure Scenarios** — Define behavior when the core assumption — that entries are private and reliably saved — breaks: an autosave failure that risks losing an in-progress entry, a consent-setting bug that could expose raw entry text beyond its intended boundary (treated as a critical incident class), and pattern-detection producing a false-positive mood signal that triggers an inappropriate cross-pillar adjustment.
9. **AI Behaviors** — Detail how derived-signal usage is gated by the Proactivity Ladder separately from entry-writing itself (a user can journal for months with zero AI use of the content), how confidence thresholds are set before a mood pattern is allowed to influence another pillar's suggestions, and how a single explicit rejection of a journal-informed suggestion should reduce reliance on that signal type.
10. **Notification Behaviors** — Define the minimal, opt-in-only set of journal-related events that could warrant a notification, such as a gentle prompt to journal after several skipped days if the user has enabled that, versus the strict default of never notifying about entry content itself, and how this integrates with the Notification System's arbitration and quiet-hours rules.
11. **Success Criteria** — A user should feel journaling is safe, effortless, and private by default, and — only if they've opted in — should notice the AI becoming quietly more attuned to their state without ever feeling surveilled.
12. **Metrics** — Define targets such as entry-writing frequency/retention, opt-in rate for AI analysis, accuracy of derived mood-pattern signals against user-reported ground truth, and rate of users revoking consent after enabling it (a trust-regression signal).
13. **Open Questions** — Capture unresolved questions such as where the line sits between a "gentle nudge" and being overly familiar with journal-derived context in other pillars, and whether safety-flag detection (e.g., crisis language) requires any action beyond surfacing a resource, given the explicit exclusion of clinical functionality.

## Deliverables
- Full Journal PRD document following the 13-section structure above.
- Entry data model and lifecycle-state diagram.
- Consent-flow diagram for AI-analysis opt-in/opt-out and its retroactive-vs-forward scope.
- Cross-pillar signal-derivation and hand-off diagram (entry → abstracted signal → consuming pillar).

## Dependencies
Phase 3: Notes PRD (scope boundary), AI Scheduler PRD, Health-pillar check-in PRDs. Phase 2: Cross-Pillar Coordination Experience, Information Architecture, Automation Philosophy, Notification System, Permissions & Consent UX. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Backend, AI/ML), Design, QA, Trust & Safety

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Entry lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] Consent scope (retroactive vs. forward-only) explicitly resolved for every AI-analysis opt-in/opt-out flow.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), AI/ML Lead (required), Trust & Safety Lead (required).
