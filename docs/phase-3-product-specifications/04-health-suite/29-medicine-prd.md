# Document 29: Medicine PRD

## Document Name
Medicine PRD

## Purpose
Define the complete specification for medication reminders and adherence tracking — how a user sets up a medication schedule, how reminders are delivered, how adherence (taken/skipped/missed) is recorded, and how the system surfaces adherence trends. Because a missed medication reminder can have real health consequences unlike other reminder types in this suite, this document places elevated emphasis on reliability, clarity, and failure handling throughout.

## Why It Exists
Medicine adherence is the one feature in the Health pillar where a notification failure or a UX ambiguity is not merely an inconvenience — it can mean a real dose is missed. This PRD exists to give this feature a stricter product contract than other reminder-driven features: reminders must be exceptionally reliable, the "did I take it" state must never be ambiguous, and the AI's growing autonomy on the Proactivity Ladder must be applied more conservatively here than elsewhere, since silent errors carry higher real-world cost than in nutrition or hydration tracking.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: medication schedule setup (drug name, dosage note, frequency/times), reminder delivery, adherence logging (taken/skipped/snoozed/missed), and adherence history/trend view. Out of scope: drug interaction checking or clinical dosing guidance (explicitly not a clinical feature — this is an adherence and reminder tool, not medical advice), and the Notification System's core arbitration engine (owned by its own PRD), though this PRD defines the elevated-priority contract this feature requires from it.
2. **User Stories** — As a user managing a daily medication, I want a reliable reminder at the right time so I don't forget a dose. As a user, I want to quickly confirm I took my medicine without a multi-step flow. As a user who takes medicine at irregular times, I want to adjust today's reminder without disrupting my regular schedule. As a caregiver-minded user, I want to see my adherence history to notice if I've been missing doses. As a user who genuinely skips a dose intentionally (e.g., doctor's instruction), I want to record that distinctly from simply missing it.
3. **Functional Requirements** — Define the medication schedule setup flow (name, dosage note, times/frequency, start/end date if applicable); define reminder delivery requirements including required redundancy/escalation behavior distinct from standard reminders; define the adherence-logging interaction (taken, skipped-intentionally, snoozed, unacknowledged/missed) and its required minimal friction; define the adherence history view and streak/trend representation; define multi-medication handling (several medications with overlapping or distinct schedules).
4. **Non-Functional Requirements** — Medication reminders must have the highest reliability guarantee of any reminder type in the product — this PRD must specify required delivery-confirmation and escalation behavior (e.g., a second nudge if the first is not acknowledged within a defined window) beyond what standard notifications provide; medication data is especially sensitive health data and must be held to the strictest consent and access-control requirements defined in Permissions & Consent UX, including explicit, unambiguous opt-in before any medication data is collected; the system must never silently fail to deliver a scheduled reminder — failure to deliver must itself be logged and surfaced.
5. **UX Requirements** — Must conform to Permissions & Consent UX and Automation Philosophy from Phase 2, with this feature held to the strictest interpretation of consent requirements in the entire Health Suite; the "mark as taken" action must be reachable in one interaction from the reminder itself, not requiring app navigation; adherence history must never guilt or shame the user — presentation must remain factual and supportive per the Encourage philosophy pillar, while still being honest about missed doses.
6. **States & Flows** — Schedule states: active, paused, ended; per-dose states: pending, reminded, acknowledged-taken, acknowledged-skipped, snoozed, unacknowledged/missed (time-boxed before it transitions to missed); flow from scheduled time to reminder delivery to acknowledgment to adherence record; flow for editing an active schedule mid-course.
7. **Edge Cases** — A dose reminder fires while the user's device is offline; a user takes a dose earlier or later than scheduled and needs to log it without it looking like a missed-then-late-recovered dose; overlapping medications scheduled at the same time; a user pauses a medication (e.g., prescription ended) but has history that must remain intact; a snoozed reminder that's snoozed repeatedly and never resolved.
8. **Failure Scenarios** — A scheduled reminder fails to deliver due to a platform/OS notification failure — this must be detected and either escalated through a fallback channel or explicitly logged as an undelivered reminder rather than silently counted as "missed" against the user; the app cannot determine whether a dose was actually taken (ambiguous acknowledgment) — the system must never assume "taken" by default, only ever by explicit user action; a user reports a medication as taken but the timestamp is clearly implausible (e.g., days in the future) — this must be caught and flagged for correction rather than silently accepted into adherence history.
9. **AI Behaviors** — Medicine reminders sit at a deliberately conservative rung of the Proactivity Ladder relative to other Health Suite features — even as trust is established, the system should not reduce reminder frequency or silently suppress reminders the way it might for lower-stakes nudges like hydration, given the real-world cost of a missed dose; the system may use Learn/Adapt to refine reminder timing to better match the user's actual routine (e.g., "usually takes it right after breakfast"), but any such adaptation must be transparent and user-confirmable, never silent.
10. **Notification Behaviors** — Medication reminders must be treated as the highest-priority notification category in the shared Notification System's arbitration — they must not be silently suppressed by quiet hours or notification-budget logic the way lower-stakes reminders can be, and this PRD must specify the override/escalation contract expected from the Notification System; missed-dose follow-ups must be handled distinctly from the original reminder, not merely repeated.
11. **Success Criteria** — Users experience medication reminders as dependable to the point of not needing an external reminder system; adherence logging feels effortless enough that users actually use it consistently rather than abandoning it; adherence history is honest, clear, and never punitive in tone.
12. **Metrics** — Reminder delivery success rate (must be tracked distinctly and held to a stricter bar than other reminder types); adherence rate (taken vs. scheduled); acknowledgment latency (time from reminder to logged response); missed-dose rate and whether it trends down over time for a given user.
13. **Open Questions** — What escalation channel (secondary notification, different modality) should be used if a primary medication reminder goes unacknowledged, and within what time window? Should the product support caregiver/family visibility into adherence, and if so, how does that interact with consent requirements? How should intentionally-skipped doses (per doctor's guidance) be distinguished in a way that doesn't get conflated with genuinely missed doses in reporting?

## Deliverables
- Medication schedule setup flow specification
- Reminder delivery and escalation requirements specification
- Adherence logging interaction specification
- Adherence history/trend view specification
- Notification System priority/override contract specification

## Dependencies
Permissions & Consent UX, Automation Philosophy (Phase 2); Notification System PRD, Health Goals PRD (Phase 3)

## Teams Using This
Product, Engineering (Mobile), Engineering (Backend/Notifications), Design, QA, Legal/Compliance

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario, with failure scenarios specifically reviewed for real-world health consequence.
- [ ] Reminder reliability and escalation requirements reviewed and approved by Engineering Lead as technically achievable.
- [ ] Consent and data-sensitivity requirements reviewed and approved by Legal/Compliance.
- [ ] UX flows validated against Permissions & Consent UX with no ambiguous acknowledgment states remaining.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Legal/Compliance (required).
