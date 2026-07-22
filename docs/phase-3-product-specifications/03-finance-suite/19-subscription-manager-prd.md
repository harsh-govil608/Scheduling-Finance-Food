# Document 19: Subscription Manager PRD

## Document Name
Subscription Manager PRD

## Purpose
Define the requirements the eventual PRD must satisfy for detecting recurring subscriptions from confirmed transaction history, tracking them over time, alerting before renewal, and nudging cancellation of subscriptions that appear unused or unwanted. This document specifies what that PRD must define; it does not itself finalize detection thresholds or nudge copy.

## Why It Exists
Subscriptions are a uniquely silent drain — individually small, automatically renewing, and easy to forget about once the initial sign-up moment passes — which makes this the Finance Suite feature most directly serving the Remember, Predict, and Suggest philosophy verbs. Without a dedicated requirements spec, "is this a subscription" detection and cancellation nudging would either be bolted onto general expense capture (mismatched cadence and confidence needs) or skipped entirely, leaving users exposed to exactly the creeping recurring cost the product exists to catch.

## Approximate Page Count
7-9 pages

## Sections
1. **Feature Scope** — in scope: recurring-pattern detection surfacing (UX only, not the underlying matching logic), subscription confirmation ("is this a subscription?"), renewal-date tracking, renewal alerts, cancellation-nudge behavior, and the subscription list view; out of scope: general expense capture and parsing (owned by Expense Capture PRD), one-off or variable bill tracking (owned by Bills PRD), and budget category limits (owned by Budget Planner PRD).
2. **User Stories** — as a user, after two or more matching charges, I want to be shown a "Netflix, ₹649, monthly" card so I can confirm or deny it as a subscription; as a user, I want a heads-up a few days before a renewal charge hits; as a user who hasn't opened a streaming app in months, I want a gentle nudge suggesting I reconsider the subscription, not a guilt trip; as a user who cancels externally, I want to mark the subscription as ended in-app so tracking stays accurate; as a user, I want to manually add a subscription the system hasn't detected yet.
3. **Functional Requirements** — the PRD must define the minimum occurrence threshold before a suspected subscription is surfaced for confirmation (as a product requirement, not an algorithm), the subscription record's required fields (merchant, amount, cadence, next expected date, status), renewal-alert timing rules, the product-level criteria for triggering a cancellation nudge (for example a usage-signal or price-increase trigger), and the manual-add flow for a subscription not yet auto-detected.
4. **Non-Functional Requirements** — the PRD must require that surfaced detections keep a false-recurring rate below an agreed threshold, that renewal alerts remain reliably timed relative to the actual renewal date, that cadence tracking survives an amount or currency change without breaking, and that subscription data is held to the same sensitivity tier as general transaction data.
5. **UX Requirements** — the PRD must conform to the Budget & Spend Intelligence Experience's Subscription Detection & Tracking state model (detected, confirmed, tracked, flagged-unrecognized), the Finance Experience Overview's tone principles, and the Automation Philosophy's rules for what may be auto-confirmed versus what requires explicit user confirmation.
6. **States & Flows** — the PRD must define detected-unconfirmed, confirmed-tracked, renewal-upcoming, renewed (charge matched), unrecognized-flagged, cancellation-nudged, marked-cancelled-by-user, and lapsed/price-changed states, and the transitions between them.
7. **Edge Cases** — the PRD must address a subscription with a variable, usage-based amount, an annually billed subscription that appears infrequent, a shared or family subscription paid by someone else appearing as an unexpected inbound, a free trial converting to paid, and a subscription cancelled externally that still produces one final overlapping charge.
8. **Failure Scenarios** — the PRD must define behavior for a recurring pattern falsely detected from coincidentally similar charges, a renewal alert that fires after the charge has already happened due to detection lag, a subscription the user marked cancelled that renews anyway and must be reconciled without appearing broken, and a merchant rebrand that breaks cadence continuity.
9. **AI Behaviors** — the PRD must define how the Remember verb underlies recognizing a pattern across billing cycles, how Predict is used to estimate the next renewal date, how Suggest drives cancellation nudges only at an appropriate Proactivity Ladder rung and with non-judgmental framing consistent with Encourage, and how user confirmations or denials refine future detection surfacing (Learn, Adapt).
10. **Notification Behaviors** — the PRD must define renewal alerts and cancellation nudges as distinct notification types arbitrated through the Notification System's interruption budget, batched together when multiple renewals cluster in the same window, and explicitly rate-limited so cancellation nudges never repeat frequently enough to become the nagging anti-pattern named in the Notification System document.
11. **Success Criteria** — a user can name their active subscriptions without checking bank statements, and unwanted subscriptions get cancelled at a measurably higher rate than before the feature.
12. **Metrics** — subscription detection confirm-vs-deny rate (a precision/recall proxy), renewal-alert lead-time accuracy, cancellation-nudge acceptance rate, and total tracked subscription spend as a self-reported-awareness proxy.
13. **Open Questions** — how many billing cycles should constitute "confirmed recurring"; how shared-household subscriptions are attributed; whether cancellation nudges should ever link to actual cancellation execution (explicitly out of scope now) or remain purely informational.

## Deliverables
* Approved Subscription Manager PRD.
* A subscription state-model reference (detected, confirmed, tracked, flagged-unrecognized, cancelled).
* A renewal-alert timing rule table.
* A cancellation-nudge trigger criteria and cooldown reference.

## Dependencies
Budget & Spend Intelligence Experience, Finance Experience Overview, Expense Capture PRD (Document 18, assumes confirmed transactions as input), Automation Philosophy, Notification System (Phase 2), Finance Tracker (Home) PRD (Document 17).

## Teams Using This
Product, Design, Engineering (Finance Feature Team), Data Science/ML, Trust & Safety, Content/Copy

## Completion Criteria
- [ ] The subscription state model has been validated against at least one unrecognized-subscription worked scenario.
- [ ] Renewal-alert timing rules have been reviewed to ensure no alert can fire after the associated charge.
- [ ] Cancellation-nudge tone and frequency have been reviewed against the Guiding Principles' anti-shaming and anti-nagging rules.
- [ ] Manual-add and auto-detected subscriptions are confirmed to converge into the same tracked-state model with no duplicate entries.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Trust & Safety (required).
