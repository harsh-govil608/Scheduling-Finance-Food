# Document 21: Onboarding UX

## Document Name
Onboarding UX

## Purpose
Define the visual and interaction design of the first-run experience — screen-by-screen layout, progress indication, permission-request visual treatment, and how the three pillars are visually introduced as facets of one assistant — implementing the sequencing and pacing rules already established in Phase 2's Onboarding Experience document (Doc 31) and the concrete screens and states committed to in Phase 3's Onboarding PRD (Doc 40). This document specifies what the onboarding visual design must contain and achieve; it does not itself contain final pixel-level mockups or a shipped component set.

## Why It Exists
Onboarding is the single highest-leverage design surface in the product — it is the only moment every user is guaranteed to see, it is the first time the user meets the AI, and it is the point at which the behavioral requirements already defined (permission sequencing starting at the lowest Proactivity Ladder rung, per-pillar "first value" moments, skip/defer states) either convert into a trusted, activated user or collapse into confusion and drop-off. A philosophy document and a feature PRD can specify what must happen and in what order, but neither specifies how it looks or feels moment to moment; without a dedicated visual/interaction requirements document, each pillar team would independently interpret pacing, progress feedback, and permission framing, producing a first-run experience that reads as three inconsistent wizards bolted together rather than one calm assistant introducing itself. This document exists so the highest-stakes design surface in the product is engineered deliberately, once, against the requirements already committed upstream.

## Approximate Page Count
8-10 pages.

## Sections
1. **Screen-by-Screen Visual Flow** — the layout requirements for each first-run screen and state named in the Onboarding PRD's screen sequence, including account creation, pillar introduction, and per-pillar setup.
2. **Progress Indication System** — how a multi-step, multi-pillar onboarding communicates forward motion without feeling like a long checklist, including requirements for how progress is shown across pillars a user has not yet engaged.
3. **Permission Request Visual Treatment** — how the plain-language, just-in-time permission asks defined in the Onboarding Experience and Permissions & Consent documents are visually framed, including the required relationship between a value moment and the permission screen that follows it.
4. **Pillar Introduction Visual Language** — the visual requirements for distinguishing Productivity, Finance, and Health from one another while still reading as one coherent assistant, not three separately branded setup wizards.
5. **First-Value Moment Visual Design** — the visual requirements for the specific "aha" moment defined per pillar in the Onboarding PRD, ensuring the result (a categorized transaction, a scheduled task, a logged meal) is presented as a real, legible outcome rather than a placeholder or sample.
6. **Skip/Defer Visual Affordances** — how a skippable step is visually marked as optional, and how the resulting degraded state is communicated in-UI without implying a dead end or broken feature.
7. **Motion & Transition Principles for First-Run** — the pacing and transition requirements between onboarding screens, ensuring motion reinforces calm progression rather than urgency or friction.
8. **Interruption & Failure State Handling** — the visual requirements for a first-run session interrupted by an OS-level permission dialog, network loss, or an unavailable backend dependency, consistent with the failure scenarios defined in the Onboarding PRD.
9. **Returning-User / Re-Onboarding Visual Differentiation** — how the re-onboarding experience (triggered by long absence, permission revocation, device change, or reinstall) is visually distinguished from first-run so a returning user is not made to feel like a brand-new user.
10. **Accessibility & Responsive Behavior** — the baseline accessibility and responsive-layout requirements specific to onboarding screens, given these are the screens a user with the least product familiarity will encounter first.

## Deliverables
* Approved Onboarding UX document.
* Annotated screen-by-screen visual specification covering every state in the Onboarding PRD's screen sequence, with no undefined or invented steps.
* Progress-indication component specification, cross-referenced to the Component Library.
* Permission-request visual specification, cross-referenced to the Permissions & Consent UX document.
* Skip/defer visual-state matrix mapped one-to-one to the Onboarding Experience document's skip/defer matrix.

## Dependencies
Requires Onboarding Experience (Phase 2, Doc 31) and Onboarding PRD (Phase 3, Doc 40) as the behavioral and functional source of truth for what this document may visually express. Requires Permissions & Consent UX (Phase 2) for the consent framing this document must visually carry. Requires the Design Foundations documents and Component Library (Phase 7, Design System sections) for tokens, type, and reusable components; requires the Motion & Interaction Principles and Accessibility Standards documents (Phase 7) for transition and accessibility baselines applied here.

## Teams
Design, Product, Engineering, Content/Copy, Growth, UX Research, QA.

## Completion Criteria
- [ ] Every screen and state named in the Onboarding PRD's screen-by-screen sequence has a corresponding visual requirement, with no undefined steps added or PRD steps omitted.
- [ ] Permission-request visual treatment reviewed against the Permissions & Consent UX document for consistent framing and language tone.
- [ ] Skip/defer visual-state matrix validated against the Onboarding Experience document's skip/defer matrix with no dead-end states.
- [ ] Progress indication and pillar-introduction visuals confirmed to read as one assistant, not three independent setup wizards.
- [ ] Accessibility requirements verified against the Accessibility Standards document for first-run screens specifically.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
