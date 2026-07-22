# Document 24: Roadmap

## Document Name
Roadmap

## Purpose
Define the living product roadmap format and prioritization framework that translates the requirements, PRDs, and architecture defined across Phases 2-7 — filtered through the business and competitive constraints defined elsewhere in Phase 8 — into a sequenced, resourced execution plan. This document specifies the roadmap's structure and governance, not the roadmap's contents; the actual sequenced roadmap is the first populated instance produced from this framework.

## Why It Exists
Nine phases of documentation define what could be built; without a roadmap, none of it gets sequenced into what gets built when — the roadmap is where strategy, architecture, and design intent meet real resourcing constraints and become an actual execution plan. Without a documented framework for how the roadmap is built and governed, prioritization defaults to whoever argues loudest in a planning meeting, dependencies discovered late block committed work, and the roadmap silently drifts out of sync with the OKRs and Success Metrics it is supposed to serve. This document exists so that sequencing decisions are made against a consistent, revisitable method rather than re-litigated from scratch every quarter.

## Approximate Page Count
8-10 pages.

## Sections
1. **Prioritization Framework** — the scoring/ranking method used to sequence features (e.g., mission-alignment × effort × confidence), including how items are compared across pillars that don't share a common unit of value.
2. **Roadmap Horizon Structure** — near-term (committed) vs. mid-term (planned) vs. long-term (directional) horizons, with the confidence and communication norms appropriate to each.
3. **Cross-Phase Traceability** — how every roadmap item traces back to a specific Phase 2/3 PRD and, where relevant, the Phase 4/5 architecture dependencies it requires, so nothing ships that wasn't specified upstream.
4. **Resourcing & Capacity Model** — how roadmap items are checked against actual team capacity and the hiring plan, not just desirability, so the roadmap reflects what can realistically be staffed rather than an aspirational wish list.
5. **Dependency Sequencing** — technical and product dependencies that force ordering (e.g., core memory architecture from Phase 5 must precede pillar-specific proactivity features), and how blocking dependencies are surfaced early rather than discovered mid-quarter.
6. **Milestone & Release Cadence** — how roadmap items map to actual release trains and milestones (alpha/beta/general availability), and how that cadence is communicated differently internally versus externally.
7. **Roadmap Review & Re-prioritization Cadence** — how often the roadmap is revisited, who holds authority to reorder it, and what evidence (Success Metrics movement, competitive intelligence from the Competitive Strategy document) is sufficient to trigger an out-of-cycle reprioritization.
8. **Scope Guardrails** — explicit rules preventing roadmap bloat inconsistent with the "never overwhelm" product philosophy, such as a cap on concurrently in-flight pillar initiatives or a minimum bar for de-scoping before adding.
9. **Stakeholder Communication Format** — how the same roadmap is presented differently to engineering (detailed, dependency-aware), to investors and the board (thematic, outcome-based), and company-wide (directional, non-committal on dates beyond the near-term horizon).
10. **Risk & Contingency Buffer** — how much of the roadmap is deliberately left unscheduled as buffer for platform-incumbent response or unplanned technical risk, and the rule for when that buffer may be spent.

## Deliverables
- Approved Roadmap framework document.
- The roadmap artifact itself (a living, quarterly-updated board) as its first populated instance.
- A prioritization scoring rubric (spreadsheet or tool) usable for every subsequent planning cycle.
- A traceability matrix linking each roadmap item to its source PRD and architecture dependency.

## Dependencies
Requires **OKRs** (Phase 8) so roadmap items are sequenced against the same objectives, the **Success Metrics Document** (Phase 1) for the North Star and KPI tree the roadmap is meant to move, **Business Model** and **Pricing Strategy** (Phase 8) for monetization-relevant sequencing, Critical Path and architecture documents from Phases 3-5, and **Competitive Strategy** (Phase 8) for sizing the contingency buffer against platform-incumbent risk.

## Teams
Executive, Product, Engineering leadership, Design, Growth

## Completion Criteria
- [ ] Prioritization framework applied to at least the first quarter's worth of roadmap items as a validation pass.
- [ ] Every near-term (committed) roadmap item traces to a specific Phase 2/3 PRD and, where relevant, a Phase 4/5 architecture dependency.
- [ ] Resourcing model checked against actual current headcount and hiring plan, not aspirational capacity.
- [ ] Scope guardrails reviewed against the Phase 1 Success Metrics Document's guardrail/anti-metrics for consistency.
- [ ] Signed off by: CEO (required), Head of Product (required), Head of Engineering (required).
