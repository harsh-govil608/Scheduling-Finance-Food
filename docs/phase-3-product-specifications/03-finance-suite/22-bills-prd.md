# Document 22: Bills PRD

## Document Name
Bills PRD

## Purpose
Define the requirements the eventual PRD must satisfy for detecting one-off and variable-amount recurring bills (electricity, water, credit card statements, rent, and similar), tracking their due dates, and delivering payment reminders. This document specifies what that PRD must define; it does not itself finalize detection thresholds or reminder copy.

## Why It Exists
Bills differ from subscriptions in that amounts vary period to period and due dates can shift, so due-date tracking and reminders need their own detection-confirmation and reminder-pacing rules rather than reusing fixed-cadence subscription logic. Missing a bill carries real financial consequence — a late fee or a service cutoff — which makes reminder reliability a trust-critical requirement in a way that a missed spend-nudge is not, and this document exists to make that reliability bar explicit before the feature is built.

## Approximate Page Count
7-9 pages

## Sections
1. **Feature Scope** — in scope: bill detection surfacing from transaction and notification history (UX only), due-date tracking, variable-amount handling, payment-reminder scheduling and delivery integration, and manual bill add/edit; out of scope: fixed recurring subscription tracking (owned by Subscription Manager PRD), budget category limits (owned by Budget Planner PRD), the underlying bill-detection matching algorithm or backend (later phase), and bill payment execution — this feature reminds, it does not pay.
2. **User Stories** — as a user, I want a reminder three days before my credit card statement is due, with the correct variable amount shown if it's known; as a user whose electricity bill amount varies monthly, I want the reminder to still fire correctly on the recurring due-date pattern even though the amount differs each time; as a user, I want to manually add a rent due date the system cannot detect from transaction history; as a user, I want a bill automatically marked paid when a matching transaction is captured, without extra bookkeeping; as a user who misses a reminder, I want a clear overdue state, not a reminder that just silently vanishes.
3. **Functional Requirements** — the PRD must define the bill record's required fields (payee, typical amount range, due-date pattern, status), the detection-to-confirmation UX flow, variable-amount handling rules, reminder-scheduling rules relative to the due date, product-level auto-mark-paid logic triggered by a matching captured transaction, and the manual bill entry and editing flow.
4. **Non-Functional Requirements** — the PRD must require reliable reminder delivery ahead of the due date, treating a late reminder as a trust-breaking failure; require due-date tracking to handle date shifts (for example a due date falling on a holiday) without silently skipping a cycle; hold bill data to the same sensitivity tier as other captured financial data; and require that no bill is ever dropped from tracking without a visible, explained reason.
5. **UX Requirements** — the PRD must conform to the Finance Experience Overview's tone principles for a bill-due nudge that informs without inducing anxiety, the Automation Philosophy's rules for what may be auto-marked-paid versus requires confirmation, and the Permissions & Consent UX given that bill detection relies on the same SMS/notification access as Expense Capture.
6. **States & Flows** — the PRD must define detected-unconfirmed, confirmed-tracked, upcoming (within the reminder window), due-today, overdue, auto-marked-paid, manually-marked-paid, manually-added, and editing states.
7. **Edge Cases** — the PRD must address a bill amount that varies widely period to period (for example a seasonal utility spike), a due date shifting due to a weekend or holiday, a bill paid early before the reminder window opens, a bill for a closed account that no longer generates charges, and a split or partial bill payment.
8. **Failure Scenarios** — the PRD must define behavior for a reminder that fails to fire due to a scheduling error, leaving a bill overdue silently; a bill incorrectly auto-marked paid by matching an unrelated transaction; a due-date pattern that breaks after the payee changes its billing cycle; and duplicate bill entries created by both auto-detection and manual add for the same payee.
9. **AI Behaviors** — the PRD must define how Remember underlies recognizing a recurring due-date pattern and Predict underlies estimating the likely amount range and next due date; how the Proactivity Ladder governs whether a newly detected bill is silently tracked or requires user confirmation first; and how Learn and Adapt apply when a due-date pattern shifts (for example a payee changes its billing cycle) so the system adjusts rather than repeatedly reminding at a stale date.
10. **Notification Behaviors** — the PRD must define payment reminders as a distinct, high-reliability notification type that escalates in urgency as the due date approaches (upcoming, due-today, overdue), still arbitrated through the Notification System's interruption budget and quiet-hours rules but weighted higher priority than general spend nudges given the real financial consequence of a missed bill, with an explicit cap preventing repeated overdue re-reminders from becoming nagging.
11. **Success Criteria** — users stop missing bill due dates after adopting the feature, and reminders are trusted enough that users retire any separate manual bill calendar.
12. **Metrics** — on-time-payment rate after feature adoption, reminder-to-payment lead time, detection confirm-vs-deny rate, overdue-rate trend, and auto-mark-paid accuracy.
13. **Open Questions** — how confidently the system can predict a variable bill's amount before it is confirmed by the payee; whether overdue bills should ever escalate beyond in-app/push notification channels given the real consequence of missing them; how bills with no reliable digital trail (cash rent, informal loans) are supported.

## Deliverables
* Approved Bills PRD.
* A bill state-model reference (detected, confirmed, upcoming, due, overdue, paid).
* A reminder-timing-and-escalation rule table.
* A product-level auto-mark-paid matching requirement.

## Dependencies
Finance Experience Overview, Transaction Capture Experience, Expense Capture PRD (Document 18), Automation Philosophy, Permissions & Consent UX, Notification System (Phase 2), Finance Tracker (Home) PRD (Document 17).

## Teams Using This
Product, Design, Engineering (Finance Feature Team), Data Science/ML, Trust & Safety, Content/Copy, QA

## Completion Criteria
- [ ] The bill state model has been validated against at least one variable-amount worked scenario.
- [ ] Reminder-timing rules have been reviewed to guarantee no reminder can be scheduled after its associated due date.
- [ ] Auto-mark-paid matching requirements have been reviewed to prevent false-positive matches against unrelated transactions.
- [ ] Overdue escalation rules have been checked against the anti-nagging patterns named in the Notification System document.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Trust & Safety (required).
