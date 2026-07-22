# Document 05: Authentication Policy

## Document Name
Authentication Policy

## Purpose
Define the policy layer over Phase 4's Authentication architecture (Doc 07) — the specific, enforceable rules for multi-factor authentication requirements, password and passwordless credential policy, credential rotation, session policy, and step-up authentication for sensitive actions. This document specifies the rules the authentication system must enforce; it does not redesign the authentication architecture or protocols themselves.

## Why It Exists
Phase 4's Authentication document specifies how login, session issuance, and credential storage are technically implemented. It does not specify the policy decisions layered on top: how many MFA factors are required for a banking-linked account versus a casual browsing session, how often credentials must rotate, what constitutes an acceptable passwordless recovery flow, or when step-up authentication is mandatory before an AI agent is allowed to execute a sensitive proactive action (e.g. initiating a transfer). Given that this platform's AI can act with increasing autonomy under the Proactivity Ladder, authentication policy must be tied to the sensitivity of the action being authorized, not just to login. This document exists to make those policy decisions explicit, consistent, and auditable rather than left to individual engineering judgment.

## Approximate Page Count
8-10 pages

## Sections
1. **MFA Requirements by Risk Tier** — mandatory MFA factor requirements tied to account risk tier and to the sensitivity of the action being performed (e.g. viewing a dashboard vs. approving a bank transfer).
2. **Passwordless & Password Policy** — policy on supported passwordless methods (passkeys, magic links), and where passwords remain supported, minimum complexity and breach-list screening requirements.
3. **Credential Rotation & Expiry Rules** — rotation cadence for passwords, API keys, and service credentials, and forced rotation triggers (suspected compromise, employee offboarding).
4. **Session Policy** — session duration limits, idle timeout, concurrent session limits, and re-authentication triggers tied to sensitivity.
5. **Step-Up Authentication for Sensitive Actions** — the policy defining which actions (financial transfers, health record export, changing recovery methods, AI autonomous actions at higher Proactivity Ladder tiers) require fresh re-authentication beyond an active session.
6. **Account Recovery Policy** — governance over recovery flows to prevent them from becoming the weakest authentication path, including identity-proofing requirements for high-value account recovery.
7. **Authentication Failure & Lockout Policy** — brute-force protection thresholds, lockout duration, and user notification requirements on suspicious authentication attempts.
8. **AI Agent Authentication to User Sessions** — policy governing how the AI system authenticates its own actions when operating on a user's behalf, distinct from user-initiated authentication.
9. **Policy Exceptions & Legacy Flow Sunset** — process for any authentication flow that doesn't yet meet policy, with mandatory remediation timeline.
10. **Relationship to Phase 4 Authentication Architecture** — explicit statement that this document defines policy thresholds and rules, while Doc 07 defines the technical authentication system that enforces them.

## Deliverables
- MFA requirement matrix mapped to risk tier and action sensitivity.
- Passwordless/password policy specification.
- Credential rotation and expiry rule set.
- Session policy specification (duration, idle timeout, concurrency limits).
- Step-up authentication trigger list tied to the Proactivity Ladder and sensitive actions.
- Account recovery identity-proofing requirements.
- Authentication failure/lockout policy.

## Dependencies
Security Program & Governance (Phase 6 Doc 01), Threat Model (Phase 6 Doc 02), Zero Trust Architecture (Phase 6 Doc 03), Identity Governance (Phase 6 Doc 04), Authentication Architecture (Phase 4 Doc 07), Authorization Policy & Access Governance (Phase 6 Doc 06).

## Teams
Security, Engineering, Product, Privacy/Legal

## Completion Criteria
- [ ] MFA requirement matrix approved and mapped to every sensitive action category, including Proactivity Ladder tiers.
- [ ] Step-up authentication policy validated against at least one real financial and one real health data flow.
- [ ] Account recovery policy reviewed for social-engineering resistance.
- [ ] Legacy authentication flows inventoried and exceptions time-boxed.
- [ ] Signed off by: CISO (required), Head of Engineering (required), Head of Product (required).
