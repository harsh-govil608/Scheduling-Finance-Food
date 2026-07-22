# Document 26: AI Cost Architecture

## Document Name
AI Cost Architecture

## Purpose
Define the architecture-level cost management levers specific to AI/ML inference — cost-aware multi-tier model routing, semantic and result caching for repeated inferences, request batching, token-level prompt cost discipline, and per-model/per-pillar cost attribution. This document covers the economics of running inference itself; it does not redefine general infrastructure cost hygiene, which remains owned by Phase 4's Cost Optimization document.

## Why It Exists
At 100M+ users, AI inference — particularly vision calls for meal/receipt recognition, voice processing, and LLM calls for coaching and suggestion generation — is structurally the platform's largest and least predictable unit cost, because unlike compute or storage it scales with model choice and prompt design as much as with raw traffic volume. Phase 4's Cost Optimization document (Document 41) already identified AI-adjacent API costs as a driver and named basic levers like caching and batching, but explicitly treats them as infrastructure hygiene, not as a model-economics discipline; it does not decide which model a task should run on, how a cache invalidates a non-deterministic generative output, or when a cost optimization is allowed to compromise suggestion quality. Without a dedicated inference cost architecture, individual feature teams will make these tradeoffs independently — routing simple tasks to expensive foundation models by default, or over-aggressively caching in ways that make the AI feel stale — and the business will have no coherent, auditable answer for why its largest cost line behaves the way it does.

## Approximate Page Count
8-10 pages

## Sections
1. **Scope Boundary Against Phase 4 Cost Optimization** — an explicit statement that this document owns inference-specific cost levers (routing, caching, batching, token economics, per-model attribution) while Document 41 owns general infrastructure COGS (compute right-sizing, storage tiering, non-AI third-party API hygiene), with no overlap in ownership.
2. **Inference Cost Model** — the unit-economics model per inference call, broken down by cost component (input/output tokens, vision/audio processing units, per-call vendor pricing tiers), and how cost-per-suggestion and cost-per-active-user targets are derived from the Phase 1 Success Metrics growth trajectory.
3. **Cost-Aware Model Routing** — the decision logic for routing a given task to the cheapest model class that still clears the quality bar defined in Model Architecture & Selection Strategy (Phase 5, Document 02), including cascading from cheap classifiers to expensive foundation models only when needed.
4. **Semantic & Result Caching** — caching strategy for repeated or near-duplicate inferences (common meal photos, recurring calendar patterns, frequently asked coaching questions), including cache-key design, staleness/invalidation rules, and the specific risk of caching non-deterministic generative outputs in a way that makes the AI feel repetitive or stale.
5. **Batching & Request Aggregation** — batching patterns for non-real-time inference workloads (nightly learning jobs, digest generation, retrospective analysis) versus real-time proactive suggestions, and the latency/cost tradeoff governance distinguishing which workloads may be batched.
6. **Token & Prompt Cost Discipline** — prompt-engineering-level cost levers including context-window trimming, retrieval-scoped context in place of full-history context, and output-length constraints, and how these interact with Memory & Context Systems (Phase 5) without degrading recall quality.
7. **Per-Model / Per-Pillar Cost Attribution** — the requirement for cost visibility broken down by model, provider, pillar (Productivity, Finance, Health), and feature, distinct from Document 41's per-service infrastructure tagging, so an AI cost anomaly is traceable to a specific model call rather than only an aggregate bill increase.
8. **Cost Budgets & Circuit Breakers** — per-user, per-tier, and platform-wide inference budget ceilings, and the automated degrade behavior (fallback to a cheaper model, cached suggestion, or reduced proactivity) triggered when a budget threshold is approached, without silently and invisibly degrading the user experience.
9. **Cost-Quality Tradeoff Governance** — the decision process and named approvers for when a cost optimization is permitted to reduce suggestion quality or proactivity, versus when the quality bars set by the AI Evaluation & Quality Framework (Phase 5) take precedence.
10. **Forecasting & Scale Projections** — how inference cost is projected forward against user growth curves from the Phase 1 Success Metrics Document, including cost-impact forecasting when a new model is added to or removed from the roster defined in Document 02.

## Deliverables
- Inference unit-cost model with per-component breakdown
- Cost-aware routing decision framework
- Semantic/result caching architecture with invalidation rules
- Batching policy distinguishing real-time from batchable workloads
- Token and prompt cost discipline guidelines
- Per-model/per-pillar/per-feature cost attribution taxonomy
- Inference budget ceiling and circuit-breaker specification
- Cost-quality tradeoff governance process
- Inference cost forecasting model tied to growth projections

## Dependencies
Requires Cost Optimization (Phase 4, Document 41) as the infrastructure-cost counterpart this document is explicitly scoped against; requires Model Architecture & Selection Strategy (Phase 5, Document 02) for the per-task quality bars routing decisions must respect; requires Prompt & Inference Architecture and Memory & Context Systems (Phase 5) for what is being routed, cached, and trimmed. Informs Multi-Model Orchestration Strategy (Phase 5, Document 28), which operationalizes routing at request time; informed by AI Observability (Phase 5, Document 27) for the cost telemetry this document's budgets and anomaly detection depend on; bounded by the Phase 1 Success Metrics Document for the growth trajectory cost must scale sustainably against.

## Teams
AI/ML Engineering, Platform Engineering, Finance/FinOps, Data Science, Product

## Completion Criteria
- [ ] Scope boundary against Phase 4 Cost Optimization (Document 41) is explicit, with no duplicated ownership of infrastructure-level levers.
- [ ] Cost-aware routing framework is validated against at least one task from each task-taxonomy category in Document 02.
- [ ] Caching architecture defines invalidation rules that account for non-deterministic generative outputs.
- [ ] Per-model/per-pillar cost attribution taxonomy covers every model and provider on the approved roster with no untagged category.
- [ ] Budget circuit-breaker behavior is defined for at least the free tier and one premium tier, with no silent UX degradation path.
- [ ] Signed off by: Head of AI/ML (required), CFO/Finance Lead (required), VP Engineering (required).
