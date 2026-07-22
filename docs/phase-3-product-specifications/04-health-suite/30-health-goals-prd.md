# Document 30: Health Goals PRD

## Document Name
Health Goals PRD

## Purpose
Define the complete specification for goal-setting across the Health Suite — nutrition, protein, water, sleep, and activity — and for surfacing progress against those goals in a unified way. This PRD owns the target values and cross-domain progress presentation; it does not own the per-domain tracking or logging mechanics themselves, which belong to their respective PRDs.

## Why It Exists
Nutrition, hydration, sleep, and activity each already have their own tracking feature, but a user's sense of "am I doing okay" is inherently cross-domain — a good day isn't just protein hit, it's protein hit and reasonably hydrated and reasonably rested. Without a unifying Health Goals feature, goal targets and progress framing would either be duplicated inconsistently across each tracking PRD or absent entirely, leaving users with numbers but no sense of whether those numbers are good. This PRD exists to define that single, coherent goals layer and to make explicit where it starts and where each tracking feature's ownership ends.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: goal-setting UX for nutrition/protein, water, sleep, and activity targets; the unified cross-domain progress view; goal adjustment and recommendation flows. Out of scope: the underlying data capture and daily summary computation for each domain (owned respectively by Nutrition Tracking, Hydration, Sleep, and Workout Tracking PRDs) — this PRD only defines the target values and the progress-against-target presentation layer.
2. **User Stories** — As a user, I want to set a daily protein and water goal that feels achievable for me, not a generic default. As a user, I want a single view showing how I'm doing across nutrition, water, sleep, and activity today, rather than checking four separate screens. As a user whose goals no longer fit my life, I want to adjust them without feeling like I'm giving up. As a new user unsure what goals to set, I want the app to suggest a reasonable starting point based on what it knows about me. As a user having a rough week, I want progress framing that doesn't feel punishing.
3. **Functional Requirements** — Define goal configuration for each domain (nutrition/protein, water, sleep, activity), including default/suggested values and manual override; define the cross-domain daily progress view that pulls current-day data from each domain's tracking feature; define goal-adjustment flow, including system-suggested adjustments based on observed patterns; define how partial goal data (e.g., a domain with no tracking enabled) is represented in the unified view.
4. **Non-Functional Requirements** — The cross-domain view must aggregate data from multiple tracking features within a defined latency budget, and must clearly indicate if any domain's data is stale, missing, or pending; goal data, like the underlying health data it targets, must respect the consent state defined in Permissions & Consent UX per domain — a user may enable nutrition tracking without enabling sleep tracking, and goals/progress must reflect only what's actually authorized; goal targets must never be presented as clinical or medical recommendations.
5. **UX Requirements** — Must conform to Nutrition & Goals Experience, Sleep & Habit Insights Experience, and Automation Philosophy from Phase 2; the unified progress view must be scannable at a glance across all enabled domains; goal-setting must avoid shame-based framing and must allow goals to be paused or removed without losing historical progress data.
6. **States & Flows** — Goal states per domain: not set, suggested (system-proposed, unconfirmed), active, paused, adjusted; unified progress states: on track, behind, met, exceeded, insufficient-data (domain not tracked or no data yet today); flow from onboarding/first-time goal suggestion to active goal to periodic adjustment prompts.
7. **Edge Cases** — A user enables a domain's tracking mid-day, producing a partial-day goal comparison; a user sets a goal that is inconsistent with their tracked history (e.g., a water goal far below their typical intake); a domain goal is disabled after being active, and historical progress data must remain viewable even though the goal is no longer active; conflicting or redundant goal suggestions across domains generated close together in time.
8. **Failure Scenarios** — One domain's underlying tracking data fails to load — the unified progress view must degrade gracefully by showing that domain as unavailable rather than silently omitting it or showing a false zero; a system-suggested goal adjustment is based on insufficient history — the system must not present it with unwarranted confidence; goal targets become out of sync after a user adjusts a goal in one place but an older cached value is shown elsewhere.
9. **AI Behaviors** — Health Goals is where Predict, Suggest, and Learn are most visible across the Health Suite — the system should suggest realistic starting goals from available history and observed patterns, and should propose adjustments when it notices a sustained mismatch between goals and actual behavior, always as a suggestion the user confirms rather than a silent change, consistent with the Proactivity Ladder's gradual-autonomy model; goal suggestions should become more personalized as more domains' data becomes available and trusted.
10. **Notification Behaviors** — Goal-adjustment suggestions and cross-domain progress check-ins are arbitrated through the shared Notification System, not fired independently by this feature; frequency must respect the Never Overwhelm principle, especially since this feature could otherwise generate one notification per domain — the goals layer should consolidate cross-domain nudges rather than multiplying them.
11. **Success Criteria** — Users have goals that feel personally realistic rather than generic; the unified progress view becomes a trusted daily reference point; goal adjustments feel like the system understanding the user better over time, not like the user failing a fixed target.
12. **Metrics** — Percentage of users with at least one active goal per domain; goal-adjustment acceptance rate when suggested by the system; unified progress view engagement rate; goal-abandonment rate (paused/removed without replacement).
13. **Open Questions** — Should goals be presented as one combined "health score" or remain per-domain without a single composite number? How aggressively should the system suggest goal adjustments before it feels intrusive? How should goals behave for a user who enables and disables domains frequently?

## Deliverables
- Per-domain goal configuration flow specification
- Cross-domain unified progress view specification
- Goal-suggestion and adjustment flow specification
- Consent-aware partial-domain display rules

## Dependencies
Nutrition & Goals Experience, Sleep & Habit Insights Experience, Automation Philosophy, Permissions & Consent UX (Phase 2); Nutrition Tracking PRD, Hydration PRD, Sleep PRD, Workout Tracking PRD, Notification System PRD (Phase 3)

## Teams Using This
Product, Engineering (Mobile), Engineering (Backend), Data/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Cross-domain aggregation contract reviewed jointly with owners of Nutrition Tracking, Hydration, Sleep, and Workout Tracking PRDs.
- [ ] UX flows validated against Nutrition & Goals Experience and Sleep & Habit Insights Experience.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Design Lead (required).
