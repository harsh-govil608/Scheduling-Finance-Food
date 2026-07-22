# Document 12: Dashboard UX

## Document Name
Dashboard UX

## Purpose
Define the visual layout and interaction design for the home Dashboard surface — grid system, card arrangement, information density, and visual prioritization — implementing the behavioral requirements already defined in Phase 2's Dashboard System document (Document 13). This document decides how the dashboard looks and is spatially organized; it does not redefine what content is eligible to appear or how it is prioritized behaviorally.

## Why It Exists
Phase 2's Dashboard System defines what appears on the dashboard and why (content eligibility, real-time prioritization, pillar-mix and life-stage adaptation), but it explicitly does not define how that content is laid out, spaced, sized, or visually prioritized on-screen. Without this document, engineering and design improvise the actual screen independently — one team building a rigid card grid, another a freeform feed — and the result risks being a dashboard that is behaviorally correct (the right content, correctly prioritized) but visually incoherent, undermining the calm, single-assistant impression the behavioral layer was written to protect. This document exists so the visual translation of "mirror, not menu" is specified once, centrally, and consistently.

## Approximate Page Count
7-9 pages.

## Sections
1. **Grid & Layout System** — the responsive grid the dashboard is built on, including column structure, margins, and gutter rules across phone and tablet breakpoints.
2. **Visual Prioritization** — how behavioral priority (from Phase 2 Document 13's real-time prioritization logic) translates into visual size, position, and prominence, including the rule for what "top of dashboard" visually communicates.
3. **Card Density Rules** — the maximum number and visual weight of cards that can appear before the dashboard feels overwhelming, operationalizing the Never Overwhelm principle at the pixel level and tying directly to Document 13's Information Density & Hierarchy Rules.
4. **Pillar-Mix Visual Adaptation** — how the same grid visually re-balances for a Finance-heavy user versus a Health-heavy user versus a balanced user, giving concrete visual form to Document 13's Pillar-Mix Adaptation section.
5. **Empty/Calm State Visual Treatment** — the specific illustration, copy placement, and layout used when no pillar has anything urgent to surface, implementing Document 13's Empty/Calm States requirement without letting the screen read as broken or blank.
6. **Card Anatomy & Visual Variants** — the visual structure of an individual dashboard card (content zones, imagery/iconography, action affordances) and how it differs by content type, referencing the Cards component (Phase 7).
7. **Motion & Refresh Behavior** — how the dashboard visually updates as content re-prioritizes in real time (e.g., reordering, fading, or appearing), so re-ranking reads as a living surface rather than a jarring layout shift.
8. **Boundary Rendering vs. Notifications and Widgets** — the visual cues that keep dashboard cards visually distinct from notification banners and widget surfaces, implementing the surface boundary Document 13 establishes at the behavioral level.
9. **Dashboard Mood Variants (Visual)** — the visual/tonal differences, if any, between a calm-day and a crunch-day dashboard, giving visual form to Document 13's Dashboard Variants Requirement.

## Deliverables
* Approved Dashboard UX specification.
* Annotated dashboard mockups for calm, normal, and overloaded density states, one per pillar-mix scenario.
* A grid and card-density spec sheet usable directly by engineering.
* A motion/refresh behavior reference for real-time re-prioritization.

## Dependencies
Requires Dashboard System (Phase 2, Document 13) for content eligibility and prioritization logic this document lays out visually; requires Navigation (Visual) (Phase 7, Document 11) for how the dashboard sits within overall navigation; requires Component Library, Cards, Color System, and Responsive Layout Grid (Phase 7, Design Foundations & Component System).

## Teams
Product, Design, Engineering (iOS), Engineering (Android), QA.

## Completion Criteria
- [ ] Layout validated against the Dashboard System's content-eligibility rules for at least one worked scenario per pillar-mix.
- [ ] Card density rules reviewed against the Never Overwhelm principle using an overloaded-day scenario.
- [ ] Empty/calm state mockup reviewed and confirmed not to read as broken, blank, or feature-poor.
- [ ] Visual boundary between dashboard, notification, and widget surfaces cross-checked with Documents 14 and 15 (Phase 7) with no unresolved overlap.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
