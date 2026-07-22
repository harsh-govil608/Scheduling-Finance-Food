# Document 25: Tablet

## Document Name
Tablet

## Purpose
Define tablet-specific layout opportunities — multi-column arrangements, master-detail side-by-side views, and split-screen multitasking — that take deliberate advantage of the tablet's larger canvas rather than simply scaling up the phone design. This document specifies where and how tablet diverges from phone layout, not just how it stretches to fit.

## Why It Exists
Treating tablet as "a bigger phone" is the default failure mode: it produces wasted whitespace, single-column layouts on a screen built for two, and a product that feels unfinished on a device many users hold for longer, more deliberate sessions — propped on a counter while cooking through a meal plan, or reviewing a weekly finance summary at a desk. Without a dedicated tablet document, engineering will apply responsive scaling rules from the phone layout and stop there, missing the qualitatively richer experience — simultaneous list-and-detail, side-by-side comparison, richer at-a-glance density — that a mobile-first product still owes its tablet users. This document exists to make those layout opportunities explicit and specified before engineering builds the phone-first version and calls it done.

## Approximate Page Count
6-8 pages

## Sections
1. **Multi-Column Layout Opportunities** — which core surfaces (Dashboard, pillar list-plus-detail views) adopt a multi-column layout on tablet instead of the single-column phone layout, and which intentionally remain single-column.
2. **Master-Detail / Side-by-Side Patterns** — how a list and its detail (task list and task detail, conversation and supporting context) are shown simultaneously on tablet instead of requiring navigation back and forth.
3. **Orientation Behavior (Portrait vs. Landscape)** — how layout responds to orientation change, including which surfaces hold their layout and which reflow between single- and multi-column as orientation changes.
4. **Split-Screen & Multitasking Support** — how the app behaves when the OS places it in split-screen or slide-over next to another app, including the minimum supported width before it degrades to the phone layout.
5. **Touch Target & Input Adjustments** — how spacing, target size, and gesture patterns differ from phone given tablet's larger screen and common two-handed or stylus use.
6. **Keyboard & External Input Accommodation** — behavior when a tablet is paired with a hardware keyboard or trackpad, bridging toward desktop-like precision without fully becoming the Desktop experience.
7. **Content Density at Tablet Scale** — what additional information (secondary metrics, expanded context, richer charts) the tablet canvas allows showing that phone must hide, and the rule for how much is "enough" before density undermines the calm, uncluttered feel established in Design Foundations.
8. **Component Library Adaptations for Tablet** — which components from the Component Library gain tablet-specific variants, such as a card that expands into a two-pane panel.

## Deliverables
* Tablet layout specification for the Dashboard and one representative screen per pillar.
* Master-detail pattern library with minimum-width thresholds.
* Orientation and split-screen behavior specification.
* Annotated tablet mockups demonstrating multi-column layout for at least three core surfaces.

## Dependencies
Requires the Component Library (Phase 7) for the components being extended with tablet variants, and the relevant Core Surface UX documents (Phase 7) for the phone-baseline layouts being adapted. Coordinates with Cross-Device UX (Phase 7, Document 23) for continuity indicators rendered on tablet, and feeds into Responsive Design (Phase 7, Document 27) as one of the platform inputs to the unified breakpoint system.

## Teams
Design, Mobile Engineering (Tablet), Product, QA

## Completion Criteria
- [ ] Multi-column layout defined and mocked for the Dashboard and at least one screen per pillar.
- [ ] Master-detail minimum-width thresholds validated against the smallest supported tablet screen size.
- [ ] Split-screen degradation behavior tested down to the minimum supported width without content loss.
- [ ] Component Library tablet variants reviewed for consistency with their phone counterparts.
- [ ] Signed off by: Head of Design (required), Head of Mobile Engineering (required).
