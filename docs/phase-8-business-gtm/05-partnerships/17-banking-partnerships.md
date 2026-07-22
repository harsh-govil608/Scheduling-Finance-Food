# Document 17: Banking Partnerships

## Document Name
Banking Partnerships

## Purpose
Define the business strategy for banking and financial-data partnerships (account aggregators, open-banking API providers, and direct bank relationships) — partner selection criteria, commercial deal structure, and how these partnerships enable the Finance pillar's automatic-capture promise beyond SMS/UPI notification parsing alone. This document specifies which partners are worth pursuing and on what commercial terms, applying the umbrella framework from Partnerships (General Strategy, Doc 16) to the single highest-risk, highest-value partnership category the company enters into.

## Why It Exists
Phase 4's Banking document (Doc 48) defines the technical architecture for direct bank and account-aggregator integration — authentication, consent, reconciliation, and elevated security controls — but it deliberately does not name a vendor or define commercial terms, and Phase 6 Doc 30 defines how a banking vendor is risk-assessed once a candidate exists, not how one is chosen or negotiated with in the first place. Without this document, banking partnerships get negotiated reactively by whichever engineer or generalist is closest to the integration deadline, with no consistent leverage, no dependency-risk plan if the partner changes terms, and no explicit check that the deal is compatible with the company's anti-data-selling commitment. Because banking access is the single most sensitive and highest-liability integration category in the product, this document exists to force the same commercial rigor applied to Doc 16's general framework onto this specific, high-stakes category before any term sheet is signed.

## Approximate Page Count
7-9 pages

## Sections
1. **Partner Selection Criteria** — what makes a banking/aggregator partner worth pursuing: regional coverage of the launch markets, reliability and uptime track record, licensing status, and data-sharing terms compatible with the Phase 1 anti-data-selling guiding principle.
2. **Commercial Deal Structure Framework** — the revenue-share versus flat-fee versus free-tier reasoning specific to banking/aggregator relationships, including how aggregator pricing models (per-connection, per-call, tiered volume) map onto the company's unit economics.
3. **Partnership Dependency Risk** — the business-continuity implications if a key banking/aggregator partner changes terms, raises pricing, restricts coverage, or exits the market, and the mitigation posture (multi-partner redundancy versus single-partner efficiency) the company adopts per launch region.
4. **Regulatory & Licensing Fit of Candidate Partners** — how candidate partners' own regulatory standing (account-aggregator licensing, open-banking framework participation) is evaluated as a commercial selection factor, distinct from Phase 4 Doc 48's architecture-level compliance mapping.
5. **Data-Sharing Terms & Anti-Data-Selling Compliance** — the specific contractual language sought or avoided in banking partner agreements to guarantee user financial data is never resold or repurposed by the partner beyond the scope the user consented to.
6. **Partner Negotiation Playbook & Leverage Points** — the negotiation posture and leverage available to an early-stage company against typically larger, more established aggregator/bank partners, including what terms are non-negotiable (data protections) versus negotiable (pricing, SLAs).
7. **Multi-Partner Coverage & Redundancy Strategy** — the rationale for maintaining more than one aggregator relationship per region where feasible, to avoid the single point of failure risk identified in Section 3.
8. **Partnership Performance & SLA Management** — how an active banking partnership's performance (uptime, data freshness, support responsiveness) is tracked against contracted SLAs, and the escalation path when a partner underperforms.
9. **Co-Marketing & Trust Signaling** — how an established, reputable banking partner's brand can be used (with the partner's agreement) to increase user trust and adoption of the Finance pillar's direct-connection feature, balanced against the risk of implying an endorsement the partner has not given.
10. **Exit, Termination & Migration Planning** — the business-level runbook for winding down a banking partnership, including user communication strategy and migration of affected users to a redundant partner or to SMS/UPI-only capture without service disruption.

## Deliverables
- Partner shortlist with completed evaluation scorecards against the selection criteria.
- Commercial terms comparison matrix across candidate aggregators/banks (pricing model, SLA terms, regional coverage).
- Partnership dependency-risk assessment with a documented redundancy decision per launch region.
- Data-sharing contract-clause checklist, reviewed against the Phase 1 anti-data-selling principle.
- Negotiation playbook with non-negotiable versus negotiable term lists.
- SLA monitoring framework and escalation runbook for active partnerships.
- Exit/termination and user-migration runbook.

## Dependencies
Requires Banking (Phase 4, Doc 48), Third-Party Risk Management (Phase 6, Doc 30), Partnerships General Strategy (Phase 8, Doc 16), Business Model (Phase 8, Doc 01), and Guiding Principles (Phase 1, Doc 07).

## Teams
Executive, Business Development, Finance, Legal/Compliance, Backend Engineering (Finance Service), Product (Finance pillar)

## Completion Criteria
- [ ] Partner selection criteria reviewed against the Phase 1 anti-data-selling guiding principle.
- [ ] Commercial deal structure validated by Finance against the company's unit economics model.
- [ ] Dependency-risk assessment and redundancy decision reviewed per launch region.
- [ ] Data-sharing contract-clause checklist reviewed and approved by Legal/Compliance.
- [ ] SLA monitoring framework confirmed compatible with Phase 4 Doc 48's failure-handling and degraded-mode architecture.
- [ ] Signed off by: CEO (required), Head of Business Development (required once hired), Chief Compliance/Legal Officer (required).
