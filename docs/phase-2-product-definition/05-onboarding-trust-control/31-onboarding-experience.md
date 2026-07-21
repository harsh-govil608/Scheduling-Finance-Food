# Document 31: Onboarding Experience

## Document Name
Onboarding Experience

## Purpose

Define the end-to-end first-run experience of the product — from install to first meaningful value — across all three pillars, including how permission requests are sequenced, how initial trust with the AI is established, and what "time to first value" means and must be measured against for Productivity, Finance, and Health.

## Why It Exists

The product's core promise is that it behaves as one intelligent assistant, not three separate apps — but onboarding is the single moment where that promise is most fragile: it is the first time the user meets the AI, the first time sensitive permissions are asked for, and the point at which the Proactivity Ladder must start at its lowest, most conservative rung. Without a unified onboarding specification, each pillar team will build its own first-run flow, permissions will likely be front-loaded into a wall of requests before any value is shown (the fastest way to trigger denial and distrust), and "time to first value" will silently drift from a target into an afterthought. This document exists so onboarding is engineered once, deliberately, as a trust-building sequence rather than a checklist of setup screens.

## Approximate Page Count

7-9 pages.

## Sections

1. **Onboarding Goals & Success Definition** — what onboarding must accomplish (first value delivered, first permission granted with understanding, first trust established) and how "successful onboarding" is defined in product terms, not just completion rate.
2. **First-Run Sequence (High-Level Flow)** — the screen-by-screen shape of the first session, described as a requirements-level flow rather than final UI, showing where the three pillars are introduced.
3. **Permission-Request Sequencing Strategy** — the rules for when each sensitive permission is asked for relative to value delivery (just-in-time vs. upfront), cross-referenced to the Permissions & Consent UX document for the request/consent experience itself.
4. **Time-to-First-Value Targets per Pillar** — the specific, falsifiable definition of "first value" for Productivity, Finance, and Health respectively, and the maximum acceptable time/steps to reach each.
5. **Progressive Disclosure of the Three Pillars** — how onboarding introduces Productivity, Finance, and Health as facets of one assistant rather than three separate setup wizards, including sequencing and pacing rules.
6. **Initial Trust Calibration & Proactivity Ladder Starting Point** — confirms every new user starts at the lowest rung of the Proactivity Ladder by default, and defines what onboarding behavior is allowed to look like at that rung (observation and passive surfacing only, no autonomous action).
7. **Skippable vs. Required Steps** — which onboarding steps a user may defer or skip entirely, and what degraded (but still functional) experience results from skipping each.
8. **Returning-User Re-Onboarding** — what onboarding-like experience is triggered after a long absence, a permission revocation, a device change, or an app reinstall, and how it differs from first-run onboarding.

## Deliverables

* Approved Onboarding Experience document.
* A first-run flow diagram at requirements level (screen sequence and decision points, not final UI).
* A time-to-first-value target sheet with one falsifiable target per pillar.
* A skip/defer matrix listing every skippable step and its resulting degraded state.

## Dependencies

Requires the Vision & Mission Document and Product (Behavioral) Philosophy Document (Phase 1) for the Proactivity Ladder starting point and the "never overwhelm" constraint on first-run pacing; requires the Product Pillars Overview (Document 02) for how the three pillars are framed as one assistant; requires the User Personas Document (Phase 1) for persona-specific onboarding needs; hands off directly into the Permissions & Consent UX document (Document 32) for the mechanics of each permission ask.

## Which Teams Use This

Product, Design, Content/Copy, Growth, Data Science/ML, Trust & Safety.

## Completion Criteria

- [ ] Every one of the three pillars has a documented, falsifiable "time to first value" target with a defined measurement method.
- [ ] The first-run sequence has been validated to start at the lowest Proactivity Ladder rung with no autonomous action taken before explicit confirmation.
- [ ] Every skippable step has a documented degraded-state behavior with no dead ends.
- [ ] Re-onboarding triggers (long absence, permission revocation, device change, reinstall) are each explicitly covered.
- [ ] Confirmed no engineering, AI/ML implementation, or backend detail has leaked into this document.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
