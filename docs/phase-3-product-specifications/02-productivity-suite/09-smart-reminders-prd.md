# Document 09: Smart Reminders PRD

## Document Name
Smart Reminders PRD

## Purpose
This PRD will define the complete specification for reminders that adapt timing, urgency, and delivery channel based on context, location, and the user's demonstrated follow-through pattern — not static time-based alerts. It defines the signals a reminder can react to, how it decides to hold, escalate, or re-fire, and how it learns from repeated snooze/defer/dismiss behavior.

## Why It Exists
Static reminders are the exact reactive pattern this company exists to replace — a 7am alarm that fires into an empty room teaches the user to ignore reminders altogether, undermining trust in every other proactive feature. Without a dedicated spec, "smart" stays a marketing adjective instead of an engineering requirement, and reminder logic risks being duplicated or contradicted by the Scheduler's own rescheduling notifications.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: context-aware retiming, location-based triggers, urgency escalation, snooze/defer learning, and channel selection (push, in-app, voice) for a single reminder. Out of scope: the calendar block a reminder may be attached to (owned by AI Scheduler PRD) and the task entity a reminder references (owned by Task Management PRD) — this PRD owns only the alerting behavior layered on top of them.
2. **User Stories** — Include stories such as: as a user with a recurring 8am medicine reminder, when I'm still asleep at 8am (per phone-activity signal), the reminder should hold and re-fire at first activity rather than firing into silence; as a user who always snoozes a specific reminder type twice before acting, I want the first fire time to shift later automatically; as a user leaving for an errand, I want a location-triggered reminder to fire when I'm near the relevant place rather than at a fixed clock time; as a user who has ignored a reminder three times running, I want its urgency/channel to escalate rather than repeat identically forever.
3. **Functional Requirements** — Define the retiming decision inputs (activity signal, location signal, calendar-busy state), the escalation ladder (what changes between attempt 1, 2, 3+: tone, channel, frequency), the snooze/defer learning loop (how a pattern of snoozes updates the default fire time), and the rule set for location-based trigger radius and dwell logic.
4. **Non-Functional Requirements** — Define the latency ceiling between a trigger condition being met and the reminder firing, battery/background-processing budget for location and activity monitoring, and the privacy boundary on location/activity data retention and on-device vs. server-side processing.
5. **UX Requirements** — This feature must conform to the Notification System and Task Management Experience (Phase 2) for how a reminder is visually and tonally distinguished from a task due-date badge or a scheduling notification; feature-specific UX rules must define how escalation is perceptible to the user without feeling punitive, and how a user reviews/edits the learned retiming behind a given reminder.
6. **States & Flows** — Enumerate the lifecycle: scheduled → held (condition not yet met) → fired → (acknowledged / snoozed / dismissed / escalated) → resolved/expired, plus the recurring-reminder loop back to scheduled.
7. **Edge Cases** — Cover reminders tied to a task that gets rescheduled mid-flight, overlapping location and time triggers firing simultaneously, a reminder whose trigger condition (e.g., location) never resolves within the day, and multi-device state where a reminder is acknowledged on one device while pending on another.
8. **Failure Scenarios** — Define behavior when this feature's core assumption — that the device can reliably observe activity/location signals — breaks: permissions revoked mid-use, prolonged offline state preventing location triggers, or a device restart clearing a pending held reminder.
9. **AI Behaviors** — Detail how reminder timing/retiming/escalation confidence moves along the Proactivity Ladder (from purely static delivery, to passive retiming suggestions, to silent autonomous retiming with notification), and how the learning loop distinguishes "user didn't need this reminder" from "user needed it delivered differently."
10. **Notification Behaviors** — Define how a held-then-fired reminder integrates with the Notification System's arbitration rules (priority relative to Scheduler/Calendar Intelligence alerts), how escalated reminders interact with quiet hours/do-not-disturb, and how multiple due reminders are batched versus delivered individually.
11. **Success Criteria** — State the qualitative bar: a user should feel reminders "know" their real-world context and rarely need to be manually retimed or silenced out of annoyance.
12. **Metrics** — Define quantitative targets such as first-fire acknowledgment rate, average snoozes-to-completion, retiming-accuracy (predicted vs. actual best fire time), and reminder-mute/disable rate as a negative signal.
13. **Open Questions** — Capture unresolved questions such as how aggressively location-based triggers should be used given battery/privacy tradeoffs, and how escalation should behave for reminders tied to time-critical health actions (e.g., medication) versus low-stakes ones.

## Deliverables
- Full Smart Reminders PRD document following the 13-section structure above.
- Retiming/escalation decision-flow diagram.
- Reminder lifecycle-state diagram.
- Snooze-pattern-to-default-time learning-loop specification.

## Dependencies
Phase 3: Task Management PRD, AI Scheduler PRD. Phase 2: Automation Philosophy, Notification System, Task Management Experience, Scheduling System Experience. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Notifications/Backend), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules for conflicts with Scheduler and Calendar Intelligence alerts.
- [ ] Escalation ladder defined with explicit stop conditions to prevent runaway notification frequency.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
