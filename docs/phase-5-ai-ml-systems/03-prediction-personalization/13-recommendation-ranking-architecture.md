# Document 13: Recommendation & Ranking Architecture

## Document Name
Recommendation & Ranking Architecture

## Purpose
Define the technical system that scores and ranks every candidate suggestion, insight, and nudge generated across all three pillars into a single ordered set of what actually surfaces to the user — the shared ranking layer feeding the Morning Dashboard, Night Summary, AI Coach, and Notification System. This document specifies what the eventual architecture document must define so that cross-pillar competition for the user's limited attention is resolved by one coherent, auditable mechanism rather than by whichever pillar's suggestion logic ships loudest.

## Why It Exists
Multiple upstream systems — the Prediction Engine, the AI Coach's pattern detection, Automation Rules, and each pillar's own suggestion logic — will independently generate far more candidate suggestions than any single user should see at once. The Product Philosophy Document's "Never Overwhelm" principle and the Dashboard System's content-eligibility model both assume some ranking mechanism ultimately decides what wins that competition, but neither defines what that mechanism is. Without a defined architecture here, "never overwhelm" is unenforceable in practice, and pillars will implicitly compete for the user's attention through engineering effort rather than through a principled, auditable ranking system — the same three-apps-stapled-together failure mode the product's whole architecture exists to prevent.

## Approximate Page Count
9-12 pages

## Sections
1. **Candidate Generation Interface** — the contract by which upstream subsystems (Prediction Engine, AI Coach, Automation Rules, pillar-specific suggestion logic) submit candidate suggestions into the shared ranking layer, including required metadata such as urgency, pillar, supporting evidence, and action type.
2. **Scoring Model** — the framework for computing a candidate's rank score from relevance, urgency, predicted value to the user, and personalization signals from the Personalization Engine, without finalizing a specific scoring algorithm.
3. **Cross-Pillar Arbitration Logic** — how candidates from different pillars competing for the same moment or slot are compared on one common scale, implementing the Product Philosophy Document's cross-pillar attention-arbitration requirement at the systems level.
4. **Attention Budget Enforcement** — the mechanism that translates the "Never Overwhelm" operationalized rules — max interruptions per day, quiet hours, batching — into an enforced ceiling the ranking layer must respect when selecting what surfaces.
5. **Surface-Specific Ranking Contracts** — how the one shared ranking output is adapted per consuming surface (the Dashboard's mirror model, Night Summary's recap model, AI Coach's insight cadence, the Notification System's interruption budget), each drawing from a single ranked candidate pool with distinct selection rules.
6. **Diversity & Repetition Controls** — rules preventing the ranked output from being dominated by a single pillar or a single repeated suggestion type, plus cooldown logic for previously dismissed or recently shown candidates.
7. **Feedback-Informed Re-Ranking** — how accept, dismiss, and ignore signals on previously surfaced candidates adjust future ranking for that user and category, interfacing with the Learning Systems feedback loop without duplicating its internals.
8. **Explainability of Ranking Decisions** — what must be retrievable about why a given candidate was or was not surfaced, supporting internal debugging and, where relevant, user-facing "why am I seeing this" transparency.
9. **Latency & Real-Time Constraints** — the serving-time requirements for ranking to complete within the AI Platform Integration Boundary's suggestion-delivery latency budget, including pre-computed versus on-demand ranking paths.
10. **Failure & Degradation Behavior** — how the ranking layer degrades when an upstream candidate source is unavailable or returns low-confidence output, ensuring the surfaces it feeds still produce a sane — possibly empty — result rather than an error state.

## Deliverables
- Candidate generation interface contract (the schema every upstream subsystem must submit against)
- Scoring model framework and cross-pillar arbitration specification
- Attention budget enforcement mechanism tied to the Never Overwhelm rules
- Per-surface ranking contract for the Dashboard, Night Summary, AI Coach, and Notification System
- Diversity/repetition and feedback-informed re-ranking specification

## Dependencies
Requires the Product Philosophy Document (Phase 1) for the Never Overwhelm and cross-pillar arbitration principles this layer enforces. Requires Dashboard System (Phase 2, Document 13) and Notification System (Phase 2, Document 14) as primary consuming surfaces, and Morning Dashboard PRD (Phase 3, Document 01), Night Summary PRD (Phase 3, Document 02), and AI Coach PRD (Phase 3, Document 33) as the feature-level contracts this layer must satisfy. Requires Prediction Engine Architecture (Phase 5, Document 11) and Personalization Engine Architecture (Phase 5, Document 12) as upstream signal sources, and AI Platform Integration Boundary (Phase 4, Document 57) for latency SLAs.

## Teams
AI/ML Engineering, Data Science, Product, Design, Backend/Platform Engineering, Trust & Safety

## Completion Criteria
- [ ] Candidate generation interface reviewed and confirmed sufficient by every upstream subsystem owner (Prediction Engine, AI Coach, Automation Rules) without requiring bespoke exceptions.
- [ ] Attention budget enforcement validated against the "Never Overwhelm" operationalized rules from the Product Philosophy Document, with at least one worked multi-pillar-conflict scenario.
- [ ] Each consuming surface's ranking contract (Dashboard, Night Summary, AI Coach, Notification System) reviewed jointly with that surface's document owner.
- [ ] Diversity and repetition controls validated against a scenario where one pillar would otherwise dominate the ranked output.
- [ ] Signed off by: Head of AI/ML (required), Head of Product (required), Head of Design (required).
