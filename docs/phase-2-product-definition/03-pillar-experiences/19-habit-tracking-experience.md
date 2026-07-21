# Document 19: Habit Tracking Experience

## Document Name
Habit Tracking Experience

## Purpose

Define habit definition, tracking, streak mechanics, and adaptive habit coaching within the product, and explicitly reconcile streak-based motivation with the company's anti-dark-pattern principles. This document specifies day-to-day habit behavior, not the weekly/monthly rollup reporting that sits above it.

## Why It Exists

Habits and streaks are one of the highest-risk areas in the product for accidentally becoming manipulative rather than supportive. The Phase 1 Guiding Principles explicitly forbid streak-shaming and dark patterns, yet streaks are also one of the most naturally motivating mechanics available to a habit product — without a document that reconciles the two directly, feature teams will default to industry-standard streak UX (broken-streak guilt framing, punitive resets) because that is the familiar pattern, quietly violating the company's own stated principles the first time a real streak breaks.

## Approximate Page Count

7-9 pages.

## Sections

1. **Habit Entity Definition** — what constitutes a trackable habit, its attributes (frequency, target, flexibility window), and how it differs from a recurring task.
2. **Habit Creation & Calibration** — how a user defines a new habit and how the AI helps calibrate an achievable target rather than an aspirational one.
3. **Streak Mechanics — Supportive Model** — defines streak tracking behavior explicitly designed against shame, including grace periods, streak "freezes," and reframing a break rather than penalizing it.
4. **Explicit Conflict Flag: Streak-Shaming vs. Standard Streak UX** — states directly that conventional streak UX patterns are presumed non-compliant until proven otherwise, and requires every streak-related decision to cite the specific Guiding Principle it satisfies.
5. **Adaptive Habit Coaching** — how the AI adjusts encouragement, timing, and framing based on a user's actual pattern (for example, loosening a target that is consistently missed) rather than repeating the same nudge regardless of outcome.
6. **Missed-Habit Recovery Flow** — the specific tone and behavior when a habit is missed or a streak breaks, and how it is designed to differ from a punitive pattern.
7. **Habit Progress Surfacing (Day-to-Day)** — how habit performance is shown to the user in the moment, distinct from the weekly/monthly rollups owned by the Sleep & Habit Insights Experience Document.
8. **Reconciliation Requirement with Gamification Philosophy** — states that any point, reward, or badge mechanic touching habits must be authored jointly with, and cannot contradict, the Gamification Philosophy document (a sibling Phase 2 document), and defines the escalation path if the two documents appear to conflict.

## Deliverables

* Approved Habit Tracking Experience document.
* A streak-mechanics compliance checklist cross-referencing every streak-related decision against the Guiding Principles' anti-dark-pattern rules.
* A missed-habit recovery flow reference (behavior and tone, not final UX copy) reusable across Health and Productivity habit types.

## Dependencies

Requires the Guiding Principles Document (Phase 1, anti-dark-pattern rules — authoritative), the Product (Behavioral) Philosophy Document (Phase 1, Encourage/Adapt verbs), and the Product Pillars Overview (Health Pillar Surface). Requires joint reconciliation with the Gamification Philosophy document (a sibling Phase 2 document) and maintains an explicit boundary with the Sleep & Habit Insights Experience Document.

## Which Teams Use This

Product, Design, Content/Copy, Data Science/ML, Trust & Safety.

## Completion Criteria

- [ ] Every streak mechanic proposed has a written citation to the specific Guiding Principle it satisfies.
- [ ] At least one Trust & Safety reviewer has explicitly signed off that no streak mechanic constitutes shame-based motivation.
- [ ] A reconciliation session has been held jointly with the Gamification Philosophy document owner, with conflicts logged and resolved rather than left open.
- [ ] The missed-habit recovery flow has been tested in concept form against at least one Phase 1 persona prone to habit abandonment.
- [ ] Signed off by: Head of Product (required), Trust & Safety Lead (required), Head of Design (required).
