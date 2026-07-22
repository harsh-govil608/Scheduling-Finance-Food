# Document 14: Proactivity Ladder Decision Engine

## Document Name
Proactivity Ladder Decision Engine

## Purpose
Define the technical decision system that determines, for any given candidate AI action, which rung of the Phase 1 Proactivity Ladder applies right now for this specific user and this specific action type — the literal implementation of "how much initiative can the AI take." This document specifies what the eventual architecture document must define; it does not itself finalize the scoring formula or thresholds.

## Why It Exists
The Proactivity Ladder is defined conceptually in Phase 1 and operationalized at the product-surface level in Phase 2's Automation Philosophy, but neither defines the actual decision logic or scoring system that computes trust per user per action category. Without it, "earned autonomy" has no mechanism and becomes either always-cautious — defeating the mission of an AI that proactively manages a user's life — or inconsistently applied across features, which breaks the trust the entire ladder depends on. This document exists so every team building an autonomous or semi-autonomous behavior calls into one shared, auditable decision system rather than inventing its own trust heuristic.

## Approximate Page Count
10-12 pages

## Sections
1. **Trust Scoring Model** — the inputs (action history, correction rate, explicit permissions) that compute a per-user, per-action-category trust score, and the framework for combining them.
2. **Rung Assignment Logic** — how a trust score and an action's risk classification together map to one of the five Ladder rungs, including how ties and boundary cases between adjacent rungs are resolved.
3. **Action Risk Classification** — how candidate actions are pre-classified by reversibility and stakes (e.g., auto-categorizing a transaction versus auto-paying a bill), and who owns keeping that classification current as new action types are added.
4. **Trust Score Update Mechanics** — how the trust score moves after each outcome — an accepted suggestion, a rejected suggestion, a corrected autonomous action, an ignored suggestion — including decay over time and the deliberate asymmetry between how quickly trust is earned versus how quickly it is lost.
5. **Per-Action-Category Independence** — the rule that trust and rung assignment are scoped per action category, so that trust earned in auto-categorizing transactions does not automatically confer trust in auto-paying bills, and how category boundaries are defined and kept from silently merging.
6. **Cold-Start & Default Rung Assignment** — the default rung a brand-new user, or an existing user's brand-new action category, starts at, and the evidence bar required to earn the first promotion off that default.
7. **Manual Override & Explicit Permission Interaction** — how a user's explicit grant or restriction, set during onboarding or in settings, interacts with the computed trust score, including which direction — grant or restrict — takes precedence when the two conflict.
8. **Real-Time Decision Evaluation** — the runtime architecture for evaluating "what rung applies right now" at the moment a candidate action is generated, including its interface with the Recommendation & Ranking Architecture and the handoff to Authorization (Phase 4, Document 08).
9. **Demotion & Trust Recovery** — the rule and mechanism for dropping a user-action-category down a rung after a bad outcome, such as an incorrectly executed autonomous action, and the defined path and evidence bar for climbing back.
10. **Auditability & Explainability** — what must be retrievable to answer "why was the AI allowed to do this without asking me," supporting the Automation Philosophy's Autonomy Level Transparency requirement and Trust & Safety review.

## Deliverables
- Trust scoring model specification (inputs, combination framework, update mechanics)
- Rung assignment decision table (trust score x risk classification -> rung)
- Action risk classification taxonomy applied to at least one action per pillar
- Real-time decision evaluation interface contract, consumed by the Recommendation & Ranking Architecture and Authorization
- Demotion/recovery and auditability specification

## Dependencies
Requires the Product Philosophy Document (Phase 1) for the Proactivity Ladder's conceptual definition, and Automation Philosophy (Phase 2, Document 08) for its product-surface rung representations. Requires Authorization (Phase 4, Document 08) as the system this engine's rung decisions must feed for enforceable, checkable permission scopes, and AI Platform Integration Boundary (Phase 4, Document 57) for the action authorization handoff contract. Requires Personalization Engine Architecture (Phase 5, Document 12) and Recommendation & Ranking Architecture (Phase 5, Document 13) as adjacent systems this engine's rung decisions gate, and the Learning Systems group's feedback-loop architecture as the source of the outcome signals this engine's trust scoring consumes.

## Teams
AI/ML Engineering, Data Science, Product, Trust & Safety, Backend/Platform Engineering, Security

## Completion Criteria
- [ ] Rung assignment logic validated against at least one worked scenario per pillar at each of the 5 rungs.
- [ ] Action risk classification taxonomy applied and reviewed against at least one action per pillar, with no ambiguous classifications.
- [ ] Trust score update mechanics — earn-versus-lose asymmetry, decay — reviewed and confirmed to prevent a single bad outcome from causing trust loss disproportionate to the harm caused.
- [ ] Real-time decision evaluation interface confirmed sufficient by both the Recommendation & Ranking Architecture and Authorization (Phase 4, Document 08) owners.
- [ ] Demotion and auditability mechanisms reviewed to confirm every autonomous action can be traced back to the rung decision that authorized it.
- [ ] Signed off by: Head of AI/ML (required), Head of Product (required), Head of Trust & Safety (required).
