# Document 13: Widgets (Visual)

## Document Name
Widgets (Visual)

## Purpose
Define the actual visual design of home-screen and lock-screen widgets across all supported size classes — layout, typography scale, iconography, and per-platform visual constraints — implementing the glanceable-content philosophy from Phase 2's Widgets document (Document 15) and the widget type catalog and data contracts from Phase 3's Widgets PRD (Document 04). This document decides how each widget type literally renders; it does not decide what data a widget is allowed to show or how often it refreshes.

## Why It Exists
Phase 2's Widgets document establishes that a widget must convey value in a few seconds without opening the app, and Phase 3's Widgets PRD defines the concrete widget type catalog, data contracts, and refresh/staleness rules engineering must build against — but neither specifies how a small versus medium versus large widget is actually laid out, what typography survives at lock-screen scale, or how a stale state visually differs from a fresh one. Without this document, iOS and Android teams each interpret "glanceable" as a visual problem independently, producing widgets that behaviorally comply but look and feel like they belong to different products. This document exists so every widget size and state has one authoritative visual specification shared across platforms wherever platform constraints allow.

## Approximate Page Count
6-8 pages.

## Sections
1. **Widget Size Class Layouts** — the concrete visual layout for each supported size (small, medium, large, lock-screen) per widget type defined in the Widgets PRD's type catalog.
2. **Typography & Legibility at Scale** — the type scale and minimum legible sizes used across widget sizes, with explicit rules for what content must be dropped rather than shrunk illegibly.
3. **Visual Hierarchy Within a Widget** — how a single widget visually signals its most important element first, consistent with the Widgets document's glance-not-open philosophy.
4. **Fresh vs. Stale Visual States** — the specific visual treatment (e.g., desaturation, timestamp, iconography) that distinguishes a stale widget from a fresh one, implementing the staleness rules from the Widgets PRD.
5. **Lock-Screen Visual Constraints** — the reduced color, contrast, and content rules specific to lock-screen widgets, including the visual treatment that withholds sensitive content by default per the Widgets PRD's privacy requirement.
6. **Empty/Calm Widget Visual Treatment** — the specific visual design of a widget with nothing notable to show, implementing Document 15's requirement that the empty state not manufacture urgency.
7. **Trust-Tier Visual Differentiation** — how a widget looks different when it carries a directly confirmable action (higher Proactivity Ladder tiers) versus when it is purely informational, implementing the widget-tier-by-trust-level table from Document 15 and the Widgets PRD.
8. **Platform Rendering Constraints (iOS/Android)** — where iOS WidgetKit and Android App Widget/Glance visual capabilities diverge, and the product's stance on where visual parity is required versus where platform-specific rendering is acceptable.

## Deliverables
* Approved Widgets (Visual) specification.
* Annotated widget mockups for every size class × widget type × state (fresh, stale, empty, actionable) combination.
* A typography and legibility reference table by widget size.
* A lock-screen content-sensitivity visual checklist.

## Dependencies
Requires Widgets (Phase 2, Document 15) for glanceability philosophy and content-eligibility boundaries; requires Widgets PRD (Phase 3, Document 04) for the widget type catalog, data contracts, and refresh/staleness logic this document renders visually; requires Dashboard UX (Phase 7, Document 12) for visual-language consistency between the dashboard and its widget extension; requires Component Library, Typography System, and Color System (Phase 7).

## Teams
Product, Design, Engineering (iOS), Engineering (Android), QA.

## Completion Criteria
- [ ] Every widget type and size class in the Widgets PRD's catalog has an approved visual layout.
- [ ] Fresh/stale visual states reviewed and confirmed distinguishable at a glance, without requiring text to communicate the difference.
- [ ] Lock-screen sensitivity-withholding visual treatment reviewed and confirmed against the Widgets PRD's privacy requirement.
- [ ] Trust-tier visual differentiation validated against at least one tier-transition example from the Widgets document.
- [ ] Signed off by: Head of Design (required), Engineering Lead (required).
