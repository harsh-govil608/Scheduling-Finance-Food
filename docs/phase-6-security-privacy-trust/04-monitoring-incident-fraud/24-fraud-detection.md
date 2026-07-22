# Document 24: Fraud Detection

## Document Name
Fraud Detection

## Purpose
Define financial fraud detection specifically: identifying stolen-card and payment-instrument abuse flowing through Expense Capture and other money-touching surfaces, fraudulent subscription and billing claims, and coordinated financial abuse patterns. This document specifies detection signals, risk scoring, and response actions for financial harm, and how this function coordinates with Phase 4's Finance Service and Banking integration rather than duplicating their transaction-processing logic.

## Why It Exists
The platform ingests and reasons over real financial transactions — expense capture, subscription billing, and bank-linked data — which makes it a direct target for payment fraud: testing stolen card numbers, disputing legitimate charges (friendly fraud), or exploiting subscription billing logic for free or unauthorized access. This is a fundamentally different problem from platform abuse (Doc 23, which harms product/growth mechanics) or general security compromise (Doc 22, which harms account confidentiality/integrity): fraud causes direct monetary loss to the company, its banking partners, and potentially users, and carries payment-network compliance obligations (chargeback handling, card-network rules) that neither other document addresses. This document exists to give the platform a dedicated, financially-literate detection and response capability that sits at the intersection of Security and Finance rather than being owned entirely by either.

## Approximate Page Count
8-10 pages

## Sections
1. **Fraud Category Catalog** — the specific fraud patterns in scope: stolen/testing card patterns surfacing through Expense Capture, fraudulent or synthetic-identity subscription signups, friendly fraud/chargeback abuse, and account-takeover-driven financial actions.
2. **Boundary vs. Platform Abuse and Security Monitoring** — an explicit scoping statement distinguishing financial fraud (direct monetary harm) from platform abuse (Doc 23) and general compromise signals (Doc 22), with a rule for shared-signal cases (e.g. an account-takeover that is then used for fraud) to avoid duplicate, conflicting response actions.
3. **Transaction Risk Signals** — the specific signals evaluated per transaction and per account: card-testing velocity patterns, geolocation/billing-address mismatch, device and behavioral anomalies at checkout, and expense-entry patterns inconsistent with a user's established spending profile.
4. **Risk Scoring & Decisioning** — how signals combine into a transaction- and account-level fraud risk score, the automated decisioning thresholds (allow, step-up verification, hold for review, block), and the human-review workflow for scores in the ambiguous middle band.
5. **Coordination with Finance Service and Banking Integration** — the interface contract with Finance Service (Phase 4 Doc 12) and the Banking integration (Phase 4 Doc 48): fraud detection consumes their transaction data and enrichment but does not own transaction processing, settlement, or the banking-partner relationship, which remain owned by those systems.
6. **Chargeback & Dispute Management** — the workflow for handling card-network chargebacks and user-disputed charges, evidence collection requirements, and the reconciliation loop back into the risk model when a dispute is resolved.
7. **Subscription & Billing Fraud Controls** — detection of free-trial abuse, synthetic-identity subscription fraud, and promo/discount-code abuse, coordinated with Growth (Phase 8) program design.
8. **Response Actions & User Communication** — the graduated response ladder (step-up verification, transaction hold, account restriction) and required user communication when a legitimate transaction is held or a suspected-fraud account action is taken, balancing fraud prevention against false-positive user friction.
9. **Regulatory & Card-Network Compliance** — the compliance obligations this function must satisfy (card-network fraud-monitoring program requirements, applicable financial-services regulation), owned jointly with Regulatory Compliance (Phase 6, Group 07).
10. **Metrics & Model Governance** — fraud-loss rate, false-positive/friction rate, and detection-model review cadence, including bias and fairness review for any automated decisioning that restricts user access to funds.

## Deliverables
- Fraud category catalog with detection ownership per category.
- Transaction and account-level risk scoring model specification.
- Documented interface contract with Finance Service and Banking integration.
- Chargeback/dispute handling workflow with evidence-collection checklist.
- Subscription/billing fraud control set coordinated with Growth.
- Fraud response ladder and user communication templates.
- Card-network and regulatory compliance crosswalk.

## Dependencies
Requires Threat Model (Phase 6 Doc 02), Security Monitoring (Phase 6 Doc 22), Finance Service (Phase 4 Doc 12), Banking integration (Phase 4 Doc 48), Incident Response (Phase 6 Doc 21) for confirmed-fraud escalation, Regulatory Compliance (Phase 6, Group 07). Coordinates with Abuse Prevention (Phase 6 Doc 23) on boundary cases.

## Teams
Security, Finance/Payments, Data Science/Risk Modeling, Compliance, Customer Support, Legal

## Completion Criteria
- [ ] Fraud category catalog validated against real card-network fraud-pattern references and reviewed with the Banking partner.
- [ ] Risk scoring thresholds calibrated against a labeled historical or simulated transaction dataset for acceptable false-positive rate.
- [ ] Finance Service and Banking integration interface contract reviewed and accepted by both owning teams.
- [ ] Chargeback workflow tested against at least one simulated dispute end-to-end.
- [ ] Boundary rules with Abuse Prevention and Security Monitoring reviewed to prevent duplicate/conflicting account actions.
- [ ] Signed off by: CISO (required), Head of Finance (required), Head of Risk/Data Science (required).
