# Document 23: Investments (Future) PRD

## Document Name
Investments (Future) PRD

## Purpose
Define the minimum viable requirements the eventual PRD must satisfy for investment-tracking as a future Finance Suite capability — read-only, largely manually entered visibility into a user's investment holdings alongside the rest of their financial picture — while explicitly flagging which parts of a full investment feature are deferred and why. This document specifies what that limited-scope PRD must define; it must not be read as authorizing a full brokerage-grade investment feature.

## Why It Exists
The Finance Suite is presently scoped around spend, budgets, bills, and subscriptions, but a life-management AI's mission eventually requires seeing the full financial picture, including investments and net worth. This document exists now, ahead of full build, so that the Finance Tracker (Home) PRD's data model and the rest of the Finance Suite leave room for investments without later rework, and so the team explicitly bounds scope rather than drifting into brokerage syncing, tax-lot tracking, or investment advice before those capabilities — and any accompanying regulatory review — are ready.

## Approximate Page Count
5-7 pages

## Sections
1. **Feature Scope** — in scope for this PRD: a read-only, primarily manually entered investment holdings summary (asset class and approximate total value) and its placement within the Finance Tracker (Home) PRD's net-position summary; explicitly out of scope and deferred to a future phase, named rather than silently dropped: automated brokerage or demat account syncing, transaction-level investment activity capture, tax-lot tracking, investment performance analytics or recommendations, and buy/sell execution.
2. **User Stories** — as a user, I want my investments reflected in my overall financial picture on Finance Home, not just my cash and spending; as a user, I want to manually enter an approximate investment total so my snapshot isn't misleadingly incomplete; as a user with no investments, I want no forced prompt pushing me to invest, since that is explicitly not a goal of this phase.
3. **Functional Requirements** — the PRD must define the minimum viable data model for a manually entered investment holding (type, approximate value, as-of date), how this value rolls into a net-worth or total-assets figure on the Finance Tracker (Home) PRD's summary, and an explicit non-requirements list carried from Feature Scope so implementers do not infer missing functionality as a bug.
4. **Non-Functional Requirements** — the PRD must require that manually entered investment values are clearly and persistently labeled as user-provided and approximate, never presented with the false precision of live market data; hold this data to the same sensitivity tier as other financial data; and require that an empty or unused Investments feature never slows or blocks the rest of the Finance Suite.
5. **UX Requirements** — the PRD must conform to the Finance Experience Overview's tone principles and the Finance Tracker (Home) PRD's summary composition rules, and must require a persistent visual distinction between "tracked/automatic" data and "manually entered/approximate" data given the deliberately deferred automation in this feature.
6. **States & Flows** — the PRD must define no-investments-tracked (default, non-intrusive), manually-added-holding, holding-value-updated (manual), and holding-removed states.
7. **Edge Cases** — the PRD must address a user entering a clearly implausible value (negative or absurdly large), a user wanting multiple holdings of different types (equity, mutual fund, fixed deposit) tracked separately rather than as one lump sum, and a manually entered value that goes stale over time with no automatic refresh.
8. **Failure Scenarios** — the PRD must define behavior for a stale manual value being silently treated as current in a net-worth calculation without a staleness indicator, and must name, as a forward-compatibility consideration even though not built now, how a future automated sync would need to reconcile against a pre-existing manual entry for the same holding.
9. **AI Behaviors** — the PRD must state that AI behavior in this phase is deliberately minimal: no prediction, suggestion, or automation is applied to investment data beyond including a manually entered total in the Finance Home summary, and that Predict/Suggest behaviors for investments (for example asset-allocation nudges) are explicitly deferred, not silently assumed to exist.
10. **Notification Behaviors** — the PRD must state that no dedicated investment notifications exist in this phase, explicitly out of scope to avoid implying investment-advice-adjacent proactive messaging before the feature and any necessary regulatory review are mature enough to support it.
11. **Success Criteria** — a user's overall financial picture on Finance Home feels complete rather than conspicuously missing a major asset category, without the feature overreaching into advice it is not scoped to give.
12. **Metrics** — percentage of users who manually add at least one holding, and the staleness of manually entered values over time, used as a signal for whether building automated sync should be prioritized next.
13. **Open Questions** — what adoption or staleness threshold would justify building the deferred automated-sync capability; what regulatory or advisory constraints apply once real investment data or guidance is involved; whether this feature should ship in the first release of the Finance Suite at all or wait for a later phase entirely.

## Deliverables
* Approved Investments (Future) PRD.
* An explicit deferred-scope list for the future full Investments buildout.
* A minimum viable manual-holding data model.

## Dependencies
Finance Experience Overview, Finance Tracker (Home) PRD (Document 17), Future Expansion Strategy (Phase 2, Document 40), Guiding Principles Document (Phase 1).

## Teams Using This
Product, Design, Engineering (Finance Feature Team), Legal/Privacy liaison, Trust & Safety

## Completion Criteria
- [ ] The deferred-scope list is confirmed to name every excluded capability explicitly rather than leaving it implicit.
- [ ] Manually entered values are confirmed to be visually distinguished from any live/automatic data everywhere they appear.
- [ ] The minimum viable data model has been reviewed for forward compatibility with a future automated-sync capability.
- [ ] Confirmed this document contains no investment-advice or recommendation behavior of any kind.
- [ ] Signed off by: Head of Product (required), Legal/Privacy liaison (required, proximity to financial advice), Head of Trust & Safety (required).
