# Phase 7 — Design System & UX Requirements

Per `phase7.md`, this document defines every UX/visual-design document required before visual design begins. As with prior phases, this is the requirements specification, not the design system itself.

**Relationship to earlier phases:** Phase 2 already defined the *behavioral* UX layer (Navigation Philosophy, Dashboard System, Widgets, Notification System, Settings Philosophy, Onboarding Experience, Premium Experience, Cross-Device Experience, Accessibility, Empty States — all describing WHAT and WHY). Phase 7 defines the *visual/interaction design* layer on top — HOW those behaviors actually look, are laid out, and feel to touch. Every Phase 7 document below names its Phase 2 (and where relevant, Phase 3/4) behavioral counterpart explicitly to avoid duplicating rather than implementing it.

---

## Document Set

### Group 1 — Design Foundations (`01-design-foundations/`)

| # | Document | Pages | File |
|---|---|---|---|
| 1 | Design Principles | 6–8 | [`01-design-principles.md`](01-design-foundations/01-design-principles.md) |
| 2 | Design Language | 7–9 | [`02-design-language.md`](01-design-foundations/02-design-language.md) |
| 3 | Typography | 6–8 | [`03-typography.md`](01-design-foundations/03-typography.md) |
| 4 | Color System | 7–9 | [`04-color-system.md`](01-design-foundations/04-color-system.md) |
| 5 | Icons | 5–7 | [`05-icons.md`](01-design-foundations/05-icons.md) |
| 6 | Illustrations | 5–7 | [`06-illustrations.md`](01-design-foundations/06-illustrations.md) |

### Group 2 — Component System (`02-component-system/`)

| # | Document | Pages | File |
|---|---|---|---|
| 7 | Component Library | 8–10 | [`07-component-library.md`](02-component-system/07-component-library.md) |
| 8 | Cards | 6–8 | [`08-cards.md`](02-component-system/08-cards.md) |
| 9 | Motion | 6–8 | [`09-motion.md`](02-component-system/09-motion.md) |
| 10 | Animation | 6–8 | [`10-animation.md`](02-component-system/10-animation.md) |

### Group 3 — Core Surface UX — Visual Layer (`03-core-surface-ux/`)

| # | Document | Pages | File |
|---|---|---|---|
| 11 | Navigation (Visual) | 6–8 | [`11-navigation-visual.md`](03-core-surface-ux/11-navigation-visual.md) |
| 12 | Dashboard UX | 7–9 | [`12-dashboard-ux.md`](03-core-surface-ux/12-dashboard-ux.md) |
| 13 | Widgets (Visual) | 6–8 | [`13-widgets-visual.md`](03-core-surface-ux/13-widgets-visual.md) |
| 14 | Notifications (Visual) | 7–9 | [`14-notifications-visual.md`](03-core-surface-ux/14-notifications-visual.md) |
| 15 | Settings UX | 6–8 | [`15-settings-ux.md`](03-core-surface-ux/15-settings-ux.md) |

### Group 4 — State & Interaction Design (`04-state-interaction-design/`)

| # | Document | Pages | File |
|---|---|---|---|
| 16 | Empty States | 6–8 | [`16-empty-states.md`](04-state-interaction-design/16-empty-states.md) |
| 17 | Error States | 6–8 | [`17-error-states.md`](04-state-interaction-design/17-error-states.md) |
| 18 | Loading States | 5–7 | [`18-loading-states.md`](04-state-interaction-design/18-loading-states.md) |
| 19 | Microinteractions | 5–7 | [`19-microinteractions.md`](04-state-interaction-design/19-microinteractions.md) |
| 20 | Haptics | 4–6 | [`20-haptics.md`](04-state-interaction-design/20-haptics.md) |

### Group 5 — Onboarding & Monetization UX (`05-onboarding-monetization-ux/`)

| # | Document | Pages | File |
|---|---|---|---|
| 21 | Onboarding UX | 8–10 | [`21-onboarding-ux.md`](05-onboarding-monetization-ux/21-onboarding-ux.md) |
| 22 | Premium UX | 7–9 | [`22-premium-ux.md`](05-onboarding-monetization-ux/22-premium-ux.md) |

### Group 6 — Platform-Specific Design (`06-platform-specific-design/`)

| # | Document | Pages | File |
|---|---|---|---|
| 23 | Cross-device UX | 6–8 | [`23-cross-device-ux.md`](06-platform-specific-design/23-cross-device-ux.md) |
| 24 | Wearables | 6–8 | [`24-wearables.md`](06-platform-specific-design/24-wearables.md) |
| 25 | Tablet | 5–7 | [`25-tablet.md`](06-platform-specific-design/25-tablet.md) |
| 26 | Desktop | 5–7 | [`26-desktop.md`](06-platform-specific-design/26-desktop.md) |
| 27 | Responsive Design | 6–8 | [`27-responsive-design.md`](06-platform-specific-design/27-responsive-design.md) |

### Group 7 — Voice & Accessibility (`07-voice-accessibility/`)

| # | Document | Pages | File |
|---|---|---|---|
| 28 | Voice UX | 7–9 | [`28-voice-ux.md`](07-voice-accessibility/28-voice-ux.md) |
| 29 | Accessibility (Design System Implementation) | 8–10 | [`29-accessibility-design-system-implementation.md`](07-voice-accessibility/29-accessibility-design-system-implementation.md) |

### Group 8 — Research & Measurement (`08-research-measurement/`)

| # | Document | Pages | File |
|---|---|---|---|
| 30 | User Testing | 7–9 | [`30-user-testing.md`](08-research-measurement/30-user-testing.md) |
| 31 | UX Metrics | 6–8 | [`31-ux-metrics.md`](08-research-measurement/31-ux-metrics.md) |
| 32 | Design System Governance & Contribution Model | 6–8 | [`32-design-system-governance-contribution-model.md`](08-research-measurement/32-design-system-governance-contribution-model.md) |
| 33 | Dark Mode & Theming | 6–8 | [`33-dark-mode-theming.md`](08-research-measurement/33-dark-mode-theming.md) |

---

## Dependency Graph

```
PHASE 2 (behavioral UX) + PHASE 1 (Philosophy, Guiding Principles)
        │
        ▼
01 Design Principles ──> 02 Design Language ──┬──> 03 Typography
                                                ├──> 04 Color System
                                                ├──> 05 Icons
                                                └──> 06 Illustrations
        │
        ▼
07 Component Library ──> 08 Cards ──> 09 Motion ──> 10 Animation
        │
        ▼
11 Navigation / 12 Dashboard UX / 13 Widgets / 14 Notifications / 15 Settings UX
        │
        ▼
16 Empty States / 17 Error States / 18 Loading States / 19 Microinteractions / 20 Haptics
        │
        ▼
21 Onboarding UX / 22 Premium UX  (highest business stakes — first impression + monetization)
        │
        ▼
23 Cross-device UX ── 24 Wearables ── 25 Tablet ── 26 Desktop ──> 27 Responsive Design
        │
        ▼
28 Voice UX / 29 Accessibility Implementation
        │
        ▼
30 User Testing ── 31 UX Metrics ── 32 Design System Governance ── 33 Dark Mode & Theming
```

## Writing Order

Group 1 (foundations, blocking) → Group 2 (components, needs Group 1) → Groups 3–5 (surface-specific, need Groups 1–2) → Group 6 (platform adaptation, needs Group 2's components defined first) → Group 7 (needs Group 2's components for accessibility acceptance criteria) → Group 8 (closes the phase — governance and measurement need everything above to govern/measure).

## Critical Path

1. **02 Design Language** — every foundation document (03–06) and therefore everything downstream depends on this.
2. **04 Color System** — the single highest-risk document for the "one assistant, not three apps" mission at the visual layer; per-pillar accent strategy is decided here.
3. **07 Component Library** — the most-referenced document in the entire phase; nearly every Group 3–7 document depends on it.
4. **21 Onboarding UX** — the highest-leverage single surface (only guaranteed universal touchpoint).
5. **29 Accessibility (Design System Implementation)** — must be baked into 03, 04, 07 from the start; retrofitting after launch is far more expensive.
6. **32 Design System Governance & Contribution Model** — without it, everything built in Groups 1–7 degrades within a year as more designers/engineers touch it.

## Estimated Total Documents

**33 documents** (+ this specification = 34 files in the phase).

## Estimated Total Pages

**~215–250 pages** across 33 documents.

---

## VP Design Self-Review

**Coverage assessment: ~99% complete against `phase7.md`'s 31-item required coverage list, all covered, plus 2 gap-closing additions** (Design System Governance & Contribution Model, Dark Mode & Theming — both standard in a mature design system but absent from the original scope list).

What remains open, honestly:

* **Brand identity (logo, brand voice beyond UI tone)** is intentionally out of scope for this phase — Design Language (02) covers the product's visual system, not corporate brand identity, which sits with Marketing/Brand and wasn't named in `phase7.md`'s scope.
* **Motion accessibility (reduced-motion support)** is addressed inside Microinteractions (19) and Accessibility Implementation (29) rather than as a standalone document — folding it in was a deliberate call since it's a cross-cutting constraint on existing documents, not an independent surface.
* **Design tooling/handoff specifics** (which design tool, which token-sync pipeline) are referenced as requirements inside Design System Governance (32) but not vendor-selected, consistent with every other phase's "requirements not final selections" framing.

No other item from `phase7.md`'s required coverage list, and no additional design-system concern the reviewer could identify as necessary before visual design begins, remains undocumented. Phase 7 is ready to move to detailed drafting, with Component Library (07) and Color System (04) recommended for earliest review given how many downstream documents depend on them.
