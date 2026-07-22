# Document 20: Monitoring Practice

## Document Name
Monitoring Practice

## Purpose
Define the day-to-day operational discipline of monitoring the AI Life Operating System — who owns which dashboards, how often they are reviewed, and what "healthy" looks like for a given service — layered on top of the Observability architecture defined in Phase 4 (Doc 31). This document specifies the requirements for the eventual Monitoring Practice document, not the practice itself.

## Why It Exists
Phase 4's Observability architecture (Doc 31) establishes the technical capability to collect signals across a multi-region, event-driven, 100M+ user platform, but capability without ownership decays: dashboards go stale, nobody notices a slow-burning regression, and "someone should be watching this" quietly becomes "no one is watching this." This document exists to assign human accountability to observability infrastructure — naming dashboard owners, setting review cadences, and defining what action is expected when a signal looks wrong — so that monitoring remains a living operational habit rather than a one-time engineering deliverable.

## Approximate Page Count
6-8 pages

## Sections
1. **Scope and Relationship to Phase 4 Observability** — explicitly states this document defines practice (who/when/how), not the underlying architecture (Doc 31 owns collection, storage, and platform design).
2. **Dashboard Ownership Model** — how every production dashboard is assigned a named owning team, with a designated backup owner for continuity.
3. **Review Cadence** — required daily, weekly, and monthly review rituals per service tier (e.g., daily glance for Tier-1 AI proactive-action services, weekly for Tier-2).
4. **Golden Signals per Domain** — the minimal set of signals (latency, traffic, errors, saturation) each team must monitor for its domain, and how domain-specific signals (e.g., proactive-suggestion acceptance rate) extend the golden set.
5. **New Service Monitoring Onboarding** — the checklist a team must complete before a new service is allowed to go to production, ensuring a dashboard exists and an owner is named.
6. **Dashboard Hygiene and Deprecation** — practice for retiring dashboards tied to deprecated services or unused metrics, preventing dashboard sprawl.
7. **Cross-Region and Global Health Views** — practice for maintaining and reviewing the aggregate multi-region view versus per-region views, and who is accountable for each.
8. **Monitoring in Incident Response** — how monitoring dashboards are used in the first minutes of an incident, and their handoff relationship to Incident Management practice.
9. **Escalation from Passive Monitoring** — when a reviewer who notices an anomaly during routine review (not via alert) is expected to escalate, and through what channel.
10. **Audit and Compliance Checks** — periodic audit that every production service has a live, owned, reviewed dashboard, with reporting to engineering leadership.

## Deliverables
- Dashboard ownership registry template (service, owning team, primary owner, backup owner).
- Review cadence matrix by service tier.
- Golden-signal checklist for new service onboarding.
- Dashboard deprecation and hygiene runbook.
- Quarterly monitoring coverage audit report template.

## Dependencies
Requires Observability (Phase 4 Doc 31), Metrics (Phase 4 Doc 33), Metrics Practice (Phase 9 Doc 22), Alerting (Phase 9 Doc 24), SRE Practice (Phase 9), Incident Management (Phase 9), Service Ownership Model (Phase 9).

## Teams
SRE, Platform Engineering, Service Owning Teams, Engineering Leadership, On-Call Engineers

## Completion Criteria
- [ ] Every Tier-1 and Tier-2 production service has a named dashboard owner recorded in the ownership registry.
- [ ] Review cadence matrix validated against at least one full quarter of actual review logs.
- [ ] New-service monitoring onboarding checklist piloted with at least two newly launched services.
- [ ] Dashboard deprecation practice tested by retiring at least one stale dashboard.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required), Director of Platform Engineering (required).
