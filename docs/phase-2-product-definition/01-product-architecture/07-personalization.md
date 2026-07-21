# Document 07: Personalization

## Document Name
Personalization

## Purpose
Define how the product visibly adapts to individual users over time — what elements personalize (tone, suggestion thresholds, defaults, timing) and what deliberately does not personalize (core philosophy and safety rules that stay constant for every user). This document sets the boundary between "the assistant learns me" and "the assistant is a different product for different people."

## Why It Exists

A product that promises to Learn and Adapt risks, without explicit boundaries, personalizing its way into inconsistency — where two users experience fundamentally different safety guarantees, or where over-personalization erodes the predictable, trustworthy core that the Proactivity Ladder depends on. This document exists so feature teams have a clear, shared answer to "is this a knob every user can move, or a fixed rule that never changes," preventing both a bland one-size-fits-all product and an unpredictable, unsafe one.

## Approximate Page Count
6-8 pages.

## Sections

1. **The Personalization Boundary** — the foundational rule distinguishing what is allowed to vary per user from what must remain constant for all users, stated as a single testable principle.
2. **What Personalizes: Tone** — how communication style (formality, encouragement style, brevity) is allowed to adapt per user, and the bounds within which that adaptation happens.
3. **What Personalizes: Suggestion Thresholds** — how sensitive the AI is to offering a suggestion (e.g., how small a budget deviation triggers an alert) adapts per user based on observed tolerance.
4. **What Personalizes: Defaults and Timing** — how default choices (e.g., default meal logging method, default reminder lead time) and timing of proactive touches adapt per user.
5. **What Does Not Personalize: Core Philosophy and Safety Rules** — the explicit, non-negotiable list of behaviors that stay identical for every user regardless of preference or trust level (e.g., irreversible actions always require confirmation below a given trust level).
6. **Personalization Onboarding vs. Personalization Over Time** — the distinction between initial preference-setting and gradual, learned adaptation, and how each is expected to be visible to the user.
7. **User Awareness and Control of Personalization** — what a user can see about how the product has personalized to them, and what they can reset or override.
8. **Personalization and Cross-Pillar Consistency** — the rule that personalization dimensions (like tone) must apply uniformly across all three pillars rather than drifting per pillar.

## Deliverables

* Approved Personalization document.
* A "personalizes / does not personalize" reference table for use in every future feature PRD.
* A user-facing personalization controls requirements list for Design.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Learn/Adapt verbs and the non-negotiable safety principles, and the Memory Model — Behavioral Perspective (Document 06) for how learned facts feed personalization.

## Which Teams Use This

Product, Design, Data Science/ML (as consumers), Trust & Safety, Content/Copy.

## Completion Criteria

- [ ] The personalization boundary rule has been tested against at least five candidate features (three that should personalize, two that should not) with unambiguous results.
- [ ] Every "does not personalize" item has been cross-checked against the Guiding Principles Document (Phase 1) for consistency.
- [ ] Tone, threshold, and default/timing personalization each have at least one worked cross-pillar example.
- [ ] User controls for viewing/resetting personalization have been defined with no unresolved gaps.
- [ ] Signed off by: Head of Product (required), Head of Trust & Safety (required).
