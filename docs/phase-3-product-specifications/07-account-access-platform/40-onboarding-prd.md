# Document 40: Onboarding PRD

## Document Name
Onboarding PRD

## Purpose
This PRD will define the concrete first-run feature — the actual screens, states, and instrumentation — that implements the sequence, pacing, and trust-calibration rules already established in Phase 2's Onboarding Experience document. It defines sign-up/account creation, the ordering of permission asks relative to initial pillar setup, the specific first-run tasks that constitute each pillar's "first value" moment, and how time-to-first-value is measured in the running product.

## Why It Exists
Phase 2's Onboarding Experience document set the philosophy — sequencing strategy, pacing, and the requirement that the AI start at the lowest Proactivity Ladder rung — but a philosophy document cannot be built against directly; engineering and design need a committed feature spec naming the exact screens, states, and pass/fail conditions of a real first session. Onboarding is also the single highest-leverage trust moment in the product: a user who is confused, over-asked, or shown no value in the first session will likely never grant the permissions the AI depends on to be proactive at all, so this PRD carries unusually high stakes for a "setup" feature.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: account sign-up/creation entry point, the first-run screen sequence, initial pillar setup tasks (the minimum input needed to activate Productivity, Finance, and Health), and time-to-first-value instrumentation. Out of scope: the plain-language consent copy and revocation mechanics for each permission (owned by Permissions & Consent PRD), and ongoing account/profile data management after first-run completes (owned by Account & Profile Management PRD).
2. **User Stories** — As a new user, I want to understand what this app does for me before I'm asked to hand over sensitive access, so I don't bail at the first permission screen; as a new user, I want to see one real, useful result (a categorized transaction, a scheduled task, a logged meal) within my first session, not just a setup wizard; as a returning user who reinstalled the app, I want onboarding to recognize I'm not new and skip redundant setup; as a user who skips a setup step, I want to know exactly what's disabled as a result and how to turn it on later; as a user who dismissed onboarding entirely, I want to still be able to use the app in a reduced but functional way.
3. **Functional Requirements** — Define the sign-up/account-creation flow and its required fields, the exact screen-by-screen first-run sequence per pillar, the specific "first value" task and its completion condition for each of Productivity/Finance/Health, the skip/defer behavior and resulting state for every optional step, and the re-onboarding trigger conditions (long absence, permission revocation, device change, reinstall) and what subset of first-run repeats in each case.
4. **Non-Functional Requirements** — Define the maximum acceptable time and step count to reach first value per pillar (falsifiable, matching the Onboarding Experience targets), the requirement that no autonomous AI action occurs during first-run before explicit confirmation, and the load/performance bar for the first-run experience on low-end devices and degraded network conditions.
5. **UX Requirements** — This feature must conform to the Onboarding Experience, Permissions & Consent UX, and User Control Model (Phase 2) documents for pacing, permission-request framing, and the Proactivity Ladder starting rung; feature-specific UX rules must define how the three pillars are visually introduced as facets of one assistant rather than three wizards, and how progress through first-run is communicated without feeling like a checklist.
6. **States & Flows** — Enumerate the lifecycle: pre-signup → account created → pillar introduction → [per-pillar setup: not started → in progress → skipped/deferred → complete] → first-value achieved (per pillar) → onboarding complete, plus the parallel re-onboarding flow entered from an active-account state.
7. **Edge Cases** — Cover a user who signs up but abandons mid-flow and returns days later, a user who completes Finance setup but never touches Productivity or Health, simultaneous sign-up attempts from two devices under one identity, and a user whose first-run session is interrupted by an OS-level permission dialog outside the app's control.
8. **Failure Scenarios** — Define behavior when core assumptions break: sign-up succeeds but a required backend dependency for first-value (e.g., bank/SMS parsing) is unavailable, network loss mid-onboarding, and a user who denies every optional permission — what floor of functional product they are left with rather than a dead-end screen.
9. **AI Behaviors** — The AI operates only at the lowest Proactivity Ladder rung throughout onboarding (observation and passive surfacing only); this PRD must define exactly what "passive surfacing" is allowed to look like in first-run (e.g., a single illustrative example, not a live autonomous suggestion) and confirm no learning-driven personalization is applied until baseline data exists.
10. **Notification Behaviors** — Define whether onboarding generates any notifications outside the active session (e.g., a nudge to finish a skipped step) and the strict frequency/tone limits on such nudges, consistent with Notification System arbitration and the "Never Overwhelm" constraint on a brand-new user relationship.
11. **Success Criteria** — State the qualitative bar: a new user should complete first-run feeling they understood what they granted and why, and should reach a genuine "aha" moment in each pillar they engage with, not just a completed setup checklist.
12. **Metrics** — Define quantitative targets such as time-to-first-value per pillar, sign-up-to-first-permission-granted conversion, first-run completion rate, per-step drop-off rate, and re-onboarding completion rate after each trigger type.
13. **Open Questions** — Capture unresolved questions such as whether all three pillars must be introduced in the same first session or may be spread across multiple sessions, and how aggressively re-onboarding should recur for a user who repeatedly skips pillar setup.

## Deliverables
- Full Onboarding PRD document following the 13-section structure above.
- First-run screen-sequence flow diagram with skip/defer branches.
- Time-to-first-value measurement specification per pillar.
- Re-onboarding trigger matrix.

## Dependencies
Phase 3: Permissions & Consent PRD, Account & Profile Management PRD. Phase 2: Onboarding Experience, Permissions & Consent UX, User Control Model, Cross-Device Experience. Phase 1: Vision & Mission Document, Product (Behavioral) Philosophy Document, User Personas Document.

## Teams Using This
Product, Design, Engineering, Content/Copy, Growth, Data Science/ML, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Time-to-first-value targets are falsifiable and measurable for all three pillars.
- [ ] AI Behaviors section confirms no action above the lowest Proactivity Ladder rung occurs during first-run.
- [ ] Skip/defer states validated against the Onboarding Experience document's skip/defer matrix with no dead ends.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Engineering Lead (required).
