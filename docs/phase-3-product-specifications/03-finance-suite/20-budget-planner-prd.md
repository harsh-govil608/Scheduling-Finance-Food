# Document 20: Budget Planner PRD

## Document Name
Budget Planner PRD

## Purpose
Define the requirements the eventual PRD must satisfy for budget creation (category-based, overall, and goal-linked), category limit setting, and real-time budget-vs-actual tracking as confirmed transactions accrue. This document specifies what that PRD must define; it does not itself finalize limit-setting formulas or progress-bar visuals.

## Why It Exists
Budgeting is the moment captured transactions become actionable rather than merely recorded, and it is the feature most exposed to the risk of feeling rigid or judgmental if built without explicit adaptive rules. Without a dedicated requirements spec, budget creation and progress tracking would be built with inconsistent limit logic per category and no shared renegotiation model, undermining the adaptive philosophy the Budget & Spend Intelligence Experience already establishes for how budgets should respond to real behavior over time.

## Approximate Page Count
8-10 pages

## Sections
1. **Feature Scope** — in scope: the budget creation flow across category, overall, and goal-linked types, limit setting including AI-suggested defaults, real-time budget-vs-actual tracking display, and the budget adjustment/renegotiation flow; out of scope: forward-looking forecasting beyond the current budget period (owned by Spend Prediction PRD), transaction capture and categorization mechanics (owned by Expense Capture PRD), and bill- or subscription-specific tracking (owned by Bills PRD and Subscription Manager PRD, though their spend rolls into budget totals).
2. **User Stories** — as a new user, I want to set a monthly Food budget with an AI-suggested starting limit based on my history; as a user, I want a live progress indicator that updates the moment a transaction is captured, not on a delay; as a user who consistently overspends Shopping, I want a renegotiation suggestion rather than the same alert every period; as a user without enough transaction history yet, I still want a usable default budget to start from; as a user, I want the option of one overall monthly budget instead of per-category limits.
3. **Functional Requirements** — the PRD must define the supported budget types (category, overall, goal-linked), the limit-setting flow including AI-assisted suggested defaults, real-time recalculation triggered by new transaction capture, budget period definitions (monthly and custom), rollover-versus-reset rules at a period boundary, and editing or deleting an existing budget.
4. **Non-Functional Requirements** — the PRD must set a bounded latency for budget-vs-actual figures to update after a transaction is captured, require that a corrected transaction retroactively updates the relevant budget's totals, require that editing a budget never silently drops its history, and require consistent currency precision and rounding across all budget displays.
5. **UX Requirements** — the PRD must conform to the Budget & Spend Intelligence Experience's Budget Definition & Setup UX and Budget Progress Surfacing sections, the Finance Experience Overview's tone principles for overspend framing, and the Automation Philosophy's rules for how much of budget-setting may be AI-proposed versus require explicit user action.
6. **States & Flows** — the PRD must define no-budget-set (empty state), budget-proposed-by-AI-pending-acceptance, active-under-limit, active-approaching-limit, active-over-limit, period-closed-summary, and renegotiation-proposed/accepted/declined states.
7. **Edge Cases** — the PRD must address mid-period budget creation and partial-period math, a category with zero transactions for the entire period, a transaction recategorized after its budget period has closed, overlapping budgets (a category budget and an overall budget both tracking the same transaction), and a user setting an unrealistically low limit.
8. **Failure Scenarios** — the PRD must define behavior for a post-close correction that changes historical budget accuracy and must be visibly reconciled, a sync delay causing budget figures to drift from the ledger, an AI-suggested starting budget that is far off due to thin transaction history, and a renegotiation suggestion that must not repeat in the same period it was declined.
9. **AI Behaviors** — the PRD must define how the Suggest verb drives AI-proposed starting budgets and renegotiation proposals, how Learn and Adapt govern moving a consistently-missed budget toward renegotiation rather than repeating alerts at an unrealistic limit, and how the Proactivity Ladder governs whether budget suggestions are proposed automatically or require the user to initiate.
10. **Notification Behaviors** — the PRD must define approaching-limit and over-limit alerts as distinct notification types paced through the Notification System's interruption budget and quiet-hours rules, firing only on threshold-crossing events rather than per-transaction, consistent with the anti-nagging rules named in the Notification System document.
11. **Success Criteria** — users maintain an active budget past the first period rather than abandoning it, and overspend alerts read as helpful rather than shaming.
12. **Metrics** — percentage of users with an active budget at 30/60/90 days after creation, budget adherence rate, renegotiation acceptance rate, and time-to-first-budget-created for new users.
13. **Open Questions** — whether goal-linked budgets belong in the first release or a later phase; how overlapping category-and-overall budgets resolve display precedence; whether non-monthly (weekly or custom) budget periods are supported in the first release.

## Deliverables
* Approved Budget Planner PRD.
* Budget type and limit-setting requirements reference.
* Budget-vs-actual real-time tracking rule set.
* Renegotiation trigger and cooldown rules.

## Dependencies
Budget & Spend Intelligence Experience, Finance Experience Overview, Expense Capture PRD (Document 18), Automation Philosophy, Notification System (Phase 2), Guiding Principles Document (Phase 1, anti-shaming), Finance Tracker (Home) PRD (Document 17).

## Teams Using This
Product, Design, Engineering (Finance Feature Team), Data Science/ML, Trust & Safety, Content/Copy, QA

## Completion Criteria
- [ ] Every budget type (category, overall, goal-linked) has a defined creation flow and progress-display rule.
- [ ] The renegotiation flow has been validated against at least one worked scenario of a consistently-missed budget.
- [ ] Post-close correction reconciliation has been reviewed to ensure no silent drift between the ledger and historical budget figures.
- [ ] Overspend tone language requirements have been reviewed against the Guiding Principles' anti-shaming rules.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of Trust & Safety (required).
