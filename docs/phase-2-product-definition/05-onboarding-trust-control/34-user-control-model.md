# Document 34: User Control Model

## Document Name
User Control Model

## Purpose

Define the concrete, user-facing override, undo, and pause mechanisms available for every AI action — the practical implementation of the Phase 1 Proactivity Ladder. This document specifies exactly how a user pauses automation for a pillar, undoes an auto-categorized transaction, permanently dismisses a suggestion, or otherwise asserts control over the AI in the moment.

## Why It Exists

The Proactivity Ladder is a philosophy that says the AI may act with increasing autonomy as trust is established, but a philosophy cannot be experienced directly — a user cannot "feel" a ladder rung, they can only feel whether they are able to stop, reverse, or redirect something the AI just did. Without one shared, consistently-applied control model, every pillar will invent its own notion of undo (Finance might allow reversing a categorization but Productivity might not allow un-completing a task the AI marked done), and the product will feel autonomous and trustworthy in one pillar while feeling reckless in another. This document exists so "the user is always in control" is a testable guarantee, not a slogan, especially at the higher rungs of the ladder where the AI is permitted to act before asking.

## Approximate Page Count

7-9 pages.

## Sections

1. **Control Primitives** — the small, fixed vocabulary of control actions (Pause, Undo, Override, Dismiss, Snooze) defined once, precisely, so every pillar implements the same primitives rather than inventing new ones.
2. **Per-Pillar Pause Mechanics** — how a user pauses AI automation for Productivity, Finance, or Health independently, and what continues to function (e.g., passive logging) versus what stops entirely while paused.
3. **Undo Windows & Reversibility Rules** — how long an autonomous or pre-filled action remains reversible, and the product's stance on actions that cannot be fully undone (what warning is required before those).
4. **Permanent Dismissal Mechanics** — how "don't suggest this again" is honored, at what scope (this instance, this category, this pillar), and how a user later discovers and reverses a permanent dismissal if they change their mind.
5. **Ladder Rung Adjustment UX** — how a user manually moves the AI's autonomy up or down for a given pillar or action type, independent of the automatic trust-building that normally drives the ladder.
6. **Bulk & Global Controls** — the existence and behavior of a master "pause everything" control, and how it interacts with per-pillar pause settings.
7. **Confirmation-Required Actions Registry** — the explicit list of action types that must always require explicit user confirmation regardless of trust level or ladder rung (irreversible or high-consequence actions).
8. **Feedback Loop: How Undo/Override Teaches the AI** — described strictly at the experience level (what the user is told happens when they correct the AI), with technical learning mechanics explicitly excluded and reserved for a later document.

## Deliverables

* Approved User Control Model document.
* A control-primitive glossary used consistently across all product documentation.
* A per-pillar pause/undo capability matrix.
* A confirmation-required actions registry listing every action that can never bypass explicit user confirmation.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Proactivity Ladder definition this document operationalizes; requires the Guiding Principles Document (Phase 1) for the trust and consent principles that bound what may ever be irreversible; works alongside the Settings Philosophy document (Document 33), which exposes many of these controls as settings; informs the Error Recovery Experience document (Document 35), which handles the case where an undo is triggered by an AI mistake rather than a user preference.

## Which Teams Use This

Product, Design, Data Science/ML, Trust & Safety, Engineering (as downstream consumers).

## Completion Criteria

- [ ] Every control primitive (Pause, Undo, Override, Dismiss, Snooze) has a precise definition applied identically across all three pillars.
- [ ] Every pillar has a documented pause mechanic specifying exactly what stops and what continues while paused.
- [ ] The confirmation-required actions registry has been reviewed against all three pillars and contains no irreversible action left unlisted.
- [ ] The Ladder Rung Adjustment UX has been validated against at least one worked scenario per pillar showing a user manually raising and lowering autonomy.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Trust & Safety Lead (required).
