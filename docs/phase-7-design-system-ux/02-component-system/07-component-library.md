# Document 07: Component Library

## Document Name
Component Library

## Purpose

Define the reusable UI component system — buttons, inputs and form controls, lists, modals/dialogs, and navigation elements — used across every surface in Productivity, Finance, and Health, and establish the governance model for proposing, reviewing, and approving any new component before it is added to the library. This document is the umbrella specification for the Component System group: Cards (Document 08) is its most heavily used child pattern, and Motion (Document 09) and Animation (Document 10) both attach to components defined here.

## Why It Exists

A dashboard-centric, three-pillar product is assembled almost entirely out of repeated components; if each pillar team is free to build its own buttons, inputs, and modals, the product regresses to "three apps stapled together" at the most granular visual level, and every downstream UX document loses a stable set of primitives to reference. This document exists to give every pillar and every platform team one governed source of truth for what a component is, what states it must support, and how a team earns the right to add a new one — so the library grows deliberately instead of by accretion.

## Approximate Page Count

10-13 pages.

## Sections

1. **Component Library Philosophy & Scope** — what qualifies as a "component" versus a "pattern" or a "surface," and the explicit boundary between this document and the Cards, Motion, and Animation documents that depend on it.
2. **Foundational Controls** — buttons, text inputs, toggles, selects, checkboxes, and radios, including the variant set each must ship with (primary/secondary/tertiary, sizes, and disabled/loading forms).
3. **Structural Components** — lists, tables, containers, dividers, and section headers used to assemble every pillar surface, including the composition rules that keep dense surfaces (e.g., transaction lists) legible.
4. **Overlay & Modal System** — modals, dialogs, bottom sheets, tooltips, and popovers, including interruption-severity rules that tie back to the "never overwhelm" principle for anything that blocks the underlying surface.
5. **Navigation Components** — tab bars, nav rails, and breadcrumbs, and how this component-level spec stays consistent with the cross-platform navigation shell defined in the Navigation Philosophy document.
6. **Cross-Pillar Theming Rules** — how a single component adapts its color and iconography to Productivity, Finance, or Health context without forking into a pillar-specific component.
7. **Component States & Accessibility Baseline** — the required state matrix (default, hover, focus, active, disabled, loading, error) and the accessibility floor every component in the library must clear before it can ship.
8. **Governance Model for New Components** — the proposal, design-review, and approval workflow required to add a net-new component, including who can approve one and when a one-off, non-library element is permitted.
9. **Component Documentation Standard** — the required documentation every library component ships with (variants, props, usage guidance, do/don't examples) so engineering and design stay in sync as the library grows.

## Deliverables

* Approved Component Library document.
* Full component inventory/catalog table (component name, variants, states, owning team, platforms supported).
* Governance workflow diagram/RACI for proposing and approving a new component.
* Component documentation template for use by every future component addition.

## Dependencies

Requires the **Design Language** document (Phase 7), the **Design Tokens** foundation covering color and typography (Phase 7), the **Navigation Philosophy** document (Phase 2, Document 04), the **Information Architecture** document (Phase 2, Document 03), the **Accessibility** document (Phase 2, Document 36), and the **Guiding Principles Document** (Phase 1, Document 07).

## Teams

Design, Design Systems, Frontend Engineering (Web, iOS, Android), Product, Accessibility.

## Completion Criteria

- [ ] Every component referenced across the 47 Phase 3 PRDs maps to an entry in the component inventory, or an approved gap is explicitly logged.
- [ ] The governance workflow has been validated end-to-end against at least one simulated new-component proposal.
- [ ] The state matrix and accessibility baseline apply uniformly across web, iOS, and Android component implementations.
- [ ] Cross-pillar theming rules have been reviewed with at least one themed example each from Productivity, Finance, and Health.
- [ ] Signed off by: Head of Design (required), Design Systems Lead (required), Head of Engineering (required).
