# Document 02: Night Summary PRD

## Document Name
Night Summary PRD

## Purpose
Define the complete engineering-facing specification for the Night Summary — the end-of-day recap surface across Productivity, Finance, and Health — including the recap-selection contract each pillar must supply, the tomorrow-prep handoff mechanism that seeds the next Morning Dashboard, and the encouragement-vs-judgment rules that govern how misses are reflected without guilt-tripping.

## Why It Exists
The Night Summary Experience document (Phase 2) establishes that closing the day must feel like proof the AI was paying attention, not a Health-app streak screen and a Finance total shown back to back, but it does not specify the data contract, the carry-forward mechanism into tomorrow, or the concrete rule engine that keeps a missed budget or a skipped workout from reading as judgment — without that, engineering has no way to build the single closing ritual the philosophy calls for, and pillar teams will each ship their own end-of-day card. This PRD exists so "Never Overwhelm" and "Encourage" are enforced as testable behavior at the one surface most likely to either build trust or quietly erode it every single night.

## Approximate Page Count
8-11 pages.

## Sections
1. **Feature Scope** — in scope: recap-item selection and composition across pillars, the "AI was watching" proof-point rendering, the tomorrow-prep handoff payload, encouragement/judgment framing rules, skippability mechanics; out of scope: weekly/monthly roll-up reporting, owned by a sibling Life Utility or Intelligence Layer PRD, and the Morning Dashboard's own rendering logic, owned by the Morning Dashboard PRD (Document 01) which only consumes the handoff payload.
2. **User Stories** — e.g., as a user who missed their step goal and skipped a scheduled task today, I see both reflected without a guilt-inducing tone, alongside something I did follow through on; as a user with an uneventful, low-signal day, I get a short, honest close rather than a padded recap; as a user who opens the app at night, an item I addressed earlier today reappears as evidence the AI noticed, not as a duplicate task.
3. **Functional Requirements** — the recap-item selection contract each pillar service must populate (what happened, whether it was a follow-through or a miss, a specific non-generic detail proving continuity), the tomorrow-prep payload structure and how it's consumed by the Morning Dashboard the next day, the encouragement/judgment rule engine applied to every miss before render, and the skip/dismiss mechanic that never blocks or nags.
4. **Non-Functional Requirements** — a strict content-selection budget so the recap stays a curated few items rather than a full event log, a continuity-integrity requirement guaranteeing every carry-forward item is verifiably passed to the next Morning Dashboard render, and an on-device or privacy-scoped assembly boundary consistent with the Morning Dashboard's.
5. **UX Requirements** — must conform to the Night Summary Experience document's recap-composition and "AI was watching" proof-point rules, the tone-of-voice and anti-pattern rules in the Product Philosophy Document, and the boundary against becoming a second mandatory daily ritual on top of the Morning Dashboard.
6. **States & Flows** — Not-Yet-Available (before day's activity is sufficient to summarize), Available-Unopened, Viewed-High-Signal, Viewed-Low-Signal, Viewed-With-Miss, Skipped, Carry-Forward-Queued.
7. **Edge Cases** — a day with only misses and no follow-throughs at all; a user who opens the app for the first time at night (no prior Morning Dashboard that day); a carry-forward item that becomes irrelevant overnight (e.g., a bill gets auto-paid before morning); a user who checks the summary multiple times in one evening as new activity lands.
8. **Failure Scenarios** — what happens when the core assumption "there is enough signal to synthesize a meaningful close" breaks: a pillar service failed to report activity for the day, the miss/follow-through classification is ambiguous or contradictory across pillars, or the tomorrow-prep payload fails to reach the Morning Dashboard pipeline.
9. **AI Behaviors** — how the Proactivity Ladder rung affects whether a miss is silently logged, passively shown, or actively framed with a suggested adjustment for tomorrow; how the system learns which proof-point details a given user actually finds meaningful versus ignores, to refine future recap selection.
10. **Notification Behaviors** — whether the Night Summary becoming available triggers a low-priority passive notification or widget update under the Notification System's arbitration rules, and how it avoids competing with any pillar's own evening reminder for the same interruption-budget slot.
11. **Success Criteria** — a working Night Summary reads as evidence the AI was present all day, closes cleanly on both eventful and uneventful days, and never leaves a user feeling worse for having opened it.
12. **Metrics** — nightly open rate, miss-framing sentiment (via lightweight in-product feedback), carry-forward-to-next-morning integrity rate, skip rate, and low-signal-night frequency.
13. **Open Questions** — how many consecutive misses in one pillar before the encouragement framing needs to shift from individual-day language to a pattern-level suggestion (handed to a sibling Habit Tracking or Goal Planning PRD); whether users should be able to permanently opt out of the ritual without losing the tomorrow-prep handoff benefit.

## Deliverables
* Approved Night Summary PRD.
* Recap-item selection contract for pillar services to implement against.
* Tomorrow-prep handoff payload specification, traced end-to-end into the Morning Dashboard PRD's candidate-item contract.
* An encouragement/judgment rule table covering miss types across all three pillars.

## Dependencies
Requires the **Night Summary Experience** document (Phase 2, Document 12), the **Morning Experience** document (Phase 2, Document 11) and the **Morning Dashboard PRD** (Document 01) for the handoff contract, the **Notification System** document (Phase 2, Document 14), the **Memory Model — Behavioral Perspective** document (Phase 2), the **Daily Flow** document (Phase 2, Document 10), and the **Product Philosophy Document** (Phase 1) for the anti-guilt and tone-of-voice rules.

## Teams Using This
Product, Engineering (Client), Engineering (Backend), Data Science/ML, Design, Content/Conversation Design, QA.

## Completion Criteria
- [ ] Recap-item selection contract accepted by Productivity, Finance, and Health pillar engineering leads.
- [ ] Tomorrow-prep handoff payload verified end-to-end against the Morning Dashboard PRD's candidate-item contract.
- [ ] Every miss-framing rule reviewed against the Product Philosophy Document's anti-guilt anti-patterns and confirmed compliant.
- [ ] Low-signal and all-miss scenarios each have a specified, non-padded, non-punitive behavior.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Design (required).
