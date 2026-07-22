# Document 18: Expense Capture PRD

## Document Name
Expense Capture PRD

## Purpose
Define the requirements the eventual PRD must satisfy for automatically detecting, parsing, and logging financial transactions from SMS and UPI notifications without requiring manual entry, including the confirmation, correction, and duplicate-handling experiences around that capture. This document specifies what that PRD must define; it does not itself finalize parsing rules, thresholds, or UI copy.

## Why It Exists
Automatic financial data capture is the single Finance Suite mechanism most likely to break trust in one wrong parse — a mis-categorized transaction, a duplicated entry, or a transaction the user did not expect the AI to have seen — because it operates on a user's private messages without being asked each time. Without a requirements spec that treats confirmation, correction, and low-confidence handling as first-class product requirements rather than edge-case afterthoughts, engineering will under-invest in exactly the moments that determine whether the ledger is trusted enough to replace manual bookkeeping.

## Approximate Page Count
10-13 pages

## Sections
1. **Feature Scope** — in scope: SMS/UPI transaction detection triggering, parsing into a structured transaction record, auto-categorization, the confirmation flow, the correction/recategorization flow, duplicate-flagging UX, and manual entry as a parallel path; out of scope: budgeting logic (owned by Budget Planner PRD), spend forecasting (owned by Spend Prediction PRD), recurring-subscription-specific detection (owned by Subscription Manager PRD), bill-specific due-date tracking (owned by Bills PRD), and the underlying banking-integration or SMS-parsing ML implementation (later phase).
2. **User Stories** — as a user, when I make a UPI payment, the transaction should appear in my ledger within seconds, pre-categorized, without me opening the app; as a user, when the system cannot confidently parse an SMS debit alert, I want to be asked rather than have it silently guessed; as a user, when I correct a mis-categorized "Swiggy" charge from Shopping to Food, I expect future Swiggy transactions to categorize correctly without me repeating the correction; as a user, I want to manually log a cash expense that has no digital trail, with the same standing as an auto-captured one; as a user, when a retried UPI payment creates a duplicate, I want to dismiss it in one tap rather than have two entries silently affect my totals.
3. **Functional Requirements** — the PRD must define the capture triggers (SMS listener, UPI notification listener) at a product-behavior level, the required fields of a structured transaction record (amount, merchant, category, date/time, source), the default auto-categorization behavior, the confidence threshold that determines whether a transaction is silently logged or surfaced for confirmation, the correction flow's requirement to feed a downstream learning signal, duplicate-flagging UX (not the underlying detection logic), and the manual entry form's required fields.
4. **Non-Functional Requirements** — the PRD must set a capture-to-ledger latency target so a transaction is visible within seconds of the underlying SMS or notification; require that parsed financial data at rest and in transit meets the product's data-sensitivity policy; require that a raw source message is never discarded before the user has had the opportunity to confirm the accuracy of what was extracted from it; set an availability target for the capture pipeline; and require that a failed parse is never silently dropped, instead falling into a reviewable low-confidence queue.
5. **UX Requirements** — the PRD must conform to the Transaction Capture Experience's confirmation, correction, duplicate, and low-confidence handling patterns; the Finance Experience Overview's trust-framing and tone principles; the Permissions & Consent UX for how SMS and notification access is disclosed and consented to; and the Automation Philosophy for what may be silently auto-logged versus what requires explicit confirmation.
6. **States & Flows** — the PRD must define captured-unconfirmed, auto-confirmed (silent), pending-user-confirmation, corrected, duplicate-flagged, duplicate-dismissed, manually-entered, and low-confidence-parked states, and the transitions between them.
7. **Edge Cases** — the PRD must address split or partial payments, refunds and reversals that match an earlier transaction, transactions in a foreign currency, an SMS format from a bank never seen before, multiple UPI apps triggering near-simultaneous notifications for the same payment, and transactions below a trivial-amount threshold.
8. **Failure Scenarios** — the PRD must define product-level behavior for a mis-parsed amount shown to the user before correction, a category mis-assignment that repeats systematically for one merchant, SMS/notification permission being revoked mid-session and capture halting silently, a duplicate wrongly auto-merged, and a new bank SMS template class the parser cannot read at all.
9. **AI Behaviors** — the PRD must define how the Proactivity Ladder governs whether newly captured transactions are silently logged versus surfaced for confirmation by default, and how that default shifts as trust is earned over time; how corrections feed the shared memory/personalization model so a re-parse of a known merchant improves going forward (Remember, Learn, Adapt); and how parse confidence is surfaced to the user rather than hidden (Predict).
10. **Notification Behaviors** — the PRD must require that confirmation-needed and low-confidence-parse notifications route through the Notification System's arbitration and interruption budget rather than firing immediately per transaction, and must define batching rules so a burst of small transactions (for example several UPI payments in a short window) becomes one digest rather than N separate interruptions.
11. **Success Criteria** — a user rarely needs to manually correct a transaction after the first few weeks of use, and a user trusts the ledger enough to stop manually checking bank SMS themselves.
12. **Metrics** — parse accuracy rate, correction rate over time (expected to trend down), capture-to-ledger latency, percentage of transactions requiring manual confirmation, and duplicate false-positive/false-negative rate.
13. **Open Questions** — how many correction instances should shift a merchant's default confirmation threshold; whether cash transactions should ever be proactively prompted for based on location (a cross-reference to the Budget & Spend Intelligence Experience) or remain purely manual; how multi-bank or multi-SIM users are supported in the first release.

## Deliverables
* Approved Expense Capture PRD.
* A confidence-to-proactivity decision table mapping parse confidence to default proactivity level to required user action.
* A correction-flow reference showing every recategorization or fix path and its downstream learning effect, described at a product level.
* A duplicate-handling UX flow reference.

## Dependencies
Transaction Capture Experience, Finance Experience Overview, Automation Philosophy, Permissions & Consent UX, Notification System (Phase 2), Finance Tracker (Home) PRD (Document 17).

## Teams Using This
Product, Design, Engineering (Finance Feature Team), Data Science/ML, Trust & Safety, QA, Content/Copy

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] The confirmation flow has been validated against at least three real-world SMS/UPI message format scenarios.
- [ ] The correction flow is confirmed to visibly change future categorization behavior, with no silent no-op corrections.
- [ ] Low-confidence handling has been reviewed to ensure the system never silently guesses on financial data above an agreed confidence threshold.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Trust & Safety (required, given data sensitivity).
