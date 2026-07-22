# Document 08: Calendar Intelligence PRD

## Document Name
Calendar Intelligence PRD

## Purpose
This PRD will define the analytical/insight layer that observes the user's calendar and surfaces conflicts, travel-time risk, meeting-load concerns, and pattern-based warnings — without itself moving or placing anything. It defines what the system notices and how it explains what it notices, drawing a firm line between "telling the user something true about their calendar" (this PRD) and "acting on the calendar" (AI Scheduler PRD).

## Why It Exists
Calendars fail users silently — a double-booking discovered mid-commute, a day of back-to-back meetings with no buffer, a flight the user forgot overlaps a standing 1:1 — and each of these is a "predict and warn" failure the product's philosophy explicitly commits to preventing. Without a dedicated spec, insight logic tends to get bolted piecemeal onto the Scheduler or the base Calendar UI, producing duplicate detection code, inconsistent warning thresholds, and confusion for engineers about which system owns which decision.

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: conflict/double-booking detection, travel-time buffer calculation and warnings, meeting-load and fragmentation analysis, pattern-based warnings (e.g., "you have back-to-back days like this three weeks running"). Out of scope: any automatic movement or placement of calendar blocks (owned by AI Scheduler PRD) and the calendar's base rendering/navigation UI (owned by Calendar Experience, Phase 2).
2. **User Stories** — Include stories such as: as a user, when two meetings overlap I want to be told before the day starts, not when I'm already late to one; as a user with back-to-back meetings across the city, I want a travel-time conflict flagged even though the events themselves don't technically overlap; as a user, I want a warning when a day crosses a meeting-density threshold I've historically found overwhelming; as a user, I want insight severity to scale with how far in advance it's caught.
3. **Functional Requirements** — Define the conflict-detection algorithm (overlap types it must catch, including travel-adjacent conflicts), the meeting-load scoring model and its inputs, the buffer-calculation logic (source of travel-time estimates, fallback when unavailable), and the requirement that every insight produced must be inspectable/explainable to the user on demand.
4. **Non-Functional Requirements** — Define the freshness requirement for insights after a calendar change (recompute latency), behavior under degraded/no connectivity for travel-time data, and privacy constraints on using event titles/locations/attendees as analysis input versus what must stay opaque to backend processing.
5. **UX Requirements** — This feature must conform to the Calendar Experience and Notification System (Phase 2) for how an insight is visually badged on an event versus surfaced as a standalone alert; feature-specific UX rules must define severity tiering (informational vs. urgent) and how an insight links directly to the AI Scheduler's proposed fix without merging the two features' UI.
6. **States & Flows** — Enumerate an insight's lifecycle: detected → surfaced → (acknowledged / dismissed / acted-upon-via-Scheduler / expired-by-time-passing), and the recompute flow triggered by any calendar mutation.
7. **Edge Cases** — Cover recurring events with per-instance conflicts, multi-calendar accounts with conflicting sources of truth, all-day/multi-day events overlapping timed events, and events with no location data available for travel-time calculation.
8. **Failure Scenarios** — Define behavior when the feature's core assumption — that the connected calendar data is complete and current — breaks: a missed sync from an external calendar provider, an event edited outside the app producing a stale insight, or a travel-time API outage during a high-conflict day.
9. **AI Behaviors** — Detail how insight confidence and surfacing aggressiveness move along the Proactivity Ladder (e.g., early on, only silently logging conflict patterns; later, proactively surfacing meeting-load warnings before the user asks), and how user dismissals of a given insight type feed back into suppressing similar future insights.
10. **Notification Behaviors** — Define which insight severities warrant a push/interruptive notification versus a passive in-app badge, how multiple insights on the same day are bundled into one notification, and how this defers to the Notification System's arbitration rules relative to Scheduler and Reminder notifications on the same day.
11. **Success Criteria** — State the qualitative bar: a user should never be surprised by a calendar conflict they could have been warned about in advance, and should trust every surfaced insight as accurate rather than noisy.
12. **Metrics** — Define quantitative targets such as conflict-detection recall/precision, average lead time between insight surfaced and event start, insight dismissal rate by type, and false-positive rate on travel-time warnings.
13. **Open Questions** — Capture unresolved questions such as how much attendee/location data external users are comfortable having analyzed for travel-time inference, and where the line sits between a "meeting-load warning" (this PRD) and a wellbeing signal (potentially owned by a Health-pillar PRD).

## Deliverables
- Full Calendar Intelligence PRD document following the 13-section structure above.
- Conflict/overlap taxonomy (types of conflicts the system must detect).
- Insight severity-tiering matrix.
- Insight lifecycle diagram.

## Dependencies
Phase 3: AI Scheduler PRD, Task Management PRD. Phase 2: Calendar Experience, Scheduling System Experience, Notification System, Automation Philosophy. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Calendar/Backend), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Conflict taxonomy covers overlap, travel-time, and load-based insight types with no ambiguity on ownership vs. AI Scheduler PRD.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
