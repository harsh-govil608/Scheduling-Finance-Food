# Document 05: Context Engine — Product Perspective

## Document Name
Context Engine — Product Perspective

## Purpose
Define what "context" means to the user-facing product — time, location, calendar state, recent activity, and cross-pillar signals — and how that context visibly shows up in suggestions the user sees. This document describes only the experienced behavior of context-awareness, not the underlying technical or ML system that produces it.

## Why It Exists

"Context-aware" is one of the product's most repeated promises across all three pillars, yet without a product-level definition of what context inputs are recognized and how they are allowed to surface, every feature team will silently invent its own notion of context — one team might treat "context" as only location, another as only time of day — and the product will feel context-aware in some moments and oblivious in others. This document exists to give Product and Design a shared, engineering-independent vocabulary for context so that suggestions across Productivity, Finance, and Health feel like they come from one assistant that notices the same things everywhere.

## Approximate Page Count
6-8 pages.

## Sections

1. **What Counts as Context (Product Definition)** — the enumerated list of context signal categories recognized by the product (time, location, calendar state, recent activity, cross-pillar signals) defined in plain user-experience language.
2. **Context-to-Suggestion Examples per Pillar** — one worked example per pillar showing a specific context input producing a specific visible suggestion.
3. **Visibility Rules — What the User Is Told** — the product's stance on whether and how the AI discloses which context triggered a suggestion (e.g., "because you're near the grocery store").
4. **Context Freshness and Staleness (Experienced)** — how "recent" context is allowed to feel to the user before a suggestion seems out of date or irrelevant, described experientially rather than technically.
5. **Cross-Pillar Context Sharing (Experienced)** — how context gathered in one pillar is allowed to visibly influence another pillar's suggestions, at the level of user perception only.
6. **User Control Over Context Signals** — what context inputs a user can see, disable, or limit (e.g., turning off location-based prompts) and how that control is presented.
7. **Failure and Absence of Context** — how the product should behave, from the user's point of view, when expected context is missing or contradictory (e.g., no location signal available).
8. **Explicit Non-Scope: The Technical Context Engine** — states plainly that data pipelines, signal weighting, inference models, and storage are excluded and reserved for a later architecture-phase document.

## Deliverables

* Approved Context Engine — Product Perspective document.
* A context-signal-to-suggestion example matrix (one example per pillar minimum).
* A user-facing "context controls" requirements list for Design.

## Dependencies

Requires the Product Architecture Overview (Document 01) for the five-component frame and the Product Pillars Overview (Document 02) for pillar-specific examples; aligns with the Product (Behavioral) Philosophy Document (Phase 1) for the Predict/Suggest steps of the Behavioral Loop.

## Which Teams Use This

Product, Design, Data Science/ML (as consumers translating this into technical requirements later), Content/Copy, Trust & Safety.

## Completion Criteria

- [ ] Every context signal category has at least one worked, pillar-specific example.
- [ ] Visibility rules have been validated against the Product (Behavioral) Philosophy Document's stance on transparency.
- [ ] User control requirements cover every context signal category with no gaps.
- [ ] Confirmed no technical/ML implementation detail has leaked into this document.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
