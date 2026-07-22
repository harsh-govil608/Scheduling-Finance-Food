# Document 16: SRE Practice

## Document Name
SRE Practice

## Purpose
Define the Site Reliability Engineering team's operating model: on-call rotation structure and staffing, the toil-reduction discipline that keeps operational load sustainable, and the engagement model through which SRE partners with feature teams rather than gatekeeping them. This document translates the targets and policy set in Reliability Engineering (Doc 15) into a concrete, staffed, day-to-day team practice.

## Why It Exists
Without an operating model, reliability targets remain aspirational: on-call becomes an unstructured burden that burns out engineers, ownership boundaries between SRE and feature teams stay ambiguous, and toil quietly crowds out the automation investment that would have prevented the next incident. Because this platform's AI agent takes continuous, proactive action on a user's finances, health, and schedule at 100M+ user scale across multiple regions, a well-staffed and sustainably run SRE organization is what actually stands between an SLO written on paper and reliability experienced by a real person. This document exists so the practice of reliability — who is paged, when, with what support, and with how much protected time to fix root causes rather than firefight — is deliberately designed rather than improvised under pressure.

## Approximate Page Count
8-10 pages.

## Sections
1. **SRE Team Structure & Embedding Model** — the balance between a centralized SRE core and SRE support embedded within feature teams, mapped to the reliability tiers defined in Reliability Engineering (Doc 15).
2. **On-Call Rotation Design** — rotation size, shift length, primary/secondary structure, follow-the-sun coverage for multi-region operation, and the compensation and time-off-in-lieu policy for on-call work.
3. **Paging & Escalation Standards** — the alert-to-page criteria (coordinated with Observability Practice, Phase 9), escalation timers, and the hard requirement that every page maps to an actionable runbook (Doc 18).
4. **Toil Definition & Toil Budget** — the operational definition of toil, the tracking mechanism used to measure it, and the enforced cap on toil as a share of SRE time that guarantees room for automation work.
5. **SRE–Feature Team Engagement Model** — the production readiness review a service must pass before it is admitted to a reliability tier requiring SRE support, and the ongoing consulting relationship that follows rather than a one-time handoff.
6. **Error Budget Enforcement in Practice** — how SRE operationalizes the Doc 15 error budget policy day to day, including the standing authority to enforce a release freeze when a budget is exhausted.
7. **Chaos Engineering & Proactive Resilience Testing** — the practice of deliberately injecting failure to validate reliability assumptions, its cadence and blast-radius controls, coordinated with the Disaster Recovery game-day program (Phase 4 Doc 35).
8. **SRE Tooling & Automation Investment** — the internal tooling roadmap SRE owns to reduce toil and accelerate incident response, coordinated with Internal Tooling (Phase 9).
9. **On-Call Health Metrics & Burnout Prevention** — required tracking of page volume, after-hours load, and rotation fairness, with defined intervention thresholds and escalation to engineering leadership when thresholds are breached.

## Deliverables
* On-call rotation policy and staffing model per reliability tier.
* Toil budget definition, tracking dashboard, and enforced cap.
* Production readiness review checklist and tier-admission gating criteria.
* Chaos engineering and game-day program cadence.
* On-call health metrics dashboard with defined burnout intervention thresholds.

## Dependencies
Requires Reliability Engineering (Phase 9 Doc 15). Coordinates with Incident Management (Phase 9 Doc 17), Runbooks (Phase 9 Doc 18), Disaster Recovery (Phase 4 Doc 35), Observability Practice (Phase 9), and Internal Tooling (Phase 9).

## Teams
SRE, Engineering, Platform/Infrastructure, Engineering Productivity, People/HR (on-call compensation policy)

## Completion Criteria
- [ ] On-call rotation staffed and piloted through at least one full rotation cycle across every Tier 0/Tier 1 service.
- [ ] Toil tracked for one full quarter with a documented automation offset against the toil budget.
- [ ] Production readiness review completed for at least one service prior to tier promotion.
- [ ] First chaos engineering exercise executed and results reviewed against Doc 15 SLOs.
- [ ] Signed off by: Head of SRE (required), VP Engineering (required).
