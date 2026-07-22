# Document 11: QA Strategy

## Document Name
QA Strategy

## Purpose
Define the role, scope, and operating practice of the dedicated QA function — manual and exploratory testing, release sign-off, and usability/correctness judgment that automated tests cannot supply — as a complement to the automated test pyramid operationalized in Doc 10, with particular emphasis on AI-influenced flows where "is this output good" is a human judgment call, not a pass/fail assertion.

## Why It Exists
Automated testing, however thorough, verifies that the system does what its tests specify; it cannot judge whether a proactive AI suggestion was actually helpful, whether a new flow feels coherent to a first-time user, or whether an edge case nobody thought to write a test for breaks a financial or health journey. A product whose core differentiator is proactive AI behavior generates exactly this category of untestable-by-assertion risk constantly, and without a dedicated QA function empowered to explore, judge, and block a release, that risk goes unchecked until it reaches 100M+ real users. This document exists to give QA an explicit mandate, distinct from — not duplicative of — engineering's automated testing responsibility.

## Approximate Page Count
6-8 pages

## Sections
1. **QA Function Scope & Mandate** — what QA owns (exploratory testing, release sign-off, usability/correctness judgment) versus what remains engineering's responsibility (automated pyramid per Doc 10), with an explicit non-overlap statement.
2. **Exploratory Testing Practice** — how QA engineers conduct structured exploratory testing sessions against new features, especially proactive AI flows with no fixed script to follow.
3. **AI-Influenced Flow Review** — the specific practice for QA judging AI-influenced output quality (proactive suggestions, autonomous actions) where automated golden-set testing per Doc 52/Doc 10 catches regressions but not "is this a good suggestion," including how findings feed back to the AI Platform and Product teams.
4. **Release Sign-Off Process** — the checklist and authority QA exercises before a release ships, what triggers a QA block, and the escalation path when QA and engineering disagree on release readiness.
5. **Financial & Health Flow QA** — elevated QA scrutiny requirements for flows touching money movement or health data, cross-referencing Phase 6 Security/Privacy/Trust requirements for what QA must verify beyond functional correctness.
6. **Bug Triage & Severity Classification** — how QA-discovered issues are classified, prioritized, and routed, and the SLA for each severity tier before it can block a release.
7. **Beta & Staged Rollout QA** — QA's role during staged/canary rollouts (cross-referencing Phase 9 Release Process), including what QA monitors during a rollout window versus what SRE monitors.
8. **QA Tooling & Environments** — the environments and tooling QA operates in, and how they relate to the test environment tiers defined architecturally in Doc 52.
9. **QA Team Structure & Scaling** — how the QA function is staffed and organized against 9+ backend services and a growing feature surface, including the model for embedded vs. centralized QA.

## Deliverables
- Documented QA mandate with explicit boundary against automated testing ownership
- Release sign-off checklist and blocking-authority definition
- AI-influenced flow review rubric and feedback-loop process to AI Platform/Product
- Bug severity classification table with routing and SLA
- Elevated QA checklist for financial and health-data flows

## Dependencies
Requires Testing Strategy (Phase 4 Doc 52) and Testing Strategy Process Layer (Doc 10) to define the automated-testing boundary QA complements. Requires Phase 6 Security/Privacy/Trust documents for financial/health flow scrutiny requirements. Coordinates with Phase 9's Release Process for staged rollout sign-off.

## Teams
QA/Test Engineering, Product, Backend Service Teams, AI Platform Team, Security/Privacy

## Completion Criteria
- [ ] QA mandate explicitly distinguishes QA's scope from automated testing ownership with no overlap or gap.
- [ ] Release sign-off checklist validated against at least one real release cycle.
- [ ] AI-influenced flow review rubric piloted on at least one proactive AI feature with feedback delivered to AI Platform/Product.
- [ ] Elevated QA checklist for financial/health flows reviewed and approved by Security/Privacy.
- [ ] Signed off by: Head of QA (required), VP Engineering (required), Head of Privacy (required for financial/health checklist).
