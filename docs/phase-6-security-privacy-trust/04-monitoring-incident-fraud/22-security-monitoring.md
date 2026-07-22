# Document 22: Security Monitoring

## Document Name
Security Monitoring

## Purpose
Define the security-specific monitoring layer that continuously watches for signs of compromise, abuse, and policy violation across the platform — distinct from Phase 4's general system-health Observability (Doc 31) and Phase 5's AI-quality Observability (Doc 27). This document specifies what security signals are collected, how they are correlated and scored, and how they become actionable alerts routed into Incident Response.

## Why It Exists
General system observability answers "is the service healthy"; AI observability answers "is the model behaving well." Neither answers "is someone trying to break in" or "is this account behaving like it has been taken over" — those questions require signals that are meaningless from an uptime or model-quality perspective (a single successful login from a new country, a burst of small transactions just under a review threshold) but critical from a security one. Without dedicated security monitoring, attacks that do not cause outages or model degradation go completely undetected until a user reports harm. Given the platform's access to money movement and health data, and an AI agent that can act autonomously, the cost of a slow detection is materially higher than typical SaaS. This document exists to make "is someone trying to break in, or already in" a continuously answered question, not a reactive one.

## Approximate Page Count
8-10 pages

## Sections
1. **Security Signal Catalog** — the specific signals monitored: impossible-travel logins, abnormal data-export volume, rapid permission or credential changes, repeated authorization failures, unusual API access patterns, and anomalous AI-agent action volume or type.
2. **Detection-to-Alert Pipeline** — how raw signals are aggregated, correlated, and scored into an actionable alert, including the deduplication and noise-reduction approach that keeps alert volume sustainable for a human team.
3. **Baselines & Anomaly Detection** — how "normal" is established per user and per system (behavioral baselining), the retraining cadence for baselines, and safeguards against baseline poisoning by a slowly-escalating attacker.
4. **Alert Severity & Routing** — how monitoring alerts map onto the Incident Response severity rubric (Phase 6 Doc 21) and the automatic routing rules to the on-call security responder.
5. **Coverage Boundaries vs. Other Observability Systems** — an explicit boundary statement distinguishing this document's security signals from Observability (Phase 4 Doc 31, system health), Logging (Phase 4 Doc 32, general event capture), and AI Observability (Phase 5 Doc 27, model quality), with a rule against duplicating their content here.
6. **Data Sources & Instrumentation Requirements** — the mandatory security instrumentation every service must emit (authentication events, authorization decisions, data-access events) and how this pipeline consumes Audit Logs (Phase 6 Doc 20) as an input without duplicating its storage.
7. **AI-Agent Action Monitoring** — dedicated monitoring for autonomous AI actions specifically for signs of manipulation or misuse (prompt-injection-driven action spikes, actions inconsistent with stated user intent), coordinating with the AI Safety & Security group (Phase 6, Group 05) for detection logic ownership.
8. **Dashboards & Analyst Tooling** — the security operations dashboard requirements giving analysts real-time visibility into active alerts, signal trends, and investigation tooling to pivot from an alert to underlying audit/log evidence.
9. **Tuning, False-Positive Management & Coverage Review** — the process for tuning detection rules, tracking false-positive rates, and periodically validating signal coverage against the Threat Model's abuse scenario catalog.
10. **Third-Party Security Monitoring Tooling** — evaluation and integration requirements for any external SIEM/detection platform, including data residency constraints given the sensitivity of the underlying data.

## Deliverables
- Security signal catalog with owning system and detection logic reference per signal.
- Detection-to-alert pipeline architecture and deduplication/noise-reduction design.
- Behavioral baselining methodology and retraining schedule.
- Alert-to-severity mapping aligned with Incident Response.
- Security operations dashboard specification.
- Quarterly coverage-review process against the Threat Model.

## Dependencies
Requires Threat Model (Phase 6 Doc 02), Audit Logs (Phase 6 Doc 20), Incident Response (Phase 6 Doc 21), Observability (Phase 4 Doc 31), Logging (Phase 4 Doc 32), AI Observability (Phase 5 Doc 27). Feeds Fraud Detection (Phase 6 Doc 24) and Abuse Prevention (Phase 6 Doc 23) with shared correlation infrastructure.

## Teams
Security, Site Reliability Engineering, Data Engineering, AI Safety, Platform Engineering

## Completion Criteria
- [ ] Security signal catalog reviewed against the Threat Model's abuse scenario catalog for coverage.
- [ ] Alert routing validated end-to-end into the Incident Response on-call path with a test alert.
- [ ] False-positive rate baseline established and an acceptable-range target agreed with the security operations team.
- [ ] Boundary review confirming no duplication with Observability (Doc 31), Logging (Doc 32), or AI Observability (Doc 27).
- [ ] Signed off by: CISO (required), Head of SRE (required), Head of AI Safety (required).
