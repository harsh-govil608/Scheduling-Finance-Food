# Document 30: Offline Experience

## Document Name
Offline Experience

## Purpose

Define what the user can still do without connectivity, and what the reconciliation experience looks and feels like when connectivity returns — entirely from the user's point of view, not the technical sync implementation.

## Why It Exists

An assistant whose entire premise is removing manual work and never overwhelming the user loses that trust instantly if it silently fails to log something offline, double-logs it on reconnection, or dumps a wall of catch-up notifications the moment the network returns. Target users will routinely experience patchy connectivity, so offline behavior cannot be left to engineering default — it needs explicit, user-facing rules for what is captured offline, how the user knows what is and isn't working, and how reconciliation is communicated without violating "Never Overwhelm." This document exists so offline and reconnection moments are designed intentionally, before any sync engineering is scoped.

## Approximate Page Count

6-8 pages.

## Sections

1. **The Offline Capability Principle** — states that core capture (logging, note-taking) must degrade gracefully rather than block the user, while proactive intelligence (suggestions, predictions) may pause with clear, calm signaling.
2. **What Works Fully Offline** — enumerates offline-safe actions per pillar (manual expense entry, meal logging, task capture, habit check-ins) that require no network dependency at all.
3. **What Is Degraded or Paused Offline** — enumerates proactive/predictive behaviors (spend prediction, context-aware notifications, dynamic rescheduling) that pause while offline, and how — or deliberately whether not — that pause is communicated to the user.
4. **Offline Status Indication** — the UX rules for if and how the user is told they are offline and what is currently unavailable, calibrated against the "Never Overwhelm" principle rather than defaulting to constant status chatter.
5. **The Reconciliation Moment** — what the user experiences the instant connectivity returns: what gets synced, in what order, and how much of that process is surfaced to the user versus handled quietly in the background.
6. **Conflict Surfacing During Reconciliation** — from the user's point of view, what happens when an offline-created entry conflicts with something the AI or the user did elsewhere while offline, cross-referenced with the Cross-Device Experience Document for the multi-device angle.
7. **Trust & Irreversibility While Offline** — reaffirms that no irreversible or high-trust-level proactive action is ever queued to fire automatically the moment connectivity returns without the confirmation appropriate to the user's current Proactivity Ladder level.
8. **Worked Scenarios per Pillar** — one full offline-to-reconciliation scenario for each of Productivity, Finance, and Health.
9. **Out of Scope** — explicitly excludes local storage architecture, sync queue engineering, and conflict-resolution algorithms, noting these belong to later engineering phases.

## Deliverables

* Approved Offline Experience document.
* An Offline Capability Matrix mapping action × pillar × offline-availability tier (fully available, degraded, paused).
* Three worked offline-to-reconciliation scenarios, one per pillar.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Proactivity Ladder and "Never Overwhelm" rules governing what may resume automatically on reconnection; the Cross-Device Experience Document (29) for overlapping reconciliation and conflict UX; and the Product Pillars Overview (Document 02) for the per-pillar capture actions that must remain available offline.

## Which Teams Use This

Product, Design, Engineering, Data Science/ML, QA, Trust & Safety.

## Completion Criteria

- [ ] Every capture action in the Offline Capability Matrix is classified into a clear availability tier (fully available, degraded, paused) with no unclassified actions.
- [ ] The Reconciliation Moment has been walked through for a scenario with at least three queued offline entries.
- [ ] At least one worked offline-to-reconciliation scenario exists per pillar (Productivity, Finance, Health).
- [ ] Reconciliation notification volume has been checked against the "Never Overwhelm" rules in the Product Philosophy Document.
- [ ] Confirmed no irreversible or high-trust action is specified to auto-fire on reconnection without appropriate confirmation.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Trust & Safety Lead (required).
