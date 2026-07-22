# Document 04: Widgets PRD

## Document Name
Widgets PRD

## Purpose
Define the complete engineering-facing specification for home-screen and lock-screen widgets: the concrete widget types and the data each must display, refresh cadence and staleness handling, tap-through deep-link targets, and the per-platform constraints (iOS WidgetKit and Android App Widgets/Glance, at a product-requirements level) that shape what a widget can and cannot do.

## Why It Exists
The Widgets document (Phase 2) establishes that widgets must stay a calm, glanceable extension of the Dashboard rather than an unregulated second notification channel, but it does not specify which concrete widget types exist, what data model each pulls from, or how refresh and tap-through actually behave on-device — without that, iOS and Android platform teams will each interpret "glanceable" differently and widgets will drift out of sync with both the Dashboard System and the Notification System's governance. This PRD exists so the widget surface stays product-defined rather than platform-API-defined, since iOS and Android widget frameworks impose real constraints (refresh budgets, interactivity limits) that must be reconciled with the product's calm/glance philosophy rather than silently overriding it.

## Approximate Page Count
7-9 pages.

## Sections
1. **Feature Scope** — in scope: widget type definitions and their data contracts, home-screen and lock-screen size variants, refresh cadence and staleness rules, tap-through deep-linking, trust-level-gated interactivity; out of scope: the underlying pillar logic that produces widget content (owned by each pillar's PRD), and platform-specific rendering/implementation details beyond product-requirements-level constraints.
2. **User Stories** — e.g., as a user with a small home-screen widget, I can see today's next task or upcoming bill without opening the app; as a user on the lock screen, I see only the single most time-sensitive item, not a dense multi-pillar summary; as a user whose widget data goes stale because the app hasn't refreshed in the background, I see a visibly stale state rather than confidently wrong information.
3. **Functional Requirements** — the defined widget type catalog (e.g., single-pillar summary, combined cross-pillar snapshot, next-action widget) and the data field contract for each; refresh cadence rules per widget size/platform and the explicit staleness threshold and visual treatment; tap-through deep-link targets that land the user in the exact relevant context, never a generic home screen; the interactivity tier available at each Proactivity Ladder rung (e.g., a confirmable action rendered directly on a widget only once a user has reached a sufficient trust level).
4. **Non-Functional Requirements** — compliance with iOS WidgetKit and Android App Widget/Glance background-refresh budgets and battery constraints described at a product-requirements level, a hard rule that widget refresh never triggers a push-equivalent interruption, and a privacy requirement for lock-screen widgets to withhold sensitive amounts/content by default given the reduced-security context of a lock screen.
5. **UX Requirements** — must conform to the Widgets document's glance-not-open philosophy, content-eligibility matrix, and widget-tier-by-trust-level table, the Dashboard System's content-eligibility rules for what may appear at all, and the Notification System's governance boundary preventing widgets from becoming a second unregulated notification channel.
6. **States & Flows** — Fresh, Stale-Visible, Empty/Calm, Locked-Insufficient-Trust (content available at app but not yet at widget tier), Actionable-Pending-Confirmation (higher trust tiers only), Tapped-Through.
7. **Edge Cases** — a widget added to the lock screen displaying content the user would not want visible to someone glancing at their phone; a user with multiple widgets of different pillars simultaneously stale due to a single background-refresh failure; a widget resized by the OS between size classes with different data-density requirements; a user who removes and re-adds a widget losing or retaining prior configuration.
8. **Failure Scenarios** — what happens when the core assumption "the widget can refresh often enough to stay accurate" breaks under OS-imposed background-refresh throttling: the specified staleness fallback, and what happens when a tap-through deep link target no longer exists (e.g., the referenced task was deleted from another device since the widget last refreshed).
9. **AI Behaviors** — how widget content selection reflects the same prediction/prioritization logic as the Dashboard rather than a fixed per-pillar template, and how the widget-tier-by-trust-level table governs the transition from purely informational to directly actionable widget content as a user climbs the Proactivity Ladder.
10. **Notification Behaviors** — the explicit rule set preventing a widget refresh or content change from functioning as a de facto notification (e.g., no attention-grabbing animation or badge-equivalent on refresh), and how widget content is required to stay consistent with, but never duplicate, an item already delivered through the Notification System.
11. **Success Criteria** — a user can glance at a widget and understand their most relevant single piece of context in under a few seconds, with no widget ever manufacturing urgency to stay visually relevant.
12. **Metrics** — widget add/retain rate, tap-through rate by widget type, staleness incidence rate, and interactivity-tier adoption rate as users climb the Proactivity Ladder.
13. **Open Questions** — whether combined cross-pillar widgets should be offered by default or only after a user demonstrates multi-pillar engagement; how much interactivity Android's Glance framework should be allowed versus iOS's more constrained WidgetKit interactivity, and whether that asymmetry is acceptable product-wide.

## Deliverables
* Approved Widgets PRD.
* Widget type catalog with data contracts per type and size class.
* A refresh-cadence and staleness-handling table per platform.
* A widget-tier-by-trust-level interactivity table.

## Dependencies
Requires the **Widgets** document (Phase 2, Document 15) for philosophy and content-eligibility rules, the **Dashboard System** document (Phase 2, Document 13), the **Notification System** document (Phase 2, Document 14) for the governance boundary, the **Automation Philosophy** document (Phase 2) for the Proactivity Ladder interactivity gating, and the **Product Philosophy Document** (Phase 1).

## Teams Using This
Product, Engineering (iOS), Engineering (Android), Design, QA.

## Completion Criteria
- [ ] Widget type catalog and data contracts reviewed and accepted by both iOS and Android platform engineering leads.
- [ ] Refresh-cadence and staleness rules validated against real iOS WidgetKit and Android App Widget background-refresh constraints.
- [ ] Widget-tier-by-trust-level table cross-checked against the Automation Philosophy document with no contradiction.
- [ ] Lock-screen content-sensitivity defaults reviewed and confirmed to withhold sensitive data by default.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Design (required).
