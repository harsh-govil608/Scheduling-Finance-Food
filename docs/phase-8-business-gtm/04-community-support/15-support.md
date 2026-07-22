# Document 15: Support

## Document Name
Support

## Purpose
Define the reactive Support Operations function — ticketing intake, response and resolution SLAs, escalation tiers, and staffing model — that sits underneath the in-product Help & Support Experience UX pattern defined in Phase 2 (Doc 41). Where Doc 41 defines what the user sees and how a request for help feels inside the product, this document defines the business and operational machinery that actually receives, routes, resolves, and reports on that request once it leaves the app.

## Why It Exists
Phase 2's Help & Support Experience document establishes that asking for help is not a failure state and defines the in-product pattern for "why did it do that" and human escalation — but a UX pattern cannot answer a ticket, staff a queue, or guarantee a response time; without an operational layer behind it, the promise made in-product ("you can always reach a human") becomes untrue the first time volume exceeds capacity. Because this product handles financial and health data, support interactions also carry unusually high sensitivity (a wrongly-routed ticket, an agent without proper data-access scoping, an SLA breach on a fraud-related question) — so this document exists to make Support Operations rigorous and auditable rather than an ad hoc inbox.

## Approximate Page Count
6-8 pages

## Sections
1. **Support Mandate & Scope** — reactive, ticket/request-driven support explicitly distinguished from proactive Customer Success (Doc 14) and peer-driven Community (Doc 13), including the handoff context checklist inherited from Phase 2's Doc 41.
2. **Ticketing & Intake Model** — channels through which a support request enters the system (in-app handoff, email, chat), the ticketing system's required fields, and how AI-context (what the user was doing, what the AI just did) carries over so the user never re-explains from scratch.
3. **Severity & Priority Tiers** — how tickets are classified (e.g., general question, bug, account/billing, sensitive financial or health-data concern, security/fraud report) and how classification drives routing and SLA.
4. **Response & Resolution SLAs** — target first-response and resolution times per severity tier, and the operational commitments required to hit them (staffing coverage, hours, holiday/weekend policy).
5. **Escalation Tiers** — the path from first-line support through specialized tiers (billing, technical, Trust & Safety, security/fraud, legal) and the criteria that trigger each escalation.
6. **Data Access Scoping for Support Staff** — the least-privilege model governing what a support agent can see about a user's financial/health data while resolving a ticket, consistent with Phase 6 access-control and data-classification standards.
7. **Sensitive-Topic Handling** — special-case procedures for tickets touching fraud, account compromise, health-data errors, or user distress, including when a ticket must be escalated beyond standard support to Trust & Safety or a crisis protocol.
8. **Quality & Feedback Loop** — how resolved tickets feed back into product (bug reports to Engineering, "why did it do that" confusion patterns to Product/AI teams), closing the loop the Phase 2 Feedback Capture section opens.
9. **Support Metrics & Reporting** — the operational metrics tracked (SLA adherence, CSAT, ticket volume by category, reopen rate) and how they're reported to leadership, distinct from the CS health score in Doc 14.
10. **Staffing & Tooling Plan** — headcount model, tooling (helpdesk platform, knowledge base integration with Phase 2's self-serve surfaces), and scaling plan against projected user growth.

## Deliverables
- A Support Operations charter with explicit scope boundaries against Customer Success (Doc 14) and Community (Doc 13).
- A ticket severity/priority taxonomy with mapped SLAs per tier.
- An escalation matrix naming tiers, triggers, and owning teams (including Trust & Safety and security/fraud paths).
- A data access scoping policy for support staff, reviewed against Phase 6 access controls.
- A support metrics dashboard specification (SLA adherence, CSAT, volume, reopen rate).

## Dependencies
Requires In-Product Help & Support Experience (Phase 2, Doc 41 — defines the UX pattern this document operationalizes), Privacy Architecture and Data Classification (Phase 6, Docs 13-14), Incident Response (Phase 6, Doc 21), Fraud Detection (Phase 6, Doc 24). Coordinates with Customer Success (Phase 8, Doc 14) and Community (Phase 8, Doc 13).

## Teams
Support Operations, Product, Trust & Safety, Engineering, Legal/Compliance

## Completion Criteria
- [ ] SLA tiers reviewed and approved as operationally achievable by Support Operations leadership.
- [ ] Data access scoping policy reviewed and approved by Security/Privacy against Phase 6 controls.
- [ ] Escalation matrix validated end-to-end with at least one worked example per severity tier.
- [ ] Handoff context checklist confirmed consistent with Phase 2 Doc 41 (no divergence between what the product promises and what operations can deliver).
- [ ] Signed off by: Head of Support (required once hired), Head of Product (required), Head of Trust & Safety (required).
