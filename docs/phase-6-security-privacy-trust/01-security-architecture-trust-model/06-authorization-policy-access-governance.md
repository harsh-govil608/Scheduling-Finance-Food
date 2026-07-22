# Document 06: Authorization Policy & Access Governance

## Document Name
Authorization Policy & Access Governance

## Purpose
Define the policy layer over Phase 4's Authorization architecture (Doc 08) — the least-privilege review process, access certification cadence, segregation-of-duties rules, and the security review process for how the Proactivity Ladder's escalating trust tiers get granted and re-certified. This document specifies the governance rules the authorization system must be operated under; it does not redesign the permission model or enforcement mechanism itself.

## Why It Exists
Phase 4's Authorization document specifies how permissions are modeled and technically enforced (roles, scopes, policy evaluation). It does not specify who reviews those permissions, how often access is re-certified, what triggers a permission to be revoked, or — critically for this product — the security governance process behind the Proactivity Ladder, where the AI is granted increasing autonomy to act on a user's behalf (from suggesting to notifying to autonomously executing). Ungoverned authorization drifts: permissions granted for a one-time need never get revoked, and an AI capability shipped at a low autonomy tier can silently expand in practice without a corresponding security review. This document exists to ensure every grant of access or autonomy — human or AI — has an owner, an expiry, and a periodic re-justification.

## Approximate Page Count
9-11 pages

## Sections
1. **Least-Privilege Review Principles** — the standing policy that every permission grant (human or service) must be scoped to the minimum necessary and time-bound where feasible.
2. **Access Certification Cadence** — the recurring schedule (e.g. quarterly) on which every standing access grant across engineering, support, and admin tooling must be re-certified by its owner.
3. **Segregation of Duties** — rules preventing a single role from having both the ability to perform and to approve sensitive actions (e.g. no single engineer can both grant access and audit access grants).
4. **Proactivity Ladder Security Review Process** — the specific governance process by which each autonomy tier of the AI (suggest, notify, act-with-confirmation, act-autonomously) is security-reviewed and approved before it can be enabled for a given action category or user cohort.
5. **Privileged Access Management** — controls over admin/root-equivalent access to production systems and sensitive data stores, including just-in-time elevation and mandatory approval workflows.
6. **Third-Party & Vendor Access Governance** — the review and certification process for access granted to external vendors or integration partners (banking, health APIs).
7. **Access Revocation & Offboarding** — mandatory timelines and verification steps for revoking access upon role change or employee/contractor offboarding.
8. **Anomalous Access Detection & Response** — policy requiring monitoring for access patterns inconsistent with a user's certified role, and the response process when detected.
9. **Access Governance Audit Trail** — requirements for logging every grant, certification, and revocation decision in a form suitable for external audit.
10. **Relationship to Phase 4 Authorization Architecture** — explicit statement that this document governs review cadence, ownership, and approval process, while Doc 08 defines the technical permission model and enforcement engine.

## Deliverables
- Access certification calendar with named reviewers per system/domain.
- Segregation-of-duties matrix for sensitive operations.
- Proactivity Ladder tier approval workflow, including required security sign-off before any autonomy-tier increase ships.
- Privileged access management policy with just-in-time elevation requirement.
- Vendor access governance checklist.
- Offboarding access-revocation runbook with maximum allowable revocation time.

## Dependencies
Security Program & Governance (Phase 6 Doc 01), Threat Model (Phase 6 Doc 02), Zero Trust Architecture (Phase 6 Doc 03), Identity Governance (Phase 6 Doc 04), Authorization Architecture (Phase 4 Doc 08), Authentication Policy (Phase 6 Doc 05), Proactivity Ladder specification (Phase 2/3 product documents).

## Teams
Security, Engineering, Product, Privacy/Legal, AI/ML

## Completion Criteria
- [ ] Access certification cadence established and first certification cycle completed for all sensitive systems.
- [ ] Proactivity Ladder tier approval process piloted on at least one autonomy-tier increase with documented security sign-off.
- [ ] Segregation-of-duties matrix reviewed with no unresolved conflicts on sensitive operations.
- [ ] Vendor access governance checklist applied to all active banking/health integrations.
- [ ] Signed off by: CISO (required), Head of Engineering (required), Head of Product (required), Head of AI/ML (required for Proactivity Ladder sections).
