# Document 06: Unit Economics

## Document Name
Unit Economics

## Purpose
Define the umbrella framework for evaluating whether the company makes or loses money on each incremental user — the contribution margin structure, the payback period model, and the set of shared assumptions (cost buckets, time windows, cohort definitions) that CAC (Doc 07) and LTV (Doc 08) must both be built on. This document does not compute final unit economics figures; it specifies the methodology and formulas so that CAC and LTV are calculated consistently rather than each inventing its own logic.

## Why It Exists
CAC and LTV are meaningless in isolation and dangerous when calculated with inconsistent assumptions — a common failure mode is a growth team calculating CAC on a 30-day blended basis while finance calculates LTV on a 36-month persona-specific basis, producing an LTV:CAC ratio that looks healthy but rests on numbers that were never comparable. This document exists to fix the shared skeleton first — what counts as a cost, what counts as revenue, what the standard time windows are, how a "user" is defined for economics purposes (free vs. trial vs. paid, individual vs. household) — so that every downstream metric in Phase 8 is measuring the same thing. It is the reconciliation layer between the Business Model's unit economics framework (Phase 8, Doc 01) and the specific, calculable metrics that follow it.

## Approximate Page Count
7-9 pages

## Sections
1. **Unit Definition** — what constitutes one "unit" for economics purposes (a single user, a household plan, a persona-segmented cohort) and how free, trial, and paid states are treated differently.
2. **Contribution Margin Model** — the formula for per-user contribution margin (revenue minus variable cost-to-serve, including AI inference cost, infrastructure cost, and support cost per active user), and which Phase 4/5 architecture decisions most affect it.
3. **Cost-to-Serve Taxonomy** — the categories of variable cost incurred per user (model inference calls, storage, third-party integration fees, human-in-the-loop escalation cost per Phase 5 Doc 25) and how each is attributed.
4. **Payback Period Methodology** — the formula and standard time window for calculating how long it takes contribution margin to recover acquisition cost, and the target payback thresholds by persona tier.
5. **Standard Time Windows & Cohorting Rules** — the shared conventions (e.g., 30/90/365-day cohorts, rolling vs. fixed windows) that every unit-economics-adjacent document in Phase 8 must use for comparability.
6. **Free-to-Paid Conversion Economics** — how the freemium tier's cost-to-serve is modeled and amortized against eventual conversion, given the Premium Experience/Subscription structure defined in Phase 8 Docs 01-04.
7. **Blended vs. Segmented Reporting** — the rule for when unit economics should be reported blended across all users versus segmented by acquisition channel, persona, or geography.
8. **Sensitivity & Break-Even Analysis Framework** — the methodology (not final numbers) for stress-testing contribution margin against changes in inference cost, support load, or pricing.
9. **Reporting Cadence & Ownership** — how often unit economics are recalculated, by whom, and how they feed into board/investor reporting.

## Deliverables
- A contribution margin formula with a fully defined cost-to-serve taxonomy.
- A payback period model with standard time windows and target thresholds by persona tier.
- A cohorting and reporting-window standard adopted as the shared convention for CAC (Doc 07) and LTV (Doc 08).
- A sensitivity/break-even analysis framework (methodology only, no final figures).
- A reporting cadence and ownership assignment for recurring unit economics review.

## Dependencies
Requires Business Model (Phase 8, Doc 01), Pricing Strategy (Phase 8, Doc 02), Subscription Plans (Phase 8, Doc 03), Technical Architecture Overview (Phase 4), AI Platform Overview and inference cost drivers (Phase 5, Doc 01), Human-in-the-Loop Escalation Architecture (Phase 5, Doc 25). Feeds directly into CAC (Doc 07) and LTV (Doc 08).

## Teams
Executive, Finance, Product, Data/Analytics, Engineering

## Completion Criteria
- [ ] Contribution margin formula reviewed and validated against actual Phase 4/5 infrastructure cost drivers by Engineering.
- [ ] Cost-to-serve taxonomy cross-checked for completeness against every variable cost category identified in Phase 4 and Phase 5.
- [ ] Standard time windows and cohorting rules formally adopted as the shared convention for all subsequent Phase 8 metrics documents.
- [ ] Payback period thresholds reviewed against persona tiers defined in the Phase 1 User Personas Document.
- [ ] Sensitivity analysis framework stress-tested against at least one adverse scenario (e.g., inference cost increase) by Finance.
- [ ] Signed off by: CEO (required), CFO/Finance Lead (required once hired), Head of Product (required).
