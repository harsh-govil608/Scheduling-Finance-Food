# Document 29: Cross-Device Experience

## Document Name
Cross-Device Experience

## Purpose

Define what state, context, and conversational continuity must carry seamlessly across phone, tablet, desktop, and wearable, described entirely from the user's point of view, so switching devices never feels like switching assistants.

## Why It Exists

The mission promises one intelligent assistant, not three apps — but that promise is just as easily broken across devices as it is across pillars: a reminder dismissed on a wearable that still nags on the phone, or a goal-planning conversation started on desktop that has to be re-explained on mobile, both tell the user the AI does not actually remember them. Without an explicit continuity contract, each platform team will make independent, locally reasonable decisions about what syncs and how fast, and the seams will show precisely where the product is claiming to be seamless. This document exists so continuity requirements are set once, from the user's experience outward, before any platform-specific engineering begins.

## Approximate Page Count

7-9 pages.

## Sections

1. **The Continuity Principle** — states that switching devices must never require the user to re-explain, re-confirm, or lose context the assistant had already established.
2. **State That Must Sync Immediately** — enumerates high-priority state (task completion, expense confirmation, reminder dismissal, active conversation context) that must be reflected across devices with no perceptible delay, from the user's perspective.
3. **State That Can Lag Gracefully** — enumerates lower-priority state (historical trend charts, long-form insights) where brief staleness is acceptable, and defines what "acceptable" means in user-perceived time.
4. **Device-Specific Capability Differences** — what is reasonable to expect only on certain devices (voice-first glanceable interaction on wearable, detailed dashboards on desktop, quick capture on phone) and how the AI adapts presentation without breaking the single-assistant feel.
5. **Notification & Interruption Coordination Across Devices** — the rules that prevent duplicate or conflicting notifications for the same event firing across multiple devices at once.
6. **Starting on One Device, Finishing on Another** — worked handoff scenarios (e.g., a goal-planning conversation started on desktop, nudged on phone, confirmed with a wearable tap) illustrating the experience of continuity in practice.
7. **Wearable-Specific Interaction Constraints** — the deliberately reduced, glanceable interaction model appropriate to a wearable, and what is explicitly deferred to phone or desktop instead.
8. **Conflict Resolution From the User's Perspective** — what a user sees and experiences — not the technical merge logic — when the same item was edited on two devices at nearly the same time.
9. **Out of Scope** — explicitly excludes sync protocol design, conflict-resolution algorithms, and offline queuing engineering, pointing to the Offline Experience Document for the connectivity-loss side of this problem.

## Deliverables

* Approved Cross-Device Experience document.
* A State Continuity Matrix mapping state type × device × required sync urgency.
* Three worked cross-device handoff scenarios, one per pillar.

## Dependencies

Requires the Product Architecture Overview (Document 01) for the shared Memory Model and notification layer that continuity depends on; the Offline Experience Document (30) for the overlapping reconciliation and conflict UX when connectivity is also a factor; and the relevant pillar experience documents (Pillar Experiences group) for pillar-specific state that must carry across devices.

## Which Teams Use This

Product, Design, Engineering (Platform/Multi-device), QA, Data Science/ML (context engine).

## Completion Criteria

- [ ] Every state type in the Continuity Matrix is classified as either immediate-sync or graceful-lag, with a stated user-perceived time bound for each.
- [ ] At least one cross-device handoff scenario has been walked through per pillar (Productivity, Finance, Health).
- [ ] Notification de-duplication rules across devices have been validated against a same-event, multi-device scenario.
- [ ] Wearable interaction constraints have been reviewed against the "Never Overwhelm" rules in the Product Philosophy Document.
- [ ] Confirmed this document does not attempt to define sync protocol or conflict-resolution engineering (reserved for later phases).
- [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Engineering (required, feasibility only).
