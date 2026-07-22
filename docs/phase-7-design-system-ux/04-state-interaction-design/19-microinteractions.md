# Document 19: Microinteractions

## Document Name
Microinteractions

## Purpose
Define the library of small, purposeful feedback moments — button press, toggle flip, checkbox completion, swipe-to-dismiss a suggestion, pull-to-refresh — that make the interface feel responsive and alive without becoming distracting or gimmicky.

## Why It Exists
Microinteractions are the moments a user feels most often and notices least consciously — every tap, swipe, and toggle across three pillars used daily — so an under-designed set makes the product feel inert or laggy, while an over-designed set feels gimmicky and undercuts the calm, competent tone established in Phase 1's Guiding Principles. Given the sheer volume of recurring interactions across Productivity, Finance, and Health, an ungoverned approach would drift into inconsistency, with the same gesture (dismissing a suggestion, for instance) behaving and feeling differently depending on which pillar or feature team built it. This document exists so every recurring interaction gesture draws from one governed set, and so gestures that carry real consequence — like dismissing an AI suggestion — visually communicate the reversibility promised by the User Control Model.

## Approximate Page Count
6-8 pages.

## Sections
1. **Microinteraction Inventory** — the enumerated list of recurring interaction moments across the product (button press, toggle, checkbox, swipe-to-dismiss, pull-to-refresh, drag-to-reorder) that require a defined feedback pattern.
2. **Feedback Timing & Easing Standards** — the shared timing curves and duration ranges microinteractions must stay within so the interface feels consistently responsive, neither sluggish nor overly snappy.
3. **Swipe-to-Dismiss & Suggestion Gestures** — the specific interaction design for dismissing, snoozing, or acting on an AI suggestion via gesture, including how the motion communicates reversibility per the User Control Model.
4. **Completion & Confirmation Feedback** — the visual feedback pattern for task completion, transaction confirmation, and similar "done" moments, applied consistently across pillars.
5. **Restraint Principles** — the guardrails for when a microinteraction should be minimal or omitted entirely, so frequently repeated actions do not become fatiguing over a day of heavy use.
6. **Sound Pairing (If Applicable)** — how microinteractions coordinate with optional sound design, and the requirement that every microinteraction communicates fully with sound off.
7. **Cross-Platform Microinteraction Parity** — how microinteractions adapt to platform conventions (iOS, Android, web) while remaining recognizably the same product across all of them.
8. **Accessibility & Reduced Motion Behavior** — the required fallback behavior for users with reduced-motion settings enabled, ensuring feedback is still communicated through non-motion means.

## Deliverables
* Approved Microinteractions document.
* A microinteraction inventory mapped to timing and easing specifications.
* A swipe-to-dismiss and suggestion-gesture interaction spec.
* A reduced-motion fallback specification per microinteraction.

## Dependencies
Requires User Control Model (Phase 2, Doc 34) for the reversibility requirements swipe/dismiss gestures must honor; requires Motion & Animation Principles (Phase 7) for shared timing and easing tokens; requires Accessibility (Phase 2, Doc 36) for reduced-motion requirements; requires Haptics (Phase 7) for interactions that pair motion with haptic feedback.

## Teams
Design, Engineering (Frontend), Product, Accessibility

## Completion Criteria
- [ ] Every microinteraction in the inventory has an approved timing and easing specification.
- [ ] Swipe-to-dismiss gestures have been validated to communicate reversibility consistent with the User Control Model.
- [ ] Reduced-motion fallback behavior has been specified and validated for every microinteraction.
- [ ] At least one restraint-principle review has been conducted to confirm no interaction is overdesigned.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
