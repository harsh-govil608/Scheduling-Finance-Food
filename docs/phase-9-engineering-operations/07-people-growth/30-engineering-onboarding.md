# Document 30: Engineering Onboarding

## Document Name
Engineering Onboarding

## Purpose
Define the structured program that takes a newly hired engineer from signed offer to fully productive team member — covering environment setup, architectural orientation across the system's 9+ backend services, and a 30/60/90-day path with explicit milestones. This document is distinct from Phase 3's Onboarding PRD (Doc 40), which specifies the end-user's first-run product experience; this one specifies the internal engineer's first-run experience of the company and codebase.

## Why It Exists
The system spans five architecture-heavy phases of documentation (technical architecture, AI/ML systems, security/privacy/trust, design system, and engineering operations itself) plus 9+ independently owned backend services, which makes ad hoc "shadow a teammate and figure it out" onboarding untenable past the first dozen hires. A new engineer who takes three months to become productive instead of three weeks is a compounding cost at hundreds-of-engineers scale, and inconsistent onboarding also means inconsistent absorption of the Guiding Principles that are supposed to govern autonomous decision-making. This document exists to make ramp-up time predictable, measurable, and independent of which team happens to hire someone.

## Approximate Page Count
7-9 pages

## Sections
1. **Pre-Day-One Preparation** — what's provisioned before the engineer's start date (accounts, hardware, access requests) so day one isn't lost to logistics.
2. **Day One & Week One** — environment setup checklist, first commit/first deploy goal, and introduction to the Guiding Principles Document as required first reading.
3. **Architectural Orientation Path** — a guided reading and hands-on sequence through the technical architecture, AI/ML systems, and security/privacy/trust phases, scoped by the engineer's team rather than requiring all five phases in full depth.
4. **30-Day Milestones** — expected state at 30 days: environment fluency, first small shipped change, assigned onboarding buddy/mentor relationship established.
5. **60-Day Milestones** — expected state at 60 days: ownership of a first non-trivial task or small project, working knowledge of the services the engineer's team directly integrates with.
6. **90-Day Milestones** — expected state at 90 days: full on-call readiness (where applicable, coordinating with the On-Call practice in Phase 9), independent contribution at the level implied by the engineer's assigned rung on the Career Ladder (Phase 9 Doc 28).
7. **Buddy & Mentor Program** — how onboarding buddies are assigned, what's expected of them, and how that time is protected against normal delivery pressure.
8. **Specialized Onboarding Tracks** — variations for AI/ML engineers, SRE/reliability engineers, and security engineers, whose ramp paths emphasize different parts of the documentation set.
9. **Onboarding Feedback Loop** — how new-engineer feedback on the onboarding program itself is collected and used to keep the program current as the architecture evolves.
10. **Manager Checklist & Accountability** — the manager-facing checklist ensuring onboarding steps aren't skipped under delivery pressure, and who is accountable if a new hire is left to sink or swim.

## Deliverables
- Day-one through 90-day milestone checklist, role- and team-parameterized
- Architectural reading/orientation path indexed to specific Phase 4/5/6 documents, scoped by team
- Buddy program definition with assignment process and time-allocation expectation
- Specialized onboarding track variants for AI/ML, SRE, and security roles
- Onboarding feedback survey and revision cadence

## Dependencies
Requires Engineering Career Ladder (Phase 9 Doc 28) to frame expected independent-contribution level by day 90, and Guiding Principles Document (Phase 1) as required first reading. Draws on Hiring Standards (Phase 9 Doc 29) for the gap assessment carried over from the interview loop. References the technical architecture (Phase 4), AI/ML systems (Phase 5), and security/privacy/trust (Phase 6) documentation sets as the orientation reading path, and Knowledge Management (Phase 9 Doc 31) for how a new engineer discovers documentation beyond the guided path. Distinct from and not to be merged with the user-facing Onboarding PRD (Phase 3 Doc 40).

## Teams
Engineering Leadership, Head of People/HR, Engineering Managers, Onboarding Buddies/Mentors, Platform Engineering (environment tooling)

## Completion Criteria
- [ ] 30/60/90-day milestones piloted with at least two real new-hire cohorts before final sign-off.
- [ ] Architectural orientation path validated by at least one lead from each of the technical architecture, AI/ML, and security phases.
- [ ] Buddy program time allocation approved by engineering leadership as protected (non-billable-to-delivery) time.
- [ ] Onboarding feedback loop produced at least one documented revision to the program.
- [ ] Signed off by: VP Engineering (required), Head of People/HR (required once hired).
