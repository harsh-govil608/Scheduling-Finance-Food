# Document 24: Alerting

## Document Name
Alerting

## Purpose
Define the operational alerting practice — alert routing, severity-to-response-time mapping, and alert-fatigue prevention — that turns the Metrics/Logging/Tracing infrastructure (Phase 4 Docs 32-34) into actionable signals for on-call engineers. This document specifies requirements for the eventual Alerting document, not the alert-evaluation infrastructure itself.

## Why It Exists
Infrastructure that can technically fire an alert doesn't guarantee the right person gets woken up appropriately; without a disciplined alerting practice, on-call engineers either miss critical alerts (buried in noise) or burn out from over-alerting, a well-documented cause of reliability-team attrition. At 100M+ user, multi-region, event-driven scale, the volume of things that could technically trigger a threshold breach is enormous, so this document exists to define the human-facing contract for what fires, who it reaches, how urgently, and how the organization keeps that contract trustworthy over time.

## Approximate Page Count
7-9 pages

## Sections
1. **Scope and Relationship to Phase 4 Infrastructure** — clarifies this document governs alerting practice and policy; Metrics (Doc 33), Logging (Doc 32), and Tracing (Doc 34) own the underlying signal collection and evaluation engines that alerts are built on.
2. **Alert Severity Tiers** — how severity maps to notification channel/urgency (page vs. Slack vs. dashboard-only), with concrete criteria for classifying a new alert into a tier.
3. **Severity-to-Response-Time Mapping** — the required acknowledgment and mitigation time targets per severity tier, and how they connect to on-call escalation policy.
4. **Alert Routing** — how alerts reach the right on-call engineer for the right service, including ownership mapping, fallback routing, and multi-region routing considerations.
5. **Alert Fatigue Prevention** — noise-reduction practices (deduplication, grouping, auto-resolution), and alert review cadence to retire low-value alerts.
6. **New Alert Creation Standards** — the requirements a new alert must meet before it can page a human (actionability, runbook link, owner) and the review gate for adding new paging alerts.
7. **Escalation Policy Practice** — how an unacknowledged or unresolved alert escalates up the chain, and the practice for keeping escalation policies current as teams reorganize.
8. **Alerting in Incident Response** — how an alert firing transitions into a declared incident, and the handoff practice to Incident Management.
9. **Post-Alert Review** — the practice of reviewing whether an alert fired correctly, too late, or unnecessarily, feeding into both alert-tuning and postmortems.
10. **Quarterly Alert Health Review** — org-wide review of paging volume, false-positive rate, and time-to-acknowledge trends per team, with a defined remediation process for teams exceeding thresholds.

## Deliverables
- Alert severity tier definitions with classification criteria and worked examples.
- Severity-to-response-time mapping table, aligned to on-call escalation policy.
- Alert routing and ownership matrix.
- New-alert creation checklist and review gate.
- Alert fatigue/noise metrics dashboard specification (paging volume, false-positive rate, time-to-acknowledge).
- Quarterly alert health review report template.

## Dependencies
Requires Metrics (Phase 4 Doc 33), Logging (Phase 4 Doc 32), Tracing (Phase 4 Doc 34), Observability (Phase 4 Doc 31), Monitoring Practice (Phase 9 Doc 20), Metrics Practice (Phase 9 Doc 22), SRE Practice (Phase 9), Incident Management (Phase 9), On-Call Rotation Practice (Phase 9).

## Teams
SRE, Platform Engineering, Service Owning Teams, On-Call Engineers, Engineering Leadership

## Completion Criteria
- [ ] Alert severity tiers reviewed against real historical incidents for correct classification.
- [ ] Severity-to-response-time targets validated against actual on-call acknowledgment data for at least one quarter.
- [ ] Alert routing matrix tested for multi-region failover scenarios.
- [ ] New-alert creation checklist adopted as a mandatory gate in the alert-provisioning workflow.
- [ ] At least one full quarterly alert health review completed with remediation actions tracked.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required).
