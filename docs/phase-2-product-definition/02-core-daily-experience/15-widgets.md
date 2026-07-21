# Document 15: Widgets

## Document Name
Widgets

## Purpose

Define the home-screen and lock-screen widget experience: what glanceable information is allowed to surface outside the main app, across Productivity, Finance, and Health, and how that content is governed so it does not become a second, uncontrolled notification channel. This document specifies the lightest-weight surface in the product's daily-experience system.

## Why It Exists

Widgets sit outside the arbitration rules that govern in-app notifications, which makes them an easy place for individual pillar teams to smuggle in urgency, badges, or nagging content that the Notification System document was specifically written to prevent. This document exists so widget content is deliberately scoped as a calm, glanceable extension of the Dashboard System rather than an unregulated side channel, keeping the product feeling like one assistant even when the user never opens the app.

## Approximate Page Count

5-7 pages.

## Sections

1. **Widget Philosophy: Glance, Not Open** — the core contract that a widget must convey its value in a few seconds without requiring the app to be opened.
2. **Widget Content Eligibility** — the categories of per-pillar information appropriate for a widget versus content reserved for the in-app Dashboard System.
3. **Single vs. Multi-Pillar Widget Strategy** — the philosophy for whether widgets are pillar-specific, combined, or user-configurable, and the rationale for the default configuration.
4. **Update Cadence & Freshness Rules** — the product-level expectations for how current widget data must be, and the specified behavior when data is stale.
5. **Lock-Screen vs. Home-Screen Differences** — what content is appropriate at each surface, given the more limited, glanceable real estate of the lock screen.
6. **Widget-to-App Handoff** — the rule that tapping a widget must deep-link into the relevant context, never simply open to a generic home screen.
7. **Widget Tiers by Trust Level** — how widget content and actionability change as a user's Proactivity Ladder trust level rises, including when an in-place confirmable action becomes available directly on the widget.
8. **Calm/Empty Widget States** — the specified behavior when there is nothing notable to show, so the widget does not manufacture urgency to stay relevant.
9. **Widget Governance & Overlap with Notifications/Dashboard** — the explicit boundary rules preventing widgets from evolving into a second, unregulated notification channel.

## Deliverables

* Approved Widgets document.
* A widget content-eligibility matrix, broken out per pillar and per surface (home screen vs. lock screen).
* A widget-tier-by-trust-level table aligned to the Proactivity Ladder.

## Dependencies

Requires the **Dashboard System** document (Document 13, widgets are a lighter-weight extension of it), the **Notification System** document (Document 14, the governance boundary preventing widget/notification overlap), the **Product (Behavioral) Philosophy Document** (Proactivity Ladder tiering), and the **Personalization** document.

## Which Teams Use This

Product, Design, Engineering.

## Completion Criteria

- [ ] The widget content-eligibility matrix has been validated to contain no item that duplicates a notification-type already governed by the Notification System document.
- [ ] The widget-tier-by-trust-level table has been checked against the Proactivity Ladder and contains at least one tier transition example.
- [ ] The calm/empty widget state has been reviewed and confirmed not to manufacture urgency.
- [ ] Widget-to-app handoff behavior has been specified for at least one scenario per pillar.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
