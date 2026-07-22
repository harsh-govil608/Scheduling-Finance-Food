# Document 03: Notification Center PRD

## Document Name
Notification Center PRD

## Purpose
Define the complete engineering-facing specification for the Notification Center — the persistent inbox that stores, groups, and lets a user review every notification the system has ever surfaced, regardless of which pillar generated it or whether it arrived as push, in-app, or widget. This PRD covers the storage, grouping, read/unread, and action-replay mechanics of that inbox; it does not define which notifications get sent or when.

## Why It Exists
The Notification System document (Phase 2) is the arbitration rulebook for what fires and when, but it deliberately does not specify a persisted, reviewable record of what already fired — without one, a suppressed or batched notification the user missed during quiet hours has no durable home, and "deferred rather than silently dropped" (a requirement of the Notification System) has no surface to actually land on. This PRD exists so every notification the arbitration engine decides to send has one canonical, cross-pillar place to live afterward, so a user can always answer "what did I miss" without hunting through three pillar-specific histories.

## Approximate Page Count
7-10 pages.

## Sections
1. **Feature Scope** — in scope: notification persistence, read/unread and grouping logic, cross-pillar chronological and categorical views, deferred-notification surfacing, action-replay from a stored notification; out of scope: the arbitration, budget, and quiet-hours logic that decides whether and when a notification is generated in the first place, owned entirely by the Phase 2 Notification System document and its eventual arbitration-engine implementation.
2. **User Stories** — e.g., as a user who was asleep during quiet hours when a Finance alert was deferred, I can open the Notification Center in the morning and see it clearly marked as deferred, not lost; as a user who dismissed a suggestion by accident, I can find it again and act on it days later; as a user reviewing my week, I can see notifications grouped by pillar and by day rather than one long undifferentiated feed.
3. **Functional Requirements** — durable storage of every notification the arbitration engine emits (including ones batched into a digest, with the digest's constituent items individually retrievable), read/unread state tracking, grouping by pillar/type/day, a distinct visual/status marker for deferred-and-later-delivered notifications, and the ability to trigger the original suggested action directly from a stored notification's card.
4. **Non-Functional Requirements** — a retention window definition (how long notifications remain browsable before archival/deletion), a strict requirement that the Notification Center never re-triggers arbitration or duplicate delivery merely by being opened, and a consistency requirement that read state syncs across the user's devices per the Cross-Device Experience document.
5. **UX Requirements** — must conform to the Notification System's taxonomy (proactive suggestion, reminder, urgent alert, confirmation-needed, celebratory/encouragement) for grouping and iconography, the Notification System's escalation/de-escalation rules for how a stored item's status changes over time, and the Product Philosophy Document's anti-pattern list (no red-badge bait accumulating unread counts that manufacture urgency).
6. **States & Flows** — Delivered-Unread, Delivered-Read, Deferred-Pending, Deferred-Delivered, Batched-Into-Digest (with expandable constituents), Action-Taken-From-Center, Expired/Archived.
7. **Edge Cases** — a notification's underlying action becomes invalid between delivery and the user opening the Center (e.g., a bill referenced in an old alert was already paid elsewhere); a digest notification whose constituent items originated from different pillars; a user with hundreds of unread items from a period of not opening the app; an escalated notification that changed priority after it was already stored.
8. **Failure Scenarios** — what happens when the core assumption "every arbitrated notification is durably captured" breaks: the storage write fails after a push is already delivered (producing a phantom notification with no Center record), or a deferred notification's later-delivery event never reaches the Center, leaving a silent gap the Notification System explicitly forbids.
9. **AI Behaviors** — how the Notification Center reflects the user's current Proactivity Ladder rung (e.g., autonomous-action notifications include a visible "AI did this" record distinct from suggestion-only entries), and how patterns of ignored-then-later-read items in the Center feed back into future arbitration weighting.
10. **Notification Behaviors** — the Notification Center is the durable record of the Notification System's arbitration decisions; this section specifies exactly which arbitration outputs (including suppressed/deferred ones) are guaranteed to be written to the Center and the maximum allowable delay between arbitration decision and Center visibility.
11. **Success Criteria** — a user can always reconstruct "what did the AI try to tell me" for any point in the past retention window, with no arbitrated notification silently missing from the record.
12. **Metrics** — unread-at-open count distribution, time-to-read for deferred items, action-replay rate from stored notifications, and Center-open frequency relative to push-open frequency.
13. **Open Questions** — whether celebratory/encouragement notifications should have a shorter retention window than actionable ones; how grouping should behave once a user has notifications spanning all three pillars in the same batched digest; whether the Center needs its own search versus relying entirely on the unified Search PRD.

## Deliverables
* Approved Notification Center PRD.
* Notification persistence and grouping data model.
* A deferred-notification delivery guarantee specification cross-checked against the Notification System's quiet-hours rules.

## Dependencies
Requires the **Notification System** document (Phase 2, Document 14) for taxonomy, arbitration, and escalation rules this feature persists and displays, the **Dashboard System** document (Phase 2, Document 13) for the boundary between dashboard and notification content, the **Cross-Device Experience** document (Phase 2, Document 29) for read-state sync, the **Search PRD** (Document 05) for potential shared search infrastructure, and the **Product Philosophy Document** (Phase 1) for anti-pattern rules.

## Teams Using This
Product, Engineering (Client), Engineering (Backend/Notifications), Design, QA.

## Completion Criteria
- [ ] Every arbitration output type defined in the Notification System document has a specified, verified persistence path into the Center.
- [ ] Deferred-notification delivery guarantee validated against at least one quiet-hours scenario end-to-end.
- [ ] Read-state sync behavior confirmed consistent with the Cross-Device Experience document.
- [ ] No notification state in this PRD contradicts the Notification System's anti-pattern list (no badge bait, no nagging).
- [ ] Signed off by: Head of Product (required), Engineering Lead (required).
