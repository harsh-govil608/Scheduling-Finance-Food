# Document 16: Empty States

## Document Name
Empty States

## Purpose
Define the visual design system for empty states — illustration style, layout composition, typography hierarchy, and call-to-action treatment — that implements the behavioral requirements set out in Phase 2's Empty States document (Doc 37) across every major surface in Productivity, Finance, and Health. This document specifies how each empty-state category defined behaviorally gets rendered, not what it must communicate.

## Why It Exists
Doc 37 established what every empty state must say and which action it must drive a user toward, but it did not specify how that gets built — the illustration language, spacing, typographic weight, and button styling that make an empty screen feel like a deliberate invitation rather than a placeholder left over from development. Because onboarding necessarily ends before any pillar has accumulated real data, the first sustained experience most new users have is an empty one, and if that moment is rendered inconsistently across Productivity, Finance, and Health it undercuts the "one assistant" framing at the exact point a user is deciding whether to trust the product. This document exists to give designers and engineers a single, reusable visual pattern for every empty-state category Doc 37 defined, so the product's zero-data moments look as intentional as its fully populated ones.

## Approximate Page Count
6-8 pages.

## Sections
1. **Empty State Illustration System** — the illustration style, subject-matter guardrails, and reuse rules for empty-state art, coordinated with the Illustrations document so empty-state art reads as part of one family rather than a one-off.
2. **Layout & Composition Template** — the standard composition (illustration placement, headline, supporting copy, CTA button) every empty state is built from, and the fallback layout for surfaces too small or dense for the full template.
3. **Typography & Copy Hierarchy** — how empty-state headline, body copy, and CTA label are sized, weighted, and spaced relative to one another, distinct from the typographic treatment of populated states.
4. **Visual Treatment Per Empty-State Category** — the visual differences, if any, between the "never populated," "transitional," and "user-cleared" states Doc 37 defines behaviorally, so a user can tell them apart without reading the copy closely.
5. **Per-Pillar Illustration Variants** — the illustration subject matter and accent-color variants for Productivity, Finance, and Health empty states that preserve each pillar's identity inside the shared visual system.
6. **CTA Button & Interaction States** — the visual and interaction design of the primary call-to-action inside an empty state, including its default, hover, pressed, and disabled states.
7. **Responsive & Cross-Platform Behavior** — how the empty-state template adapts across phone, tablet, desktop, and widget-sized breakpoints without losing its core composition.
8. **Dark Mode & Theming Requirements** — how empty-state illustrations, colors, and accents adapt under dark mode and any future theming without requiring a redraw per theme.

## Deliverables
* Approved Empty States (Visual) document.
* An empty-state layout template usable across every surface enumerated in Doc 37's inventory.
* Illustration style guide entries covering each pillar's empty-state variants.
* A light/dark mode specification for the empty-state template.

## Dependencies
Requires Empty States (Phase 2, Doc 37) for the behavioral requirements this document implements; requires Illustrations (Phase 7) for the base illustration style; requires Color System (Phase 7) and Typography (Phase 7) for the visual tokens applied to layout and copy; requires Information Architecture (Phase 2, Doc 03) for the full surface inventory.

## Teams
Design, Product, Content/Copy, Engineering (Frontend)

## Completion Criteria
- [ ] Every empty-state surface enumerated in Doc 37 has an approved visual template applied.
- [ ] The three empty-state categories (never populated, transitional, user-cleared) are visually distinguishable without relying on copy alone.
- [ ] Illustration variants exist and are approved for all three pillars.
- [ ] Light and dark mode treatments have been validated on at least one example per pillar.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
