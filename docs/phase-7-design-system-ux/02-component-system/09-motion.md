# Document 09: Motion

## Document Name
Motion

## Purpose

Define the general-purpose motion system — the standardized timing scale, easing curves, and transition types used whenever a screen, component, or surface changes state — and specify explicitly when motion should be used versus deliberately withheld, so transitions read as one coherent assistant rather than a patchwork of per-team animation choices.

## Why It Exists

Motion is one of the fastest ways for a product to either communicate hierarchy and continuity, or to violate the "never overwhelm" principle when applied inconsistently across three pillars each reaching for their own flourish. Without a shared motion system, engineering has no single timing/easing contract to implement against, and the product accumulates competing transition styles that make it feel stitched together rather than built as one experience. This document exists to give every surface one motion vocabulary and to draw a firm line around when motion is appropriate at all.

## Approximate Page Count

6-8 pages.

## Sections

1. **Motion Philosophy** — the guiding stance that motion communicates state and hierarchy and is never decorative for its own sake, tying directly to the "never overwhelm" principle.
2. **Timing & Duration Scale** — the standardized set of duration tokens (e.g., short, medium, long) and the class of transition each is intended for.
3. **Easing Standards** — the approved easing curves and the rationale for matching a given curve to entrance, exit, and continuous transitions.
4. **Transition Types Catalog** — the recurring transition patterns: navigation push/pop, modal present/dismiss, list reorder, tab switch, and cross-fade.
5. **When Motion Is Used vs. Avoided** — explicit suppression rules, including reduced-motion accessibility settings, high-frequency data updates, and background/proactive interruptions, so motion never becomes visual noise.
6. **Cross-Pillar & Cross-Platform Consistency** — how the same timing and easing tokens are expressed identically across Productivity, Finance, and Health, and across web, iOS, and Android.
7. **Performance Budget for Motion** — the technical constraints, including frame-rate targets and CPU/GPU cost thresholds, that bound what any transition is allowed to cost.
8. **Relationship to Animation** — the boundary between this general-purpose transition system and the bespoke, meaning-carrying animated moments defined in the Animation document, so teams know which document governs a given decision.

## Deliverables

* Approved Motion document.
* Timing and easing token table.
* Transition-type catalog with usage guidance for each pattern.
* Reduced-motion and accessibility override specification.

## Dependencies

Requires the **Design Language** document (Phase 7), the **Component Library** document (Phase 7, Document 07), the **Navigation Philosophy** document (Phase 2, Document 04), the **Accessibility** document (Phase 2, Document 36), and the **Guiding Principles Document** (Phase 1, Document 07).

## Teams

Design, Design Systems, Frontend Engineering, Accessibility, Product.

## Completion Criteria

- [ ] Every transition type in the catalog has an assigned duration and easing token with no ad hoc exceptions.
- [ ] Reduced-motion behavior is specified for every transition type in the catalog.
- [ ] The motion performance budget has been validated against the lowest-supported device tier.
- [ ] The boundary between the Motion and Animation documents has been reviewed with no unresolved overlap.
- [ ] Signed off by: Head of Design (required), Design Systems Lead (required), Head of Engineering (required).
