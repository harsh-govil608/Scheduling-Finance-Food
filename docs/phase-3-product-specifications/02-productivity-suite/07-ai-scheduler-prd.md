# Document 07: AI Scheduler PRD

## Document Name
AI Scheduler PRD

## Purpose
This PRD will define the product behavior of the engine that actively places, moves, and defends time blocks on the user's behalf: how it proposes a schedule, how it negotiates conflicts with the user's existing commitments, and how it adjusts that schedule as the day changes. It defines the scheduling *actions* the system is permitted to take, at what trust level, and with what evidence — not the calendar's read-only insights, which live in the Calendar Intelligence PRD.

## Why It Exists
Without this spec, "auto-scheduling" collapses into either a gimmicky one-time slot-finder or an untrusted black box that silently moves things the user cares about; this document exists to force explicit answers to when the AI may act versus merely suggest, tying every scheduling action back to the Proactivity Ladder so autonomy is earned through demonstrated reliability rather than assumed on day one. Given that manual calendar management is one of the clearest "reactive" behaviors the company's mission is built to eliminate, this is one of the highest-leverage PRDs in the Productivity Suite and must be precise about the boundary between the Scheduler's actions and the Calendar's insights, or engineering will build overlapping and contradictory systems.

## Approximate Page Count
10-13 pages

## Sections
1. **Feature Scope** — In scope: proposing new time blocks for tasks/goals lacking a fixed time, detecting and resolving scheduling conflicts by moving negotiable blocks, rescheduling in response to missed or run-over events, and holding/releasing tentative blocks pending confirmation. Out of scope: conflict *detection* surfaced without action (owned by Calendar Intelligence PRD), the underlying task/recurrence data model (owned by Task Management PRD), and habit-specific time placement (owned by the sibling Habits PRD).
2. **User Stories** — Include stories such as: as a user who added a task with no time, I want the Scheduler to propose a slot that respects my known focus-hours pattern rather than dropping it at the next free minute; as a user whose meeting ran long, I want negotiable blocks after it to shift automatically without me re-typing times; as a user who habitually declines the Scheduler's first suggestion for a task type, I want it to stop proposing that slot type after the pattern is clear; as a user, I want to designate certain blocks (e.g., a therapy appointment) as never auto-movable.
3. **Functional Requirements** — Define slot-proposal logic inputs (task duration, deadline, priority, historical acceptance patterns), the negotiation/conflict-resolution algorithm's decision order (what moves before what), the rules for what counts as a "movable" vs "fixed" block, rescheduling triggers (missed block, running late, external calendar change), and the confirmation/undo mechanism for any block the system places or moves.
4. **Non-Functional Requirements** — Define latency ceiling for schedule re-computation after a triggering event (e.g., a moved block must resolve and reflect in-app within a defined threshold), consistency requirements when the same schedule is edited concurrently on two devices, and the privacy boundary on how much of the day's contents can be used as scheduling signal versus what must stay purely on-device.
5. **UX Requirements** — This feature must conform to the Scheduling System Experience and Calendar Experience (Phase 2) for how proposed/placed blocks are visually distinguished from user-placed ones, and to the Automation Philosophy for consent language at each ladder rung; feature-specific UX rules must cover how a "proposed but unconfirmed" block is rendered distinctly from a "confirmed and placed" one.
6. **States & Flows** — Enumerate the lifecycle a scheduled block moves through: unscheduled → proposed → awaiting confirmation → confirmed/placed → in-progress → completed/missed → rescheduled, plus the branch where a user rejects a proposal and the system must record that rejection as a learning signal.
7. **Edge Cases** — Cover cascading reschedules where moving one block forces a chain of others, blocks that have no valid slot before their deadline, conflicting fixed commitments added after a proposal was already confirmed, and multi-day tasks that span a rescheduling event mid-execution.
8. **Failure Scenarios** — Define behavior when the Scheduler's core assumption — that it has an accurate, current picture of the user's calendar — breaks: stale external calendar sync, a device offline during a triggering event, or a scheduling loop where two automated adjustments repeatedly re-trigger each other.
9. **AI Behaviors** — Detail how the Scheduler's autonomy level (silent observation of accepted/rejected proposals, passive surfacing of a suggested slot, active suggestion with one-tap accept, pre-filled placement awaiting confirmation, fully autonomous placement with notification) is earned per user and per task-category, and how repeated rejections demote autonomy back down the ladder.
10. **Notification Behaviors** — Define which scheduling events generate a notification versus a silent in-app update, how a batch of cascading reschedules is summarized into a single notification rather than a flood, and how this integrates with the Notification System's arbitration and quiet-hours rules.
11. **Success Criteria** — State the qualitative bar: a user should feel their schedule "keeps itself honest" without feeling ambushed by changes they didn't approve.
12. **Metrics** — Define quantitative targets such as proposal-acceptance rate, average time-to-confirmation, rate of manual overrides after an autonomous placement, and reschedule-cascade frequency.
13. **Open Questions** — Capture unresolved questions such as how far in advance the Scheduler is allowed to reorganize a day, and whether cross-pillar blocks (e.g., a health habit vs. a work task) are arbitrated by this PRD or a cross-pillar coordination layer.

## Deliverables
- Full AI Scheduler PRD document following the 13-section structure above.
- Slot-proposal and conflict-resolution decision-flow diagram.
- Block-state lifecycle diagram.
- Ladder-rung-to-autonomy mapping table for scheduling actions.

## Dependencies
Phase 3: Calendar Intelligence PRD, Task Management PRD, Smart Reminders PRD (for reschedule-triggered reminder updates). Phase 2: Automation Philosophy, Scheduling System Experience, Calendar Experience, Notification System, Task Management Experience. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Scheduling/Backend), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Block lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules for conflicts with other pillars.
- [ ] Scope boundary against Calendar Intelligence PRD confirmed with no functional overlap.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), AI/ML Lead (required).
