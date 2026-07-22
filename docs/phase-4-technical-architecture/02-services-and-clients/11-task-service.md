# Document 11: Task Service

## Document Name
Task Service

## Purpose
Define the architecture of the service owning tasks, projects, goals, and habits — the core productivity entities referenced by nearly every Productivity Suite PRD. This document specifies the entity model and hierarchy, the API surface other services and the AI layer build on, and the consistency and scaling model, without defining the AI logic that generates suggestions or automations (Phase 5).

## Why It Exists
Task, project, goal, and habit are related but distinct entities that many features touch simultaneously — Task Management, Projects, Goals, Habits, Daily Planning, Weekly Review, and Monthly Review all read and write against this data, and AI Scheduler and Smart Reminders depend on it to place work on the calendar. If the hierarchy and ownership rules between these entities aren't fixed at the architecture level, features built independently will model "what is a task vs. a project vs. a habit" differently, breaking rollups (e.g., project completion percentage) and cross-feature consistency at scale.

## Approximate Page Count
9-11 pages

## Sections
1. **Service Boundary** — what this service owns (task, project, goal, habit, and their relationships) versus what it does not (calendar placement and time-blocking live in Calendar Service; task-related reminders are scheduled by Smart Reminders against events this service publishes).
2. **Data Model & Entity Hierarchy** — the goal-to-project-to-task hierarchy, habit as a distinct recurring entity, and how cross-links (task linked to a calendar event, task linked to a goal) are represented without duplicating ownership.
3. **API Surface** — operations exposed to the Gateway and to other services, including the contract for quick task capture (a latency-sensitive, high-frequency write path).
4. **Event Publishing & Cross-Feature Consumption** — the events this service emits (task completed, habit streak broken/extended, project rollup changed) and which downstream consumers (AI Coach, Weekly/Monthly Review, Notification Center) rely on each.
5. **Consistency Model for Rollups** — how derived state (project completion percentage, goal progress, habit streaks) is computed and kept consistent as underlying tasks change, including whether rollups are computed synchronously or via event-driven aggregation.
6. **Offline & Cross-Device Sync Considerations** — how the service's write model supports offline task capture and multi-device conflict resolution, in coordination with the Offline Mode and Cross-Device Sync PRDs.
7. **Scaling Characteristics** — write-heavy load profile (quick capture, frequent status toggles) at 100M+ users, and the resulting implications for write-path latency and storage growth.
8. **Analytics & Review Data Feed** — the architectural contract by which Weekly Review and Monthly Review consume historical task/habit data without querying live operational tables directly.
9. **Multi-Region Considerations** — placement and replication of task/project/goal/habit data given multi-region deployment.
10. **Failure Modes & Degraded Operation** — behavior when the service is degraded, and which client-side capture flows must continue working locally regardless (tie to Offline Mode PRD).

## Deliverables
- Service boundary diagram showing the task/project/goal/habit hierarchy and links out to Calendar Service and Smart Reminders.
- Entity-relationship diagram covering hierarchy, cross-links, and soft-delete/archive semantics.
- API contract summary, with the quick-capture write path called out explicitly.
- Event catalog entries (e.g., task.completed, habit.streak.updated, project.rollup.changed).
- Rollup computation model (synchronous vs. event-driven aggregation) with consistency guarantees stated per rollup type.
- Offline write/conflict-resolution model for task capture.
- Capacity model for write-heavy peak load.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Domain Boundaries, Event Architecture, API Architecture, Gateway Architecture, Authentication Architecture, and Authorization Architecture, plus User Service and Calendar Service for cross-entity references. Also informed by the Task Management PRD, Projects PRD, Goals PRD, Habits PRD, Daily Planning PRD, Weekly Review PRD, Monthly Review PRD, AI Scheduler PRD, Smart Reminders PRD, Offline Mode PRD, and Cross-Device Sync PRD.

## Teams
Platform Engineering, Productivity team, Data Engineering, Site Reliability Engineering, Mobile/Client Engineering (as a consuming team)

## Completion Criteria
- [ ] Entity hierarchy reviewed against all seven Productivity Suite PRDs to confirm no feature requires a relationship this model doesn't support.
- [ ] Rollup consistency model reviewed for correctness under concurrent edits (e.g., two devices completing subtasks of the same project simultaneously).
- [ ] Offline write/conflict model reviewed against Offline Mode PRD and Cross-Device Sync PRD for consistency of approach.
- [ ] Event catalog entries cross-checked against Event Architecture for naming and schema conventions.
- [ ] Signed off by: CTO/VP Engineering (required), Principal Architect (required), Productivity Tech Lead (required).
