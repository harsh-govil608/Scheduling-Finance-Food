# Document 03: Typography

## Document Name
Typography

## Purpose
Define the platform's typographic system — type scale, hierarchy rules, weight usage, and readability requirements — including how that system must hold up across every supported language and script, so text remains calm, legible, and consistently hierarchical everywhere the assistant speaks to the user.

## Why It Exists
Typography carries most of the product's information density: numbers on a budget screen, a task list, a health metric trend. If each pillar or platform team sets its own type scale, the product both looks fragmented and risks becoming hard to read at a glance — directly conflicting with the "one-glance comprehension" design principle. Typography is also the design element most exposed to internationalization risk: a scale and hierarchy tuned only for Latin script can break for languages with taller ascenders, different line-height needs, or non-Latin scripts, silently harming legibility for a subset of users. This document exists so type decisions are made once, systematically, and validated for every locale the product ships in, rather than improvised per screen or discovered broken after translation.

## Approximate Page Count
8-10 pages

## Sections
1. **Type Scale & Hierarchy** — the ramp of type sizes/weights and the rules for which hierarchy level each represents (e.g., screen title, section header, body, caption).
2. **Typeface Selection Criteria** — the criteria a typeface must satisfy (legibility at small sizes, multi-script support, licensing, calm/neutral character) rather than a final named typeface choice.
3. **Weight & Emphasis Usage** — rules for when bold, medium, and regular weights may be used to convey emphasis without creating visual noise.
4. **Line Height, Measure & Spacing** — readability requirements for line height and line length across dense financial tables, conversational assistant text, and short glanceable labels.
5. **Multi-Language & Script Support** — requirements for the type system to remain legible and hierarchically consistent across all Phase 2 Localization-supported languages and scripts, including non-Latin scripts.
6. **Responsive & Cross-Platform Scaling** — how the type scale adapts across mobile, web, tablet, and wearable/glanceable surfaces.
7. **Accessibility & Dynamic Type** — requirements for supporting user-controlled text size, screen reader compatibility, and minimum contrast/legibility thresholds.
8. **Numeric & Tabular Typography** — special rules for how numbers (currency, health metrics, dates) are set so figures stay aligned, scannable, and calm rather than alarming.
9. **Voice-to-Visual Consistency** — how typographic tone (e.g., friendly vs. clinical) reflects the assistant's conversational voice defined in Phase 2.
10. **Usage Examples & Anti-Patterns** — annotated examples of correct hierarchy use and common mistakes (e.g., overusing bold, mixing too many sizes on one screen).

## Deliverables
- A defined type scale with named hierarchy levels and usage rules.
- Typeface selection criteria (not final typeface names) covering legibility, licensing, and multi-script coverage.
- Line-height, measure, and responsive scaling rules per platform/surface.
- A validated multi-language/script legibility requirement set, cross-checked against supported locales.
- Accessibility requirements for dynamic type and screen readers.

## Dependencies
Requires Design Principles (Phase 7 Doc 01) and Design Language (Phase 7 Doc 02); informed by Phase 2 Localization document and Phase 2 Accessibility document (Doc 36); coordinates with Color System (Phase 7 Doc 04) for text-on-background contrast.

## Teams
Design, Localization, Accessibility, Engineering

## Completion Criteria
- [ ] Type scale validated for legibility across all Phase 2 Localization-supported languages and scripts.
- [ ] Dynamic type and screen-reader behavior confirmed feasible with Engineering.
- [ ] Contrast requirements cross-checked with the Color System document.
- [ ] Signed off by: Head of Design (required), Localization Lead (required), Accessibility Lead (required).
