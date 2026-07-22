# Document 28: Hydration PRD

## Document Name
Hydration PRD

## Purpose
Define the complete specification for water intake tracking, including how intake is logged, how progress against a daily target is displayed, and how reminder cadence is determined and arbitrated through the shared Notification System rather than fired independently by this feature.

## Why It Exists
Hydration is a high-frequency, low-effort-per-log health behavior, which makes it a good proving ground for the product's broader reminder philosophy — if hydration reminders feel naggy, they set a bad precedent for every other reminder-driven feature. This PRD exists to specify a lightweight logging experience and a reminder cadence that is genuinely adaptive to the user's day rather than a fixed timer, consistent with Never Overwhelm and the gradual-autonomy model of the Proactivity Ladder.

## Approximate Page Count
6-9 pages

## Sections
1. **Feature Scope** — In scope: water intake logging (quantity, quick-add presets), the daily hydration progress view, and the product-level rules that determine when a hydration reminder is eligible to fire. Out of scope: the Notification System's arbitration engine itself (owned by the Notification System's own specification), and daily water target-setting (owned by Health Goals PRD) — this PRD only defines tracking and reminder eligibility, not the target value.
2. **User Stories** — As a user, I want to log a glass of water in one tap without navigating a form. As a user, I want to see how close I am to my water goal at a glance. As a user who's been busy, I want a gentle reminder if I've gone a long stretch without logging water, but not a reminder every hour regardless of context. As a user who already drank water but forgot to log it, I want an easy way to add a backdated entry. As a user, I want reminders to stop once I've hit my goal for the day.
3. **Functional Requirements** — Define the quick-add logging interaction (preset quantities plus custom amount); define the daily progress display and its reset boundary; define the candidate-reminder logic — the conditions under which this feature proposes a hydration reminder to the Notification System (e.g., time since last log relative to time of day and remaining goal gap) — versus the arbitration of whether/when it actually fires, which belongs to the Notification System; define backdated entry support.
4. **Non-Functional Requirements** — Logging must be near-instant (minimal taps, no required navigation away from wherever the user currently is); reminder-candidate generation must not spam the Notification System with redundant requests; hydration data, while lower-sensitivity than other health data, must still respect the consent state defined in Permissions & Consent UX for any cross-feature use (e.g., correlating with sleep or nutrition).
5. **UX Requirements** — Must conform to Nutrition & Goals Experience and Automation Philosophy from Phase 2; quick-add must be reachable from a persistent, low-friction entry point; progress display must clearly show remaining gap to goal, not just cumulative total.
6. **States & Flows** — Daily progress states: not started, in progress, goal met, goal exceeded; reminder-candidate states: eligible, suppressed (by user's recent activity or arbitration), fired, dismissed, acted-on (user logs water in response); flow from time-based/context-based trigger to reminder candidacy to Notification System handoff.
7. **Edge Cases** — User logs an unusually large single entry (e.g., a liter at once) — progress and remaining-reminder logic must handle it without producing an absurd "goal exceeded by 400%" display; user disables hydration reminders but keeps logging manually; goal is met early in the day — reminder candidacy must stop generating for the rest of that day; user travels across time zones mid-day, shifting the reset boundary.
8. **Failure Scenarios** — A logged entry fails to sync and the progress display understates intake, potentially triggering an unwanted reminder — the system must distinguish "actually behind" from "sync pending" before proposing a reminder; reminder-candidate logic misfires repeatedly in a short window due to a bug — this must be caught by a rate ceiling within this feature's own logic, independent of Notification System arbitration, as a defense-in-depth measure.
9. **AI Behaviors** — Hydration reminder candidacy should move up the Proactivity Ladder as trust is established — starting from simple elapsed-time heuristics and evolving toward predicting a user's typical hydration rhythm (e.g., they usually log by mid-morning) and only flagging genuine deviations; the system should learn from dismissal patterns to reduce reminder frequency for users who consistently ignore or dismiss them.
10. **Notification Behaviors** — This feature only ever proposes reminder candidates; final timing, suppression, and delivery are owned by the Notification System's arbitration rules, which this PRD must explicitly defer to and describe the handoff contract for (what data accompanies a candidate, what the Notification System is expected to consider); reminders must respect quiet hours and overall daily notification budget.
11. **Success Criteria** — Users log water consistently enough to make the daily progress view meaningful; reminders are perceived as helpful nudges rather than nagging; users who don't want reminders can easily reduce or disable them without losing tracking ability.
12. **Metrics** — Daily logging frequency; goal-completion rate; reminder-candidate-to-log conversion rate; reminder dismissal/mute rate; time-to-log from app open via quick-add.
13. **Open Questions** — Should quick-add preset quantities be fixed defaults or learned per-user? How should the feature handle users who track hydration via non-water fluids? What is the minimum elapsed-time floor before a reminder candidate is even eligible for consideration, regardless of arbitration?

## Deliverables
- Water intake logging and quick-add specification
- Daily hydration progress view specification
- Reminder-candidate eligibility logic specification
- Notification System handoff contract specification

## Dependencies
Nutrition & Goals Experience, Automation Philosophy, Permissions & Consent UX (Phase 2); Health Goals PRD, Notification System PRD (Phase 3)

## Teams Using This
Product, Engineering (Mobile), Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Reminder-candidate handoff contract reviewed jointly with the Notification System PRD owner.
- [ ] UX flows validated against Nutrition & Goals Experience and Automation Philosophy.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
