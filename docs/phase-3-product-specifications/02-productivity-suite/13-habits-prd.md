# Document 13: Habits PRD

## Document Name
Habits PRD

## Purpose
Define the complete buildable specification for recurring-behavior tracking: habit definition, frequency and flexibility windows, streak/momentum computation, adaptive calibration, and the missed-habit recovery flow, translating the Phase 2 Habit Tracking Experience document's philosophy into a concrete, engineering-ready feature spec. This document owns day-to-day habit mechanics only, not the weekly/monthly pattern-level insights built on top of them.

## Why It Exists
The Phase 2 Habit Tracking Experience document already establishes that habits and streaks are one of the highest-risk areas in the product for accidentally becoming manipulative rather than supportive, and it explicitly requires any streak, point, or badge mechanic touching habits to be reconciled with the Gamification Philosophy document before shipping. That reconciliation requirement does not build itself — this PRD exists to carry the supportive-streak model, the anti-shaming constraints, and the missed-habit recovery flow from philosophy into an implementable spec, so engineering does not default to industry-standard punitive streak UX (reset-to-zero counters, guilt-toned copy) simply because it is the familiar pattern.

## Approximate Page Count
9-12 pages.

## Sections
1. **Feature Scope** — in scope: habit entity CRUD, frequency/flexibility-window definition, streak/momentum computation under the supportive model, adaptive target calibration, missed-habit recovery flow, day-to-day progress surfacing; out of scope: weekly/monthly rollup pattern surfacing (owned by Weekly Review PRD, Monthly Review PRD, and the Sleep & Habit Insights Experience document), reminder delivery timing itself (owned by Smart Reminders PRD), and the gamification mechanic policy itself, which this PRD implements but does not redefine (owned by Gamification Philosophy, Phase 2).
2. **User Stories** — 3-5 concrete stories, e.g., a user who misses a daily water-intake habit and receives a forgiving re-entry message rather than a broken-streak notice; a user whose habit target is consistently too aggressive and the AI proposes loosening it; a user who wants a habit linked to an active Goal.
3. **Functional Requirements** — habit entity (frequency, target, flexibility window, pillar tag, optional goal linkage), creation and AI-assisted calibration, streak/momentum tracking per the supportive model (grace periods, resumable breaks, no reset-to-zero framing), missed-habit recovery flow, adaptive coaching loop that adjusts target or timing based on demonstrated pattern.
4. **Non-Functional Requirements** — daily recompute performance across a user's full habit set, retention of habit history sufficient for adaptive calibration, guarantee that a computation error in streak/momentum logic fails closed into neutral framing rather than failing open into a punitive display.
5. **UX Requirements** — must conform to the Habit Tracking Experience document (Phase 2) for entity definition and recovery-flow tone, and must pass the Gamification Philosophy document's (Phase 2) Encouragement-vs-Guilt Test for every streak, momentum, or progress-visualization element before it ships; also conforms to the Dashboard System for day-to-day surfacing placement.
6. **States & Flows** — habit lifecycle: proposed → active → daily instance (done / missed / grace) → at-risk-of-abandonment → paused → archived; momentum sub-states framed as building / maintained / broken-but-resumable, never "reset."
7. **Edge Cases** — a user completing a habit twice in one day, a user backfilling a missed day retroactively, a mid-stream frequency change, a timezone change during travel shifting the "day" boundary, a habit linked to a goal that is later archived.
8. **Failure Scenarios** — a habit's auto-logged data source (e.g., a Health-pillar step-count integration) desyncs; the AI calibrates a target too aggressively, producing repeated misses; a streak/momentum computation bug must default to neutral, non-punitive framing rather than displaying an incorrect broken-streak state.
9. **AI Behaviors** — Proactivity Ladder application: silent observation of daily completion pattern, passive surfacing of momentum state, active suggestion to adjust target or flexibility window, never autonomous modification of a habit's target without explicit confirmation; prediction of days a habit is likely to be missed; learning loop feeding observed pattern back into calibration suggestions.
10. **Notification Behaviors** — habit reminders and recovery messages are arbitrated within the Notification System's shared interruption budget; recovery-flow messaging tone is governed directly by the Gamification Philosophy document and must not escalate into a nagging sequence on repeated misses.
11. **Success Criteria** — a user can miss a habit day without experiencing shame-coded UI or copy, and can resume without friction; adaptive calibration measurably reduces repeated-miss patterns over time.
12. **Metrics** — % of habits maintained without abandonment over a rolling window, recovery rate after a missed day (return-within-N-days), rate of AI-proposed calibration adjustments accepted by users.
13. **Open Questions** — how habit-linked goal progress reconciles with the trend-level surfacing Weekly Review and Monthly Review later build on top of the same data; how aggressively adaptive calibration should intervene before it starts to feel like the AI second-guessing the user's own target.

## Deliverables
* Approved Habits PRD.
* Habit entity data model and streak/momentum computation reference implementing the Habit Tracking Experience document's supportive model.
* A missed-habit recovery flow specification cross-checked against the Gamification Philosophy document's Encouragement-vs-Guilt Test, with a documented pass result.

## Dependencies
Requires the Habit Tracking Experience document (Phase 2, Document 19 — entity definition, streak philosophy, recovery-flow tone, authoritative for day-to-day mechanics) and the Gamification Philosophy document (Phase 2, Document 39 — anti-shaming constraints this PRD's streak/momentum mechanics must satisfy; any conflict between the two is escalated per that document's reconciliation process). Also requires the Product Philosophy Document (Phase 1, Proactivity Ladder, anti-pattern list) and the Notification System (Phase 2). Sibling dependencies: Goals PRD (Document 12, goal linkage), Weekly Review PRD and Monthly Review PRD (Documents 15-16, rollup boundary), Smart Reminders PRD (Document 09, reminder delivery mechanics).

## Teams Using This
Product, Design, Engineering, Data Science/ML, Content/Copy, Trust & Safety.

## Completion Criteria
- [ ] Every streak/momentum mechanic in this PRD cites the specific Gamification Philosophy rule it satisfies.
- [ ] At least one Trust & Safety reviewer has confirmed no mechanic in this PRD constitutes shame-based motivation.
- [ ] The missed-habit recovery flow has been run through the Encouragement-vs-Guilt Test with a documented pass outcome.
- [ ] The day-to-day vs. weekly/monthly rollup boundary is stated explicitly with no content overlap against Weekly Review and Monthly Review PRDs.
- [ ] Signed off by: Head of Product (required), Trust & Safety Lead (required), Head of Design (required).
