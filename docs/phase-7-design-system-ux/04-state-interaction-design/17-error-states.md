# Document 17: Error States

## Document Name
Error States

## Purpose
Define the visual and copy design pattern for every error state across the product — how errors are presented so they inform without alarming, implementing Phase 2's Error Recovery Experience document (Doc 35) at the visual layer. This covers the full range from a minor inline validation issue to a failed sync to an AI mistake surfaced to the user.

## Why It Exists
An AI that "never overwhelms" cannot present errors in a way that spikes user anxiety — red full-screen takeovers, alarming iconography, or panicked copy for a minor, easily corrected issue — even when the underlying error-handling and correction-flow logic defined in Doc 35 is sound. Without a systematic error-state design, severity gets miscommunicated visually: a trivial validation error might look as dire as a failed payment sync, and a genuinely important AI mistake might be rendered so lightly it goes unnoticed. This document exists so every visible error, regardless of which pillar or surface it originates from, is rendered through one severity-aware system that matches the calm, accountable tone the product has committed to.

## Approximate Page Count
6-8 pages.

## Sections
1. **Error Severity Visual Mapping** — how Doc 35's error and mistake categories map to visual treatment (inline message, toast, banner, or modal), so severity is communicated by presentation, not just wording.
2. **Tone in Error Copy** — how error messages stay calm and actionable rather than alarming or robotic, per Phase 1's Guiding Principles tone standard, including required and forbidden phrasing patterns.
3. **Iconography & Color for Errors** — the restrained icon and color palette reserved for error states, distinct from the palette used for destructive-but-intentional actions, so errors read as "something to fix" rather than "something catastrophic."
4. **AI-Mistake Error Treatment** — the distinct visual and copy pattern for surfacing that the AI itself made a mistake (miscategorized transaction, missed reminder, misread food photo), including the correction affordance defined behaviorally in Doc 35.
5. **Inline Validation Error Pattern** — the standard for lightweight, in-context errors (form fields, quick-entry flows) that never interrupt the user's flow with a modal.
6. **System & Connectivity Error Pattern** — the visual treatment for errors outside the user's or AI's control (network failure, service outage, sync failure), distinguished from user-actionable and AI-mistake errors.
7. **Error-to-Recovery Visual Continuity** — how an error state visually hands off into its correction or retry flow so the path out of the error is as clear as the error itself.
8. **Cross-Pillar Consistency Requirements** — the shared error grammar applied identically across Productivity, Finance, and Health so an error in one pillar looks and feels like an error from the same assistant as in another.

## Deliverables
* Approved Error States document.
* A severity-to-visual-treatment mapping table covering every error category in Doc 35.
* An error iconography and color specification, distinct from destructive-action styling.
* Copy guidelines with approved/forbidden phrasing examples per severity tier.

## Dependencies
Requires Error Recovery Experience (Phase 2, Doc 35) for the underlying error taxonomy and correction-flow behavior this document renders visually; requires Guiding Principles Document (Phase 1, Doc 07) for tone boundaries; requires Color System (Phase 7) and Illustrations (Phase 7) for the visual tokens used; requires Loading States (Phase 7) for the handoff when a loading state resolves into an error.

## Teams
Design, Product, Content/Copy, Engineering (Frontend), Customer Support

## Completion Criteria
- [ ] Every error category in Doc 35's taxonomy has an approved visual treatment mapped to a severity tier.
- [ ] Error copy has been validated against at least one worked example per severity tier and per pillar.
- [ ] The AI-mistake error pattern has been reviewed to confirm it reads as accountable rather than alarming.
- [ ] Error-to-recovery visual continuity has been validated for at least one full correction flow.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
