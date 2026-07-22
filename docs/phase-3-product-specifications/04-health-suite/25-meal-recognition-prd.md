# Document 25: Meal Recognition PRD

## Document Name
Meal Recognition PRD

## Purpose
Define the complete specification for logging meals via photo or voice, minimizing manual text entry while keeping nutrition data accurate enough to be trustworthy. This covers the capture flow, the confirmation/correction UX, and the product-level rules for when the system may auto-accept a result versus asking the user to review it.

## Why It Exists
Manual food logging is the single biggest reason nutrition-tracking apps get abandoned — typing every ingredient is tedious and people stop doing it within weeks. The mission of approaching zero manual work depends on photo and voice capture actually working well enough, and being corrected easily enough, that users trust the shortcut. This PRD exists to specify that experience precisely, separating it from the underlying data model (Nutrition Tracking PRD) and from the computer-vision implementation itself (a later, technical phase), so product and design can commit to a capture experience before the recognition model's accuracy is even fully known.

## Approximate Page Count
10-13 pages

## Sections
1. **Feature Scope** — In scope: photo capture flow, voice capture flow, the confidence-based proposal the system returns, the one-tap confirm/correct UI, and multi-item meal handling. Out of scope: the computer-vision/NLP model implementation and training (later technical phase), the canonical nutrition data model and daily/weekly summaries (owned by Nutrition Tracking PRD), and goal comparison (owned by Health Goals PRD).
2. **User Stories** — As a user, when I photograph my plate, I want the app to propose a meal breakdown I can correct in one tap rather than typing every ingredient. As a user, I want to describe what I ate out loud while my hands are busy and have it logged accurately. As a user, when the app misidentifies my food, I want a fast correction path that doesn't feel like starting over. As a user, I want to log a meal I ate an hour ago without the app assuming it was just eaten. As a user eating a multi-component meal (e.g., a plate with three items), I want to adjust individual items rather than the whole entry.
3. **Functional Requirements** — Define the photo capture flow (camera launch, retake, multi-angle support if applicable); define the voice capture flow (recording, transcription hand-off, ambient noise handling at a product level); define the confidence-tiering rule set that determines auto-accept vs. review-required; define the correction UI (swap item, adjust portion, add/remove item, mark as fully wrong); define how a corrected result feeds back into the proposal for that same entry.
4. **Non-Functional Requirements** — Capture-to-proposal latency must stay within a defined budget so the flow doesn't feel broken; the flow must work with degraded/offline connectivity by queuing capture and deferring recognition; because meal photos and voice recordings are sensitive personal data, capture and any temporary storage must respect the consent state defined in Permissions & Consent UX, and raw media must not be retained longer than the minimum needed to produce and allow correction of a result.
5. **UX Requirements** — Must conform to Food Logging Experience and Nutrition & Goals Experience from Phase 2; correction must never require more taps than the original manual-entry flow it's meant to replace; low-confidence proposals must be visually distinguishable from high-confidence ones so users know when to double-check.
6. **States & Flows** — Capture states: capturing, processing, proposed (awaiting confirmation), confirmed, corrected, discarded; flow variants for photo vs. voice entry; flow for retroactive/backdated logging; flow for multi-item meals where items can be independently confirmed or corrected.
7. **Edge Cases** — Photo contains no identifiable food; voice input is ambient noise or unrelated speech; user photographs a packaged/branded item with a barcode-like label; meal contains items the system has never seen before; user logs the same meal twice from two capture attempts; partial network failure leaves a capture "processing" indefinitely.
8. **Failure Scenarios** — Meal photo is misrecognized with high confidence (wrong-but-confident) — correction path must be equally accessible regardless of stated confidence; recognition service is unavailable — capture must still be savable in a "pending recognition" state rather than lost; voice transcription fails silently — user must be told capture didn't complete rather than assuming it logged.
9. **AI Behaviors** — Meal Recognition operates at a mid-rung of the Proactivity Ladder: it predicts and proposes but does not log without at least implicit confirmation at low ladder levels, moving toward auto-accept-on-high-confidence only as a user grants more autonomy; it should learn from repeated corrections (e.g., a user's usual portion size for a specific dish) to improve future proposals for that individual, consistent with Learn/Adapt philosophy pillars.
10. **Notification Behaviors** — Meal Recognition does not independently notify; any prompt to "confirm your logged meal" or reminder to log a meal is arbitrated through the shared Notification System and must respect quiet hours, per the Automation Philosophy's Never Overwhelm principle.
11. **Success Criteria** — Users log the majority of their meals via photo or voice rather than manual text entry; corrections feel fast enough that users don't abandon logging after a bad recognition; user-reported trust in the resulting nutrition numbers increases over time as the system learns their patterns.
12. **Metrics** — Percentage of entries logged via photo/voice vs. manual; correction rate by confidence tier; time from capture to confirmed entry; capture abandonment rate (started but never confirmed).
13. **Open Questions** — What confidence threshold, if any, should permit true auto-accept without user review, and does that threshold need to be user-configurable per the Proactivity Ladder? How should multi-item meals with mixed confidence per item be presented? Should voice capture support conversational follow-up ("did you mean X?") or remain single-shot?

## Deliverables
- Photo capture flow specification
- Voice capture flow specification
- Confidence-tiering and auto-accept rule specification
- Correction UI specification
- Multi-item meal handling specification

## Dependencies
Food Logging Experience, Nutrition & Goals Experience, Automation Philosophy, Permissions & Consent UX (Phase 2); Nutrition Tracking PRD, Health Goals PRD (Phase 3)

## Teams Using This
Product, Engineering (Mobile), Engineering (ML/Vision), Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Confidence-tiering rules reviewed jointly by Product and ML leads.
- [ ] UX flows validated against Food Logging Experience and Nutrition & Goals Experience.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), ML Lead (required).
