# Document 26: Developer Experience

## Document Name
Developer Experience

## Purpose
Define the dedicated practice of measuring and improving internal engineer productivity — local dev environment speed, build times, CI feedback latency, and PR review turnaround — treating engineers as internal customers with their own product requirements. This document specifies what a Developer Experience (DevEx) function measures, how it prioritizes friction against the feature roadmap, and how it is staffed and held accountable for measurable improvement over time.

## Why It Exists
Every hour lost to a slow build, a flaky local environment, or a PR stuck waiting for review is an hour not spent advancing the mission of an AI that proactively manages a user's life; at the scale this documentation program targets — dozens of engineers today, hundreds as the company grows toward supporting 100M+ users — Developer Experience debt compounds silently unless someone owns measuring and fixing it deliberately. Without a named practice, DevEx problems get treated as individual annoyances each engineer works around privately, and the productivity loss never becomes visible enough to justify fixing at the root. This document exists to convert engineer productivity from a background complaint into an owned, measured, continuously improved internal product.

## Approximate Page Count
7-9 pages

## Sections
1. **DevEx Metrics** — build time, local environment setup time, PR-to-merge latency, and how each is instrumented and tracked over time.
2. **Local Development Environment Standard** — the target experience for spinning up the full multi-service stack locally, including the maximum acceptable time-to-running and the supported operating environments.
3. **Golden Path Tooling** — the supported, opinionated tooling path (languages, package managers, local orchestration) versus unsupported alternatives, and the support boundary the DevEx team commits to.
4. **Build & CI Feedback Loop Speed** — the target time from commit to first CI signal, and the practice for defending that target as the codebase, service count, and test suite grow.
5. **PR Review Experience** — norms for review turnaround time, reviewer load balancing across teams, and how a PR that has stalled past a defined threshold is surfaced rather than silently aging.
6. **Onboarding Time-to-First-Commit** — the measured target for how long a new engineer takes to land their first production change, and the checklist and tooling that get them there.
7. **DevEx Feedback Channel** — the standing mechanism (periodic survey plus an always-open feedback channel) through which engineers report friction, and how reported friction is triaged into the DevEx team's backlog.
8. **Boundary with Internal Tooling** — where DevEx's environment, build, and workflow-speed responsibilities end and Internal Tooling's (Doc 27) product-specific internal tools begin, so ownership of a given tool is never ambiguous.
9. **DevEx Team Charter & Staffing Model** — how the dedicated DevEx function is scoped, staffed, and prioritized against feature-team roadmaps, including the point at which a dedicated Head of Developer Experience is hired.
10. **Continuous Improvement Cadence** — the recurring review of DevEx metrics against targets, how regressions are triaged and prioritized, and how before/after impact of a DevEx investment is demonstrated.

## Deliverables
* DevEx metrics dashboard definition, covering build time, environment setup time, and PR-to-merge latency
* Local development environment standard specification with a maximum time-to-running target
* Golden path tooling list, with unsupported alternatives explicitly named
* Onboarding time-to-first-commit checklist and target duration
* DevEx feedback intake and triage process, with a defined backlog and prioritization method
* DevEx team charter defining scope, staffing model, and reporting line

## Dependencies
Requires the Repository Strategy and CI/CD Process Layer documents (Phase 9, delivery-process group) for the build and pipeline mechanics DevEx measures against, and Internal Tooling (Phase 9, Doc 27) for the ownership boundary between environment/workflow tooling and product-specific internal tools. Cross-references Testing Strategy (Process Layer) (Phase 9, Doc 10) for how test suite runtime factors into CI feedback loop speed, and CI/CD (Phase 4, Doc 30) for the underlying pipeline architecture DevEx targets are measured against.

## Teams
Developer Experience (DevEx), Platform Engineering, Engineering Leadership, Backend Service Teams, People/Talent (for onboarding coordination)

## Completion Criteria
- [ ] DevEx metrics baseline established with a documented before/after comparison methodology.
- [ ] Local development environment standard validated by at least one engineer completing a from-scratch setup within the target time.
- [ ] Golden path tooling list published with explicit support boundaries for unsupported alternatives.
- [ ] Onboarding time-to-first-commit target validated against at least one new-hire cohort.
- [ ] DevEx feedback channel is live with a documented triage process and backlog.
- [ ] Signed off by: VP Engineering (required), Head of Developer Experience (required once hired).
