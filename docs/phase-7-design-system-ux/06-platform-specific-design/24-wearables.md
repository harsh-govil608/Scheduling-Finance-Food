# Document 24: Wearables

## Document Name
Wearables

## Purpose
Define the visual design constraints and interaction patterns appropriate to wearable displays — glanceable content, minimal interaction, and voice-first fallback — for the smartwatch and fitness-tracker surfaces the AI Life OS supports. This document specifies what the assistant actually shows and how a user actually interacts on a screen viewable for only a second or two at a time.

## Why It Exists
Phase 4's Wearables document (Document 49) defines how sleep, workout, and vitals data gets *into* the platform from a fragmented ecosystem of manufacturer APIs and OS-level health aggregators, but it does not define what the assistant renders *back* to the user on that same small screen. Wearables have the least screen real estate and the shortest attention windows of any supported form factor — a design that simply shrinks the phone UI will truncate text, miss tap targets, and turn a two-second glance into a frustrating fumble. This document exists to define an intentionally reduced, glanceable-first design language for wearables, so the platform team designs for this constraint deliberately rather than discovering it after a phone-scaled prototype fails on-wrist.

## Approximate Page Count
6-8 pages

## Sections
1. **Glanceable Content Hierarchy** — what a user must be able to understand within a 1-2 second glance, and the priority order for additional information shown if the user's attention lingers.
2. **Complication & Watch-Face Integration** — how core AI Life OS signals (next task, today's spend, sleep score) surface via native complications or widgets on the wearable's home face without opening the app.
3. **Minimal-Interaction Patterns** — the reduced interaction vocabulary appropriate to a wearable (single tap, short scroll, brief voice command) and what interactions are explicitly excluded, such as multi-field forms or long text entry.
4. **Notification & Haptic Alert Design** — the visual and haptic treatment of proactive nudges on-wrist, distinguishing urgency levels within a far smaller design vocabulary than phone notifications allow.
5. **Voice-First Fallback for Wearable Input** — how voice becomes the primary input mode when screen-based interaction is impractical, and how the wearable visually and haptically confirms a voice command was understood.
6. **Typography & Legibility at Wearable Scale** — minimum type sizes, contrast ratios, and content-truncation rules validated for wrist-worn viewing distance and real-world conditions like motion and sunlight.
7. **Cross-Platform Wearable Variance** — how the design language accommodates differing screen shapes, sizes, and inputs (round vs. square face, rotating crown vs. touch-only) across the platform categories defined in Phase 4's Wearables document.
8. **Deferral to a Richer Device** — the explicit visual pattern for when a flow is too complex for the wearable, prompting the user to continue on phone, tablet, or desktop instead of forcing it on-wrist.

## Deliverables
* Wearable design specification covering glanceable hierarchy, typography, and legibility standards.
* Complication/widget mockups for at least one signal per pillar (Productivity, Finance, Health).
* Notification and haptic pattern library with mapped urgency levels.
* Voice-confirmation visual pattern for wearable voice input.
* Deferral pattern spec for redirecting complex flows to another device.

## Dependencies
Requires Wearables (Phase 4, Document 49) for the underlying data model, platform categories, and sync-cadence constraints this design must present honestly. Requires Cross-Device Experience (Phase 2, Document 29) for the wearable interaction constraints it establishes, and Cross-Device UX (Phase 7, Document 23) for the continuity indicator language reused here. Requires the Component Library (Phase 7) and coordinates with the Voice & Accessibility document group (Phase 7) for legibility and voice-confirmation standards.

## Teams
Design, Wearable Engineering, Product (Health pillar), Accessibility, QA

## Completion Criteria
- [ ] Glanceable content hierarchy validated against a 1-2 second comprehension test for each pillar's top wearable signal.
- [ ] Typography and legibility standards validated against minimum wrist-worn viewing conditions (motion, sunlight, small round face).
- [ ] Notification and haptic urgency mapping reviewed against the wearable interaction constraints in Document 29.
- [ ] Deferral pattern confirmed to route every out-of-scope wearable flow to a specific alternate device, with no dead ends.
- [ ] Signed off by: Head of Design (required), Head of Wearable Engineering (required), Head of Product — Health pillar (required).
