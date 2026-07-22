# Document 22: Premium UX

## Document Name
Premium UX

## Purpose
Define the visual and interaction design of premium-tier differentiation and upgrade moments — how a gated capability is visually indicated, how and where an upgrade prompt is presented, and how the tier/entitlement summary screen is laid out — implementing the felt free/premium contrast described in Phase 2's Premium Experience document (Doc 38) and the concrete entitlement and gating feature committed to in Phase 3's Premium Features PRD (Doc 43). This document specifies what the premium visual design must contain and achieve; it does not set price points, packaging, or final pixel-level mockups.

## Why It Exists
The upgrade moment is the single design surface with the highest potential to violate the product's own stated principles: Phase 1's Guiding Principles Document commits the product to an anti-dark-pattern stance, and Phase 2's Premium Experience document depends on every upgrade moment reading as a natural extension of realized value rather than an interruption, guilt trip, or manufactured scarcity — but philosophy and PRD requirements alone cannot guarantee a visual designer, months later and under growth pressure, will not reach for a countdown timer, a disguised close button, or shaming copy to lift conversion. Because Productivity, Finance, and Health are experienced as one assistant, a premium visual treatment that looks or feels different across pillars would fracture that unity just as surely as inconsistent gating logic would. This document exists so the upgrade moment is designed once, deliberately, against the anti-dark-pattern constraint and the free-tier completeness bar already committed to upstream, rather than left to per-team interpretation under conversion pressure.

## Approximate Page Count
7-9 pages.

## Sections
1. **Gated Capability Visual Indication** — the visual requirements for showing a user that a capability is premium-gated before they attempt to use it, so a gate is never discovered as a silent failure or an after-the-fact error.
2. **Upgrade Moment Visual Design & Placement** — the layout and placement requirements for the upgrade prompt itself, directly implementing the tone and trigger rules defined in the Premium Experience document's "Upgrade Moment" section.
3. **Tier & Entitlement Summary Screen** — the layout requirements for the single screen where a user can see their current tier and what it unlocks, per the Premium Features PRD.
4. **Anti-Dark-Pattern Visual Compliance Checklist** — an explicit list of visual patterns that are forbidden in any upgrade surface (manufactured countdowns, disguised or hidden dismiss controls, guilt- or shame-framed copy treatment, false scarcity indicators), checked directly against the Guiding Principles Document.
5. **Free-Tier Visual Completeness** — the visual requirements confirming the free tier never looks deliberately hobbled, nagged, or incomplete, consistent with the Premium Experience document's Free Tier Completeness Bar.
6. **Cross-Pillar Visual Consistency for Premium** — the method and requirements for verifying the premium visual treatment feels like one coherent upgrade of one assistant across Productivity, Finance, and Health surfaces, not three independently gated apps.
7. **Downgrade & Cancellation Visual Experience** — the visual and tonal requirements for the downgrade/cancellation flow, ensuring the assistant reads as narrower, never resentful, punitive, or degraded in tone.
8. **Motion & Micro-interaction for Upgrade Moments** — the transition and animation requirements ensuring an upgrade prompt's motion reinforces "natural extension of value" rather than interruption or urgency.
9. **Accessibility & Responsive Behavior for Premium Surfaces** — the baseline accessibility and responsive-layout requirements for gated-capability indicators, upgrade prompts, and the entitlement summary screen.

## Deliverables
* Approved Premium UX document.
* Gated-capability visual-indicator specification, mapped to the Premium Features PRD's capability-gating map.
* Upgrade-moment visual specification and placement rules for at least one hypothetical trigger per pillar.
* Tier & entitlement summary screen specification.
* A single-page Anti-Dark-Pattern Visual Compliance Checklist usable by any designer to self-review an upgrade surface before handoff.

## Dependencies
Requires Premium Experience (Phase 2, Doc 38) and Premium Features PRD (Phase 3, Doc 43) as the behavioral and functional source of truth for what this document may visually express. Requires the Guiding Principles Document (Phase 1) for the anti-dark-pattern stance every upgrade moment is checked against. Requires Onboarding UX (Phase 7, Doc 21) for tonal and visual consistency between the first-run and upgrade-moment surfaces. Requires the Design Foundations documents and Component Library (Phase 7, Design System sections) for tokens, type, and reusable components applied here.

## Teams
Design, Product, Growth, Engineering, Content/Copy, Trust & Safety, QA.

## Completion Criteria
- [ ] Every upgrade-moment visual has been checked against the Anti-Dark-Pattern Visual Compliance Checklist with zero unresolved violations.
- [ ] Gated-capability visual indicators verified against the Premium Features PRD's capability-gating map, with no capability left ambiguously or silently gated.
- [ ] Free-tier visual treatment audited to confirm no element implies deliberate hobbling, nagging, or artificial incompleteness.
- [ ] Downgrade/cancellation visuals confirmed non-punitive in tone, layout, and copy framing.
- [ ] Cross-pillar consistency check performed and documented across Productivity, Finance, and Health surfaces.
- [ ] Signed off by: Head of Design (required), Head of Product (required), Trust & Safety Lead (required).
