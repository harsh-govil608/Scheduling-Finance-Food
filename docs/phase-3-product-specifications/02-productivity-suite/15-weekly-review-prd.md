# Document 15: Weekly Review PRD

## Document Name
Weekly Review PRD

## Purpose
Define the specification for the recurring weekly reflection feature: a once-a-week, cross-pillar recap of the past seven days compared against the prior week, paired with a lightweight forward-looking intention-setting moment for the coming week. This document specifies weekly-cadence, week-over-week-depth reflection only, distinct from the nightly recap owned by the Night Summary Experience and from the multi-week trend detection owned by the Monthly Review PRD.

## Why It Exists
The Night Summary Experience document (Phase 2) already flags that a nightly recap and any periodic rollup must not become duplicates of each other, and explicitly defers the weekly/monthly boundary to a document that "owns" that cadence. Without a dedicated Weekly Review PRD, that boundary stays unresolved and a team under deadline pressure will default to simply repeating the nightly recap seven times, which produces no new insight and trains users to ignore it. Weekly Review exists to give the user the one moment per week where the AI demonstrably connects the last seven days into a pattern the user could not have seen from any single day's summary alone.

## Approximate Page Count
8-10 pages.

## Sections
1. **Feature Scope** — in scope: 7-day recap composition across Productivity, Finance, and Health; week-over-week comparison against the immediately prior week; forward-looking next-week intention setting; surfacing goals or habits at risk within the week. Out of scope: nightly recap (owned by Night Summary Experience, Phase 2), multi-week/month trend and correlation detection (owned by Monthly Review PRD, Document 16), day-plan generation itself (owned by Daily Planning PRD, Document 14), individual habit day-to-day mechanics (owned by Habits PRD, Document 13).
2. **User Stories** — 3-5 concrete stories, e.g., a user who wants a Sunday-evening recap of what got done and what didn't across all three pillars in one view; a user whose goal fell behind pace during the week and wants that flagged without judgment; a user who was largely inactive that week and expects the review to reflect that plainly rather than invent content; a user who wants to skip a week without the AI treating it as a miss.
3. **Functional Requirements** — recap generation trigger (fixed day/time, user-configurable), 7-day pillar-by-pillar data pull, week-over-week comparison against the prior 7-day window, next-week intention prompt, at-risk goal/habit surfacing scoped to the week, user edit/acknowledge/skip actions.
4. **Non-Functional Requirements** — generation must complete before the user's configured review time; the 7-day window boundary must compute correctly across timezone changes and daylight-saving transitions; the review must remain lightweight enough to be skippable without breaking any downstream flow.
5. **UX Requirements** — must conform to the Night Summary Experience document's (Phase 2) weekly/monthly rollup boundary so no recap content duplicates the nightly summary, the Dashboard System's placement rules for periodic surfaces, and the Gamification Philosophy document's tone rules for how a missed goal or habit is reflected within the week.
6. **States & Flows** — scheduled → generated → presented → reviewed/acknowledged or skipped → archived; next-week intentions captured here carry forward into the following week's Daily Planning and Goals check-ins.
7. **Edge Cases** — a user inactive for the entire week, a goal or habit added mid-week with no full-week baseline to compare against, a user who changes their configured review day mid-cycle, two or more consecutive weekly reviews skipped in a row.
8. **Failure Scenarios** — underlying pillar data incomplete at generation time due to a sync gap, no prior-week baseline available (first week of use), generation running late and missing the user's configured window.
9. **AI Behaviors** — Proactivity Ladder application: passive surfacing of the recap and week-over-week comparison, active suggestion for next-week adjustments (e.g., proposing a loosened goal target based on the week's pace); learning from which recap items a user engages with versus dismisses, to refine what gets included in future weekly recaps.
10. **Notification Behaviors** — a single weekly notification arbitrated within the Notification System's shared interruption budget, never stacked with a same-day daily-planning or night-summary notification, with no escalation if the notification goes unacknowledged, per the Notification System's anti-nagging rule.
11. **Success Criteria** — a user can see, in one view, a genuinely new cross-pillar pattern from the past week that no single day's summary showed them; skipping a week produces no guilt-coded follow-up.
12. **Metrics** — weekly review open/engagement rate, % of next-week intentions later completed, skip rate trend over time, at-risk-goal recovery rate following a weekly flag.
13. **Open Questions** — whether the review day/time should be fixed by default or user-chosen from first use; whether users who want pillar-specific separate weekly reviews should be supported or steered toward the unified view.

## Deliverables
* Approved Weekly Review PRD.
* A recap composition rule set validated against at least one high-activity and one low-activity week.
* A weekly-to-monthly boundary reference confirming no content overlap with Monthly Review.

## Dependencies
Requires the Night Summary Experience document (Phase 2, Document 12, weekly/monthly rollup boundary this PRD resolves concretely), the Product Philosophy Document (Phase 1, tone-of-voice and anti-pattern rules), the Gamification Philosophy document (Phase 2), and the Notification System (Phase 2). Sibling dependencies: Goals PRD (Document 12), Habits PRD (Document 13), Daily Planning PRD (Document 14), Monthly Review PRD (Document 16, adjacent cadence boundary).

## Teams Using This
Product, Design, Content/Conversation Design, Data Science/ML.

## Completion Criteria
- [ ] Recap composition rules validated against at least one high-signal and one low-signal week.
- [ ] The boundary with Night Summary Experience and Monthly Review PRD is stated explicitly with no content overlap.
- [ ] At least one miss/at-risk scenario reviewed against the Gamification Philosophy document's tone rules and confirmed compliant.
- [ ] Next-week intention handoff traced end-to-end into at least one Daily Planning and Goals scenario.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
