# Document 11: Projects PRD

## Document Name
Projects PRD

## Purpose
This PRD will define the Project entity: how a set of related tasks is grouped into a multi-step initiative with milestones, how progress rolls up across that group, and how a project's overall health (on-track, at-risk, stalled) is derived. It defines the container and milestone layer that sits above individual Tasks and is distinct from a single, standalone Goal.

## Why It Exists
Users think in terms of outcomes larger than any single task — "launch the website," "plan the move" — and without a dedicated Project layer, the system either forces users to manually track a bundle of tasks themselves (reintroducing the manual overhead the mission exists to eliminate) or conflates a multi-step initiative with a single Goal, muddying both entities' semantics and the AI's ability to reason about progress at the right altitude. This PRD exists to give the Scheduler, Task Management, and cross-pillar coordination logic one unambiguous definition of what a project is and how its health is computed.

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: project creation and structure (grouping tasks, defining milestones), cross-task progress rollup, project-health derivation, and milestone sequencing/dependencies. Out of scope: the definition and tracking of a single standalone Goal (owned by the sibling Goals PRD) and the individual task entity's own fields/lifecycle (owned by Task Management PRD) — this PRD owns only the grouping, milestone, and rollup layer above tasks.
2. **User Stories** — Include stories such as: as a user planning a multi-week initiative, I want to add tasks under a project and see overall completion percentage without manually calculating it; as a user, I want milestones to flag as at-risk when their constituent tasks are falling behind, before the milestone date itself arrives; as a user, I want to reorder or mark a milestone dependent on another so tasks aren't suggested out of sequence; as a user, I want the AI to suggest breaking a large, vague project into a first milestone automatically rather than leaving me staring at an empty project.
3. **Functional Requirements** — Define the project data model (title, description, task membership, milestone list, dependency links), the progress-rollup calculation (task-completion-weighted vs. milestone-weighted), the health-status derivation logic (on-track/at-risk/stalled thresholds and their inputs), and the milestone dependency/sequencing rules.
4. **Non-Functional Requirements** — Define recomputation latency for project health after any constituent task changes state, scale limits (max tasks/milestones per project before UI or performance degradation), and consistency requirements when a task belongs to a project that is deleted or archived.
5. **UX Requirements** — This feature must conform to the Task Management Experience and Calendar Experience (Phase 2) for how project-linked tasks are visually tagged and how milestones appear on the calendar; feature-specific UX rules must define the project-health indicator's visual language and how it differs from a single task's overdue state.
6. **States & Flows** — Enumerate the lifecycle: created (empty/being structured) → active → (on-track / at-risk / stalled, recomputed continuously) → milestone-completed (repeating) → project-completed or archived/abandoned.
7. **Edge Cases** — Cover a task removed from a project mid-milestone, circular milestone dependencies, a project with zero tasks assigned, and a task that belongs to a project being reassigned to a different project.
8. **Failure Scenarios** — Define behavior when this feature's core assumption — that constituent task states are accurate and current — breaks: a task deleted outside the project context leaving a rollup miscount, a milestone dependency chain that can never resolve due to a stalled prerequisite, or conflicting health-status recomputation from concurrent edits on two devices.
9. **AI Behaviors** — Detail how the AI's role in project structuring (suggesting milestones, flagging at-risk status, recommending task reprioritization to protect a milestone date) moves along the Proactivity Ladder, from passively surfacing a health status to proactively suggesting a milestone restructure with pre-filled changes awaiting confirmation.
10. **Notification Behaviors** — Define which project-health transitions (e.g., on-track to at-risk) warrant a notification versus a passive dashboard update, how milestone-approaching alerts are bundled with task-level Smart Reminders rather than duplicating them, and how this integrates with the Notification System's arbitration rules.
11. **Success Criteria** — State the qualitative bar: a user should be able to glance at a project and know its true health without manually auditing every constituent task.
12. **Metrics** — Define quantitative targets such as project-completion rate, health-status prediction accuracy (predicted at-risk vs. actual miss), average time projects spend in each health state, and AI milestone-suggestion acceptance rate.
13. **Open Questions** — Capture unresolved questions such as where the line sits between a "project" and a "goal" when a user's mental model blurs the two, and how project health should weigh a single blocked critical-path task versus many minor slipped ones.

## Deliverables
- Full Projects PRD document following the 13-section structure above.
- Project/milestone/task data model schema and relationship diagram.
- Project-health derivation logic specification (thresholds and inputs).
- Project lifecycle-state diagram.

## Dependencies
Phase 3: Task Management PRD, AI Scheduler PRD, Goals PRD (sibling, boundary definition). Phase 2: Task Management Experience, Calendar Experience, Automation Philosophy, Notification System. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Backend/Data Model), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Scope boundary against Goals PRD and Task Management PRD confirmed with no functional overlap.
- [ ] Health-status derivation logic reviewed for determinism and explainability to the user.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
