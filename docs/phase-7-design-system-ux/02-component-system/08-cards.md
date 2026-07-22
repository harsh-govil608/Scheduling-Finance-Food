# Document 08: Cards

## Document Name
Cards

## Purpose

Define the card component system — the primary content-container pattern used across the Dashboard, Search results, and every pillar surface — including its anatomy, variant catalog, states, and the cross-pillar consistency rules that keep a Finance card, a Health card, and a Productivity card reading as the same family of object.

## Why It Exists

Cards are the single most reused visual pattern in a dashboard-centric, cross-pillar product; without a systematic card specification, each pillar team builds subtly different cards — different padding, different action placement, different loading behavior — and that inconsistency undermines visual coherence at the most repeated component in the product. This document exists so "card" means one governed thing with defined variants, not a loose convention each team reinvents.

## Approximate Page Count

7-9 pages.

## Sections

1. **Card Anatomy** — the base structural pattern (header, content, action area) that every card variant inherits from, and which parts are optional versus mandatory.
2. **Variant Catalog** — suggestion cards, transaction cards, task cards, insight cards, and their content and interaction differences.
3. **State Matrix** — default, loading, error, and dismissed states, specified per variant so no variant ships with an undefined state.
4. **Cross-Pillar Consistency Rules** — how the shared card anatomy stays constant while pillar-specific content (Productivity, Finance, Health) varies within it, so a user can recognize "this is a card" regardless of pillar.
5. **Card Density & Sizing** — compact versus expanded sizing rules, and how a card's size responds to its placement on the Dashboard, in Search results, or within a pillar-specific surface.
6. **Interaction & Affordance Rules** — tap, swipe, dismiss, and expand behaviors, and the line between actions a card can resolve on its own versus actions that require drilling into a full surface.
7. **Card Composition in Lists & Grids** — stacking, spacing, and scan-order rules for when multiple cards appear together, so dense card lists (e.g., a transaction feed) remain legible.
8. **Card Motion & Feedback Hooks** — the specific touchpoints where the Motion and Animation documents attach to a card (entrance, dismissal, celebratory state), referenced rather than duplicated here.

## Deliverables

* Approved Cards document.
* Card variant catalog with anatomy described for each variant.
* State matrix table covering every variant across default, loading, error, and dismissed states.
* Cross-pillar theming example set with one annotated card per pillar.

## Dependencies

Requires the **Design Language** document (Phase 7), the **Component Library** document (Phase 7, Document 07), the **Dashboard System** document (Phase 2, Document 13), the **Dashboard UX** document (Phase 7), and the **Search Experience** document (Phase 2, Document 28).

## Teams

Design, Design Systems, Frontend Engineering, Product, Productivity/Finance/Health pillar leads.

## Completion Criteria

- [ ] Card variant catalog covers every card type referenced across the 47 Phase 3 PRDs.
- [ ] State matrix has been validated against at least one variant from each pillar (Productivity, Finance, Health).
- [ ] Cross-pillar consistency rules have been reviewed with no pillar-specific deviation from the shared card anatomy.
- [ ] Card density and sizing rules have been checked against the Dashboard, Search, and at least one dense pillar surface (e.g., transaction feed).
- [ ] Signed off by: Head of Design (required), Design Systems Lead (required).
