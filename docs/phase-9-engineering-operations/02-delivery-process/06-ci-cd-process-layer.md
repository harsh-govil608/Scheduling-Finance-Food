# Document 06: CI/CD Operating Practice

## Document Name
CI/CD Operating Practice

## Purpose
Define the day-to-day operational practice of using the CI/CD pipeline described architecturally in Phase 4 Doc 30 — who is authorized to merge and to which branches, which automated gates are hard-blocking versus override-able and under what conditions, and how pipeline failures get triaged and resolved. This document specifies the human process wrapped around the pipeline, not the pipeline stages or infrastructure themselves.

## Why It Exists
Phase 4 Doc 30 guarantees that "passed CI/CD" means the same fixed set of checks regardless of which of the 9 services produced the build, but an architecture document cannot by itself determine who is allowed to click merge, who can override a blocking gate at 11pm during an incident, or how a flaky integration test gets triaged before it silently becomes something every engineer routes around. Without an operating practice layer, gate enforcement drifts team by team — one service's engineers treat a red pipeline as blocking, another's treat it as advisory — which quietly erodes the exact platform-wide quality guarantee the architecture was built to provide. This document exists to make gate enforcement, override authority, and failure response consistent and accountable across every team touching the pipeline.

## Approximate Page Count
6-8 pages.

## Sections
1. **Merge Authorization & Review Requirements** — who can approve and merge pull requests, required reviewer count and expertise per service tier, and elevated review requirements for code touching financial, health, or autonomous-action logic.
2. **Gate Enforcement Policy** — which of Doc 30's automated quality gates are strictly blocking versus override-able in defined circumstances, and the standard each gate enforces in practice.
3. **Pipeline Failure Triage Process** — the rotation responsible for diagnosing red pipelines, the flaky-test quarantine workflow, and time-to-resolution targets before a failing check is treated as a platform-wide blocker.
4. **Override & Exception Approval** — who is authorized to bypass a blocking gate in an emergency, the required written justification, and the mandatory audit log entry and post-hoc review.
5. **Cross-Service Pipeline Incident Response** — the process for when a shared pipeline template, shared build infrastructure, or shared runner capacity failure blocks multiple of the 9 services simultaneously.
6. **Merge Freeze Governance** — who can declare a merge freeze (pre-major-release, active incident, security response), its scope, and the criteria to lift it.
7. **Pipeline Metrics & Health Reporting** — the recurring cadence at which pipeline health metrics (lead time, failure rate, mean time to green) are reviewed, and by whom.
8. **New Service Onboarding to the Shared Pipeline** — the practical checklist a new service team follows to adopt the Doc 30 pipeline template, and who signs off that onboarding is complete.
9. **Continuous Improvement & Pipeline Debt Review** — the periodic review process for retiring outdated pipeline stages and updating thresholds as the platform and team count grow.

## Deliverables
* Merge authorization matrix by service tier and code sensitivity.
* Gate override approval workflow with mandatory audit log requirement.
* Pipeline failure triage runbook and on-call rotation model.
* Merge freeze declaration and lift procedure.
* Pipeline health reporting cadence and required dashboard.

## Dependencies
Requires CI/CD (Phase 4 Doc 30). Coordinates closely with Release Management (Phase 9 Doc 07) and Feature Flag Governance (Phase 9 Doc 08). Depends on Engineering Handbook (Phase 9 Doc 01) for baseline code review norms. Coordinates with the Quality & Testing process documents (Phase 9 group 03).

## Teams
Platform/Infrastructure, Engineering (all service teams), SRE, QA, Security.

## Completion Criteria
- [ ] Merge authorization matrix ratified for all 9 services, including sensitive-domain overlays.
- [ ] Gate override workflow piloted on at least one real emergency exception with a complete audit trail.
- [ ] Pipeline failure triage runbook validated against a real historical red-pipeline incident.
- [ ] Merge freeze process tested in at least one dry run.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required), Head of QA (required).
