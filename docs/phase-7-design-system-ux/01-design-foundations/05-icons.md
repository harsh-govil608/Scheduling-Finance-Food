# Document 05: Icons

## Document Name
Icons

## Purpose
Define the platform's icon system — visual style consistency, per-pillar iconography needs, sizing/grid rules, and accessibility requirements — so icons act as a fast, reliable comprehension aid rather than a decorative or ambiguous layer.

## Why It Exists
Icons are used throughout the product as shorthand for navigation, status, and actions across Productivity, Finance, and Health, and a mismatched or inconsistent icon set is one of the fastest ways to make an interface feel unpolished or fragmented across pillars. Because the assistant supports moments where users are moving quickly or scanning glanceable surfaces, icons must be instantly legible; and because they are frequently the only visual element attached to a status (e.g., a category glyph, a severity marker), an ungoverned icon system risks becoming an accessibility failure point where meaning is conveyed by shape or color alone. This document exists to give icon design and usage the same rigor as typography and color, rather than treating icons as an afterthought sourced ad hoc per feature.

## Approximate Page Count
6-8 pages

## Sections
1. **Icon Style & Construction Rules** — stroke weight, corner radius, grid, and geometric consistency criteria that every icon must be built against.
2. **Icon Sizing & Grid System** — the size ramp and pixel grid icons must align to for crispness across densities and platforms.
3. **Per-Pillar Iconography Needs** — the distinct icon vocabularies required for Productivity, Finance, and Health, and how they stay stylistically unified.
4. **Semantic & Status Icons** — rules for icons that carry meaning (severity, category, state) and their required pairing with color, label, or text so meaning is never conveyed by shape alone.
5. **Accessibility Requirements** — the rule that icons must never be the only signal (always paired with text label, color, or accessible name), plus screen-reader and touch-target requirements.
6. **Icon Library Governance** — process for requesting, designing, reviewing, and adding new icons so the set doesn't fragment over time.
7. **Animated & Interactive Icons** — criteria for when an icon may animate (e.g., loading, confirmation) and constraints to keep motion calm per the Design Principles.
8. **Platform Adaptation** — how the icon system adapts across mobile, web, wearable, and any voice/glanceable surfaces where icons may not render at all.
9. **Usage Examples & Anti-Patterns** — annotated examples of correct icon usage and common failures (e.g., icon-only buttons with no accessible label).

## Deliverables
- Icon construction and grid specification (style criteria, not a final icon set).
- Per-pillar icon vocabulary requirements and cohesion rules.
- Semantic/status icon pairing rules enforcing "never icon-alone" meaning.
- Accessibility requirements including accessible naming and touch target sizing.
- Icon library governance/request process.

## Dependencies
Requires Design Principles (Phase 7 Doc 01) and Design Language (Phase 7 Doc 02); coordinates with Color System (Phase 7 Doc 04) for semantic color pairing; informed by Phase 2 Accessibility document (Doc 36).

## Teams
Design, Accessibility, Engineering, Brand

## Completion Criteria
- [ ] Icon construction rules validated for consistency across a sample spanning all three pillars.
- [ ] "Never icon-alone" accessibility rule confirmed against WCAG and screen-reader testing guidance.
- [ ] Icon governance/request process reviewed by Design Systems Lead.
- [ ] Signed off by: Head of Design (required), Accessibility Lead (required).
