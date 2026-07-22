# Document 05: Documentation Standards

## Document Name
Documentation Standards

## Purpose
Define the standards for how engineering documentation itself is written and maintained — READMEs, architecture decision records, code-level comments, API reference docs, and runbooks — as distinct from the nine-phase company documentation program this very document is part of. This document governs how the codebase and services document themselves; it does not govern product, company, or GTM documentation.

## Why It Exists
At 100M+ user scale, engineers routinely operate on services they did not build and code they did not write; when documentation is missing or stale, every incident takes longer to diagnose, every new hire takes longer to become productive, and tribal knowledge concentrates in a shrinking set of people who happen to remember. This document exists to make engineering documentation a maintained artifact with an explicit minimum bar, an owner, and a freshness check, rather than an optional afterthought that decays the moment its author moves teams.

## Approximate Page Count
6-8 pages

## Sections
1. **Scope: Engineering Docs vs. Company Documentation Program** — an explicit boundary statement: this document governs in-repo and in-service technical documentation; it is not the nine-phase product/company documentation program defined by the root requirements and is not a substitute for it.
2. **Required Documentation Per Repository/Service** — the minimum bar every repository must meet (README, architecture overview, runbook, ownership contact), scoped against the repository inventory in Repository Strategy (Doc 03).
3. **Architecture Decision Records (ADRs)** — the ADR format, the criteria for when a decision requires one, and how ADRs are stored and cross-linked to the RFC/decision-log process defined in the Engineering Handbook (Doc 01).
4. **Code-Level Documentation Standards** — inline comment expectations, docstring conventions per language, and explicit guidance on what should not be commented (self-evident code, restating the obvious).
5. **API & Interface Documentation** — the requirement to keep interface documentation synchronized with API Contracts (Phase 4, Doc 50), and the tradeoffs between auto-generated and hand-written API docs.
6. **Runbook & Operational Documentation Standards** — the minimum content a runbook must contain, feeding directly into the Reliability & SRE and Oncall & Productivity Metrics groups (Phase 9), which depend on this standard existing before they can require its use.
7. **Documentation Freshness & Ownership** — the staleness-detection mechanism, review cadence, the requirement that every document has a named owner, and the deprecation/archival process for documentation that no longer applies.
8. **Tooling & Storage** — where engineering documentation lives (in-repo markdown versus a wiki), the templates available for each document type, and any automated documentation linting.
9. **Review Requirements for Documentation Changes** — whether and how documentation changes are reviewed, and the expectation that a PR changing behavior updates the relevant documentation in the same PR rather than "as a follow-up."

## Deliverables
- Per-repository documentation minimum-bar checklist
- ADR template with a storage and indexing convention
- Code comment/docstring style guide, specified per language in use
- Runbook template
- Documentation staleness review cadence and per-document ownership policy

## Dependencies
Depends on API Contracts (Phase 4, Doc 50) for interface documentation requirements. Depends on Repository Strategy (Phase 9, Doc 03) for the repository inventory this standard applies across, and on the Engineering Handbook (Phase 9, Doc 01) for the ADR-to-RFC linkage. Is a forward dependency for the Reliability & SRE and Observability Practice groups (Phase 9), which will require the runbook standard defined here. Explicitly distinct from, and not a substitute for, the overall nine-phase documentation program defined in the root requirements.

## Teams
Engineering (all), Platform Engineering, Developer Experience (DevEx), Technical Writing (if constituted)

## Completion Criteria
- [ ] Per-repository documentation minimum-bar checklist applied to every repository in the Repository Strategy inventory.
- [ ] ADR template adopted and at least three ADRs recorded and indexed.
- [ ] Runbook template validated by Reliability & SRE stakeholders as sufficient for oncall use.
- [ ] Documentation freshness/ownership process piloted on at least one repository's full documentation set.
- [ ] Signed off by: VP Engineering (required), Platform Engineering Lead (required).
