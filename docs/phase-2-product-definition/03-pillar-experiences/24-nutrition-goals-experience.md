# Document 24: Nutrition & Goals Experience

## Document Name
Nutrition & Goals Experience

## Purpose

Define protein and nutrition goal tracking, water tracking, and progress surfacing — how the product shows a user where they stand against their health goals — distinct from how food and water entries are captured in the first place. This document assumes a confirmed log as its input.

## Why It Exists

Goal progress is where nutrition data either becomes motivating or becomes another anxiety-inducing number to check. Without explicit rules for how progress is surfaced — framing, frequency, and comparison to target — the Health pillar risks the same overwhelm and shame failure mode that habit tracking must guard against, except applied to calories, protein, and water intake instead of streaks.

## Approximate Page Count

6-8 pages.

## Sections

1. **Nutrition Goal Definition** — how a user's protein and nutrition targets are set or AI-suggested, and what inputs those targets are based on at a product (not algorithmic) level.
2. **Water Tracking UX** — the capture and progress-display experience for water intake, including how it is reconciled with food logging as a related but separate stream.
3. **Daily Progress Surfacing** — how progress-against-goal is shown through the course of a day (running totals, remaining-budget framing), tying to Never Overwhelm.
4. **Goal Miss & Overshoot Framing** — the tone contract for communicating a day where a nutrition goal was missed or exceeded, consistent with the anti-shaming principles established for habits.
5. **Adaptive Goal Recalibration** — how the AI suggests adjusting a goal that is persistently unmet or trivially exceeded, mirroring the adaptive philosophy used in scheduling and budgeting.
6. **Cross-Meal Awareness** — how the experience helps a user understand a running daily and weekly picture rather than only per-meal feedback.
7. **Goal Progress Handoff to the Habit System** — the point at which nutrition goal completion feeds into the broader habit-tracking surface (for example, "log water" as a trackable habit), without redefining habit mechanics owned by the Habit Tracking Experience Document.
8. **Explicit Non-Scope: Capture Mechanics** — states plainly that how food and water entries are captured (photo, voice, manual) is owned entirely by the Food Logging Experience Document; this document assumes confirmed entries as input.

## Deliverables

* Approved Nutrition & Goals Experience document.
* A progress-surfacing reference showing running-total and remaining-budget display logic at a product level.
* A goal recalibration flow reference, sharing a common pattern conceptually with the Scheduling System and Budget & Spend Intelligence Experience documents.

## Dependencies

Requires the Food Logging Experience Document (assumes its output as input), the Habit Tracking Experience Document (handoff for habit-linked nutrition goals), the Product (Behavioral) Philosophy Document (Phase 1, Adapt/Never Overwhelm), and the Guiding Principles Document (Phase 1, anti-shaming principle).

## Which Teams Use This

Product, Design, Content/Copy, Data Science/ML, Trust & Safety.

## Completion Criteria

- [ ] Progress surfacing has been validated against at least one goal-missed and one goal-exceeded scenario with tone review.
- [ ] The adaptive recalibration flow is confirmed to trigger only after a defined pattern, not a single off day, consistent with Never Overwhelm.
- [ ] The boundary with the Food Logging Experience Document is confirmed; no capture-flow UX is duplicated here.
- [ ] The handoff to the Habit Tracking Experience Document for habit-linked goals has been reviewed jointly, with no conflicting streak or goal terminology.
- [ ] Signed off by: Head of Product (required), Trust & Safety Lead (required).
