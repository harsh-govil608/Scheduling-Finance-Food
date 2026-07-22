# Document 01: Morning Dashboard PRD

## Document Name
Morning Dashboard PRD

## Purpose
Define the complete engineering-facing specification for the Morning Dashboard — the specific first-open-of-the-day state of the home surface — including the exact trigger condition that distinguishes "first open" from any other app open, the data contract every pillar must supply into the briefing, the cross-pillar ranking and tie-break logic, and the observable behaviors that separate a synthesized briefing from a stacked list of unread items.

## Why It Exists
The Morning Experience document (Phase 2) establishes that a briefing must read as one act of synthesis rather than a notification digest, but it does not specify the trigger boundary, data contract, or ranking implementation an engineering team needs to actually build it — left unspecified, each pillar team will ship its own "morning card" and the composition will collapse into exactly the stapled-together digest the philosophy document was written to prevent. This PRD exists so "Remember, Predict, Suggest" is enforced as a testable, engineerable contract at the one surface every user sees before anything else each day.

## Approximate Page Count
9-12 pages.

## Sections
1. **Feature Scope** — in scope: first-session-of-day detection, the one-time briefing assembly and render, the candidate-item contract each pillar service must implement, cross-pillar ranking/tie-breaking, "why this, why now" annotation, overnight-event injection, and the low-content composition rule; out of scope: intraday (post-first-open) dashboard re-ranking and persistence, which remains governed by the Phase 2 Dashboard System philosophy pending a dedicated Dashboard Surface PRD, and individual pillar reminder timing, owned by the Smart Reminders PRD.
2. **User Stories** — e.g., as a user opening the app for the first time on a day with a calendar conflict, an overdue bill, and a broken sleep streak, I see one ranked briefing that leads with the calendar conflict and explains why, not three separate pillar cards; as a new user with almost no data in two of three pillars, my briefing does not pad itself with manufactured content to look "full."
3. **Functional Requirements** — the first-open detection window definition (including app-kill/reopen vs. backgrounded resume), the candidate-item interface each pillar service must populate (title, urgency score, source pillar, justification payload, suggested action), the ranking/tie-break engine's required inputs, the mandatory "why this, why now" field on every rendered item, overnight-event detection and mid-assembly injection, and the empty/low-content fallback composition.
4. **Non-Functional Requirements** — a hard render-latency budget so assembly never feels like a loading digest, graceful per-pillar degradation if one pillar's service is unavailable rather than blocking the whole briefing, on-device assembly boundaries for privacy on shared devices, and a deterministic replay mode so QA can reproduce a given day's ranking output from a fixed input snapshot.
5. **UX Requirements** — must conform to the Morning Experience document's briefing-vs-digest distinction and tone-of-voice rules, the Dashboard System's content-eligibility rules, the Automation Philosophy's rules for how confidently a pre-filled action may appear inline at the user's current Proactivity Ladder rung, and the Notification System's boundary that briefing items are not notifications and must never double-fire as push.
6. **States & Flows** — Not-Yet-Triggered, Assembling, Rendered-Full, Rendered-Low-Content, Rendered-With-Overnight-Event, Viewed, Item-Acted-On, Item-Dismissed, Stale-Reopen (opened well after the morning window, e.g., 3pm).
7. **Edge Cases** — user opens the app twice in rapid succession before assembly completes; a system modal (permission prompt, force-update) preempts first open; a traveling user crosses timezones, shifting what "first open of day" means; a brand-new user has rich data in one pillar and none in the other two; an overnight event arrives seconds after the briefing has already rendered.
8. **Failure Scenarios** — what happens when the core assumption "we can synthesize across all three pillars in real time" breaks: one pillar's service times out during assembly, a ranking signal is missing or corrupted, or the memory-recall service needed to populate "why this, why now" is unavailable and an item would otherwise render without justification.
9. **AI Behaviors** — how the user's current Proactivity Ladder rung determines whether a briefing item is silently included, passively surfaced, actively suggested, or rendered as a pre-filled action awaiting one-tap confirmation; how predicted-priority signals from the Context Engine reorder items (e.g., a same-day calendar collision outranking a routine streak reminder); how acted-on versus ignored briefing items feed back into future composition weighting.
10. **Notification Behaviors** — whether opening to the Morning Dashboard consumes or suppresses proactive notifications already queued for the same items under the Notification System's interruption budget, and how badge counts reconcile once a briefing item has been shown in-surface.
11. **Success Criteria** — a working Morning Dashboard reads, on any given day, as a single considered set of priorities a user could not have assembled faster themselves, with every item traceable to a stated reason and no item present purely to fill space.
12. **Metrics** — briefing open-to-first-interaction time, per-pillar item action rate, dismiss-without-view rate, low-content-day frequency, overnight-event surfacing accuracy, and day-over-day briefing composition stability for a given user.
13. **Open Questions** — how many competing high-urgency items from different pillars can co-lead a briefing before it stops reading as "one thing to focus on"; whether the first-open trigger should be calendar-day or sleep-cycle based for irregular schedules; how briefing composition should behave for users who never open the app before noon.

## Deliverables
* Approved Morning Dashboard PRD.
* First-open detection specification with timezone and app-lifecycle edge cases enumerated.
* Candidate-item data contract for pillar services to implement against.
* Cross-pillar ranking and tie-break rule set, with a worked scenario matrix (low-content, high-content, overnight-event).

## Dependencies
Requires the **Morning Experience** document (Phase 2, Document 11), the **Dashboard System** document (Phase 2, Document 13), the **Notification System** document (Phase 2, Document 14), the **Automation Philosophy** document (Phase 2), the **Memory Model — Behavioral Perspective** document (Phase 2), the **Context Engine — Product Perspective** document (Phase 2), the **Daily Flow** document (Phase 2, Document 10), and the **Product Philosophy Document** (Phase 1).

## Teams Using This
Product, Engineering (Client), Engineering (Backend/Ranking), Data Science/ML, Design, QA.

## Completion Criteria
- [ ] First-open detection logic is specified precisely enough to resolve all edge cases in Section 7 without ambiguity.
- [ ] The candidate-item data contract has been reviewed and accepted by Productivity, Finance, and Health pillar engineering leads.
- [ ] The ranking/tie-break rule set has been validated against the low-content, high-content, and overnight-event scenario matrix.
- [ ] Every functional requirement maps to at least one edge case and one failure scenario.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Design (required).
