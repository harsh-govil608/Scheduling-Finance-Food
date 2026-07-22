# Document 20: Meal Recognition (Computer Vision) Architecture

## Document Name
Meal Recognition (Computer Vision) Architecture

## Purpose
Define the ML architecture for recognizing food items, portions, and estimated nutrition content from a user-submitted meal photo — the vision model pipeline behind the Phase 3 Meal Recognition PRD's confidence-tiered confirmation flow. This document specifies pipeline stages, confidence behavior, and model-selection criteria; it does not select a final vision model or vendor, and it does not redefine the media upload, storage, or handoff plumbing already owned by Phase 4's Media Service.

## Why It Exists
Meal Recognition's entire UX promise — log a meal in one photo, correct in one tap — depends on a vision pipeline accurate enough that most corrections are minor, not total rewrites, and on a confidence model that never presents a wrong guess with false authority; without a dedicated architecture defining pipeline stages, confidence thresholds, and fallback behavior, the feature either over-trusts a wrong recognition and erodes nutrition-data credibility, or under-trusts a right one and reintroduces the manual-entry friction the feature exists to eliminate. This document exists to give AI/ML engineering a specification precise enough to build against before any specific vision model is chosen, and to give Product a way to validate the PRD's confidence-tiering rules against a real architecture rather than an assumption.

## Approximate Page Count
9-11 pages

## Sections
1. **Vision Pipeline Stages** — image preprocessing, food detection/segmentation, portion estimation, and nutrition-database lookup, and the interface each stage exposes to the next.
2. **Multi-Item & Multi-Angle Detection** — how a plate containing several distinct food items is segmented into independently identifiable and independently correctable items, per the PRD's multi-item handling requirement.
3. **Portion Estimation Approach** — the architecture-level approach to estimating quantity/volume from a single 2D image (e.g., reference-object or learned depth-proxy methods), and the accuracy bounds the model must be validated against before shipping.
4. **Confidence Scoring & Thresholds** — how per-item and overall model confidence maps to the PRD's auto-accept-vs-confirm-vs-manual-fallback tiers, including how confidence is computed independently per item in a multi-item meal.
5. **Nutrition Database Integration** — how detected food items resolve to structured nutrition data, including regional food database coverage requirements tied to Phase 2 Localization.
6. **Voice-Described Meal Handoff** — the shared entity schema by which a voice-described meal, parsed by the Voice/NLU Architecture (Phase 5, Document 21), produces output compatible with a photo-recognized meal, so the two capture paths converge on one proposal format.
7. **Correction Feedback Loop & Personalization** — how corrections captured by the PRD's correction UI (swap item, adjust portion, mark fully wrong) feed back into per-user personalization, such as learning a user's typical portion size for a recurring dish.
8. **On-Device vs. Server-Side Inference Split** — the criteria for deciding which pipeline stages run on-device versus server-side, balancing the PRD's capture-to-proposal latency budget against the sensitivity of meal photos as health-adjacent personal data.
9. **Model Selection Criteria** — vendor-neutral criteria for evaluating candidate vision models or providers (accuracy on regional food benchmarks, multi-item segmentation capability, latency, fine-tuning/customization support, cost per inference), explicitly deferring the final selection to implementation.
10. **Failure & Degraded-Mode Handling** — architecture-level behavior when the photo contains no identifiable food, the recognition service is unavailable, or the item is a packaged/branded product, consistent with the PRD's failure scenarios.

## Deliverables
- Vision pipeline architecture diagram from uploaded-image intake through structured meal-proposal output.
- Confidence-tiering specification mapped explicitly to the Meal Recognition PRD's auto-accept/confirm/fallback rules.
- Portion-estimation approach and accuracy-target specification.
- Nutrition database integration specification, including regional coverage plan.
- Shared meal-entity schema aligning photo-recognized and voice-described output.
- Vendor-neutral model selection criteria rubric.
- Correction feedback loop and personalization specification.

## Dependencies
Requires AI Platform Overview (Phase 5, Document 01) for the platform's shared subsystem map; requires Media Service (Phase 4, Document 16) for the upload/storage/processing-handoff boundary this pipeline consumes; requires the Meal Recognition PRD (Phase 3, Document 25) as the product-behavior contract this architecture must satisfy, including its confidence-tiering rules, edge cases, and failure scenarios; requires the Nutrition Tracking PRD (Phase 3, Document 24) for the canonical nutrition data model detected items resolve into; informed by Phase 2 Localization for regional food coverage; requires the Voice/NLU Architecture (Phase 5, Document 21) for the voice-described-meal handoff schema; draws on the Learning Systems and Memory & Context Systems subsystem groups (Phase 5) for the correction feedback loop, and the Privacy-Preserving AI Platform Contract (Phase 5) for on-device processing constraints on health-adjacent media.

## Teams
AI/ML Engineering (Computer Vision), Data Science, Backend Engineering (Health Service), Mobile Engineering, Design, Privacy/Legal, Product (Health pillar)

## Completion Criteria
- [ ] Confidence thresholds validated against the Meal Recognition PRD's stated success criteria and confidence-tiering rules.
- [ ] Portion-estimation accuracy targets reviewed against a representative, regionally-varied food-photo test set.
- [ ] On-device vs. server-side inference split reviewed against Media Service's retention and privacy requirements for meal photos.
- [ ] Voice-described-meal handoff schema reviewed jointly with the Voice/NLU Architecture owner for compatibility.
- [ ] Correction feedback loop cross-checked against the Learning Systems subsystem group's architecture.
- [ ] Signed off by: Head of AI/ML (required), Principal Architect (required), Head of Product — Health pillar (required).
