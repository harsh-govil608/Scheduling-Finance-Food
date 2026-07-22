# Document 23: Cross-Device UX

## Document Name
Cross-Device UX

## Purpose
Define the visual and interaction design of continuity moments across devices — the actual indicators, transitions, and micro-interactions a user sees and feels when a task, conversation, or notification moves from one device to another. This document translates Phase 2's Cross-Device Experience (Document 29) behavioral continuity contract into concrete, designed patterns that every platform team implements identically.

## Why It Exists
Phase 2's Cross-Device Experience document establishes what state must sync and how fast, described entirely from the user's point of view, but it deliberately stops short of specifying what any of that looks like on screen. Without a dedicated design document, each platform team would invent its own visual treatment for handoff — a different icon for "continued from your phone" on tablet than on desktop, a notification that looks unrelated to the wearable tap that dismissed it, or no indicator at all — and the continuity the behavioral contract promises would be undermined by inconsistent visual execution. This document exists so continuity is not just technically true but visibly, consistently, and reassuringly true to the user, on every device, using one shared design vocabulary.

## Approximate Page Count
7-9 pages

## Sections
1. **Continuity Indicator Language** — the shared visual system (icons, labels, subtle animation) used to signal that content, a conversation, or a task originated on or is synced from another device.
2. **Handoff Transition Design** — the on-screen transition pattern when a user resumes an in-progress conversation or task on a new device, e.g., picking up on phone a goal-planning conversation started on desktop.
3. **Cross-Device Notification Visual Treatment** — how a notification eligible to fire on multiple devices is visually distinguished per device and coordinated so it doesn't read as three unrelated alerts.
4. **"Resume Where You Left Off" Entry Points** — the persistent, low-friction UI element (banner, card, prompt) that surfaces an in-progress cross-device task without the user having to search for it.
5. **Multi-Device Session Awareness** — the subtle in-app signal (e.g., "also open on your tablet") that reassures a user the assistant is aware of their other active sessions without being intrusive.
6. **Conflict Surfacing UX** — the visual design for the moment a user is shown that the same item was edited on two devices near-simultaneously, implementing the experience Document 29 describes from the user's perspective.
7. **Sync-in-Progress & Staleness States** — loading, syncing, and "last updated on [device]" indicator design for content that lags gracefully per Document 29's sync-urgency classification.
8. **Device-Aware Content Presentation** — how the same underlying content (e.g., a health insight) is visually reformatted per device without breaking the sense that it's the same assistant speaking.

## Deliverables
* Continuity indicator component spec (icons, labels, animation timing).
* Handoff transition specifications for phone-to-tablet, phone-to-desktop, and wearable-to-phone paths.
* Annotated screens for the three cross-device handoff scenarios defined in Document 29 (one per pillar).
* Cross-device notification visual treatment guide.
* Conflict-surfacing UX mockups for at least one concurrent-edit scenario.

## Dependencies
Requires Cross-Device Experience (Phase 2, Document 29) for the behavioral continuity contract this document gives visual form to, and Client & Mobile Application Architecture (Phase 4, Document 18) for the sync and conflict-resolution mechanics the UX must represent honestly. Requires the Component Library (Phase 7) for base components being extended with continuity indicators, and coordinates with Wearables, Tablet, and Desktop (Phase 7) for how handoff renders on each device.

## Teams
Design, Product, Mobile Engineering, Web/Desktop Engineering, Wearable Engineering, QA

## Completion Criteria
- [ ] Continuity indicator language validated as visually consistent across phone, tablet, desktop, and wearable renderings.
- [ ] All three pillar handoff scenarios from Document 29 have corresponding annotated screens in this document.
- [ ] Cross-device notification treatment reviewed against Document 29's de-duplication rules with no contradicting behavior.
- [ ] Conflict-surfacing UX reviewed and confirmed to match the user-perspective description in Document 29.
- [ ] Signed off by: Head of Design (required), Head of Product (required), Head of Engineering (required, feasibility only).
