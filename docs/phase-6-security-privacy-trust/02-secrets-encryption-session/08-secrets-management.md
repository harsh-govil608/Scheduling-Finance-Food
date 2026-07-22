# Document 8: Secrets Management

## Document Name
Secrets Management

## Purpose
Define the company-wide policy governing every non-key credential the platform depends on — API keys, database passwords, service-to-service tokens, webhook signing secrets, and third-party OAuth client secrets. This document specifies how secrets must be stored, who and what may request them, how often they rotate, and how the organization detects and responds when one leaks. It applies across all services that touch financial, health, and behavioral data, and to every environment from local development through production.

## Why It Exists
Secrets are the most common real-world cause of breach at companies that otherwise have strong architecture, because a single hardcoded API key in a repository, a credential pasted into a support ticket, or an over-privileged service token can bypass every other control the platform has built. Without a single, binding policy, individual teams default to inconsistent and often unsafe practices — secrets in environment files committed to source control, shared credentials with no attribution, tokens that never expire. This document exists to make secrets handling a uniform, auditable, and enforced discipline rather than a matter of individual engineer judgment, and to give the organization a rehearsed response when a secret is exposed rather than an improvised one.

## Approximate Page Count
7-9 pages

## Sections
1. **Secrets Taxonomy & Scope** — Defines what counts as a secret (API keys, DB credentials, signing keys, service tokens, third-party client secrets) and distinguishes secrets policy from the cryptographic-key policy owned by Key Management.
2. **Storage & Vaulting Requirements** — Specifies that secrets must never reside in source control, container images, logs, or client-side code, and sets the policy-level requirements a centralized secrets vaulting system must meet (encryption at rest, access via short-lived leases, no plaintext export).
3. **Access Control & Least Privilege** — Defines how secret access is scoped per service/environment, requires named ownership for every secret, and prohibits shared or generic credentials.
4. **Rotation Policy** — Sets mandatory rotation cadences by secret sensitivity tier and mandates automatic rotation wherever the downstream system supports it, with manual rotation as a documented exception.
5. **Secrets in CI/CD & Developer Workflows** — Governs how build pipelines, local development, and staging environments obtain secrets without persisting them to disk or pipeline logs.
6. **Detection & Prevention** — Requires automated secret-scanning on every commit, pull request, and container build, and defines the blocking vs. alerting behavior when a secret is detected.
7. **Leak Incident Response** — Defines the mandatory sequence when a secret is confirmed or suspected exposed: revoke, rotate, assess blast radius, notify, and document — with target time-to-revoke by secret sensitivity tier.
8. **Audit Logging & Review Cadence** — Requires every secret access, creation, and rotation to be logged and attributable to a human or service identity, and sets the cadence for access reviews and unused-secret cleanup.
9. **Third-Party & Vendor Secrets** — Governs credentials issued to or received from external vendors, contractors, and integration partners, including offboarding requirements.
10. **Exceptions & Break-Glass Procedures** — Defines the narrow, time-boxed process for emergency access to a secret outside normal tooling, with mandatory post-hoc review.

## Deliverables
- Secrets taxonomy and sensitivity classification table
- Vault architecture requirements (policy-level, technology-agnostic)
- Rotation cadence matrix by secret sensitivity tier
- Secret-scanning enforcement policy (blocking gates and exceptions)
- Leak incident response runbook reference and time-to-revoke SLAs
- Access review and audit logging requirements
- Break-glass procedure specification

## Dependencies
Security Program & Governance (Phase 6), Data Classification & Sensitivity Tiers (Phase 6), Encryption Standards & Policy (Phase 6, Document 9), Key Management (Phase 6, Document 10), Authentication (Phase 4, Document 7), Authorization (Phase 4, Document 8), CI/CD (Phase 4, Document 30), Configuration (Phase 4, Document 27).

## Teams
Security Engineering, Platform Engineering, DevOps/SRE, Compliance & Legal, Engineering Leadership

## Completion Criteria
- [ ] Secrets taxonomy reviewed against every service in the Service Decomposition inventory (Phase 4, Document 2).
- [ ] Rotation cadence matrix validated against each data sensitivity tier defined in Data Classification (Phase 6).
- [ ] Leak incident response sequence tabletop-tested with Security Engineering and SRE.
- [ ] Secret-scanning gate requirements confirmed compatible with existing CI/CD pipeline (Phase 4, Document 30).
- [ ] Signed off by: CISO (required), Head of Platform Engineering (required), Head of Engineering (required).
