# Document 17: Preference & Reinforcement Learning Architecture

## Document Name
Preference & Reinforcement Learning Architecture

## Purpose
Define how the AI platform learns a user's soft, subjective preferences — tone, suggestion timing, suggestion style, and proactivity comfort — from implicit behavioral signals accumulated over time, as distinct from and complementary to the explicit correction-based learning defined in the Feedback Loop Architecture (Document 15). This document specifies the preference dimensions in scope, the implicit signals used to infer them, and the learning approach used to update them — it does not redefine what is and is not allowed to personalize, which remains governed by the Personalization document (Phase 2, Document 07).

## Why It Exists
Some of what "Adapt" means in the product philosophy is not correcting a factual mistake but tuning a matter of taste the user will rarely, if ever, state outright — nobody files an explicit correction for "your notifications feel slightly too chipper" or "I always ignore suggestions before 8am." Without a dedicated architecture for learning from implicit engagement signals, the Personalization document's promise that tone, thresholds, defaults, and timing adapt per user has no technical mechanism to become true, and personalization stays a static onboarding questionnaire instead of something that keeps quietly improving across a user's lifetime with the product.

## Approximate Page Count
8-10 pages

## Sections
1. **Preference Dimensions in Scope** — the enumerated soft-preference dimensions this architecture learns (tone/formality, encouragement style, suggestion timing, suggestion density/frequency tolerance, channel preference), cross-referenced to the Personalization document's "what personalizes" list.
2. **Implicit Signal Sources** — the behavioral signals used as preference proxies (dismiss latency, accept rate by time of day, read/engagement depth, snooze patterns, post-suggestion session behavior), and how each maps to a specific preference dimension.
3. **Learning Approach & Exploration Strategy** — the class of technique used (e.g., contextual bandits, lightweight reinforcement learning, preference-ranking models) at a conceptual level, and how the system balances exploring a user's true preference against exploiting its current belief without subjecting the user to visible, disruptive test variations.
4. **Reward Signal Design** — how positive or negative implicit reward is derived without an explicit label, the risk of misreading silence or inaction as a signal, and the safeguards against the system optimizing for the wrong proxy (e.g., raw engagement instead of genuine user benefit).
5. **Cold and Warm Preference States** — how a given preference dimension moves from an untuned default to a confidently learned value, and the confidence threshold required before the system acts on a learned preference rather than the default; ties directly to Document 18 (Cold-Start Strategy).
6. **Separation from Explicit Correction Learning** — the explicit boundary between this document's implicit preference learning and Document 15's explicit correction handling, including the precedence rule for when the two disagree (an explicit setting always overrides an inferred preference).
7. **Non-Negotiable Bounds on Preference Learning** — the fixed boundary, inherited from the Personalization document, describing what preference learning is never allowed to change (safety rules, core philosophy, non-negotiable notification classes) regardless of what implicit signals suggest.
8. **User Visibility and Override of Learned Preferences** — what a user can see about preferences the system has inferred about them, and how they can correct or reset an inferred preference, converting it into an explicit signal handled by Document 15.
9. **Evaluation of Preference Learning Quality** — how the platform measures whether learned preferences are genuinely improving the experience (e.g., declining dismiss rate over time, fewer "too many notifications" complaints) rather than merely drifting.
10. **Cross-Pillar Preference Consistency** — the rule that a learned preference dimension like tone must apply consistently across all three pillars rather than being learned independently per pillar, cross-referencing the Personalization document's cross-pillar consistency rule.

## Deliverables
- Preference dimension catalogue mapped to implicit signal sources
- Learning/exploration approach specification with a worked exploration-vs-exploitation example
- Reward signal design specification including misuse/misread safeguards
- Explicit-vs-implicit precedence rule specification
- User-facing preference visibility and override requirements list for Design

## Dependencies
Requires Feedback Loop Architecture (Phase 5, Document 15) for the explicit-signal precedence rule and event routing; requires Personalization Engine Architecture (Phase 5) as the consuming system; requires Personalization (Phase 2, Document 07) for the personalization boundary this document must respect; requires Cold-Start Strategy (Phase 5, Document 18) for how preference confidence bootstraps for new users.

## Teams
AI/ML Engineering, Data Science, Product, Design, Trust & Safety, Content/Copy

## Completion Criteria
- [ ] Every preference dimension has at least one mapped implicit signal source and one worked example per pillar (Productivity, Finance, Health).
- [ ] Reward signal design reviewed against at least one scenario where a naive signal would have optimized for the wrong outcome.
- [ ] Explicit-vs-implicit precedence rule validated against at least one conflict scenario.
- [ ] Non-negotiable bounds cross-checked against the Personalization document's "does not personalize" list with no contradictions.
- [ ] Signed off by: Head of AI/ML (required), Head of Product (required), Head of Trust & Safety (required).
