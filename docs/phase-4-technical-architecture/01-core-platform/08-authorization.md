# Document 08: Authorization

## Document Name
Authorization

## Purpose
Define the permission and access-control model for the platform — how services determine "is this authenticated user allowed to do this" — distinct from Document 07's determination of "who is this user." This document specifies how the Proactivity Ladder's increasing AI autonomy tiers and Shared Family Mode's multi-user access are translated into an enforceable, service-checkable authorization model.

## Why It Exists
The product's central premise — an AI that proactively acts on a user's behalf with manual work approaching zero — makes authorization the mechanism that keeps proactivity safe: every rung of the Proactivity Ladder represents the AI being granted a wider scope of unsupervised action, and if that scope is not modeled as an explicit, checkable authorization construct, "consent central" becomes a UI promise with no architectural enforcement behind it. Layered on top, Shared Family Mode means a single piece of data (e.g., a shared calendar event or joint account) may be legitimately visible or actionable by multiple distinct identities with different permission levels, and without a documented model each service invents its own ad hoc rules, producing both data leaks (a family member sees what they shouldn't) and product failures (the AI is blocked from acting because no service agrees on who authorized it). At 100M+ users this must be a centrally reasoned model, not 9 independently drifting implementations.

## Approximate Page Count
12-14 pages.

## Sections
1. **Authorization Model Foundations** — the chosen model family (e.g., role-based, attribute-based, relationship-based, or a hybrid) and the criteria used to select it for this product's needs.
2. **Proactivity Ladder as an Authorization Construct** — how each rung of the Proactivity Ladder (suggest -> confirm -> auto-execute-with-notice -> fully autonomous, per the Phase 1/2 definition) is modeled as a distinct, per-action-category, per-user authorization scope that services must check before the AI acts.
3. **Consent Scope & Granularity** — the granularity at which consent is captured and enforced (per domain, per action type, per specific integration), and how consent state is stored, versioned, and made queryable by every service.
4. **Permission Evaluation Architecture** — where and how permission checks are evaluated (centralized policy service vs. embedded per-service logic), latency implications, and consistency guarantees needed so a revoked permission takes effect promptly everywhere.
5. **Resource-Level Access Control** — how access control is enforced at the level of individual resources (a specific calendar, account, or health record) rather than only coarse feature flags.
6. **Shared Family Mode Access Model** — the multi-user permission structure for households: owner/member/dependent-style roles, per-resource sharing rules, and how a family member's authorization is scoped separately from the primary account holder's.
7. **AI-Initiated Action Authorization** — the distinct authorization path for actions the AI itself initiates (versus a human-initiated request), including how the system proves after the fact that an autonomous action was within the user's granted proactivity scope.
8. **Delegation & Revocation** — how a user grants, narrows, or fully revokes AI or family-member permissions, and the propagation guarantee (time bound) for revocation to take effect across all services and cached decisions.
9. **Audit Trail for Authorization Decisions** — what must be logged for every permission grant, check, and denial, supporting both user-facing transparency ("why did the AI do that") and compliance needs.
10. **Cross-Domain Authorization Consistency** — how authorization stays coherent when a single cross-pillar action requires permission checks across multiple domains simultaneously (e.g., an AI action touching both Calendar and Finance).

## Deliverables
* Approved Authorization document with the permission model specification.
* Proactivity Ladder-to-authorization-scope mapping table.
* Shared Family Mode role and resource-sharing matrix.
* Consent/permission audit log schema.

## Dependencies
Requires Overall System Architecture, Domain Boundaries, Authentication. Requires Phase 1 Company Foundation (Proactivity Ladder definition), Phase 2 Product Definition (consent UX docs, Shared Family Mode behavior docs), and relevant Phase 3 PRDs.

## Teams
Security, Engineering, AI/ML, Platform/Infrastructure, Legal/Compliance.

## Completion Criteria
- [ ] Every rung of the Proactivity Ladder has a documented, enforceable authorization scope with no ambiguous rungs.
- [ ] Shared Family Mode role matrix validated against at least two real Phase 3 PRD scenarios involving shared resources.
- [ ] Revocation propagation time bound defined and confirmed technically achievable by Platform/Infrastructure.
- [ ] AI-initiated action authorization path independently reviewed to confirm no action can execute outside its granted proactivity scope.
- [ ] Audit trail schema reviewed by Legal/Compliance for sufficiency.
- [ ] Signed off by: CISO/Security Lead (required), Principal Architect (required), Head of AI/ML (required), VP Engineering (required).
