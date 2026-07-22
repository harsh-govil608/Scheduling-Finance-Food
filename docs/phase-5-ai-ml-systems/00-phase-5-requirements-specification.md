# Phase 5 — AI/ML Systems Architecture Requirements

**Note on this phase's origin:** `phase5.md` in this repository was found to be a duplicate of `phase4.md`. Per user direction, Phase 5 was instead scoped as the logical gap Phase 4 itself points to: Phase 4's Technical Architecture explicitly excludes "AI internals" and defines Document 57, "AI Platform Integration Boundary," as the interface contract to whatever sits on the other side. Phase 5 is that other side — the AI/ML Systems Architecture.

This document is **NOT the architecture itself** — it defines every AI/ML architecture document that must exist, in the same format and rigor as Phases 1–4.

---

## Document Set (in dependency order)

### Group 1 — AI Platform Foundations (`01-ai-platform-foundations/`)

| # | Document | Pages | File |
|---|---|---|---|
| 1 | AI Platform Overview | 9–11 | [`01-ai-platform-overview.md`](01-ai-platform-foundations/01-ai-platform-overview.md) |
| 2 | Model Architecture & Selection Strategy | 8–10 | [`02-model-architecture-selection-strategy.md`](01-ai-platform-foundations/02-model-architecture-selection-strategy.md) |
| 3 | Prompt & Inference Architecture | 9–11 | [`03-prompt-inference-architecture.md`](01-ai-platform-foundations/03-prompt-inference-architecture.md) |
| 4 | Model Serving Infrastructure | 7–9 | [`04-model-serving-infrastructure.md`](01-ai-platform-foundations/04-model-serving-infrastructure.md) |
| 5 | On-Device vs Cloud Inference Strategy | 7–9 | [`05-on-device-vs-cloud-inference-strategy.md`](01-ai-platform-foundations/05-on-device-vs-cloud-inference-strategy.md) |
| 6 | Model Versioning & Rollout | 7–9 | [`06-model-versioning-rollout.md`](01-ai-platform-foundations/06-model-versioning-rollout.md) |

### Group 2 — Memory & Context Systems (`02-memory-context-systems/`)

| # | Document | Pages | File |
|---|---|---|---|
| 7 | Memory System Architecture | 10–12 | [`07-memory-system-architecture.md`](02-memory-context-systems/07-memory-system-architecture.md) |
| 8 | Context Engine Architecture | 9–11 | [`08-context-engine-architecture.md`](02-memory-context-systems/08-context-engine-architecture.md) |
| 9 | Retrieval Architecture | 8–10 | [`09-retrieval-architecture.md`](02-memory-context-systems/09-retrieval-architecture.md) |
| 10 | Embedding & Vector Store Strategy | 8–10 | [`10-embedding-vector-store-strategy.md`](02-memory-context-systems/10-embedding-vector-store-strategy.md) |

### Group 3 — Prediction & Personalization (`03-prediction-personalization/`)

| # | Document | Pages | File |
|---|---|---|---|
| 11 | Prediction Engine Architecture | 8–10 | [`11-prediction-engine-architecture.md`](03-prediction-personalization/11-prediction-engine-architecture.md) |
| 12 | Personalization Engine Architecture | 8–10 | [`12-personalization-engine-architecture.md`](03-prediction-personalization/12-personalization-engine-architecture.md) |
| 13 | Recommendation & Ranking Architecture | 8–10 | [`13-recommendation-ranking-architecture.md`](03-prediction-personalization/13-recommendation-ranking-architecture.md) |
| 14 | Proactivity Ladder Decision Engine | 9–11 | [`14-proactivity-ladder-decision-engine.md`](03-prediction-personalization/14-proactivity-ladder-decision-engine.md) |

### Group 4 — Learning Systems (`04-learning-systems/`)

| # | Document | Pages | File |
|---|---|---|---|
| 15 | Feedback Loop Architecture | 8–10 | [`15-feedback-loop-architecture.md`](04-learning-systems/15-feedback-loop-architecture.md) |
| 16 | Online Learning vs Batch Retraining Strategy | 7–9 | [`16-online-learning-vs-batch-retraining-strategy.md`](04-learning-systems/16-online-learning-vs-batch-retraining-strategy.md) |
| 17 | Preference & Reinforcement Learning Architecture | 7–9 | [`17-preference-reinforcement-learning-architecture.md`](04-learning-systems/17-preference-reinforcement-learning-architecture.md) |
| 18 | Cold-Start Strategy | 7–9 | [`18-cold-start-strategy.md`](04-learning-systems/18-cold-start-strategy.md) |

### Group 5 — Domain-Specific AI Models (`05-domain-specific-models/`)

| # | Document | Pages | File |
|---|---|---|---|
| 19 | SMS/Transaction Parsing ML Architecture | 8–10 | [`19-sms-transaction-parsing-ml-architecture.md`](05-domain-specific-models/19-sms-transaction-parsing-ml-architecture.md) |
| 20 | Meal Recognition (Computer Vision) Architecture | 9–11 | [`20-meal-recognition-computer-vision-architecture.md`](05-domain-specific-models/20-meal-recognition-computer-vision-architecture.md) |
| 21 | Voice/NLU Architecture | 8–10 | [`21-voice-nlu-architecture.md`](05-domain-specific-models/21-voice-nlu-architecture.md) |

### Group 6 — AI Quality & Safety Architecture (`06-ai-quality-safety/`)

| # | Document | Pages | File |
|---|---|---|---|
| 22 | AI Evaluation & Quality Framework | 8–10 | [`22-ai-evaluation-quality-framework.md`](06-ai-quality-safety/22-ai-evaluation-quality-framework.md) |
| 23 | Hallucination & Error Mitigation Architecture | 8–10 | [`23-hallucination-error-mitigation-architecture.md`](06-ai-quality-safety/23-hallucination-error-mitigation-architecture.md) |
| 24 | AI Explainability Architecture | 8–10 | [`24-ai-explainability-architecture.md`](06-ai-quality-safety/24-ai-explainability-architecture.md) |
| 25 | Human-in-the-Loop Escalation Architecture | 7–9 | [`25-human-in-the-loop-escalation-architecture.md`](06-ai-quality-safety/25-human-in-the-loop-escalation-architecture.md) |

### Group 7 — AI Operations & Cost (`07-ai-operations-cost/`)

| # | Document | Pages | File |
|---|---|---|---|
| 26 | AI Cost Architecture | 7–9 | [`26-ai-cost-architecture.md`](07-ai-operations-cost/26-ai-cost-architecture.md) |
| 27 | AI Observability | 7–9 | [`27-ai-observability.md`](07-ai-operations-cost/27-ai-observability.md) |
| 28 | Multi-Model Orchestration Strategy | 7–9 | [`28-multi-model-orchestration-strategy.md`](07-ai-operations-cost/28-multi-model-orchestration-strategy.md) |
| 29 | AI Training Data Pipeline & Feature Store | 8–10 | [`29-ai-training-data-pipeline-feature-store.md`](07-ai-operations-cost/29-ai-training-data-pipeline-feature-store.md) |

### Group 8 — Privacy-Preserving AI & Platform Contract (`08-privacy-preserving-ai-platform-contract/`)

| # | Document | Pages | File |
|---|---|---|---|
| 30 | Privacy-Preserving AI Techniques | 8–10 | [`30-privacy-preserving-ai-techniques.md`](08-privacy-preserving-ai-platform-contract/30-privacy-preserving-ai-techniques.md) |
| 31 | AI Platform Integration Contract Implementation | 7–9 | [`31-ai-platform-integration-contract-implementation.md`](08-privacy-preserving-ai-platform-contract/31-ai-platform-integration-contract-implementation.md) |
| 32 | AI Platform Capacity & Scaling Strategy | 7–9 | [`32-ai-platform-capacity-scaling-strategy.md`](08-privacy-preserving-ai-platform-contract/32-ai-platform-capacity-scaling-strategy.md) |

---

## Dependency Graph

```
PHASE 4 Document 57 (AI Platform Integration Boundary — backend side)
        │
        ▼
01 AI Platform Overview ──> 02 Model Architecture & Selection ──> 03 Prompt & Inference Architecture
        │                                                              │
        ▼                                                              ▼
04 Model Serving Infra ── 05 On-Device vs Cloud ── 06 Model Versioning & Rollout
        │
        ▼
07 Memory System Architecture ──> 08 Context Engine Architecture ──> 09 Retrieval Architecture ──> 10 Embedding & Vector Store
        │
        ▼
11 Prediction Engine ── 12 Personalization Engine ── 13 Recommendation & Ranking ──> 14 Proactivity Ladder Decision Engine
        │
        ▼
15 Feedback Loop Architecture ──> 16 Online vs Batch Learning / 17 Preference & RL / 18 Cold-Start Strategy
        │
        ▼
19 SMS Parsing ML / 20 Meal Recognition CV / 21 Voice & NLU  (each consumes 01, 07-10)
        │
        ▼
22 AI Evaluation & Quality ──> 23 Hallucination Mitigation / 24 Explainability / 25 Human-in-the-Loop Escalation
        │
        ▼
26 AI Cost Architecture / 27 AI Observability / 28 Multi-Model Orchestration / 29 Training Data Pipeline
        │
        ▼
30 Privacy-Preserving AI Techniques ──> 31 AI Platform Integration Contract Implementation ──> 32 AI Capacity & Scaling
```

## Critical Path

1. **01 AI Platform Overview** — the root map every other Phase 5 document positions against.
2. **07 Memory System Architecture** — the single most load-bearing document in the phase: "the AI remembers" is the mission's central claim, and Documents 08–10, 14, and every domain model (19–21) read from or write to it.
3. **14 Proactivity Ladder Decision Engine** — the literal implementation of the company's core autonomy promise from Phase 1; every AI action across all 47 Phase 3 PRDs routes through this document's decision logic.
4. **15 Feedback Loop Architecture** — without it, "Learn" and "Adapt" (2 of the 8 Phase 1 philosophy verbs) have no mechanism.
5. **31 AI Platform Integration Contract Implementation** — the closing seam with Phase 4; must be jointly reviewed with Phase 4 Document 57's owner since a one-sided contract isn't a contract.

## Estimated Total Documents

**32 documents** (+ this specification = 33 files in the phase).

## Estimated Total Pages

**~250–290 pages** across 32 documents (averaging ~8 pages/document).

---

## Principal AI Architect Self-Review

**Coverage assessment: ~99% complete for the AI/ML systems layer implied by Phases 1–4's product and architecture commitments.**

Every AI-touching capability named across Phase 2 (Context Engine, Memory Model, Personalization, Automation Philosophy), Phase 3 (AI Memory, Context Timeline, AI Coach, Automation Rules PRDs, plus every pillar PRD's "AI Behaviors" section), and Phase 4 (Document 57's boundary) now has a corresponding Phase 5 architecture document. What remains open, honestly:

* **Model fine-tuning vs. prompting strategy** is covered at the selection-criteria level (Document 02) but a dedicated deep-dive on fine-tuning economics/risk was judged unnecessary until vendor selection happens — folding it into Document 02 rather than adding a 33rd document was a deliberate call to avoid speculative depth ahead of a real decision.
* **Multi-agent orchestration** (if the AI platform evolves toward specialized sub-agents coordinating rather than one model per task) is not separately documented — Document 28 (Multi-Model Orchestration) covers routing between models but not agent-to-agent coordination patterns. Flagged as a candidate addition once/if the product roadmap commits to that architecture.
* **Deep AI security** (prompt injection defense, jailbreak defense, adversarial input handling) is deliberately excluded from this phase's Quality & Safety group (Documents 22–25 cover reliability/quality, not adversarial security) and is explicitly deferred to the Trust, Security & Privacy phase, where it belongs alongside the platform's broader threat model.

No other AI-system concern implied by the first four phases' commitments remains undocumented. Phase 5 is ready to move to detailed drafting, with Document 07 (Memory) and Document 14 (Proactivity Ladder Decision Engine) recommended for the earliest and most rigorous review given their centrality.
