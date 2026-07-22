# Document 02: Threat Model

## Document Name
Threat Model

## Purpose
Define the platform's systematic threat model — the categorized list of adversaries, attack surfaces, and abuse scenarios specific to a product holding financial, health, and behavioral-memory data — used to prioritize every other security investment in this phase. This document specifies what the completed threat model artifact must contain and how it must be maintained, not the finished model itself.

## Why It Exists
Without a documented threat model, security work becomes reactive — teams patch whatever incident just happened instead of defending against the highest-likelihood, highest-impact scenarios. For a platform whose core asset is user trust in an AI that reads their finances, health records, SMS content, location, and forms memories about their life, undirected security spend is itself a risk: it leaves genuinely dangerous scenarios (e.g. AI-abuse via crafted inputs) uncovered while over-investing in generic web threats. This document exists to force a disciplined, product-specific enumeration of adversaries and abuse paths that every subsequent Phase 6 policy document can reference and prioritize against.

## Approximate Page Count
10-12 pages

## Sections
1. **Adversary Categories** — opportunistic attackers, targeted account-takeover attempts, insider threats, supply-chain/third-party compromise, and AI-abuse actors (e.g. prompt injection attempts delivered via SMS or email content ingested by the AI).
2. **Data Classification Tie-In** — mapping adversary motivation to the specific data classes at risk (financial, health, location, SMS content, AI-formed memories), referencing the platform's data classification scheme.
3. **Attack Surface Inventory** — every ingress point (client apps, SMS/email ingestion pipelines, banking/health integrations, AI inference endpoints, admin tooling) mapped to its exposure level and owning team.
4. **Abuse Scenario Catalog** — concrete, product-specific scenarios (e.g. an attacker crafting an SMS designed to manipulate the transaction-parsing AI into approving a transfer; a compromised device triggering a proactive financial action).
5. **AI-Specific Threat Vectors** — prompt injection, memory poisoning, model extraction, and adversarial manipulation of the Proactivity Ladder's autonomous actions, scoped against the AI/ML Systems Architecture (Phase 5) documents.
6. **STRIDE/Kill-Chain Mapping per Surface** — a structured method (STRIDE or equivalent) applied to each attack surface to ensure spoofing, tampering, repudiation, information disclosure, denial of service, and elevation-of-privilege are each explicitly considered.
7. **Likelihood & Impact Scoring Model** — the scoring rubric used to rank scenarios, feeding directly into the Security Program's risk register (Doc 01).
8. **Mitigating Control Mapping** — each high-priority scenario mapped to the existing or planned control that addresses it, cross-referenced to Phase 4 architecture documents.
9. **Review & Update Triggers** — conditions that require the threat model to be revisited (new integration, new AI capability, new data class, prior incident).
10. **Red Team / Adversarial Testing Alignment** — how the threat model catalog feeds planned penetration testing and red-team exercise scope.

## Deliverables
- Adversary category taxonomy document.
- Attack surface inventory spreadsheet/table with exposure ratings and owners.
- Scored abuse scenario catalog (minimum 25 product-specific scenarios) including AI-abuse scenarios.
- Likelihood/impact scoring rubric.
- Control-to-scenario mapping table cross-referenced to Phase 4 documents.
- Defined update-trigger checklist.

## Dependencies
Security Program & Governance (Phase 6 Doc 01), Security Architecture Overview (Phase 4 Doc 55), Authentication Architecture (Phase 4 Doc 07), Authorization Architecture (Phase 4 Doc 08), AI/ML Systems Architecture documents (Phase 5, particularly inference and memory-formation architecture).

## Teams
Security, Engineering, AI/ML, Trust & Safety

## Completion Criteria
- [ ] Threat model reviewed against every data class the product handles.
- [ ] Every attack surface has a named owning team.
- [ ] At least one AI-specific abuse scenario validated against the actual inference pipeline with the AI/ML team.
- [ ] High-scored scenarios fed into the Security Program risk register (Doc 01).
- [ ] Signed off by: CISO (required), Head of Engineering (required), Head of AI/ML (required).
