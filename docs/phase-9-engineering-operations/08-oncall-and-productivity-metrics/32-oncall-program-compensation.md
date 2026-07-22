# Document 32: On-Call Program & Compensation

## Document Name
On-Call Program & Compensation

## Purpose
Define the sustainable on-call program — rotation fairness, compensation for on-call burden, and load-shedding practices — that keeps SRE Practice (Phase 9 Doc 16) and Incident Management (Phase 9 Doc 17) staffed by engineers who are not burning out. This document specifies what the eventual on-call program document must contain: rotation design, pay/comp-time mechanics, escalation relief, and the fairness metrics used to audit the program over time.

## Why It Exists
A reliability program that runs on unsustainable on-call burden eventually loses the engineers who best understand the system, right when that expertise matters most; without an explicit compensation/fairness program, on-call quietly becomes a retention risk rather than a reliability asset. At 100M+ user, multi-region scale, on-call load is not evenly distributed by default — some services and some time zones absorb disproportionate pages — so the program must be designed deliberately rather than left to emerge from whichever team happens to own the noisiest service. This document exists because the original Phase 9 scope covered incident response and SRE practice in depth but never specified who bears the cost of carrying the pager, how that cost is compensated, or how fairness is measured, leaving a gap between "we expect 24/7 coverage" and "we have committed to making that coverage sustainable."

## Approximate Page Count
6-8 pages

## Sections
1. **Rotation Design & Fairness** — rotation size, shift length, and frequency targets per service tier, calibrated to keep individual on-call burden (pages per shift, night wake-ups per week) within sustainable bounds, with minimum rotation-size thresholds before a service is allowed to go on-call at all.
2. **On-Call Compensation Model** — the framework for compensating on-call time (stipend, hourly differential, or comp-time banking), how compensation scales with severity/frequency of pages actually received versus passive availability, and how the model stays consistent across regions with different labor norms and laws.
3. **Load-Shedding & Escalation Practice** — how an overloaded or fatigued on-call engineer gets relief mid-shift, including secondary/shadow on-call activation, manager-initiated rotation swaps, and a no-blame path for saying "I need backup."
4. **Follow-the-Sun & Multi-Region Rotation Structure** — how rotations are structured across time zones to minimize night-shift pages given the multi-region user base, including handoff protocol between regional on-call engineers.
5. **Eligibility, Onboarding & Shadow Rotations** — criteria for when a new engineer is ready to join a primary rotation, the mandatory shadow period, and how training debt is tracked before someone carries the pager solo.
6. **Fairness & Burden Auditing** — the metrics (pages per engineer per month, off-hours pages, distribution variance across the rotation) reviewed on a recurring cadence to detect and correct emerging imbalance.
7. **Opt-Out, Accommodation & Exemption Policy** — the process for temporary or permanent on-call exemptions (medical, caregiving, tenure-based) and how coverage is backfilled without penalizing remaining rotation members.
8. **Post-Shift Recovery & Workload Adjustment** — guaranteed recovery time after high-load shifts (comp-time, next-day flexibility, sprint workload reduction) so on-call burden does not silently compound into sprint commitments.
9. **Compensation Transparency & Review Cadence** — how the compensation model itself is communicated to engineers and how often it is reviewed against actual pager-load data and market benchmarks.

## Deliverables
- On-call rotation design standard covering rotation size, shift length, and multi-region handoff structure.
- On-call compensation framework (stipend/differential/comp-time model) applicable across regions.
- Load-shedding and escalation runbook for mid-shift relief.
- Fairness/burden dashboard specification (pages per engineer, off-hours distribution).
- Opt-out and accommodation policy with backfill process.
- Onboarding checklist and shadow-rotation criteria for new on-call engineers.

## Dependencies
Requires SRE Practice (Phase 9), Incident Management (Phase 9), Observability Practice (Phase 9), Engineering Career Ladder (Phase 9, People & Growth group), Engineering Culture & Standards (Phase 9 Doc 01).

## Teams
SRE, Engineering Leadership, People/HR, Legal & Compliance, Finance, Service-Owning Engineering Teams

## Completion Criteria
- [ ] Rotation fairness reviewed against real on-call load data once available.
- [ ] Compensation model validated against labor law in each operating region.
- [ ] Load-shedding and escalation runbook dry-run tested with at least one live rotation.
- [ ] Fairness/burden dashboard reviewed at defined cadence with corrective-action process defined.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required), Head of People/HR (required once hired), Legal/Compliance lead (required for compensation and regional labor terms).
