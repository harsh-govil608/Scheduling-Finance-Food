# Document 04: Color System

## Document Name
Color System

## Purpose
Define the platform's color system — palette structure, semantic color roles (success/warning/danger/info), and per-pillar accent strategy — that translates the brand into every screen while staying accessible and calm.

## Why It Exists
Without a systematic color framework, each pillar team (Finance, Health, Productivity) picks colors independently, producing a product that visually reads as three apps stapled together — directly undermining the "one assistant" mission at the most visible layer. Color is also the fastest channel through which the product can accidentally violate the "encourage, never guilt" principle: an overly aggressive red on a missed budget or a skipped workout can read as alarm or shame rather than gentle course-correction. This document exists to make color a deliberate, accessible, and emotionally calibrated system rather than a set of ad hoc choices made screen by screen.

## Approximate Page Count
8-10 pages

## Sections
1. **Core Palette** — primary/secondary/neutral color roles and the selection criteria they must satisfy (not final hex values).
2. **Semantic Color Roles** — success, warning, danger, info — and their consistent meaning across all three pillars (a red must always mean the same severity everywhere).
3. **Per-Pillar Accent Strategy** — whether/how Productivity/Finance/Health get distinguishing accent colors without breaking the "one assistant" cohesion.
4. **Emotional Calibration of Warning/Danger Colors** — criteria for tuning alert colors so they inform without inducing anxiety or guilt, consistent with the Design Principles.
5. **Light & Dark Mode Behavior** — rules for how the palette and semantic roles translate between appearance modes without changing meaning.
6. **Accessibility & Contrast Requirements** — minimum contrast ratios, color-blind-safe pairing rules, and the requirement that color is never the sole carrier of meaning.
7. **Data Visualization Color Rules** — how the palette extends to charts and graphs (budgets, health trends, task trends) while remaining distinguishable and calm.
8. **Surface, Elevation & Neutral Usage** — how neutral tones establish backgrounds, cards, and elevation without introducing visual noise.
9. **Token Naming & Theming Architecture** — how colors are named and structured as tokens so they can be themed, updated, or extended without breaking downstream components.
10. **Usage Examples & Anti-Patterns** — annotated right/wrong examples, including misuse of semantic colors across pillars.

## Deliverables
- A structured palette definition (roles and selection criteria, not final hex values).
- A semantic color role table with cross-pillar consistency rules.
- Per-pillar accent strategy and its cohesion boundaries.
- Light/dark mode mapping rules.
- Accessibility/contrast requirements validated against WCAG.
- Color token naming architecture for engineering handoff.

## Dependencies
Requires Design Principles (Phase 7 Doc 01) and Design Language (Phase 7 Doc 02); informed by Phase 1 Guiding Principles, Phase 2 Doc 02 Product Philosophy Document, and Phase 2 Accessibility document (Doc 36); coordinates with Typography (Phase 7 Doc 03) for text contrast and with Icons (Phase 7 Doc 05) for semantic color use in iconography.

## Teams
Design, Brand, Accessibility, Engineering

## Completion Criteria
- [ ] Semantic color roles validated against WCAG contrast requirements.
- [ ] Warning/danger color calibration reviewed against the "encourage, never guilt" principle.
- [ ] Per-pillar accent strategy reviewed by all three pillar design leads for cohesion.
- [ ] Color token architecture confirmed implementable by Engineering.
- [ ] Signed off by: Head of Design (required), Design Systems Lead (required), Accessibility Lead (required).
