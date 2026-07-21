# Document 25: Sleep & Habit Insights Experience

## Document Name
Sleep & Habit Insights Experience

## Purpose

Define how sleep insights and weekly/monthly rollup insights across the Health pillar are presented to the user — the reflective, backward-looking counterpart to day-to-day capture and progress. This document owns the rollup and pattern-level layer, not the day-to-day surfaces beneath it.

## Why It Exists

Without a dedicated document, insight-surfacing tends to get bolted onto whichever feature was built last — sleep insights crammed into the habit screen, for instance — producing an inconsistent reporting cadence and format across Health sub-features. Insights are the primary moment the AI demonstrates it has actually been remembering and learning over time, so an inconsistent or superficial insights experience directly undercuts user confidence in the AI's memory, one of the product's core promises.

## Approximate Page Count

6-8 pages.

## Sections

1. **Sleep Data Presentation** — how sleep duration and quality signals are shown to the user day to day and in rollup form, at a product level rather than a device/sensor integration level.
2. **Weekly Insight Structure** — the standard shape of a weekly Health rollup: what categories of insight it always contains and in what order.
3. **Monthly/Trend Insight Structure** — how longer-horizon trends are surfaced differently from weekly insights, at the level of patterns rather than individual events.
4. **Insight Framing Principles** — how an insight is worded to be useful and non-alarming (for example, a sleep-debt trend), consistent with Encourage and Never Overwhelm.
5. **Cross-Habit Rollup Reporting** — how performance across multiple habits is aggregated into one rollup view, distinct from the day-to-day per-habit surfacing owned by the Habit Tracking Experience Document.
6. **Actionable Follow-Through from an Insight** — how an insight can lead directly into a suggestion or action (for example, a sleep insight prompting an earlier wind-down reminder) without redefining suggestion mechanics owned elsewhere.
7. **Insight Delivery Timing & Cadence** — when and how often insights are delivered (a fixed weekly moment versus a threshold-triggered one) so they do not compound into overwhelm alongside daily notifications.
8. **Explicit Non-Scope: Day-to-Day Habit and Nutrition Progress** — states plainly that day-to-day per-habit and per-goal progress surfacing is owned by the Habit Tracking Experience Document and the Nutrition & Goals Experience Document respectively; this document owns only the rollup/reflective layer.

## Deliverables

* Approved Sleep & Habit Insights Experience document.
* A weekly/monthly insight template structure reusable by Content/Copy and Design.
* An insight-to-action handoff reference showing where a rollup insight may connect into a live suggestion.

## Dependencies

Requires the Habit Tracking Experience Document and the Nutrition & Goals Experience Document as rollup input sources with explicit boundaries, the Product (Behavioral) Philosophy Document (Phase 1, Learn/Encourage verbs and Never Overwhelm), and the Product Architecture Overview (memory model — insights are a visible expression of accumulated memory).

## Which Teams Use This

Product, Design, Content/Copy, Data Science/ML.

## Completion Criteria

- [ ] The weekly and monthly insight structures have each been validated against at least one full month of a plausible synthetic user history.
- [ ] Insight framing has been reviewed for at least one negative-trend scenario (for example, worsening sleep) to confirm a non-alarming tone.
- [ ] Delivery cadence is confirmed not to overlap or compound with daily notification volume defined elsewhere (a Never Overwhelm cross-check).
- [ ] The boundary with the Habit Tracking Experience Document and the Nutrition & Goals Experience Document is confirmed, with no duplicated day-to-day progress UX.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
