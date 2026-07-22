# Document 14: Customer Success

## Document Name
Customer Success

## Purpose
Define the customer success function's mandate — proactively helping users get value from the product (not just reactively fixing problems), especially in the critical first-weeks period where the Proactivity Ladder's trust-building matters most. It specifies who is watched, what signals trigger intervention, and what the intervention playbooks contain, distinct from the reactive Support Operations function defined in Doc 15.

## Why It Exists
A product whose core value compounds with trust and history (the AI gets better and more proactive the longer it knows a user) has an unusually high early-churn cost — losing a user in week 2 loses far more long-term value than losing one in month 8, because the Proactivity Ladder never had the chance to climb. Customer Success exists to protect that early period deliberately, rather than leaving early trust-building to chance or to Support, which by design only engages once a user has already hit a problem and asked for help.

## Approximate Page Count
6-8 pages

## Sections
1. **CS Mandate & Scope** — proactive value-realization versus reactive support, explicitly distinguished from Support Operations (Doc 15), including where the two functions hand off to each other.
2. **At-Risk User Identification** — the behavioral and usage signals indicating a user is likely to churn before value has compounded (e.g., stalled onboarding, permission grants without follow-through, Proactivity Ladder stagnation), tied to the Phase 1 Success Metrics Document and the Phase 3 Onboarding PRD's time-to-first-value instrumentation.
3. **Lifecycle Segmentation** — how the user base is segmented by lifecycle stage (first-week, first-month, established, at-risk, lapsed) and how CS priorities differ by segment.
4. **Intervention Playbooks** — what CS does when an at-risk signal fires: outreach channel, message content boundaries (never referencing specific financial/health data without explicit context), timing, and escalation to a human touch when automated nudges underperform.
5. **High-Touch vs. Tech-Touch Model** — which user segments receive human CS attention versus automated/in-product nudges, and the criteria (plan tier, risk severity, life-stage sensitivity) that decide which model applies.
6. **CS Metrics & Health Score** — the composite health score used to prioritize CS attention, its inputs, and how it's validated against actual retention outcomes over time.
7. **Cross-Pillar Coordination** — how CS handles a user at risk across multiple pillars simultaneously (e.g., stalled in both Finance and Health setup) without duplicating or conflicting outreach.
8. **Relationship to Community & Support** — the explicit boundary with Community (Doc 13, peer-driven and optional) and Support (Doc 15, reactive and ticket-driven), so a user's need is never dropped between functions.
9. **Privacy-Safe Outreach Standards** — the rules governing what CS staff can see and reference about a user's financial/health data during proactive outreach, consistent with Phase 6 data access controls.
10. **CS Tooling & Staffing Plan** — the systems (health-score dashboard, playbook automation, CRM) and staffing model required to run the function at scale, phased against expected user growth.

## Deliverables
- CS Mandate document with an explicit, non-overlapping boundary against Support Operations.
- A validated (initially hypothesis-based) at-risk signal model tied to Success Metrics instrumentation.
- A library of intervention playbooks, one per major at-risk signal, including message-content guardrails.
- A CS health score specification (inputs, weighting rationale, review cadence).
- A privacy-safe outreach standard for CS staff data access.

## Dependencies
Requires Success Metrics Document (Phase 1), Onboarding PRD (Phase 3, Doc 40), Premium Experience / Proactivity Ladder definitions (Phase 2), Privacy Architecture and Data Classification (Phase 6, Docs 13-14). Coordinates with Community (Phase 8, Doc 13) and Support (Phase 8, Doc 15); feeds Retention (Phase 8).

## Teams
Customer Success, Product, Data/Analytics, Growth, Trust & Safety

## Completion Criteria
- [ ] At-risk signals validated against real cohort data once available (hypothesis-stage sign-off acceptable pre-launch).
- [ ] Intervention playbooks reviewed for privacy-safe language by Trust & Safety/Legal.
- [ ] Boundary with Support Operations (Doc 15) and Community (Doc 13) confirmed non-overlapping by all three document owners.
- [ ] CS health score formula reviewed by Data/Analytics for statistical soundness.
- [ ] Signed off by: CEO (required), Head of Customer Success (required once hired), Head of Product (required).
