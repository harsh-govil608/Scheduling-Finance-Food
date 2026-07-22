# Document 18: Cold-Start Strategy

## Document Name
Cold-Start Strategy

## Purpose
Define how the AI behaves for a brand-new user who has no observed history — what it can safely predict, suggest, or automate on day one using only onboarding inputs and population-level priors, what must explicitly wait for weeks of accumulated personal signal, and how the system transitions a user off cold-start defaults as real behavioral data accrues. This document ties every cold-start decision back to the Proactivity Ladder's default starting rung so "new user behavior" is a deliberate, systems-level specification rather than an emergent side effect of empty databases.

## Why It Exists
The product's proactive promise is only earned through history, but a user's very first sessions have none — if the AI stays silent until it has "enough" data, it breaks the "proactive, not waiting for commands" mission on day one; if it guesses confidently with no basis, it risks a wrong, presumptuous first impression that damages trust before the Proactivity Ladder has had any chance to build it. This document exists to give every feature team one shared, deliberate answer for what a new user should experience before personalization has anything real to work with, rather than each team inventing its own ad hoc "new user" fallback that produces an inconsistent first impression across pillars.

## Approximate Page Count
7-9 pages

## Sections
1. **Cold-Start Problem Definition** — what "no history" precisely means per data domain (zero transactions, zero tasks, zero sleep logs), since a user may be cold-start in one pillar while already warm in another.
2. **Day-One Default Proactivity Rung** — the specific default starting rung on the Proactivity Ladder (Phase 1) every new user begins at regardless of pillar, and the explicit rule that no capability starts above that rung absent prior signal.
3. **What Can Be Safely Inferred on Day One** — the bounded set of predictions and suggestions considered safe using only onboarding inputs, stated preferences, and population-level or cohort priors (e.g., typical bill due dates, common meal categories), with the confidence ceiling applied to any such prior.
4. **What Explicitly Requires Weeks of Signal** — the set of behaviors deliberately withheld until sufficient personal history exists (e.g., personalized spend-anomaly thresholds, tone adaptation, autonomous action), and the minimum signal volume or duration required before each unlocks.
5. **Population and Cohort Priors** — how onboarding inputs (stated goals, optionally shared demographics, persona selection) are used to select a cohort-level starting prior, and the guardrails preventing cohort priors from becoming stereotyping or feeling presumptuous to the user.
6. **Graceful Ramp: Cold to Warm Transition** — the mechanism by which a user moves off cold-start defaults onto personalized behavior as real signal accrues, whether that transition is gradual per-dimension or a discrete milestone, and how the underlying models update as this happens (cross-referencing Document 16).
7. **Explicit Onboarding Signal Capture** — what the onboarding flow (Phase 3, Onboarding PRD) is expected to capture specifically to shorten cold-start, and the limit on how much onboarding may ask before it becomes a burdensome interrogation rather than a fast start.
8. **Re-Entering Cold-Start (Resets and Gaps)** — how the system handles a user who resets personalization, returns after a long absence, or adds a new pillar or device, and whether they re-enter cold-start fully or only for the affected dimension.
9. **Cold-Start Failure Modes and Safeguards** — what happens when even a cohort prior is unavailable or wrong, and the fallback behavior that keeps the AI from making a confident, wrong first impression.
10. **Cold-Start Success Metrics** — how the organization measures whether the cold-start strategy is working (e.g., day-1 through day-30 suggestion acceptance rate trend, time-to-first-personalized-suggestion), distinct from steady-state personalization metrics.

## Deliverables
- Per-pillar cold-start data-availability definition
- Day-one default Proactivity Ladder rung assignment, confirmed with no undocumented exceptions
- Safe-to-infer-on-day-one list with confidence ceilings
- Signal-volume-to-unlock table per withheld behavior
- Cold-to-warm transition specification
- Cold-start success metric definitions

## Dependencies
Requires the Product Philosophy Document (Phase 1) for the Proactivity Ladder; requires Automation Philosophy (Phase 2, Document 08) for the on-screen mapping of each rung; requires the Onboarding Experience (Phase 2, Document 31) and Onboarding PRD (Phase 3, Document 40) for onboarding signal capture; requires Trust Scoring Model (Phase 5) for how initial trust is seeded; requires Feedback Loop Architecture (Phase 5, Document 15) and Online Learning vs. Batch Retraining Strategy (Phase 5, Document 16) for how the cold-to-warm transition is technically executed.

## Teams
AI/ML Engineering, Data Science, Product, Design, Trust & Safety, Content/Copy, QA

## Completion Criteria
- [ ] Day-one default Proactivity Ladder rung confirmed identical across all three pillars with no undocumented exceptions.
- [ ] Safe-to-infer list and withheld-behavior list validated against at least one worked example per pillar (Productivity, Finance, Health).
- [ ] Cold-to-warm transition mechanism reviewed against a full-lifecycle scenario spanning day 1, week 2, and month 3.
- [ ] Re-entry behavior (reset, long absence, new pillar) validated with no unresolved edge cases.
- [ ] Cold-start success metrics reviewed and approved by Data Science.
- [ ] Signed off by: Head of AI/ML (required), Head of Product (required), Head of Trust & Safety (required).
