# Document 04: Premium Features (Business Layer)

## Document Name
Premium Features (Business Layer)

## Purpose
Define which capabilities are gated behind premium tiers and the business rationale for each individual gate — the commercial decision layer that sits between Phase 3's Premium Features PRD (the feature-implementation layer, defining *how* a feature works) and Phase 7's Premium UX (the visual layer, defining *how premium feels*). This document answers "why is this specific capability behind this specific paywall," feature by feature, in business terms.

## Why It Exists
Feature gating decisions are frequently made informally — an engineer or designer decides a feature "feels premium" without a documented business justification, which leads to inconsistent gating logic, features gated for the wrong reasons (e.g., gating something that should be free to drive acquisition), and disputes between Product and Growth about what belongs where. This document exists to give every gating decision a written, defensible rationale (value-based, cost-based, or strategic), so that Subscription Plans (Doc 03) has a real feature list behind its conceptual tiers, and so that gating logic can be audited against the Phase 1 anti-dark-pattern principle rather than optimized purely for conversion.

## Approximate Page Count
8-10 pages

## Sections
1. **Gating Philosophy** — the company's overarching stance on what earns a paywall: high inference/compute cost, high perceived value, competitive differentiation, or strategic scarcity — and which of these is prioritized and why.
2. **Gating Decision Framework** — a repeatable rubric (e.g., a scored matrix of value delivered, cost to serve, competitive necessity, and free-tier trust impact) used to decide whether any given feature should be free, core-paid, or premium.
3. **Productivity Pillar Gating Rationale** — which categories of Productivity capability are gated and why, referencing the proactive-AI capabilities defined in Phase 2/3.
4. **Finance Pillar Gating Rationale** — which categories of Finance capability are gated and why, with attention to capabilities that touch sensitive financial data and how gating interacts with trust (Phase 6).
5. **Health Pillar Gating Rationale** — which categories of Health capability are gated and why, with attention to the ethical sensitivity of gating health-related proactivity.
6. **Cross-Pillar / "One Assistant" Gating Rationale** — the rationale for gating capabilities that only exist at the intersection of pillars (e.g., a proactive insight that combines Finance and Health data), and why these are treated as a distinct, high-value gating category.
7. **Free-Tier Feature Protection List** — the categories of capability that must never be gated, because doing so would break the core trust/value promise established in Phase 1 and Phase 6, stated as a binding constraint.
8. **Gate Reversibility & Feature Migration Policy** — the framework for moving a feature from premium to free (or vice versa) over time as costs change or competitive pressure shifts, and how existing subscribers are protected during such moves.
9. **Relationship to Phase 3 PRD and Phase 7 UX** — an explicit cross-reference map showing, for each gated capability, where its implementation spec lives (Phase 3) and where its visual premium treatment lives (Phase 7, Doc 22), so the three layers stay synchronized.
10. **Gating Governance & Review Cadence** — who approves new gates, how disputes between Product/Growth/Finance are resolved, and how often the full gating list is re-reviewed.

## Deliverables
- A scored gating decision rubric (framework, reusable).
- A per-pillar gating rationale narrative (Productivity, Finance, Health, Cross-Pillar).
- A free-tier protection list stated as binding constraints.
- A cross-reference map linking business-layer gates to Phase 3 PRD entries and Phase 7 UX treatments.
- A gate reversibility/migration policy.

## Dependencies
Requires Business Model (Doc 01), Pricing Strategy (Doc 02), Subscription Plans (Doc 03), Guiding Principles Document (Phase 1), Premium Experience (Phase 2), Premium Features PRD (Phase 3), Security/Privacy/Trust framework (Phase 6), Premium UX (Phase 7, Doc 22). Feeds directly into Monetization Strategy (Doc 05).

## Teams
Product, Finance, Growth, Design, Engineering Leadership

## Completion Criteria
- [ ] Every gated capability has a documented rationale traceable to the gating decision framework, not an ad hoc judgment call.
- [ ] Free-tier protection list reviewed and confirmed as non-negotiable by Executive leadership.
- [ ] Each pillar's (Productivity, Finance, Health) gating rationale reviewed for internal consistency and cross-pillar fairness.
- [ ] Cross-reference map to Phase 3 PRD and Phase 7 UX confirmed complete with no orphaned gates.
- [ ] Gating logic audited against Phase 1 anti-dark-pattern and anti-data-monetization principles with no violations found.
- [ ] Signed off by: Head of Product (required), CFO/Finance Lead (required once hired), Head of Trust & Safety or equivalent (required), CEO (required).
