# Document 20: Audit Logs

## Document Name
Audit Logs

## Purpose
Define the security and compliance audit trail: the immutable, tamper-evident record of who accessed or changed what — across user data, permissions, financial records, and administrative actions — and for how long that record must be retained, protected, and made available to auditors, regulators, and incident responders. This document specifies the audit-log requirement, not the general application logging pipeline.

## Why It Exists
Phase 4's Logging (Doc 32) exists to help engineers debug and operate the system — it is optimized for volume, searchability, and short retention, and any engineer with log access can typically read it. That is the wrong shape for a record that must answer "prove that only authorized staff touched this user's bank-linked transactions" or "show every access to this user's health record in the last two years" during a regulatory inquiry or breach investigation. Without a purpose-built audit trail — append-only, access-restricted, retained on a compliance schedule, and covering privileged/administrative actions that operational logs often omit — the platform cannot support SOC 2, HIPAA-adjacent health-data obligations, PCI-relevant financial handling, or basic breach forensics. This document exists to make "who did what, when, and was it authorized" independently verifiable, even against a compromised or malicious insider.

## Approximate Page Count
8-10 pages

## Sections
1. **Audit Log Scope & Event Catalog** — the exhaustive list of event classes that must be audit-logged: data access (read of financial/health/message data), data mutation, permission and role changes, authentication events, admin/support-tool actions, and AI-agent autonomous actions taken on a user's behalf.
2. **Audit Event Schema** — the required fields on every audit record (actor identity, on-behalf-of subject, action, target resource, timestamp, source IP/device, authorization basis, outcome) and the rule that schema fields are additive-only once shipped.
3. **Immutability & Tamper-Evidence** — write-once storage (WORM), cryptographic chaining or hash-linking of records, and how tampering or deletion attempts are themselves detected and alarmed.
4. **Access Control Over Audit Logs** — the restricted, break-glass-audited roles permitted to read audit data, explicitly excluding the general engineering population and requiring dual control for any export.
5. **Retention & Legal Hold** — the retention schedule per data class (mapped to financial, health, and general-PII regulatory minimums), the legal-hold process that suspends normal deletion, and secure end-of-life destruction.
6. **Distinction from Application Logging** — an explicit boundary statement separating this document from Logging (Phase 4 Doc 32): audit logs are compliance-grade, access-restricted, and long-retained; application logs are operational, broadly accessible, and short-retained; no event class may satisfy an audit requirement by living only in application logs.
7. **Audit Trail for AI Autonomous Actions** — the requirement that every action the AI takes without a synchronous human confirmation (bookings, payments, message sends) produces an audit record indistinguishable in rigor from a human-initiated action, including the reasoning/trigger context that justified it.
8. **Query, Export & Investigator Workflow** — how Security, Legal, and Compliance request and receive audit data for investigations, including SLAs for turnaround and chain-of-custody documentation for anything used in a legal or regulatory proceeding.
9. **Monitoring & Alerting on the Audit Pipeline Itself** — health checks and alerts for audit-log ingestion gaps, since a silent audit-pipeline failure is itself a security incident.
10. **Compliance Mapping** — a crosswalk from audit log capabilities to the specific regulatory/framework clauses they satisfy (SOC 2 CC-series, relevant health and financial data regulations), owned jointly with Regulatory Compliance (Phase 6, Group 07).

## Deliverables
- Canonical audit event catalog with owning service per event class.
- Audit event schema definition with versioning rules.
- WORM storage and tamper-evidence design (hash-chaining or equivalent).
- RBAC policy restricting audit-log read/export access, with break-glass logging.
- Retention schedule table by data class and legal-hold procedure.
- Investigator request workflow and chain-of-custody template.
- Compliance-clause crosswalk document.

## Dependencies
Requires Security Program & Governance (Phase 6 Doc 01), Threat Model (Phase 6 Doc 02), Logging (Phase 4 Doc 32) for the explicit boundary definition, Authorization Architecture (Phase 4 Doc 08), Finance Service (Phase 4 Doc 12), Health Service (Phase 4 Doc 13). Feeds Incident Response (Phase 6 Doc 21), Security Monitoring (Phase 6 Doc 22), and Regulatory Compliance (Phase 6, Group 07).

## Teams
Security, Compliance, Legal, Platform Engineering, Data Engineering, Site Reliability Engineering

## Completion Criteria
- [ ] Audit event catalog reviewed against every service that touches financial, health, or identity data for completeness.
- [ ] Tamper-evidence mechanism validated by a red-team attempt to alter or delete a record undetected.
- [ ] Access-restriction policy confirmed to exclude general engineering and require dual control for export.
- [ ] Retention schedule cross-checked against Regulatory Compliance requirements per data class.
- [ ] Signed off by: CISO (required), Head of Compliance (required), General Counsel (required).
