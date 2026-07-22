# Document 14: Daily Planning PRD

## Document Name
Daily Planning PRD

## Purpose
Define the specification for the engine that assembles a single, prioritized day plan by pulling together due tasks, goal-linked action items, calendar commitments, and habit check-ins into one sequence, rather than presenting the user three separate lists to reconcile themselves. This document specifies the assembly and prioritization logic itself, distinct from any one delivery moment (e.g., the Morning Experience) and distinct from calendar time-block optimization (owned by the AI Scheduler PRD).

## Why It Exists
Tasks, goals, calendar events, and habits are each owned and stored by separate PRDs, and left alone each would render its own list — leaving the user to do the actual planning work of deciding what order to do things in, which is precisely the manual work the mission commits to driving toward zero. Daily Planning exists as the one place that merges all four inputs into a single prioritized sequence, so "what should I do today" has one authoritative answer instead of four data sources the user has to mentally merge every morning.

## Approximate Page Count
9-11 pages.

## Sections
1. **Feature Scope** — in scope: pulling in today's due/overdue tasks, goal-linked action items, calendar events, and habit check-ins; ranking and sequencing logic; presenting one merged plan; re-planning on mid-day change; out of scope: calendar time-block optimization itself (owned by AI Scheduler PRD), reminder delivery timing (owned by Smart Reminders PRD), the morning delivery ritual and tone (owned by the Morning Experience document, Phase 2) — Daily Planning generates the plan, Morning Experience is one surface it is delivered through.
2. **User Stories** — 3-5 concrete stories, e.g., a user who opens the app in the morning and sees one ranked list instead of a task list, a calendar, and a habit tracker; a user whose meeting moves mid-morning and the plan re-sequences the remaining items automatically; a user with more items than fit in the day who is warned before overcommitting rather than silently overloaded.
3. **Functional Requirements** — plan generation inputs (tasks, goal action items, calendar events, habit windows), ranking factors (deadline proximity, goal importance, fixed-commitment anchoring, estimated effort, habit flexibility window), capacity awareness against available time, re-planning triggers (item added/removed, calendar change, habit missed), user override and manual reordering with the override persisted for that day.
4. **Non-Functional Requirements** — plan generation latency must feel instant on open; re-plan recompute must complete within a bounded time after a triggering calendar or task change; re-planning must not thrash on rapid successive small changes.
5. **UX Requirements** — must conform to the Automation Philosophy's Ladder-rung presentation (a generated plan is an active suggestion or pre-filled-awaiting-confirmation surface depending on trust level, never autonomously executed or rescheduled without notification), the Dashboard System's placement rules, and the Morning Experience document's (Phase 2) handoff point for where the plan is first delivered.
6. **States & Flows** — plan draft generated → presented → user adjusts/accepts → in-progress through the day → re-planned on trigger → closes out and hands off into the Night Summary Experience at day's end.
7. **Edge Cases** — more tasks/goal items than fit in the available day (overpacking), a day with no tasks or events at all, conflicting fixed calendar events, a habit whose flexibility window spans into the next day, a user manually reordering the plan just before a new urgent task arrives.
8. **Failure Scenarios** — calendar sync unavailable at generation time, task data source unreachable, goal decomposition yields zero actionable items for today, re-planning trigger fires but produces no meaningful change and must not needlessly re-notify the user.
9. **AI Behaviors** — Proactivity Ladder application: plan generation itself is an active suggestion / pre-filled-awaiting-confirmation surface per the user's current trust level, never an autonomous execution; prediction of realistic daily capacity from the user's historical completion rate; learning from which plan items are accepted versus manually reordered to refine future ranking.
10. **Notification Behaviors** — "plan ready" notification is arbitrated within the Notification System, typically anchored to the Morning Experience delivery moment; re-plan notifications are batched rather than fired for every minor change, per the Notification System's batching and anti-nagging rules.
11. **Success Criteria** — a user can open the app once and see a single ordered plan that already accounts for calendar, tasks, goals, and habits, without needing to cross-reference separate surfaces; overpacked days are flagged before they happen, not after.
12. **Metrics** — % of plan items completed by end of day, % of generated plans accepted without manual edits, reduction in overpacked-day frequency over time, re-plan trigger frequency per user per day.
13. **Open Questions** — how much manual reordering should feed back into the ranking model versus being treated as a one-off override; how capacity constraints should interact with Cross-Pillar Coordination when Finance or Health also want same-day attention.

## Deliverables
* Approved Daily Planning PRD.
* A ranking-factor reference table (deadline, goal importance, fixed-anchor, effort, habit window) usable by engineering to implement the prioritization engine.
* A re-planning trigger matrix mapping input-source changes to expected plan-recompute behavior.

## Dependencies
Requires the Product Philosophy Document (Phase 1, Manual-Work-to-Zero effort curve, Never Overwhelm), the Automation Philosophy (Phase 2), the Notification System (Phase 2), the Morning Experience document (Phase 2, Document 11, primary delivery surface), the Daily Flow document (Phase 2, Document 10), and the Night Summary Experience document (Phase 2, Document 12, end-of-day handoff). Sibling dependencies: AI Scheduler PRD and Calendar Intelligence PRD (calendar input), Task Management PRD and Projects PRD (task input), Goals PRD (Document 12, goal-linked action items), Habits PRD (Document 13, habit check-ins).

## Teams Using This
Product, Design, Engineering, Data Science/ML, Content/Copy.

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] The boundary with AI Scheduler PRD (time-block optimization) and Morning Experience (delivery ritual) is stated explicitly with no ownership overlap.
- [ ] The ranking-factor reference table has been validated against at least one worked example with competing high-priority items.
- [ ] Re-planning behavior checked against the Notification System's anti-thrash and batching rules.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
