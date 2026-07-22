# Document 27: Sleep PRD

## Document Name
Sleep PRD

## Purpose
Define the complete specification for the sleep tracking and insight-surfacing feature — how sleep sessions are captured or inferred, how they're presented to the user, and how the system turns raw sleep data into meaningful, actionable insight without overwhelming the user with metrics they can't act on.

## Why It Exists
Sleep is a foundational input to nearly every other health signal (energy, appetite, activity motivation) but is also one of the least actively "logged" behaviors — users don't want to interact with their phone while trying to fall asleep or right after waking. This PRD exists to specify a sleep experience that asks almost nothing of the user during the sleep window itself, while still surfacing insight that feels earned and useful, consistent with the mission of an AI that manages life proactively rather than demanding manual input.

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: sleep session capture (automatic and manual fallback), the sleep summary view, and insight surfacing (e.g., patterns correlated with sleep quality). Out of scope: the underlying sleep-stage detection algorithm/sensor fusion (later technical phase) and cross-domain habit correlation logic beyond sleep itself (owned by Sleep & Habit Insights Experience at the UX layer, and by a broader Habit Tracking PRD if one exists for cross-domain habits).
2. **User Stories** — As a user, I want my sleep to be tracked automatically without having to start or stop anything manually. As a user, I want a simple morning summary of how I slept rather than a wall of unexplained charts. As a user, I want to understand what factors seem to correlate with my better sleep nights. As a user who wakes up at inconsistent times, I want the app to correctly identify one continuous sleep session rather than splitting it. As a user without a wearable, I want a manual way to log sleep that isn't tedious.
3. **Functional Requirements** — Define automatic sleep session detection triggers and boundaries (sleep start/end inference) at a product level; define the manual sleep logging fallback (bedtime/wake time entry) for users without applicable sensors; define the daily sleep summary (duration, consistency vs. usual pattern, one to two headline insights); define the insight-generation rules for surfacing correlations (e.g., late meals and shorter sleep) without overstating causation; define historical sleep trend views.
4. **Non-Functional Requirements** — Automatic detection must not require user interaction during the sleep window; insight generation must only surface a correlation once there is sufficient data to support it, avoiding premature or misleading conclusions; because sleep data is sensitive health data, capture and any correlation analysis must respect the consent state defined in Permissions & Consent UX; detection must work reasonably across irregular schedules (shift work, naps) without hard-coding a single "night" window.
5. **UX Requirements** — Must conform to Sleep & Habit Insights Experience from Phase 2; the morning summary must be readable in seconds, not requiring the user to interpret raw charts; insights must be presented as observations, not prescriptions, consistent with the Suggest-not-command philosophy.
6. **States & Flows** — Session states: in-progress (detected but not yet closed), completed, manually logged, corrected, discarded (e.g., a false-positive nap misidentified as sleep); flow from detected session to morning summary to available insight; flow for manual entry when no automatic session exists for a given night.
7. **Edge Cases** — Naps versus main sleep session distinction; travel across time zones shifting the "night" boundary; a sleep session that's interrupted (multiple wake periods) and how it's represented as one entry; a user who sleeps during the day (shift work) and how the "day" the sleep counts toward is determined; missing data for one or more nights breaking a trend view.
8. **Failure Scenarios** — Automatic detection fails to register a real sleep session — the morning summary must clearly show "no sleep detected" and offer manual entry rather than showing a misleading zero or blank as if by default; a detected session is wildly inaccurate (e.g., includes waking hours) — correction must be simple and must inform future detection; insight engine has insufficient data — it must say so rather than presenting a low-confidence correlation as fact.
9. **AI Behaviors** — Sleep operates primarily at the Remember/Predict/Learn tiers of the Proactivity Ladder — it observes patterns over time and predicts what "normal" looks like for this specific user before it can meaningfully flag deviations; insight surfacing should adapt to which types of insights a given user finds useful versus dismisses, per the Learn/Adapt philosophy pillars.
10. **Notification Behaviors** — The morning sleep summary and any insight are arbitrated through the shared Notification System rather than appearing as an intrusive alert; timing must respect the user's actual wake time (inferred from the sleep session itself) rather than a fixed clock time, and must never surface during the sleep window itself, honoring quiet hours and Never Overwhelm.
11. **Success Criteria** — Users understand their sleep patterns without manual tracking effort; surfaced insights feel accurate and earned rather than generic; users increasingly check the sleep summary because they find it useful, not because they're prompted.
12. **Metrics** — Percentage of nights with a successfully auto-detected session; manual-entry fallback usage rate; morning summary view rate; insight dismissal vs. positive-engagement rate; correction rate for detected sessions.
13. **Open Questions** — What is the minimum data history required before the system surfaces its first correlation insight? How should naps be treated in daily/weekly summaries — separately or folded into total sleep? How should the feature behave for users with highly irregular schedules where "a night" isn't a meaningful unit?

## Deliverables
- Automatic sleep detection product-requirements specification
- Manual sleep logging fallback specification
- Morning summary and insight-surfacing specification
- Sleep trend/history view specification

## Dependencies
Sleep & Habit Insights Experience, Automation Philosophy, Permissions & Consent UX (Phase 2); Health Goals PRD (Phase 3)

## Teams Using This
Product, Engineering (Mobile), Engineering (Sensors/ML), Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Insight-generation confidence thresholds reviewed jointly by Product and ML leads.
- [ ] UX flows validated against Sleep & Habit Insights Experience.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
