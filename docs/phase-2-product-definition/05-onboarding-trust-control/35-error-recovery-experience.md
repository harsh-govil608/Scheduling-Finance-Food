# Document 35: Error Recovery Experience

## Document Name
Error Recovery Experience

## Purpose

Define how the product handles AI mistakes gracefully from the user's point of view — a wrongly categorized expense, a missed reminder, an incorrectly recognized food photo — including the correction flow for each and how trust is repaired after an error becomes visible to the user.

## Why It Exists

An AI that proactively manages a user's life across Productivity, Finance, and Health will make mistakes by construction — it is guessing, categorizing, and acting ahead of explicit instruction, and every increase in autonomy on the Proactivity Ladder is also an increase in the surface area for visible error. Without a deliberately designed error recovery experience, mistakes will be handled inconsistently across pillars, corrections will feel like extra chores rather than quick taps, and — most damaging to a product whose entire value proposition rests on trust — a single visible mistake risks undoing the confidence that took many correct actions to build. This document exists so an AI mistake becomes a moment that demonstrably repairs trust rather than one that quietly erodes it.

## Approximate Page Count

6-8 pages.

## Sections

1. **Error Taxonomy (Product-Facing)** — the enumerated categories of visible AI mistakes per pillar (e.g., miscategorized transaction, missed or wrong-time reminder, misrecognized food photo, incorrect habit inference), defined in user-experience terms.
2. **Correction Flow Standard** — the single, consistent interaction pattern a user follows to flag and fix any AI mistake, applied identically across all three pillars regardless of error type.
3. **Acknowledgment & Tone Guidelines When the AI Is Wrong** — the required tone and content of the AI's response to being corrected (no over-apologizing, no defensiveness, no silent correction with no acknowledgment).
4. **Trust Repair After High-Visibility Errors** — the specific product response required after an error with outsized consequence (e.g., a missed reminder for an important event, a budget alert based on a miscategorized large transaction), beyond the standard correction flow.
5. **Silent vs. Visible Error Handling** — the rules for when the product proactively surfaces that it may have made a mistake versus when it waits for the user to notice, and why.
6. **Learning Signal Communication** — how the product communicates, at the experience level only, that a correction has been received and will change future behavior (e.g., "got it, I'll remember this"), with underlying learning mechanics explicitly out of scope.
7. **Escalation Path for Repeated or Systemic Errors** — the user-facing channel for reporting a mistake that keeps recurring, distinct from one-off in-the-moment corrections.
8. **Cross-Pillar Consistency Requirements** — the requirement that a correction made in one pillar feels like the same action and tone as a correction made in another, reinforcing the "one assistant" framing.

## Deliverables

* Approved Error Recovery Experience document.
* An error taxonomy list enumerating known mistake categories per pillar.
* A correction-flow requirements specification usable identically across all three pillars.
* Tone and copy guidelines for how the AI acknowledges and responds to being corrected.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Learn/Adapt steps of the Behavioral Loop and the overall trust posture; requires the User Control Model (Document 34) for the undo/override mechanics a correction flow relies on; requires the Guiding Principles Document (Phase 1) for tone boundaries; requires the Success Metrics Document (Phase 1) for the trust-related metrics an error recovery experience is ultimately measured against.

## Which Teams Use This

Product, Design, Content/Copy, Data Science/ML, Trust & Safety, Customer Support.

## Completion Criteria

- [ ] Every pillar has a documented, non-overlapping list of known AI mistake categories.
- [ ] The correction flow standard has been validated to work identically across all three pillars with no pillar-specific exceptions left undocumented.
- [ ] Tone guidelines have been tested against at least one worked example per error category.
- [ ] The trust-repair requirement for high-visibility errors has been validated against at least one severe scenario per pillar.
- [ ] Confirmed no AI/ML implementation detail (model retraining, correction weighting, etc.) has leaked into this document.
- [ ] Signed off by: Head of Product (required), Head of Design (required).
