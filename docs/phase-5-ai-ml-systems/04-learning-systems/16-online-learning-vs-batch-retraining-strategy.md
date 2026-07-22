# Document 16: Online Learning vs. Batch Retraining Strategy

## Document Name
Online Learning vs. Batch Retraining Strategy

## Purpose
Define which learning updates in the AI platform happen in near-real-time (online/streaming) versus on a periodic batch retraining cadence, the decision criteria for assigning a given learning signal to one path or the other, and the operational tradeoffs — cost, staleness, and risk of instability — that come with each path. This document does not define the feedback capture mechanism itself (Document 15) or the specific model architectures being trained; it defines the timing strategy that governs how quickly any given piece of learning becomes real.

## Why It Exists
Not every "the AI learns" moment can or should happen at the same speed — updating a single user's trust score after one correction is cheap and safe to apply instantly, but retraining a shared prediction model on millions of users' feedback is expensive, slow, and risky to ship without validation. Without an explicit strategy, engineers will default either to over-eager real-time updates that destabilize shared models under noisy or adversarial input, or to slow, uniform batch cycles that make the product feel like it "forgot" an obvious correction for weeks — undermining the Learn/Adapt promise from both directions at once. This document exists so every learning signal in the platform has one deliberate, documented answer for how fast it is allowed to change user-visible behavior.

## Approximate Page Count
8-10 pages

## Sections
1. **Learning Update Classification Framework** — the decision criteria (blast radius: per-user vs. shared model; cost of update; risk of instability; latency sensitivity) used to classify any given learning signal as online, batch, or hybrid.
2. **Online/Near-Real-Time Learning Candidates** — the concrete list of state that updates in near-real-time (e.g., trust score adjustments, short-term preference weights, per-user suggestion suppression), with expected update latency targets per candidate.
3. **Batch Retraining Candidates** — the concrete list of models retrained on a periodic cadence (e.g., shared prediction models, domain-specific classifiers), with cadence ranges and the rationale for why each is not handled online.
4. **Hybrid Patterns** — cases that need both: a fast per-user online adjustment layered on top of a slower shared batch-retrained base model, and the rule for how the two layers combine without conflicting or double-counting.
5. **Online Learning Safety Guardrails** — bounds, decay functions, and rollback mechanisms preventing a single noisy or adversarial signal from immediately destabilizing a user's experience.
6. **Batch Retraining Pipeline & Validation Gates** — the stages a batch retrain must pass (offline evaluation, shadow deployment, canary rollout) before it is allowed to replace a production model.
7. **Staleness Budgets** — the maximum acceptable delay between "a user corrected the AI" and "the AI's behavior actually reflects it," defined per learning class, and how staleness is measured and alerted on.
8. **Cost & Infrastructure Tradeoffs** — the relative infrastructure cost profile of the online versus batch paths at scale, and the criteria for migrating a signal from one path to the other as usage or data volume grows.
9. **Rollback & Regression Handling** — what happens when a batch retrain or an online update regresses quality, how the regression is detected, and how each path is reverted.
10. **Decision Log Template** — a reusable template for documenting why a specific future learning signal was assigned to online, batch, or hybrid, so classification stays consistent as new signals are added after this document ships.

## Deliverables
- Learning update classification framework with worked examples per pillar
- Online learning candidate list with per-signal latency targets
- Batch retraining candidate list with cadence and validation-gate definitions
- Staleness budget table per learning class
- Decision log template for classifying future learning signals

## Dependencies
Requires Feedback Loop Architecture (Phase 5, Document 15) as the source of the signals being classified; requires Prediction Engine Architecture (Phase 5) and Trust Scoring Model (Phase 5) as consumers of both paths; requires Background Jobs (Phase 4, Document 23), Scheduling (Phase 4, Document 24), and Message Queues (Phase 4, Document 22) for the underlying execution substrate.

## Teams
AI/ML Engineering, Data Science, Platform Engineering, SRE, Product

## Completion Criteria
- [ ] Every learning signal identified in the Feedback Loop Architecture has been classified as online, batch, or hybrid, with no unclassified signals remaining.
- [ ] Staleness budgets defined and validated against at least one worked example per pillar (Productivity, Finance, Health).
- [ ] Online learning guardrails reviewed against at least one simulated noisy or adversarial signal scenario.
- [ ] Batch retraining validation gates confirmed sufficient to block a regressive model from reaching production.
- [ ] Signed off by: Head of AI/ML (required), Principal Architect (required), SRE Lead (required).
