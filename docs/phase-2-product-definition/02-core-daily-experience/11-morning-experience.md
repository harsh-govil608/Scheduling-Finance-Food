# Document 11: Morning Experience

## Document Name
Morning Experience

## Purpose

Define the specific first-touch-of-the-day experience — the daily briefing — including what categories of content are eligible to appear, in what priority order, and why, and how this experience is structurally different from a generic notification digest. This document specifies the anchor moment that opens the day defined in the Daily Flow document.

## Why It Exists

A morning briefing built by combining "top items from each pillar's backlog" collapses into exactly the notification digest this product is meant to replace — a list of unread items rather than a considered, remembered, predicted set of priorities. This document exists so the morning anchor moment is designed as a single act of synthesis across Productivity, Finance, and Health, directly demonstrating the "Remember → Predict → Suggest" steps of the Behavioral Loop rather than merely aggregating unread counts.

## Approximate Page Count

6-8 pages.

## Sections

1. **The Briefing vs. The Digest** — the definitional distinction between a synthesized morning briefing and a generic notification digest, stated as a rule the eventual document must be checked against.
2. **Composition Rules** — the categories of content eligible to appear in the briefing and the product-level ranking logic used to select and order them.
3. **Cross-Pillar Prioritization Order** — the rules for which pillar leads the briefing on a given morning (e.g., a calendar collision outranking a routine reminder) and how ties are broken.
4. **Tone & Framing Requirements** — how morning content must read, drawing directly on the tone-of-voice principles set in the Product (Behavioral) Philosophy Document.
5. **The "Why This, Why Now" Requirement** — the rule that every item in the briefing must be traceable to a reason the user can understand, not just a scheduled trigger.
6. **Handling Nothing Urgent** — the specified low-content morning case, so an uneventful morning does not get padded with manufactured items.
7. **Handling Overnight Events** — how the briefing surfaces something significant that occurred while the user was asleep (e.g., a large transaction, a missed alarm, a health anomaly).
8. **Personalization Boundaries** — how much the morning experience may vary by persona or life-stage while still reading as the same single assistant, referencing the Personalization document for the mechanics.
9. **Success Signals for the Morning Experience** — the behavioral definitions of a working briefing (opened, acted on, dismissed, ignored) that later measurement work will depend on.

## Deliverables

* Approved Morning Experience document.
* A canonical briefing composition rule set (content eligibility + ranking + tie-breaking).
* An example scenario matrix covering low-content, high-content, and overnight-event mornings, described narratively rather than as UX copy.

## Dependencies

Requires the **Daily Flow** document (Document 10, the morning anchor sits inside that arc), the **Product (Behavioral) Philosophy Document** (Behavioral Loop, tone of voice), the **Memory Model — Behavioral Perspective** document (what the AI must be able to recall to justify a briefing item), and the **Personalization** document (how briefings vary by user).

## Which Teams Use This

Product, Design, Content/Conversation Design, Data Science/ML.

## Completion Criteria

- [ ] The Briefing-vs-Digest distinction has been stated as a checkable rule and applied to at least three example briefings.
- [ ] Cross-pillar prioritization order has been validated against at least one scenario where all three pillars have competing candidate items.
- [ ] Every example briefing item in the scenario matrix carries a stated "why this, why now" justification.
- [ ] The low-content and overnight-event cases each have a specified, non-padded behavior.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
