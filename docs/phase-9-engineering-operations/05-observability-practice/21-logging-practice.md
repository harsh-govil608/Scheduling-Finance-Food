# Document 21: Logging Practice

## Document Name
Logging Practice

## Purpose
Define the day-to-day discipline of writing, reading, and querying structured logs across engineering teams — log-level usage conventions, query practice during live incidents, and log hygiene review — built on top of the Logging architecture defined in Phase 4 (Doc 32). This document specifies requirements for the eventual Logging Practice document, not the logging pipeline itself.

## Why It Exists
Phase 4's Logging architecture (Doc 32) defines how logs are structured, shipped, and stored at 100M+ user scale, but a technically sound pipeline still produces useless logs if engineers log inconsistently — mislabeling severities, omitting correlation identifiers, or dumping sensitive data into free-text fields. This document exists so that logs remain trustworthy and fast to query under incident pressure: the moment an on-call engineer needs to find the one log line that explains a cascading failure across dozens of proactive-action microservices, the practice defined here is what determines whether that search takes ninety seconds or ninety minutes.

## Approximate Page Count
6-8 pages

## Sections
1. **Scope and Relationship to Phase 4 Logging** — clarifies this document governs how engineers use the logging system day-to-day; Doc 32 owns pipeline architecture, retention, and storage tiering.
2. **Log-Level Discipline** — concrete, enforced conventions for DEBUG/INFO/WARN/ERROR/CRITICAL usage, including examples of correct and incorrect classification for common event types.
3. **Structured Field Conventions in Practice** — practice-level requirements for correlation IDs, user/session context, and region tags being present on every log line emitted from proactive-action and event-driven code paths.
4. **Query Practice During Incidents** — the expected query workflow (query builder templates, saved queries per service) for locating relevant logs quickly during live incident response.
5. **Sensitive Data Discipline** — the reviewer-facing practice (not the architecture) for catching PII or secrets before they land in logs, including pre-merge review expectations for log statements.
6. **Log Review Cadence** — periodic review of noisy or low-value log statements by owning teams, with a bar for what justifies a log line's continued existence.
7. **Cross-Team Log Correlation Practice** — how engineers trace a single user action or event across multiple services' logs using shared identifiers, and the escalation path when correlation breaks down.
8. **Logging in Postmortems** — the expectation that postmortems cite specific log evidence, and the practice for attaching log excerpts to incident records.
9. **New Engineer Logging Onboarding** — the training and shadowing practice for new hires to reach fluency in the org's query tooling and conventions within their first weeks.
10. **Logging Practice Audit** — periodic sampling of production log output to check compliance with level discipline and field conventions, with findings routed back to owning teams.

## Deliverables
- Log-level usage guide with worked examples per event type.
- Required structured-field checklist for proactive-action and event-driven services.
- Incident-response query playbook with saved/templated queries per major service family.
- Sensitive-data pre-merge review checklist for log statements.
- Quarterly log-quality audit report template.

## Dependencies
Requires Logging (Phase 4 Doc 32), Observability (Phase 4 Doc 31), Monitoring Practice (Phase 9 Doc 20), Incident Management (Phase 9), Data Privacy and Handling Standards (Phase 6).

## Teams
SRE, Platform Engineering, Service Owning Teams, Security and Privacy, On-Call Engineers, Engineering Enablement

## Completion Criteria
- [ ] Log-level conventions validated against a sample of production log output across at least three service families.
- [ ] Incident-response query playbook tested during a live or simulated incident.
- [ ] Sensitive-data review checklist adopted into standard code review process.
- [ ] At least one full quarterly log-quality audit completed with findings actioned.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required), Chief Privacy Officer (required for sensitive-data sections).
