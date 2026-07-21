# Document 21: Transaction Capture Experience

## Document Name
Transaction Capture Experience

## Purpose

Define the user-facing UX of automatic expense capture — SMS parsing and UPI transaction tracking — including confirmation flows, correction flows, and how trust is built in a ledger that populates itself. This document covers everything from a transaction entering the system to it being confirmed as accurate.

## Why It Exists

Automatic financial data capture is the single feature most likely to break trust in one wrong parse — a mis-categorized transaction, a duplicated entry, or a transaction the user did not expect the AI to have seen. Without an explicit confirmation and correction UX contract, engineering will treat parsing errors as an edge case rather than the central design problem they actually are for a system whose core mechanism is reading a user's messages without being asked each time.

## Approximate Page Count

7-9 pages.

## Sections

1. **Capture Sources & Their Visibility to the User** — what sources feed the ledger (SMS parsing, UPI tracking, manual entry) and how each is disclosed at the point it is used, not only once during onboarding.
2. **Auto-Capture Confirmation Flow** — the default proactivity level for a newly captured transaction (silent log vs. surfaced for confirmation) and what promotes or demotes that level per the Proactivity Ladder.
3. **Correction & Recategorization Flow** — how a user fixes a mis-parsed amount, merchant, or category, and what the system does with that correction going forward.
4. **Duplicate & Conflict Handling (UX Only)** — how the experience surfaces a possible duplicate or conflicting entry to the user for resolution, without detailing the underlying detection logic.
5. **Low-Confidence Parse Handling** — what the user sees when the system cannot confidently parse a transaction, and how it asks rather than silently guesses.
6. **Trust-Building Feedback Loop** — how the ledger visibly demonstrates it is learning from corrections over time, so accuracy improvement is perceptible to the user.
7. **Manual Entry as a Parallel Path** — how manual transaction entry coexists with automatic capture without feeling like a demotion or a fallback for a broken feature.
8. **Explicit Non-Scope: Budgeting and Prediction** — states plainly that once a transaction is captured and confirmed, everything about how it informs budgets, predictions, or subscription tracking is owned by the Budget & Spend Intelligence Experience Document.

## Deliverables

* Approved Transaction Capture Experience document.
* A confirmation-flow decision table mapping parse confidence to proactivity level to required user action.
* A correction-flow reference showing every recategorization or fix path and its downstream learning effect, described at a product level.

## Dependencies

Requires the Finance Experience Overview Document (umbrella framing), the Product (Behavioral) Philosophy Document (Phase 1, Proactivity Ladder), and the Product Architecture Overview (memory model — corrections must feed into the shared memory concept, not a Finance-only cache).

## Which Teams Use This

Product, Design, Engineering (Finance feature team), Data Science/ML, Trust & Safety, QA.

## Completion Criteria

- [ ] Every capture source has a defined disclosure moment, not only a one-time onboarding mention.
- [ ] The confirmation flow has been validated against at least three real-world SMS/UPI message formats as worked scenarios.
- [ ] The correction flow is confirmed to visibly change future behavior in terms this document describes, with no silent no-op corrections.
- [ ] Low-confidence handling has been reviewed to ensure the system never silently guesses on financial data above an agreed confidence threshold.
- [ ] Signed off by: Head of Product (required), Trust & Safety Lead (required), Finance Feature Team Lead (required).
