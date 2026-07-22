# Document 16: Monthly Review PRD

## Document Name
Monthly Review PRD

## Purpose
Define the specification for the recurring monthly reflection feature: a cross-pillar, trend-and-pattern-level review that looks across several weeks of Productivity, Finance, and Health data to surface month-over-month deltas and cross-pillar correlations (e.g., spending rising in weeks with poor sleep) that no single week's review could show. This document specifies month-level, correlation-depth reflection only, distinct from Weekly Review's single-week-over-single-week snapshot.

## Why It Exists
Some patterns only become visible with enough data to be statistically meaningful — a single bad week proves nothing, but a spending increase that recurs across three of the last four weeks alongside a habit-consistency decline is a real, actionable signal. Without a dedicated Monthly Review PRD, that class of insight either never gets built (because Weekly Review's 7-day window structurally cannot see it) or gets bolted onto Weekly Review as an afterthought, diluting its lighter weekly cadence with heavier analysis it isn't designed to carry. Monthly Review exists to be the one place the AI is allowed to make a confidence-gated cross-pillar claim about a pattern, which is also why it carries the highest data-integrity bar of any reflection surface in the product.

## Approximate Page Count
9-11 pages.

## Sections
1. **Feature Scope** — in scope: month-level cross-pillar trend and correlation detection, month-over-month comparison, goal pacing checks against original timeframe, monthly narrative summary; out of scope: weekly snapshot mechanics (owned by Weekly Review PRD, Document 15), day-level data (owned by Daily Planning PRD and Night Summary Experience), the underlying pillar-specific trend engines this feature consumes but does not own (Sleep & Habit Insights Experience, Budget & Spend Intelligence Experience, both Phase 2).
2. **User Stories** — 3-5 concrete stories, e.g., a user who wants to see that their spending consistently rises in weeks logged with poor sleep, presented as a tentative pattern rather than a stated fact; a user whose goal has been pacing behind target for two consecutive months and wants that surfaced with a concrete recalibration option; a new user in their first month who should not receive a fabricated trend built from insufficient data; a user who wants to see month-over-month habit consistency without re-deriving it themselves.
3. **Functional Requirements** — month-level pillar data aggregation, cross-pillar correlation detection against a defined confidence/significance threshold, month-over-month delta computation, goal pacing recalibration prompts, narrative composition combining the above into one coherent summary.
4. **Non-Functional Requirements** — correlation surfacing must meet a stated statistical confidence bar before being shown, to prevent misleading or coincidental patterns from being presented as real; generation performance across a full month of cross-pillar data; privacy handling when a surfaced correlation touches both Finance and Health data simultaneously.
5. **UX Requirements** — must conform to the Night Summary Experience document's (Phase 2) weekly/monthly rollup boundary, the Cross-Pillar Coordination Experience's (Phase 2) rules for visually distinguishing a cross-pillar insight from a single-pillar one, the Dashboard System's placement rules, and the Gamification Philosophy document's tone rules for framing a declining trend without judgment.
6. **States & Flows** — scheduled → data aggregation → trend/correlation detection → narrative composed → presented → acknowledged/dismissed/archived; goal pacing recalibration prompts here can hand off into Goals PRD adjustments.
7. **Edge Cases** — insufficient history for trend detection (new user, first month of use), a detected correlation that is statistically spurious or coincidental, a user active in only one pillar with no cross-pillar data to correlate, a partial calendar month at signup skewing the comparison baseline.
8. **Failure Scenarios** — an underlying pillar trend engine (Sleep & Habit Insights, Budget & Spend Intelligence) is unavailable at aggregation time; the correlation engine produces a low-confidence or sensitive correlation that should not be surfaced without more evidence and must fail closed rather than display it; generation fails to complete before the scheduled delivery time.
9. **AI Behaviors** — Proactivity Ladder application: passive surfacing of trends and correlations only, never autonomous action taken from a monthly-level insight; pattern/correlation detection is the primary AI function of this feature and must be explicitly confidence-gated; learning from which trend types a user finds valuable versus dismisses, to prevent noise creep in future monthly reviews.
10. **Notification Behaviors** — a single monthly notification, positioned at the lowest-frequency, least-urgent tier within the Notification System's taxonomy, arbitrated within the shared interruption budget, and never escalated if unacknowledged.
11. **Success Criteria** — a user receives at least one genuinely new cross-pillar insight per month that neither a daily nor weekly surface could have shown, presented with appropriate confidence framing rather than overstated certainty.
12. **Metrics** — monthly review engagement rate, correlation-insight accuracy / false-positive rate validated against user feedback, goal pacing recalibration acceptance rate, retention correlation with monthly review usage.
13. **Open Questions** — what statistical confidence threshold is required before a cross-pillar correlation is surfaced at all; whether users should be able to request an ad hoc trend window outside the fixed monthly cadence.

## Deliverables
* Approved Monthly Review PRD.
* A correlation-confidence threshold specification usable directly by Data Science/ML to gate what gets surfaced.
* A monthly-to-weekly boundary reference confirming no content overlap with Weekly Review PRD.

## Dependencies
Requires the Night Summary Experience document (Phase 2, Document 12, rollup boundary), the Cross-Pillar Coordination Experience (Phase 2), the Sleep & Habit Insights Experience and Budget & Spend Intelligence Experience (Phase 2, underlying trend data sources), the Gamification Philosophy document (Phase 2), and the Notification System (Phase 2). Sibling dependencies: Weekly Review PRD (Document 15, adjacent cadence boundary), Goals PRD (Document 12, pacing recalibration hand-off), Habits PRD (Document 13).

## Teams Using This
Product, Design, Content/Conversation Design, Data Science/ML, Trust & Safety.

## Completion Criteria
- [ ] The correlation-confidence threshold is defined as a specific, testable bar, not a general statement of caution.
- [ ] The boundary with Weekly Review PRD is stated explicitly with no content overlap.
- [ ] At least one cross-pillar correlation scenario has been reviewed by Trust & Safety for sensitive-data framing and confirmed compliant.
- [ ] The insufficient-history edge case (first-month user) has been validated to produce no fabricated trend content.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Data Science/ML Lead (required).
