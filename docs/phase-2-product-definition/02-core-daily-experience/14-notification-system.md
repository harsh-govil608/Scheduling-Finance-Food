# Document 14: Notification System

## Document Name
Notification System

## Purpose

Define the product-level philosophy and rules for every proactive interruption the AI is allowed to make: the taxonomy of notification types, the priority arbitration used when Finance, Health, and Productivity simultaneously want the user's attention, and the quiet-hours and batching logic that keeps the system from overwhelming the user. This document is the operational rulebook behind the "never overwhelm" commitment.

## Why It Exists

"Never overwhelm" cannot be enforced by three independent pillar teams each shipping their own reminder logic — left alone, a Finance spend alert, a Health water reminder, and a Productivity deadline nudge will all fire in the same ten-minute window with no shared concept of a daily interruption budget. This document exists so there is exactly one arbitration authority for proactive interruptions across the whole product, directly operationalizing the Never Overwhelm section of the Product (Behavioral) Philosophy Document and the automation boundaries set in the Automation Philosophy document.

## Approximate Page Count

8-10 pages.

## Sections

1. **Notification Taxonomy** — the defined types (proactive suggestion, reminder, urgent alert, confirmation-needed, celebratory/encouragement) and which step of the Behavioral Loop each type corresponds to.
2. **Interruption Budget** — the product-level rules for a maximum number of proactive interruptions per day and per time-window, defined as a single shared budget across all three pillars rather than a budget per pillar.
3. **Cross-Pillar Priority Arbitration** — the decision rules for what wins, what waits, and what gets merged when Finance, Health, and Productivity all want the user's attention at the same moment.
4. **Quiet Hours & Context-Aware Suppression** — the rules for respecting sleep, focus, and meeting contexts, including the requirement that suppressed notifications are deferred rather than silently dropped.
5. **Batching & Digesting Logic** — the rules for when multiple lower-priority items are combined into a single notification instead of arriving as separate interruptions.
6. **Trust-Level Gating of Notification Types** — how a user's current rung on the Proactivity Ladder determines which notification types (e.g., an autonomous-action confirmation) are even eligible to fire for that user.
7. **Escalation & De-escalation Rules** — the specified behavior when a notification goes unacknowledged: whether it escalates, downgrades, or expires, with an explicit rule against nagging.
8. **Channel Selection Philosophy** — the product-behavior decision rules for choosing push, in-app, widget, or voice as the channel for a given notification, independent of technical delivery implementation.
9. **Notification Failure Modes to Prevent** — an explicit, named list of anti-patterns (duplicate cross-pillar pings, guilt notifications, red-badge bait) carried forward from the Product (Behavioral) Philosophy Document's anti-patterns section.

## Deliverables

* Approved Notification System document.
* A priority-arbitration decision table (pillar × urgency × trust level).
* A quiet-hours and batching rule set usable directly by every pillar team.

## Dependencies

Requires the **Product (Behavioral) Philosophy Document** (Never Overwhelm, Proactivity Ladder, anti-patterns), the **Automation Philosophy** document (what may be automated without a notification at all), the **Guiding Principles Document**, and the **Daily Flow** document (Document 10, the arbitration mechanics referenced there are fully specified here).

## Which Teams Use This

Product, Design, Engineering, Data Science/ML, Trust & Safety.

## Completion Criteria

- [ ] The cross-pillar priority arbitration table has been validated against at least one scenario where all three pillars compete simultaneously.
- [ ] The interruption budget is defined as a single shared number, not a sum of three independent per-pillar budgets.
- [ ] Every notification type in the taxonomy has an explicit trust-level gate defined against the Proactivity Ladder.
- [ ] Escalation/de-escalation rules have been checked against the anti-patterns list and confirmed to contain no nagging pattern.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Trust & Safety (required).
