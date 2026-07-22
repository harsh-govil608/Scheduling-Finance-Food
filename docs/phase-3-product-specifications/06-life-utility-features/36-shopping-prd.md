# Document 36: Shopping PRD

## Document Name
Shopping PRD

## Purpose
Define the shopping list / purchase-intent feature as a forward-looking signal source for Finance's spend prediction — capturing what a user intends to buy before they buy it so the AI can predict near-term cash flow and surface budget conflicts before checkout, rather than defining a price-comparison or shopping-discovery product. It defines the list/item data model, how intent items convert into predicted and then actual spend, and the boundary between this feature and generic list-taking, which is owned by the Notes PRD.

## Why It Exists
Every other Finance signal in the product is retrospective — a transaction has already happened by the time the AI can react to it — so a shopping list is the one structured place a user tells the AI about spend *before* it occurs, which is exactly the leading indicator the "Predict" pillar of the product philosophy requires. Without deliberately wiring purchase intent into Finance's forecasting loop, this feature is indistinguishable from any commodity checklist app and has no claim to belonging in an AI Life Operating System. It must also resist scope creep toward becoming a shopping-discovery or price-comparison tool — its only job is capturing intent and feeding the prediction loop, not helping the user shop better.

## Approximate Page Count
6-9 pages

## Sections
1. **Feature Scope** — In scope: shopping list creation and item entry (manual or AI-suggested from consumption patterns), per-item estimated cost, list-to-budget-category mapping, conversion of list items into Finance's near-term spend forecast, and marking items purchased (linking to an actual transaction when detected). Out of scope: price comparison or deal-finding, in-app checkout/payment, recipe-to-list generation logic (a possible future Health-pillar feature, not owned here), and generic non-shopping checklists (owned by the Notes PRD).
2. **User Stories** — As a user who adds "new tires" to a shopping list with an estimated cost, I want Finance to show that upcoming expense in my forecast before I actually buy them; as a user who buys the same household items on a cycle, I want the AI to suggest adding them to my list before I run out, based on past purchase timing; as a user near my monthly discretionary limit, I want to be warned when adding a non-essential item to my list, not just after I buy it; as a user, I want purchased items automatically checked off when a matching transaction is detected; as a user, I want to mark an item as "recurring" so it regenerates on my list on a cycle.
3. **Functional Requirements** — Define the list/item schema (name, estimated cost, category, recurrence flag, status: pending/purchased/removed), the matching logic that links a completed transaction back to a list item (merchant/amount/timing heuristics with manual-confirmation fallback), the recurring-item regeneration schedule logic, the AI-suggested-item generation logic (from consumption-pattern detection), and how estimated costs feed into Finance's forecast calculation and budget-threshold checks.
4. **Non-Functional Requirements** — Define the acceptable false-positive/negative rate for transaction-to-item matching before it undermines trust, the latency ceiling between adding an item and it reflecting in Finance's forecast, and the privacy boundary on what consumption-pattern data (e.g., purchase history) can be used to generate suggested items versus requiring explicit opt-in.
5. **UX Requirements** — This feature must conform to the Cross-Pillar Coordination Experience (Phase 2) for how list items visibly connect to their Finance forecast impact rather than appearing as an isolated checklist, and to the Information Architecture (Phase 2) for where shopping lists live relative to Notes and Finance navigation; feature-specific UX rules must cover how an AI-suggested item is visually distinguished from a user-added one and how a pending item's forecasted cost is shown inline.
6. **States & Flows** — Enumerate the item lifecycle: suggested (AI-proposed, unconfirmed) → added (user-confirmed, pending) → forecasted (counted in Finance projections) → purchased (matched to a transaction) → archived, plus the recurring-item branch where a purchased recurring item regenerates a new pending instance.
7. **Edge Cases** — Cover an item purchased for a different amount than estimated (forecast correction), a single transaction that fulfills multiple list items (e.g., one grocery run), an item added and purchased before the next forecast cycle runs, and a recurring item's pattern being broken by a one-time behavior change (e.g., cancelled a subscription-like purchase).
8. **Failure Scenarios** — Define behavior when the core assumption — that list items map cleanly to future transactions — breaks: an item never purchased and left stale on the list indefinitely, ambiguous matching when multiple similar transactions occur close together, and a user who abandons the list feature entirely, leaving Finance's forecast to silently lose this signal source without alerting anyone.
9. **AI Behaviors** — Detail how AI-suggested items are gated by the Proactivity Ladder (silent pattern detection with no visible action, low-confidence suggestion requiring explicit add, high-confidence auto-add with easy removal), and how repeated dismissal of a suggested item type demotes future suggestions of that category.
10. **Notification Behaviors** — Define which list events warrant a notification (a suggested recurring item is due, a list addition would cross a budget threshold) versus silent in-app updates, and how these integrate with the Notification System's arbitration to avoid duplicating a budget-threshold alert already generated by the Finance pillar for the same underlying event.
11. **Success Criteria** — A user should feel their shopping list actively improves their budget accuracy rather than existing as a disconnected errand-list, and should trust that adding an item is a meaningful financial signal, not busywork.
12. **Metrics** — Define targets such as percentage of list items later matched to an actual transaction, forecast-accuracy improvement attributable to list-derived signals, AI-suggested-item acceptance rate, and recurring-item regeneration accuracy.
13. **Open Questions** — Capture unresolved questions such as whether shared/household shopping lists are in scope for v1, and how aggressively the AI should suggest items from consumption patterns before it feels invasive rather than helpful.

## Deliverables
- Full Shopping PRD document following the 13-section structure above.
- List/item data model and lifecycle-state diagram.
- Transaction-to-item matching decision-flow diagram.
- AI-suggested-item generation logic outline mapped to Proactivity Ladder rungs.

## Dependencies
Phase 3: Budgeting PRD, Finance spend-prediction/forecasting PRD, Notes PRD (scope boundary). Phase 2: Cross-Pillar Coordination Experience, Information Architecture, Automation Philosophy, Notification System. Phase 1: Product Philosophy Document, Guiding Principles Document.

## Teams Using This
Product, Engineering (Finance, Backend), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Item lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] Scope boundary against Notes PRD confirmed with no functional overlap.
- [ ] AI Behaviors section reviewed against the Proactivity Ladder for consistency with Automation Philosophy.
- [ ] Notification Behaviors section reviewed against Notification System arbitration rules for duplication with Finance pillar alerts.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), AI/ML Lead (required).
