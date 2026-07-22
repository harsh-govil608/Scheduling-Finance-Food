# Document 01: Engineering Handbook

## Document Name
Engineering Handbook

## Purpose
Define the single onboarding-to-daily-practice reference every engineer uses — how decisions get made, how work gets planned, what "good engineering" looks like at this company — the document every other Phase 9 document is a deep-dive chapter of. It is the front door into all of Engineering Operations, not a restatement of it.

## Why It Exists
At 100M+ user scale with dozens (eventually hundreds) of engineers who never onboarded together and never sat in the same room when a norm was set, unwritten "how we do things here" knowledge stops being shared knowledge and becomes inconsistent per-team folklore — one team's "obviously how reviews work" is another team's surprise. The Handbook exists to be the single canonical answer to "how do we work here," resolving ambiguity by linking out to every specialized Phase 9 document rather than duplicating them, so that no engineering norm exists only in one team's institutional memory.

## Approximate Page Count
8-10 pages

## Sections
1. **How This Handbook Works** — its role as an index/entry point into the rest of Phase 9; the rule that this document orients and points, and specialized documents define; what to do when the Handbook and a deep-dive document appear to conflict.
2. **Engineering Values** — how Phase 1's Guiding Principles Document translates into day-to-day engineering practice (e.g., what "proactive, not reactive" or user-trust principles mean when writing a PR description, scoping a feature, or deciding whether to ship a workaround).
3. **How Decisions Get Made** — the RFC process for architecturally significant decisions, the Architecture Review process, when a decision needs an RFC versus a Slack thread versus a unilateral call, and where decisions get recorded so they are not re-litigated every quarter.
4. **Team Structure & Ownership Model** — how the engineering org is organized (service teams, platform teams, AI/ML teams), how ownership maps to the repository and service boundaries defined in Repository Strategy (Doc 03), and who to go to when ownership is unclear.
5. **The Engineering Lifecycle** — the end-to-end path of a unit of work from planning through build, review, ship, and operate, with each stage pointing to the owning Phase 9 group (Engineering Culture & Standards, Delivery Process, Quality & Testing Practice, Reliability & SRE, Observability Practice, Capacity & DevEx, People & Growth, Oncall & Productivity Metrics).
6. **Onboarding** — first-day, first-week, and first-30/60/90-day expectations for a new engineer, the buddy/mentor assignment, and the minimum set of Phase 9 documents a new hire must read before their first PR merges.
7. **Communication Norms** — sync versus async defaults, meeting culture and meeting-free expectations, the documentation-first bias (pointer to Documentation Standards, Doc 05), and how cross-team questions get routed.
8. **Career & Growth Pointer** — a short pointer into the People & Growth group (Phase 9) so engineers know where leveling, promotion, and feedback norms live rather than duplicating them here.
9. **Standards Directory** — a living index table of every Phase 9 document published to date, one line of description each, kept current as new documents ship.
10. **Handbook Governance** — who owns the Handbook, how often it is reviewed for staleness, and the lightweight process for proposing a change to it.

## Deliverables
- Published Handbook (living document) with an index linking to every other Phase 9 document
- New-engineer onboarding checklist covering first 30/60/90 days
- RFC template and a searchable decision log
- Standards directory table, refreshed on every new Phase 9 document publication

## Dependencies
Requires the Guiding Principles Document (Phase 1). Is the parent index for every other Phase 9 document, including Coding Standards — Practice Layer (Doc 02), Repository Strategy (Doc 03), Git Workflow (Doc 04), Documentation Standards (Doc 05), and forward-referenced groups: Delivery Process, Quality & Testing Practice, Reliability & SRE, Observability Practice, Capacity & DevEx, People & Growth, and Oncall & Productivity Metrics.

## Teams
Engineering (all), VP Engineering, CTO, Engineering Managers

## Completion Criteria
- [ ] Every published Phase 9 document is linked from the Handbook's Standards Directory index.
- [ ] RFC process has been piloted on at least one real architecture decision and the decision recorded in the decision log.
- [ ] Onboarding checklist validated against at least one new-hire cohort's actual first-90-days experience.
- [ ] Handbook governance process (owner, review cadence) is documented and an initial review date is scheduled.
- [ ] Signed off by: VP Engineering (required), CTO (required).
