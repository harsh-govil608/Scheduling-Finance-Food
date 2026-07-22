# Document 15: Settings UX

## Document Name
Settings UX

## Purpose
Define the actual visual layout and interaction pattern for settings screens — the navigational structure of the settings menu, the basic-versus-advanced disclosure pattern, and the visual/interaction treatment of individual controls (toggles, sliders, pickers) — implementing the organizational and defaults philosophy already defined in Phase 2's Settings Philosophy document (Document 33). This document decides how settings are laid out and interacted with on-screen; it does not decide what qualifies as a setting or what its default value should be.

## Why It Exists
Phase 2's Settings Philosophy establishes the settings taxonomy, the defaults philosophy anchored to the Proactivity Ladder, and the basic-versus-advanced progressive-configurability rule — but it does not specify what the settings menu actually looks like, how deeply nested the advanced layer visually goes before it becomes its own source of overwhelm, or how a control visually confirms a change has taken effect. Without this document, engineering builds settings screens ad hoc per pillar, producing exactly the "duplicate settings across pillars that drift out of sync" anti-pattern the Settings Philosophy already named and rejected — just expressed visually instead of structurally. This document exists so the taxonomy and disclosure rules from Document 33 are given one consistent, buildable screen pattern used everywhere a setting appears.

## Approximate Page Count
6-8 pages.

## Sections
1. **Settings Navigation Structure** — the visual hierarchy of the settings menu (top-level categories, sub-screens, search entry point), implementing the taxonomy from the Settings Philosophy document.
2. **Basic vs. Advanced Visual Disclosure** — the concrete interaction pattern (e.g., an "Advanced" expander, a separate screen tier) used to separate front-and-center settings from the deeper advanced layer, implementing Document 33's Progressive Configurability section.
3. **Control Design by Setting Type** — the visual and interaction design for each control type used across settings (toggle, slider/threshold, single-select, multi-select), and the rule for which control type is used for which kind of setting.
4. **Per-Pillar Automation Control Layout** — the specific visual pattern for exposing per-pillar AI initiative controls, implementing Document 33's Per-Pillar Automation Controls section and connecting visually to override/pause affordances from the User Control Model.
5. **Settings Search & Findability** — the visual design of in-settings search and how search results map back into the navigational hierarchy, implementing Document 33's Search & Discoverability requirement.
6. **Change Confirmation Feedback** — the visual/interaction pattern (e.g., inline confirmation, transient state change) that confirms a setting change has taken effect, implementing Document 33's Settings Change Feedback section.
7. **Cross-Device Consistency Indicators** — any visual treatment needed to reassure a user that a setting changed on one device is reflected elsewhere, implementing Document 33's Cross-Device/Cross-Session Consistency expectation.
8. **Settings Anti-Pattern Visual Checklist** — the literal on-screen expressions of Document 33's anti-patterns (e.g., a settings screen used as a dumping ground, duplicated per-pillar toggles) that this layout is designed to prevent.

## Deliverables
* Approved Settings UX specification.
* Annotated mockups for the settings navigation hierarchy, basic and advanced tiers.
* A control-type usage reference mapped to setting categories from the Settings Philosophy taxonomy.
* A settings anti-pattern visual checklist for design review of any new setting.

## Dependencies
Requires Settings Philosophy (Phase 2, Document 33) for taxonomy, defaults philosophy, and disclosure rules this document lays out visually; requires Navigation (Visual) (Phase 7, Document 11) for where settings sit within overall navigation; requires the User Control Model (Phase 2, Document 34) for override/pause affordances surfaced within settings; requires Component Library and Color System (Phase 7).

## Teams
Product, Design, Engineering, Content/Copy, QA.

## Completion Criteria
- [ ] Settings navigation structure validated against every category in the Settings Philosophy taxonomy with no orphaned category.
- [ ] Basic-versus-advanced disclosure pattern reviewed against at least three worked example settings per pillar, consistent with Document 33's own completion criteria.
- [ ] Change-confirmation feedback pattern validated for at least one automation-level setting and one data/privacy setting.
- [ ] Settings anti-pattern visual checklist reviewed against current design explorations with no violations outstanding.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
