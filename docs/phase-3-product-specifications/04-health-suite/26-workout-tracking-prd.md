# Document 26: Workout Tracking PRD

## Document Name
Workout Tracking PRD

## Purpose
Define the complete specification for logging and tracking physical activity, both manually entered and auto-detected via phone sensors at a product-requirements level. This covers what counts as a workout, how it's captured, and how activity data is surfaced back to the user — not the sensor-fusion or activity-classification algorithms themselves.

## Why It Exists
Activity is one of the core inputs to a holistic view of health alongside nutrition and sleep, and it is one of the easiest health behaviors to under-log because manual entry after a workout is an extra step people skip when tired. This PRD exists so that auto-detection (a lower-effort path toward the mission's "manual work approaches zero" goal) has a clearly specified product contract — what triggers detection, what confirmation the user sees, how false positives are handled — before engineering builds detection logic against sensor data.

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: manual workout entry (type, duration, intensity), auto-detected activity proposals sourced from phone sensors, confirmation/correction of detected activity, and the workout history view. Out of scope: the sensor-fusion/activity-classification algorithm itself (later technical phase), calorie-burn estimation methodology (owned by a nutrition/energy-balance specification if introduced), and activity goal targets (owned by Health Goals PRD).
2. **User Stories** — As a user, I want to log a workout in a few taps after I finish rather than filling out a long form. As a user, I want the app to notice I went for a run without me having to open it during the run. As a user, I want to correct a mislabeled auto-detected activity (e.g., "walk" logged as "run") easily. As a user, I want to see my activity history over the past weeks to notice patterns. As a user who forgot to log yesterday's workout, I want to add it retroactively with the correct date and time.
3. **Functional Requirements** — Define the manual entry schema (activity type, duration, start time, intensity/effort, optional notes); define the auto-detection proposal flow (what sensor-derived signal triggers a proposed entry, what confidence/thresholding gates a proposal being shown); define confirm/edit/discard behavior for detected activity; define the workout history list and detail view; define how manual and auto-detected entries merge into one unified activity timeline.
4. **Non-Functional Requirements** — Auto-detection proposals must not drain battery disproportionately to their value — sensor polling cadence is a product constraint even though the algorithm itself is out of scope; detected-activity proposals must have a bounded delay from activity end so they still feel timely; because location/motion sensor data is sensitive, all detection must respect the consent state defined in Permissions & Consent UX and must be pausable/disable-able independent of other health tracking.
5. **UX Requirements** — Must conform to Automation Philosophy and Permissions & Consent UX from Phase 2 for how auto-detected data is proposed and confirmed; manual entry must be completable in a small number of taps for common activity types; detected-but-unconfirmed activity must be visually distinct from confirmed activity everywhere it appears.
6. **States & Flows** — Entry states: manually logged, detected-pending-confirmation, confirmed, edited, dismissed/discarded; flow from sensor-derived signal to proposal to confirmation; flow for retroactive manual entry; flow for editing a confirmed entry after the fact.
7. **Edge Cases** — Two overlapping detected activities (e.g., a walk during a longer errand trip that isn't exercise); a manual entry logged for a time window that overlaps an unconfirmed detected activity; very short "activities" that are likely noise (e.g., walking across a room); a user who disables sensor-based detection mid-history — past detected entries must remain intact; multi-day or unusually long duration entries.
8. **Failure Scenarios** — Sensor-based detection produces a false positive (e.g., a car ride logged as a run) — dismissal must be a single, low-friction action and must feed back into reducing similar false positives; detection fails to fire for a real workout — manual entry must remain equally fast as the primary fallback, not a degraded afterthought; a detected proposal arrives days late due to a sync delay — it must be clearly timestamped to its actual occurrence, not to arrival time.
9. **AI Behaviors** — Auto-detection is a Predict/Suggest-tier capability on the Proactivity Ladder — it proposes, it does not silently log, until a user has explicitly granted a higher autonomy rung for this feature; the system should learn a user's typical activity patterns (common routes, usual workout times) to improve detection confidence and reduce false positives/negatives over time.
10. **Notification Behaviors** — A detected-activity confirmation prompt is a notification-worthy event and must be arbitrated through the shared Notification System rather than firing immediately upon detection, respecting quiet hours and the Never Overwhelm principle; repeated dismissals of similar proposals should reduce the frequency of that notification type per the system's learning behavior.
11. **Success Criteria** — Users log a meaningfully higher share of their real-world activity than they would with manual-only entry; false-positive detected activity is rare enough that users don't lose trust in the feature; correcting a mislabeled entry feels effortless.
12. **Metrics** — Percentage of logged activity from auto-detection vs. manual; confirmation vs. dismissal rate for detected proposals; time from activity end to logged entry; retention of sensor-based detection enabled (opt-out rate).
13. **Open Questions** — What activity types are worth auto-detecting in v1 versus manual-only? How should intensity be captured for manual entries — subjective effort scale, heart-rate-derived, or both? Should overlapping manual and detected entries auto-merge or always require user resolution?

## Deliverables
- Manual workout entry flow specification
- Auto-detection proposal and confirmation flow specification
- Unified activity timeline/history specification
- False-positive handling and feedback loop specification

## Dependencies
Automation Philosophy, Permissions & Consent UX, Nutrition & Goals Experience (Phase 2); Health Goals PRD, Nutrition Tracking PRD (Phase 3)

## Teams Using This
Product, Engineering (Mobile), Engineering (Sensors/ML), Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Auto-detection proposal thresholds reviewed jointly by Product and Engineering leads.
- [ ] UX flows validated against Automation Philosophy and Permissions & Consent UX.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
