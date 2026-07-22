# Document 31: UX Metrics

## Document Name
UX Metrics

## Purpose
Define the specific interface- and interaction-level metrics — task completion rate, time-to-comprehension, error rate, satisfaction scores, component-level health — that are tracked continuously to evaluate whether the design system and the surfaces built on it are actually usable. This document specifies what the UX Metrics measurement program must define: metric taxonomy, precise definitions, instrumentation ownership, and thresholds, not the analytics pipeline itself.

## Why It Exists
The Success Metrics Document (Phase 1, Doc 6) defines business-level, lagging indicators — North Star, KPI tree, Manual Effort Index, retention — that tell leadership whether the company is winning. Those metrics are necessary but too coarse and too slow for design teams to act on day to day: a redesigned onboarding flow can quietly tank task completion for weeks before that ever shows up in a retention number, by which point the damage is compounded across thousands of users. This document exists to give design and research teams their own leading indicators, owned at the flow and component level, so usability regressions are caught and fixed before they surface downstream as a business-metric problem.

## Approximate Page Count
7-9 pages

## Sections
1. **Metric Taxonomy** — the four families tracked: effectiveness (task completion rate, error rate), efficiency (time-on-task, time-to-comprehension, steps-to-completion), satisfaction (SUS, single-ease question scores, flow-level CSAT), and learnability (first-time vs. repeat-use performance delta).
2. **Task Completion Rate Definition** — precise per-flow definition and instrumentation approach, with differentiated target thresholds for core-loop flows (morning check-in, transaction capture, meal logging) versus edge-case or premium features.
3. **Time-to-Comprehension** — a metric specific to this product's proactive nature: how long it takes a user to understand what an AI-initiated action or suggestion is and why it happened, measured via first-click/first-interaction timing and session-replay heuristics rather than self-report alone.
4. **Error Rate & Recovery Metrics** — the distinction between user-induced slips/mistakes and system-induced errors, and recovery-time-to-resolution, explicitly scoped against the Error Recovery Experience (Phase 2, Doc 35) which defines the recovery UX patterns being measured here.
5. **Component-Level UX Health Metrics** — metrics attributable to individual design-system components rather than whole flows (dismissal rate on a card pattern, mis-tap/mis-input rate on a control, abandonment at a specific step), giving the Design Systems team component-by-component visibility.
6. **Trust & Comprehension of Proactive Actions** — UX-specific measures distinct from Phase 1's business-level trust metrics: "explain this" tap rate on AI-initiated actions, undo/reversal rate on autonomous actions, and confusion-signal rate (e.g., immediately re-checking a setting after an AI action).
7. **Accessibility UX Metrics** — task completion rate, error rate, and time-on-task segmented specifically for assistive-technology users, coordinated with the Voice & Accessibility group (Phase 7, group 07), to prevent an aggregate metric from masking a broken experience for a minority segment.
8. **Metric Instrumentation & Ownership** — how each metric is captured in product analytics, the boundary between the team that instruments/collects a metric and the team accountable for acting on it.
9. **Thresholds, Alerts & Design Debt Triggers** — defined thresholds that automatically flag a screen or component as requiring a redesign review, feeding directly into the deprecation/contribution triage defined in Design System Governance (Phase 7, Doc 32).
10. **Reporting Cadence & Dashboard** — the shared dashboard structure, sprint-level review cadence for actively developed flows, and quarterly review cadence for system-wide UX health.

## Deliverables
- UX metrics taxonomy and definitions catalog.
- Per-flow task completion rate and error rate instrumentation specification.
- Time-to-comprehension measurement methodology.
- Component-level UX health scorecard template.
- Trust/comprehension metric set specific to proactive AI actions.
- Accessibility-segmented metrics reporting specification.
- Threshold and alert rule set with defined design-debt trigger points.
- UX metrics dashboard specification.

## Dependencies
Explicitly scoped against Success Metrics Document (Phase 1, Doc 6) — this document must state, section by section, why each UX metric is a leading indicator distinct from and not a duplicate of Phase 1's business KPIs. Depends on User Testing (Phase 7, Doc 30) for qualitative context behind the quantitative signals, Component Library (Phase 7, Doc 07) for the components being measured, Error Recovery Experience (Phase 2, Doc 35), and the Proactivity Ladder Decision Engine (Phase 5, Doc 14) for defining trust/comprehension segments. Feeds Design System Governance & Contribution Model (Phase 7, Doc 32) through the design-debt trigger mechanism.

## Teams
UX Research, Design Systems, Product, Data/Analytics, Engineering, Accessibility

## Completion Criteria
- [ ] Every metric in this document has an explicit one-sentence statement of how it differs from its nearest Phase 1 Success Metrics counterpart.
- [ ] Every metric has a named instrumentation owner and a named action owner, and the two are allowed to be different teams.
- [ ] Task completion rate and error rate are defined and piloted on at least one core-loop flow per pillar (Productivity, Finance, Health) before sign-off.
- [ ] Accessibility-segmented metrics are reported as their own row on the dashboard, never folded silently into an aggregate.
- [ ] Design-debt threshold values are reviewed and approved by both UX Research and Design Systems before they can trigger a mandatory review.
- [ ] Signed off by: Head of Design (required), Head of Data/Analytics (required), UX Research Lead (required).
