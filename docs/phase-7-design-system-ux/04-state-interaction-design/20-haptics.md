# Document 20: Haptics

## Document Name
Haptics

## Purpose
Define the haptic feedback patterns used on supported devices — which product moments trigger haptic feedback, what intensity or pattern each uses, and the restraint principle that governs when haptics are deliberately withheld rather than fired.

## Why It Exists
Haptics are a powerful but easily overused channel, and a proactive AI managing three pillars of a user's life has many candidate moments for a haptic buzz — a completed task, a delivered suggestion, a budget alert, a fired reminder — but triggering haptic feedback on every one of them would leave the phone buzzing constantly, which directly contradicts the "never overwhelms" standard set in Phase 1's Guiding Principles. Without a single governing document, individual feature teams would each decide independently whether their moment deserves a buzz, producing a haptic experience that is either fatiguing from overuse or inconsistent from underuse. This document exists to reserve haptic feedback for moments that genuinely matter and to define one map from product moment to haptic pattern, so haptic use across Productivity, Finance, and Health is deliberate rather than incidental.

## Approximate Page Count
5-7 pages.

## Sections
1. **Haptic Feedback Inventory** — the enumerated list of product moments that are candidates for haptic feedback across Productivity, Finance, and Health.
2. **Restraint Principle & Selection Criteria** — the criteria used to decide whether a given moment should receive haptic feedback at all, favoring significant confirmations and warnings over routine, high-frequency taps.
3. **Haptic Pattern Library** — the defined set of haptic intensities and patterns (light tap, success confirmation, warning pulse, and similar) and which category of moment each maps to.
4. **Platform Capability Mapping** — how haptic patterns adapt or degrade gracefully across devices with differing haptic engines and on devices with no haptic engine at all.
5. **Haptics Paired with Microinteractions** — the coordination rule for when a haptic accompanies a visual microinteraction versus when it is used standalone without a paired animation.
6. **User Control Over Haptics** — the settings-level control a user has to reduce, customize, or fully disable haptic feedback, and how that preference is respected system-wide.
7. **Critical vs. Routine Moment Classification** — the classification used to decide which pillar-specific moments (a budget-limit breach versus a routine task check-off, for example) warrant a distinct, more noticeable haptic signature.

## Deliverables
* Approved Haptics document.
* A haptic feedback inventory mapped to defined patterns and per-platform behavior.
* Documented restraint-principle criteria with examples of moments deliberately excluded from haptic feedback.
* A settings specification for user control over haptic intensity and on/off state.

## Dependencies
Requires Microinteractions (Phase 7) for the visual interactions haptics pair with; requires Guiding Principles Document (Phase 1, Doc 07) for the "never overwhelms" tone this restraint principle enforces; requires Settings Philosophy (Phase 2, Doc 33) for where haptic controls live within the settings hierarchy; requires Notification System (Phase 2, Doc 14) for coordination between haptic feedback and notification-triggered alerts.

## Teams
Design, Engineering (Mobile), Product, Accessibility

## Completion Criteria
- [ ] Every haptic-feedback candidate moment has a documented decision: an assigned pattern or a deliberate exclusion.
- [ ] The restraint principle has been validated against at least three example moments that were denied haptic feedback.
- [ ] Platform capability mapping covers graceful degradation on devices without a haptic engine.
- [ ] User-level haptic controls have been specified and linked to the Settings Philosophy document.
- [ ] Signed off by: Head of Design (required), Head of Engineering (required).
