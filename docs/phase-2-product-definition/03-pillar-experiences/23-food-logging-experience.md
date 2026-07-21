# Document 23: Food Logging Experience

## Document Name
Food Logging Experience

## Purpose

Define the photo-based and voice-based food logging UX — capture flow, confirmation, and correction — and how friction is minimized relative to manual text entry. This document covers everything from a photo or spoken description being captured to a confirmed food log entry.

## Why It Exists

Food logging historically fails as a sustained habit because manual entry is tedious, and the product's core bet in the Health pillar is that photo and voice capture remove that friction entirely. If the capture UX itself introduces new friction — slow confirmation, clunky correction — the product's central differentiator collapses, users revert to not logging at all, and every downstream nutrition feature is left with no data to act on.

## Approximate Page Count

7-9 pages.

## Sections

1. **Capture Modality Overview** — the distinct entry points (photo, voice, manual as fallback) and when each is presented as the primary suggested path.
2. **Photo Capture Flow** — the step-by-step experience of logging via photo, from camera trigger to confirmed entry, including handling multiple items in one photo.
3. **Voice Capture Flow** — the step-by-step experience of logging via spoken description, including handling ambiguous or incomplete descriptions.
4. **Confirmation UX for Recognized Items** — how the system presents what it believes was eaten and the minimum-friction way a user confirms or adjusts it.
5. **Correction Flow for Misidentified Food** — how a user fixes a wrong recognition, and what happens to future recognition confidence for that user's common foods.
6. **Speed & Friction Benchmarks (Experience-Level)** — the product-level expectation for how few steps and how little time a log should take relative to manual entry, framed as a design constraint rather than a technical service-level agreement.
7. **Partial & Low-Confidence Logging** — what happens when the system can identify a photo or voice log only partially (for example, recognizing "a bowl of rice" but not the toppings).
8. **Explicit Non-Scope: Nutrition Goals and Progress** — states plainly that once a food item is logged and confirmed, everything about how it counts toward protein, nutrition, or water goals and progress display is owned by the Nutrition & Goals Experience Document.

## Deliverables

* Approved Food Logging Experience document.
* A capture flow diagram for both the photo and voice paths, step by step to a confirmed log.
* A friction benchmark reference (a steps and/or time ceiling) usable as an acceptance bar for Design and Engineering.

## Dependencies

Requires the Product Pillars Overview (Health Pillar Surface) and the Product (Behavioral) Philosophy Document (Phase 1, "manual work approaches zero" principle). Maintains an explicit boundary with the Nutrition & Goals Experience Document, which owns everything after a confirmed log.

## Which Teams Use This

Product, Design, Engineering (Health feature team), Data Science/ML, QA.

## Completion Criteria

- [ ] The photo and voice capture flows have each been validated against at least three real food-logging scenarios, including one ambiguous or partial case.
- [ ] The friction benchmark is defined as a concrete number (steps and/or seconds) and compared explicitly against manual text entry as the baseline.
- [ ] The correction flow is confirmed to produce a visible improvement in future recognition, consistent with the trust-building principle shared with the Transaction Capture Experience Document.
- [ ] Confirmed no goal or progress UX is defined here that duplicates the Nutrition & Goals Experience Document.
- [ ] Signed off by: Head of Product (required), Health Feature Team Lead (required).
