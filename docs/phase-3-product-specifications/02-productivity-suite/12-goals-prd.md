# Document 12: Goals PRD

## Document Name
Goals PRD

## Purpose
Define the complete specification for the Goals feature: the entity that lets a user declare a cross-pillar outcome (e.g., "save $3,000 for a Japan trip," "run a 10k in three months") and have the AI track, decompose, and surface progress against it across Productivity, Finance, and Health as needed. This document defines what a goal *is*, how it links to tasks, projects, habits, and budgets owned by sibling PRDs, and how unified progress is computed and presented.

## Why It Exists
Every existing planning primitive in the product — a task, a project, a habit, a budget category — lives inside one pillar's data model. A goal is the one entity explicitly designed to sit above all three, because real user intentions ("get financially ready for a baby," "get healthier this year") routinely require coordinated action across Productivity, Finance, and Health at once. Without a dedicated Goals PRD, teams will either bolt goal-like fields onto Tasks (recreating the ambiguity the Task Management Experience document already flags at the goal-to-task decomposition boundary) or leave cross-pillar intentions unrepresented entirely, forcing the user to manually stitch together a savings tracker, a habit, and a task list themselves — the exact manual-work burden the mission commits to driving toward zero.

## Approximate Page Count
9-12 pages.

## Sections
1. **Feature Scope** — in scope: goal entity definition, cross-pillar linkage to tasks/projects/habits/budget targets, unified progress computation, goal check-ins and lifecycle; out of scope: individual task execution (owned by Task Management PRD), multi-step single-pillar work bodies (owned by Projects PRD), recurring-behavior mechanics (owned by Habits PRD), the budget/savings mechanics themselves (owned by Finance suite PRDs) — Goals only links to and reads from these, never redefines them.
2. **User Stories** — 3-5 concrete stories, e.g., a user setting "save for a trip" who expects one merged progress view instead of two disconnected trackers; a user setting a fitness goal who is offered an auto-linked habit; a user whose goal has gone quiet for two weeks who wants a gentle check-in, not silence or nagging.
3. **Functional Requirements** — goal entity (title, target metric, timeframe, owning pillar set, linked sub-items), creation pathways (manual, AI-suggested from conversation), linkage mechanics to tasks/projects/habits/budget items, progress aggregation logic, decomposition hand-off into Task Management/Projects, cross-goal conflict detection, archiving and completion handling.
4. **Non-Functional Requirements** — progress recompute latency after a linked item changes, data consistency guarantees when a linked item (task, habit, budget target) is deleted or modified independently, privacy handling for goals that surface sensitive Finance data alongside Health or Productivity context.
5. **UX Requirements** — must conform to the Automation Philosophy's Ladder-rung presentation for AI-proposed decomposition (active suggestion, never autonomous creation of linked tasks without confirmation), the Dashboard System's placement rules for cross-pillar surfaces, and the Cross-Pillar Coordination Experience's rules for presenting mixed-pillar data as one coherent view rather than three stitched panels.
6. **States & Flows** — draft → active → at-risk → stalled → completed / abandoned / archived, plus the linkage sub-states (linked, link-broken, link-pending) a sub-item can be in relative to its parent goal.
7. **Edge Cases** — a goal that only ever ends up touching one pillar despite being created as cross-pillar; a linked task or habit deleted independently of the goal; a goal's timeframe expiring with partial progress; two active goals competing for the same time or money.
8. **Failure Scenarios** — a linked pillar's data source is unavailable (e.g., bank sync down, so a savings-linked goal cannot compute current progress); AI-proposed decomposition produces irrelevant or duplicate tasks; progress silently diverges from reality due to a stale sync and must fail visibly rather than show false confidence.
9. **AI Behaviors** — Proactivity Ladder application: passive surfacing of progress and at-risk status, active suggestion for decomposition into tasks and habit linkage, never autonomous creation or modification of linked sub-items without confirmation; prediction of at-risk goals from pace-of-progress; learning from past goal completion/abandonment patterns to calibrate future suggested targets and timeframes.
10. **Notification Behaviors** — goal check-in prompts, at-risk alerts, and milestone celebrations are arbitrated within the Notification System's shared cross-pillar interruption budget rather than a separate Goals-only budget, and follow its escalation/de-escalation rules (no repeated nagging on a stalled goal).
11. **Success Criteria** — a user can state a cross-pillar intention once and see it tracked as one coherent goal without manually reconciling separate pillar trackers; stalled goals are surfaced supportively, not silently abandoned or shamed.
12. **Metrics** — % of goals with at least one cross-pillar linkage, goal completion rate, time-to-first-linked-action after goal creation, at-risk-to-recovery rate after an AI check-in.
13. **Open Questions** — how deeply the decomposition engine should auto-propose tasks versus wait for explicit user request; how goal progress reconciles with the trend-level insights Monthly Review later surfaces about the same goal.

## Deliverables
* Approved Goals PRD.
* Goal entity data model reference, including linkage contract to Task Management, Projects, Habits, and Finance budget entities.
* A decomposition hand-off diagram showing exactly where Goals ownership ends and Task Management/Projects ownership begins.

## Dependencies
Requires the Product Philosophy Document (Phase 1, Proactivity Ladder and Manual-Work-to-Zero effort curve), the Guiding Principles Document (Phase 1), the Automation Philosophy (Phase 2), the Notification System (Phase 2), the Cross-Pillar Coordination Experience (Phase 2), and the Task Management Experience (Phase 2), which already flags the goal-to-task decomposition boundary this PRD must resolve concretely. Sibling dependencies within this PRD group: Task Management PRD, Projects PRD (Document 07-11 group), Habits PRD (Document 13), Daily Planning PRD (Document 14).

## Teams Using This
Product, Design, Engineering, Data Science/ML, Content/Copy, Trust & Safety.

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] The goal-to-task decomposition boundary with Task Management PRD is stated as a concrete hand-off, not a general statement of coordination.
- [ ] Cross-pillar linkage contract validated against at least one worked example touching all three pillars.
- [ ] Notification behaviors checked against the Notification System's shared interruption budget for double-counting risk.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Design (required).
