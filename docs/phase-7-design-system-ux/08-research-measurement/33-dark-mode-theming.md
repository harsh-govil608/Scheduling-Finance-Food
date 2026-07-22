# Document 33: Dark Mode & Theming

## Document Name
Dark Mode & Theming

## Purpose
Define the token-level theming architecture that supports light mode, dark mode, and future theme variants (high-contrast accessibility theme, potential seasonal or white-label brand themes) so that every semantic color decision made in the Color System (Phase 7, Doc 04) and every component in the Component Library (Phase 7, Doc 07) supports theme-switching from the moment it is built. This document specifies what the theming architecture must define: token layering, per-theme value tables, switching mechanics, and per-component validation requirements — not the theme values themselves.

## Why It Exists
This is a gap-closing addition identified by self-review: the original Phase 7 scope covered visual and interaction design extensively but left theming implicit rather than an explicit, owned document, and dark mode is one of the most expensive things to retrofit into a design system after launch. Retrofitting means hunting down every hardcoded color reference across hundreds of screens and replacing it with a token reference, under production pressure, with regressions guaranteed. Defining theming architecture now, at the same time as the Color System and Component Library, turns theme-switching into what it should be — a configuration change — rather than a company-wide rewrite six months after launch.

## Approximate Page Count
6-8 pages

## Sections
1. **Theming Architecture & Token Layers** — the separation between raw palette tokens (fixed hex/HSL values) and semantic tokens (e.g., `surface.primary`, `text.onSurface`) whose meaning stays constant while their resolved value changes per theme, built directly on the Color System (Phase 7, Doc 04).
2. **Light & Dark Mode Token Specification** — the required semantic token set (background, surface, text, border, elevation substitute) that must have both a light and a dark value defined before a token is considered complete, with minimum contrast-ratio requirements for each pairing.
3. **Theme Switching Mechanics** — system-preference detection, manual user override, and cross-device sync of the chosen theme, coordinated with Cross-Device Experience (Phase 2, Doc 29) and Cross-Device Sync (Phase 3, Doc 45), plus the requirement that switching never produces a visible flash of the wrong theme.
4. **Component-Level Theming Requirements** — the rule that no component is merged into the Component Library without both-theme support verified, enforced as a line item in the Design System Governance review checklist (Phase 7, Doc 32).
5. **Elevation & Depth in Dark Mode** — how shadow-based elevation cues, which read poorly against dark surfaces, are replaced with a surface-tint/lightness elevation scale instead of a naive shadow-opacity reduction.
6. **Imagery, Iconography & Illustration Theming** — rules for how icons, illustrations, and photography adapt per theme (tinted icon sets, alternate dark-mode illustration assets) rather than relying on naive color inversion, which frequently breaks brand imagery.
7. **Future Theme Extensibility** — the architectural requirement that adding a new theme (a high-contrast accessibility theme, a future white-label brand theme) means adding one new value table, not a new token layer or component rework.
8. **Platform-Specific Theming Constraints** — how each platform's native theme APIs (iOS appearance, Android day/night, Web `prefers-color-scheme`) map into the shared semantic token model, scoped against Platform-Specific Design (Phase 7, group 06).
9. **Testing & Validation** — the visual regression testing requirement across both themes, automated contrast-ratio checks against WCAG AA minimums, and a per-screen theme audit checklist.
10. **Migration & Rollout Plan** — since theming is being defined at the same stage as the Color System and Component Library rather than retrofitted, this section defines how theme support is validated during each component's initial build rather than deferred to a later "dark mode project."

## Deliverables
- Token architecture specification (raw vs. semantic token layers).
- Light and dark mode token value tables with contrast-ratio validation.
- Theme-switching mechanics specification (detection, override, sync, no-flash requirement).
- Component theming checklist, wired into Design System Governance's review gate.
- Elevation/depth-in-dark-mode design guidance.
- Icon, illustration, and imagery theming guidelines.
- Extensibility specification for future themes beyond light/dark.
- Platform-to-token theming mapping specification.
- Visual regression and automated contrast-ratio test plan.

## Dependencies
Requires Color System (Phase 7, Doc 04) as the source of the raw and semantic palette being themed, and Component Library (Phase 7, Doc 07) as the set of components that must implement both themes. Enforced through Design System Governance & Contribution Model (Phase 7, Doc 32), whose review checklist is the mechanism preventing single-theme components from shipping. Scoped against Platform-Specific Design (Phase 7, group 06) for platform API mapping and against the Voice & Accessibility group (Phase 7, group 07) for contrast and high-contrast-theme requirements. Coordinates with Cross-Device Experience (Phase 2, Doc 29) and Cross-Device Sync (Phase 3, Doc 45) for theme-preference sync.

## Teams
Design Systems, Design, Engineering (Web, iOS, Android), Accessibility, QA

## Completion Criteria
- [ ] Every semantic token defined in the Color System has both a light and a dark value before that token is considered final.
- [ ] Automated contrast-ratio checks pass WCAG AA minimums in both themes for all text/background token pairings.
- [ ] At least one full pilot screen per pillar (Productivity, Finance, Health) validated end-to-end in both themes before sign-off.
- [ ] Theme-switching mechanics tested for no-flash behavior and cross-device sync consistency.
- [ ] Component theming checklist is active as a mandatory gate in Design System Governance before this document is closed.
- [ ] Signed off by: Head of Design (required), Design Systems Lead (required), Accessibility Lead (required).
