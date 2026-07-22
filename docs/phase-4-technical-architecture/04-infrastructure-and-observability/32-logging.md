# Document 32: Logging

## Document Name
Logging

## Purpose
Define structured logging standards, log aggregation pipeline requirements, and PII/PHI/financial-data redaction requirements for all 9 backend services and client applications. This document treats logging as both an operational necessity and a major privacy liability, given the platform's access to financial history, health records, and cumulative AI memory of a user's life.

## Why It Exists
A single unredacted log line containing a raw account number, a diagnosis code, or a verbatim fragment of the AI's memory of a user's life is not a minor bug — it is a potential regulatory incident and a breach of the trust the product depends on to be allowed into a user's finances and health in the first place. At 100M+ users, log volume is also enormous, so redaction cannot rely on manual review; it must be enforced structurally, at the logging library and pipeline level, before data ever reaches a durable log store. This document exists so that every service logs in a consistent, machine-parseable format and so that sensitive data categories are defined once and enforced everywhere, rather than left to each engineer's individual judgment about what is safe to log.

## Approximate Page Count
7-9 pages.

## Sections
1. **Structured Log Format Standard** — the required fields (timestamp, service, trace ID, correlation IDs, severity) and machine-parseable schema every service must emit.
2. **Log Levels & Usage Policy** — standard severity levels and rules governing what may and may not be logged at each level.
3. **PII/PHI/Financial Data Redaction Requirements** — the categories of sensitive fields (from Finance, Health, and AI memory) that must never appear in plaintext logs, and the redaction/tokenization mechanisms enforced at the logging library level.
4. **Log Aggregation & Pipeline Architecture** — collection agents, transport, and central aggregation store requirements sized for 100M+ user log volume.
5. **Correlation with Tracing & Metrics** — the requirement that every log line carries trace and span IDs to cross-link with Tracing and Metrics.
6. **Access Control & Audit Logging** — tiered access to logs (raw vs. redacted), and the distinct requirement for immutable audit logs of sensitive actions, including any action the AI takes on a user's behalf.
7. **Retention & Deletion Policy** — retention windows per log sensitivity class, and alignment with user deletion and right-to-be-forgotten requests.
8. **Cost & Sampling Strategy at Scale** — sampling and tail-based retention strategies to control log volume cost at 100M+ users without losing debuggability of rare failures.
9. **Compliance Mapping** — how logging requirements map to relevant financial and health data handling obligations, at a scoping level only, deferring detailed legal analysis to compliance-specific documentation.

## Deliverables
* Approved structured logging schema specification.
* PII/PHI/financial-data redaction field catalog enforced at the logging library level.
* Log retention policy by sensitivity class.
* Immutable audit logging requirements for sensitive and AI-initiated actions.

## Dependencies
Requires Observability, Overall System Architecture, Service Decomposition. Coordinates with Data Architecture and Security/Compliance documentation for sensitive field definitions. Feeds Tracing through shared correlation IDs.

## Teams
Platform/Infrastructure, SRE, Security, Compliance/Legal, Data Engineering, Engineering.

## Completion Criteria
- [ ] Structured log schema adopted by all 9 backend services.
- [ ] Redaction field catalog covers 100% of identified Finance, Health, and AI-memory sensitive fields, with enforcement verified at the library level.
- [ ] Immutable audit logging implemented for all AI-initiated proactive actions.
- [ ] Retention policy reconciled with user deletion and right-to-be-forgotten requirements.
- [ ] Signed off by: Head of SRE (required), CISO/Head of Security (required), DPO/Compliance Lead (required).
