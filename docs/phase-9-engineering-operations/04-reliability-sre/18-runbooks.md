# Document 18: Runbooks

## Document Name
Runbooks

## Purpose
Define the standard format and required-coverage policy for operational runbooks — the step-by-step, service- and failure-mode-specific procedures that responders execute during an active incident. This document governs how runbooks are written, where they live, how their freshness is enforced, and which services and failure modes must have one; it exists to directly speed up the response defined in Incident Management (Phase 9 Doc 17).

## Why It Exists
During an active incident, minutes matter. An on-call engineer paged at 3 a.m. for an unfamiliar subsystem needs a current, trustworthy, step-by-step procedure — not source code to reverse-engineer under pressure or a Slack thread to be reconstructed from memory. Without a standard format and an enforced coverage and freshness requirement, runbooks either don't exist for the services that need them most or rot quietly until an incident proves them wrong. Given that 100M+ users depend on continuous, correct AI proactive care, this document exists to make the gap between "an alert fires" and "the correct mitigation step is taken" a matter of seconds spent reading, not minutes spent investigating.

## Approximate Page Count
6-8 pages.

## Sections
1. **Runbook Standard Format** — the required structure for every runbook: symptom/trigger, diagnostic steps, mitigation steps, escalation path, rollback procedure, and owning team.
2. **Required Coverage Policy** — the rule that every Tier 0/Tier 1 service (per the tiering in Reliability Engineering, Doc 15) and every paging alert must map to at least one runbook before that alert is permitted to page in production.
3. **Runbook Authoring & Review Process** — who writes each runbook (owning feature team versus SRE), the required peer review, and the definition-of-done before a new runbook is considered complete.
4. **Runbook Storage & Discoverability** — the single source-of-truth location, the requirement that alerts and dashboards link directly to the relevant runbook, and search/indexing requirements.
5. **Freshness & Staleness Enforcement** — the mandatory review cadence, automated staleness flagging, and the policy for retiring or rewriting runbooks that reference deprecated systems.
6. **Runbook Validation via Game Days & Drills** — the requirement that runbooks be exercised, not merely written, on a recurring cadence coordinated with chaos engineering (Phase 9 Doc 16) and Disaster Recovery game days (Phase 4 Doc 35).
7. **AI-Assisted Runbook Execution** — the requirements and guardrails for AI-assisted or partially automated execution of diagnostic and mitigation steps, and the categories of action for which human approval remains mandatory regardless of automation.
8. **Runbook Gaps & Post-Incident Feedback Loop** — the requirement that any incident revealing a missing or inadequate runbook automatically generates a tracked action item to write or fix one, closing the loop with Postmortems (Phase 9 Doc 19).

## Deliverables
* Runbook template with required fields, published and enforced.
* Required-coverage policy tied to reliability tiering and paging alert configuration.
* Central runbook repository with direct alert-to-runbook linkage.
* Staleness-detection mechanism with a mandatory review cadence.
* AI-assisted execution guardrail standard specifying required human-approval boundaries.

## Dependencies
Requires Reliability Engineering (Phase 9 Doc 15) for tiering, SRE Practice (Phase 9 Doc 16). Coordinates with Incident Management (Phase 9 Doc 17), Postmortems (Phase 9 Doc 19), Observability Practice (Phase 9) for alert linkage, and Disaster Recovery (Phase 4 Doc 35) for failover-specific runbooks.

## Teams
SRE, Engineering, Platform/Infrastructure, AI/ML (AI-assisted execution guardrails), Engineering Productivity

## Completion Criteria
- [ ] 100% of Tier 0 services and their paging alerts have a linked, reviewed runbook.
- [ ] Runbook template piloted and validated through at least one live or simulated incident response.
- [ ] Staleness-detection mechanism live with an enforced review cadence.
- [ ] At least one runbook validated end-to-end via a game day drill.
- [ ] Signed off by: Head of SRE (required), VP Engineering (required).
