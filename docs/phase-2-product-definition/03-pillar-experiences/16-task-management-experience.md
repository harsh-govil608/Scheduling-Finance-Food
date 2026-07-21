# Document 16: Task Management Experience

## Document Name
Task Management Experience

## Purpose

Define the full lifecycle of a task as a product entity — creation, organization, prioritization, editing, and completion — independent of when it gets time-committed on a calendar or how reminders are delivered. This document establishes what a "task" is and how it behaves from birth to resolution.

## Why It Exists

The Productivity pillar's daily prioritization, goal planning, and adaptive rescheduling capabilities all operate on a shared underlying entity: the task. If Engineering teams building voice capture, quick-add, goal decomposition, and daily prioritization each invent their own notion of what a task is and what "done" means, the product ends up with silently divergent task states — a task marked complete in one surface and still open in another. That directly breaks the Remember and Learn parts of the Behavioral Loop, since the AI cannot reliably learn from a history it cannot consistently represent, and it erodes the basic trust that the assistant actually tracked what it was told.

## Approximate Page Count

7-9 pages.

## Sections

1. **Task Entity Definition** — what constitutes a task, its required and optional attributes (title, due context, priority, source, goal linkage), and how it differs from a reminder or a calendar event.
2. **Task Creation Pathways** — the distinct ways a task enters the system (manual quick-add, voice capture, AI-suggested from a goal, AI-inferred from conversation or context) and the confirmation expectations for each.
3. **Task Organization Model** — how tasks are grouped, tagged, and linked to goals or projects, and how they are surfaced in lists, independent of any calendar placement.
4. **Prioritization Signal Surface** — how the AI's daily-prioritization signal is presented on a task (not how it is computed, which is ML territory), and how a user overrides it.
5. **Task Editing & Correction** — what a user can change about a task after creation, and what happens to the AI's confidence or learned pattern when they do.
6. **Completion & Abandonment States** — the full set of end states a task can reach (done, skipped, deferred indefinitely, auto-archived) and the tone and behavior associated with each.
7. **Goal-to-Task Decomposition Boundary** — where goal planning hands off into individual tasks, and what remains owned by goal planning versus by task management.
8. **Relationship to Scheduling and Reminders** — an explicit boundary statement: this document owns the task's existence and state; the Scheduling System Experience Document owns when and how it gets time-committed; reminder delivery is owned by the shared automation/notification layer described in the Product Architecture Overview.

## Deliverables

* Approved Task Management Experience document.
* A task lifecycle state diagram covering every creation pathway through every end state.
* A task attribute reference (fields and their meaning at a product level, not a database schema).

## Dependencies

Requires the Product Architecture Overview (five-component frame, automation/notification layer) and the Product Pillars Overview (Productivity Pillar Surface) from the Phase 2 Product Architecture group, and the Product (Behavioral) Philosophy Document (Phase 1, Remember/Learn verbs). Shares an explicit boundary with the Scheduling System Experience Document and the Calendar Experience Document, both of which must be read alongside this one to avoid redefining each other's scope.

## Which Teams Use This

Product, Design, Engineering (Productivity feature team), Data Science/ML, QA.

## Completion Criteria

- [ ] Every task end-state has a defined user-facing behavior with no undefined or ambiguous states.
- [ ] The creation-pathway list has been validated against at least one real scenario per pathway (manual, voice, AI-suggested, AI-inferred).
- [ ] The boundary with the Scheduling System Experience Document and the Calendar Experience Document has been reviewed jointly with no overlapping ownership.
- [ ] The task attribute reference has been reviewed by Engineering for feasibility without prescribing implementation.
- [ ] Signed off by: Head of Product (required), Productivity Feature Team Lead (required).
