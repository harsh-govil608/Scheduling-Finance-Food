# Document 48: Banking

## Document Name
Banking

## Purpose
Define the architecture for direct bank and account-aggregator integrations that provide authoritative account balance, transaction, and statement data, distinct from the inferential SMS/UPI-notification-based capture defined in Document 42. This document specifies authentication, data-access, and reconciliation architecture for direct banking connectivity, and flags this integration as the highest-security surface in the entire system, requiring elevated architecture, review, and compliance requirements beyond every other integration in this group.

## Why It Exists
SMS/UPI notification parsing (Doc 42) gives the platform an inferred, best-effort view of a user's transactions, but it is inherently incomplete and error-prone — it cannot see full account balances, historical statements, or transactions that never generate a parseable notification. Direct bank connectivity, via account-aggregator frameworks or bank-provided APIs, gives the Finance pillar an authoritative source of truth to reconcile against and to power features SMS parsing alone cannot support (net worth tracking, full statement analysis). Because this integration involves direct, credentialed access to a user's financial accounts, it carries materially higher regulatory, security, and liability exposure than any other integration in this document set, and this document exists specifically to force that elevated bar to be addressed explicitly and separately rather than inherited implicitly from the lighter-weight SMS integration.

## Approximate Page Count
10-14 pages

## Sections
1. **Regulatory & Compliance Framework** — the regulatory regimes (region-specific account-aggregator/open-banking frameworks, financial data protection law) this integration must comply with, and how compliance requirements are mapped into the architecture per launch region.
2. **Account-Aggregator / Open-Banking Connection Model** — the architecture for connecting to bank data via licensed account-aggregator intermediaries versus direct bank API access, and selection criteria between the two models per region.
3. **Consent & Authorization Architecture** — the end-to-end consent flow architecture (consent artifact generation, scope/duration limits, renewal, revocation) required by account-aggregator frameworks, coordinated with Phase 2 Permissions & Consent UX.
4. **Credential & Token Security Architecture** — elevated security requirements for storing, transmitting, and rotating banking access tokens/credentials, including mandatory encryption-at-rest and in-transit standards beyond the platform baseline.
5. **Data Reconciliation with SMS/UPI Capture** — how direct banking data is reconciled against the inferential SMS/UPI-derived transaction set from Document 42 to resolve duplicates and conflicts.
6. **Elevated Access Controls & Auditability** — architecture requirements for restricted internal access to banking data (need-to-know access controls, mandatory audit logging of every access) beyond standard platform data-access policy.
7. **Incident Response for Financial Data** — the elevated incident-response architecture and disclosure obligations specific to a breach or unauthorized access involving banking data, distinct from the platform's general incident-response plan.
8. **Third-Party Aggregator/Bank Selection Criteria** — the criteria (licensing status, regional coverage, reliability SLAs, security certifications such as ISO 27001/SOC 2 equivalence) against which account-aggregator or bank API partners will be evaluated, without naming a final vendor.
9. **Consent Revocation & Data Deletion** — architecture for immediate access termination and data deletion when a user revokes banking consent, including propagation to all downstream stores.
10. **Failure Handling & Degraded Mode** — behavior when aggregator/bank APIs are unavailable, including fallback to SMS/UPI-only capture without silently misrepresenting data completeness to the user.

## Deliverables
- Regulatory compliance map by launch region, reviewed by Legal.
- Consent and authorization flow architecture, including consent-artifact lifecycle.
- Elevated security architecture specification for credential storage, encryption, and rotation.
- Reconciliation logic specification between direct banking data and SMS/UPI-derived data.
- Access control and audit logging specification for internal systems handling banking data.
- Incident response addendum specific to financial data exposure scenarios.
- Vendor evaluation scorecard against defined aggregator/bank selection criteria.

## Dependencies
Requires Finance Service, Event Architecture, the SMS Integration document (Doc 42), and the platform's core Security Architecture and Data Residency documents; informed by Phase 1 Trust & Data Stewardship, Phase 2 Permissions & Consent UX, and the Phase 3 Expense Capture PRD.

## Teams
Backend Engineering (Finance Service), Security, Privacy/Legal, Compliance, Data Platform, Product (Finance pillar), Executive/Risk

## Completion Criteria
- [ ] Regulatory compliance map reviewed and approved by Legal/Compliance for every launch region.
- [ ] Elevated security architecture reviewed and approved by Head of Security, with penetration-test plan defined.
- [ ] Consent and revocation flows validated against account-aggregator framework requirements.
- [ ] Incident response addendum reviewed and approved by Security and Legal.
- [ ] Vendor evaluation criteria reviewed and approved by Compliance.
- [ ] Signed off by: CTO/VP Engineering (required), Head of Security (required), Head of Privacy (required), Chief Compliance/Legal Officer (required), CEO/Executive Sponsor (required).
