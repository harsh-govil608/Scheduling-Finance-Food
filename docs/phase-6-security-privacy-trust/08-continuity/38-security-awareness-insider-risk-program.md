# Document 38: Security Awareness & Insider Risk Program

## Document Name
Security Awareness & Insider Risk Program

## Purpose
Define the employee-facing security training curriculum and the insider-threat mitigation program that governs how the company manages the risk of its own people — the support agents, engineers, and operators who have legitimate access to users' financial records, health data, and AI memory. This document covers awareness, monitoring, and response for risk that originates inside the organization, complementing the external-attacker focus of the rest of Phase 6.

## Why It Exists
Every other Phase 6 document assumes the primary adversary is external — an attacker probing the network, a malicious actor attempting to jailbreak the model, a third-party vendor with weak controls. But at 100M+ users, the company itself employs hundreds to thousands of people, a meaningful subset of whom will have legitimate, necessary access to extremely sensitive user data: support agents resolving billing disputes who can see financial history, engineers debugging production incidents who can query health records, and operators with access to the AI memory store that constitutes a user's most private information. A single untrained employee who falls for a phishing email, or a single disgruntled employee with standing access and no monitoring, can cause harm no external control can prevent. This document exists because the original Phase 6 scope covered external threats extensively but left the internal/employee-access dimension as a gap, and a platform holding irreplaceable personal data cannot treat its own workforce as an unmanaged risk.

## Approximate Page Count
6-8 pages

## Sections
1. **Security Awareness Training Curriculum** — mandatory onboarding and annual refresher training, with role-based modules differentiated for support agents handling sensitive data, engineers with production access, and executives with financial/strategic exposure.
2. **Phishing & Social Engineering Simulation** — recurring simulated-phishing cadence, scoring, and the escalation path for employees with repeated failures, up to mandatory retraining or access review.
3. **Insider Risk Tiering** — classification of roles and individuals by the sensitivity of data they can access (e.g., direct query access to raw finance, health, or AI memory stores), with monitoring intensity scaled to tier.
4. **Privileged & Sensitive Data Access Monitoring** — anomaly detection on internal employee access patterns (e.g., a support agent viewing an unusually high volume of records, off-hours access to health data), built on the audit logging and monitoring capability defined in Security Monitoring (Phase 6).
5. **Least-Privilege & Need-to-Know Enforcement for Internal Tooling** — required access review cadence for internal admin and support tools, and just-in-time access grants for production debugging rather than standing access.
6. **Offboarding & Access Revocation SLA** — the maximum time allowed to revoke all system access after termination or role change, with a stricter, expedited SLA for involuntary terminations.
7. **Insider Threat Investigation Process** — how a suspected insider risk case is opened, investigated, and closed, including required coordination between Security, HR, and Legal, and the employee privacy protections that apply during an investigation.
8. **Reporting Culture & Non-Retaliation** — the anonymous internal reporting channel for suspicious behavior, an explicit non-retaliation policy, and a security-champions program embedding awareness within engineering and support teams.
9. **Contractor & Third-Party Personnel Coverage** — extension of training requirements and access monitoring to contractors, vendors, and outsourced support staff with any access to user data.
10. **Program Metrics & Board Reporting** — training completion rates, phishing simulation click/report rates, and insider risk case counts and outcomes, reported to leadership on a recurring cadence.

## Deliverables
- Role-based security awareness training curriculum with completion tracking.
- Phishing simulation program with defined cadence, scoring, and escalation thresholds.
- Insider risk tiering model mapping roles to data sensitivity and monitoring level.
- Access revocation runbook with defined SLAs for standard and involuntary terminations.
- Insider threat investigation playbook with named HR/Legal/Security coordination points.
- Anonymous reporting channel and non-retaliation policy statement.

## Dependencies
Requires Security Program & Governance (Phase 6, Doc 1), Authorization Policy & Access Governance (Phase 6), Security Monitoring (Phase 6), Data Classification & Sensitivity Tiers (Phase 6).

## Teams
Security, People/HR, Legal/Compliance, Engineering Leadership, Support/Customer Experience Leadership

## Completion Criteria
- [ ] Training curriculum mapped to every role tier defined in the Insider Risk Tiering model.
- [ ] Phishing simulation program piloted with at least one full quarterly cycle and escalation thresholds validated.
- [ ] Access revocation SLA tested against at least one real offboarding event, standard and involuntary.
- [ ] Insider threat investigation process reviewed jointly by Security, HR, and Legal.
- [ ] Signed off by: CISO (required), Head of People/HR (required), Head of Legal/Compliance (required).
