# Document 08: Automation Philosophy

## Document Name
Automation Philosophy

## Purpose
Define product-level rules for what the product is allowed to automate and how that automation is presented to users — confirmations, undo, and visible autonomy levels. This document takes the Phase 1 Proactivity Ladder and makes it concrete at the product-surface level: what a given rung on the Ladder literally looks like on screen.

## Why It Exists

The Proactivity Ladder from Phase 1 establishes that autonomy is earned gradually, but by itself it is a behavioral policy, not a product specification — it does not say what a "pre-filled action awaiting confirmation" screen contains, or what distinguishes it visibly from a fully autonomous action with notification. Without this translation, feature teams building autonomous behaviors in Finance (e.g., auto-categorizing a transaction) and Productivity (e.g., auto-rescheduling a task) will each invent their own confirmation and undo patterns, producing a product where the user cannot reliably predict what "the AI just did something" looks or feels like. This document exists to give every rung of the Ladder one canonical on-screen representation used everywhere automation appears.

## Approximate Page Count
8-10 pages.

## Sections

1. **From Ladder to Product Surface** — restates the five Proactivity Ladder rungs (silent observation, passive surfacing, active suggestion, pre-filled action awaiting confirmation, autonomous action with notification) and maps each to a general on-screen pattern.
2. **What "Silent Observation" Looks Like** — confirms this rung has no visible surface by definition, and defines what evidence (if any) a user can later find that observation occurred.
3. **What "Passive Surfacing" Looks Like** — the visible pattern for information shown without a prompt to act (e.g., a dashboard insight), and the rule that it never demands a response.
4. **What "Active Suggestion" Looks Like** — the visible pattern for a suggestion that proposes an action but takes none, including how accept/reject/dismiss are presented.
5. **What "Pre-Filled Action Awaiting Confirmation" Looks Like** — the visible pattern for an action drafted by the AI that requires explicit user confirmation before executing, including what must be shown to the user before they confirm.
6. **What "Autonomous Action with Notification" Looks Like** — the visible pattern for an action taken without prior confirmation, including the mandatory notification, and the undo window that must accompany it.
7. **Undo and Reversibility Rules** — the product-level rule that no autonomous action is permitted unless a corresponding undo or reversal path exists, and what "reversible enough" means as a product test.
8. **Confirmation Fatigue Safeguards** — the product's rules for preventing confirmation prompts from becoming so frequent that users blindly approve them, tying back to the "Never Overwhelm" rules from the Product (Behavioral) Philosophy Document.
9. **Autonomy Level Transparency** — how a user can, at any time, see what autonomy level the AI currently holds for a given capability, and how that is presented consistently across pillars.
10. **Cross-Pillar Automation Consistency** — the rule that identical Ladder rungs must look and behave identically regardless of which pillar triggers them (e.g., a Finance auto-categorization and a Health auto-logged meal use the same confirmation pattern).

## Deliverables

* Approved Automation Philosophy document.
* A canonical "Ladder rung to on-screen pattern" reference table, reusable in every future feature PRD involving automation.
* An undo/reversibility checklist required for sign-off on any autonomous-action feature.
* A confirmation-fatigue safeguard checklist for Design and Trust & Safety review.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Proactivity Ladder and the "Never Overwhelm" operationalization, the Product Architecture Overview (Document 01) for the automation/notification layer definition, and the Personalization document (Document 07) for how automation thresholds may vary per user.

## Which Teams Use This

Product, Design, Engineering, Trust & Safety, QA, Data Science/ML.

## Completion Criteria

- [ ] Every one of the five Proactivity Ladder rungs has exactly one canonical on-screen pattern with no ambiguity between adjacent rungs.
- [ ] The undo/reversibility rule has been validated against at least one worked example per pillar of an autonomous action.
- [ ] Confirmation fatigue safeguards have been checked against the "Never Overwhelm" rules in the Product (Behavioral) Philosophy Document for consistency.
- [ ] Autonomy level transparency has a defined, consistent presentation validated across all three pillars.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Trust & Safety (required).
