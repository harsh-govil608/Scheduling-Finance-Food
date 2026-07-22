# Document 14: Notifications (Visual)

## Document Name
Notifications (Visual)

## Purpose
Define the actual visual and interaction design of every on-screen notification surface — the in-app banner/toast, the system push presentation, and the Notification Center list view — implementing the arbitration and taxonomy rules from Phase 2's Notification System document (Document 14) and the persistence, grouping, and status model from Phase 3's Notification Center PRD (Document 03). This document decides how a notification looks and how the Center is laid out; it does not decide whether, when, or how often a notification fires.

## Why It Exists
Phase 2's Notification System defines the taxonomy, priority arbitration, quiet-hours logic, and anti-patterns to avoid, and Phase 3's Notification Center PRD defines the durable states a stored notification can be in (delivered-unread, deferred-pending, batched-into-digest, and so on) — but neither specifies what a five-type taxonomy actually looks like on-screen, how a deferred item visually reads as "deferred, not lost," or how a digest visually expands into its constituent items. Without this document, a well-arbitrated, well-persisted notification system can still fail its own anti-pattern rules visually — for example, an unread count that is behaviorally justified but is styled exactly like the red-badge bait the Notification System explicitly forbids. This document exists so the taxonomy, escalation states, and Center states are each given one deliberate visual form that reinforces calm rather than urgency by default.

## Approximate Page Count
7-9 pages.

## Sections
1. **Notification Type Visual Language** — the distinct visual treatment (color, iconography, weight) for each type in the Notification System's taxonomy (proactive suggestion, reminder, urgent alert, confirmation-needed, celebratory/encouragement), ensuring urgency is visually proportionate to actual priority.
2. **In-App Banner & Toast Design** — the layout, placement, entrance/exit motion, and dismissal interaction for a notification surfaced while the user is already in the app.
3. **System Push Presentation** — the visual content and formatting rules for the OS-level push notification, including how much of the Notification System's taxonomy distinction can survive the platform's push UI constraints.
4. **Notification Center List View** — the visual layout of the persistent inbox from the Notification Center PRD, including grouping by pillar/type/day, read/unread visual distinction, and the deferred-and-later-delivered status marker.
5. **Digest & Batching Visual Expansion** — how a batched digest notification visually presents its constituent items and the interaction pattern for expanding/collapsing them, implementing the Notification Center PRD's Batched-Into-Digest state.
6. **Unread & Badge Treatment** — the specific, deliberately restrained visual approach to unread counts and badges, cross-checked directly against the Notification System's and Product Philosophy's anti-pattern rules against red-badge bait.
7. **Action-Replay Interaction Design** — the visual affordance and interaction flow for triggering a stored notification's original action directly from the Center, implementing the Notification Center PRD's action-replay requirement.
8. **AI-Did-This Visual Marker** — the distinct visual treatment for autonomous-action notifications versus suggestion-only entries, implementing the Notification Center PRD's AI Behaviors section at the visual level.
9. **Empty & Zero-State Design** — the visual treatment of the Notification Center when there is nothing to review, consistent with the calm tone established elsewhere in this surface family.

## Deliverables
* Approved Notifications (Visual) specification.
* Annotated mockups for each notification type across banner, push, and Center list contexts.
* A Notification Center layout spec covering all states from the Notification Center PRD (Delivered-Unread through Expired/Archived).
* An unread/badge visual-treatment review checklist mapped to the anti-pattern list.

## Dependencies
Requires Notification System (Phase 2, Document 14) for taxonomy, arbitration, and anti-pattern rules; requires Notification Center PRD (Phase 3, Document 03) for the persisted state model this document renders; requires Navigation (Visual) (Phase 7, Document 11) for how the Center is reached; requires Component Library, Color System, Iconography System, and Motion Principles (Phase 7).

## Teams
Product, Design, Engineering (Client), Engineering (Backend/Notifications), QA.

## Completion Criteria
- [ ] Every notification type in the Notification System's taxonomy has a distinct, approved visual treatment proportionate to its actual priority.
- [ ] Every state defined in the Notification Center PRD has an approved corresponding visual layout, including deferred-and-later-delivered.
- [ ] Unread/badge visual treatment reviewed against the Notification System's and Product Philosophy's anti-pattern lists with no red-badge-bait pattern present.
- [ ] Action-replay interaction validated for at least one notification type per pillar.
- [ ] Signed off by: Head of Design (required), Head of Product (required), Head of Trust & Safety (required).
