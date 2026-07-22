# Document 17: Finance Tracker (Home) PRD

## Document Name
Finance Tracker (Home) PRD

## Purpose
Define the requirements the eventual PRD must satisfy for the Finance pillar's home/overview surface — the single screen a user lands on when opening Finance, showing a consolidated snapshot of net position, recent activity, budget health, and anything needing attention, plus the entry points into every other Finance Suite feature. This document specifies what that PRD must define; it does not itself finalize the layout or copy.

## Why It Exists
Six distinct Finance Suite features (Expense Capture, Subscription Manager, Budget Planner, Spend Prediction, Bills, Investments) each generate their own data and their own sense of what deserves attention, and without a home surface with its own requirements spec, each feature team would build a competing "landing" experience or leave the user to hunt across six disconnected screens. This document exists so exactly one PRD owns the composed, at-a-glance surface — analogous to the product-wide Dashboard System but scoped to Finance — while explicitly deferring every feature's own detail screen to its sibling PRD.

## Approximate Page Count
7-9 pages

## Sections
1. **Feature Scope** — in scope: the home summary composition (net position, spend-to-date, budget health at a glance, "needs attention" surfacing) and the navigation entry points into Expense Capture, Budget Planner, Spend Prediction, Bills, Subscription Manager, and Investments; out of scope: transaction-level ledger detail (owned by Expense Capture PRD), category budget mechanics (owned by Budget Planner PRD), and individual bill or subscription detail screens (owned by Bills PRD and Subscription Manager PRD respectively).
2. **User Stories** — as a user, when I open Finance I should immediately understand where I stand without reading transaction lines; as a user, I want one tap from home to log an expense manually; as a user, I want to see what needs attention today (a bill due, a budget near its limit) without visiting six screens; as a returning user after several days away, I want a caught-up summary, not a wall of every transaction I missed.
3. **Functional Requirements** — the PRD must define the home summary's data composition rules, the entry-point tile/card set and what each must surface as a preview, the single "needs attention" arbitration slot that pulls signals from other features without duplicating their logic, and the empty-state requirements for a brand-new user with no captured data yet.
4. **Non-Functional Requirements** — the PRD must set a load-time budget appropriate to a first-screen expectation, require graceful offline degradation via a cached last-known snapshot, require every displayed figure to carry a visible "as of" freshness indicator when not live, and prohibit client-side re-derivation of raw sensitive transaction detail beyond what the summary needs.
5. **UX Requirements** — the PRD must conform to the Finance Experience Overview's Finance Home Surface and trust-framing sections, the Dashboard System's card composition patterns, and the Navigation Philosophy's rules for pillar entry consistency.
6. **States & Flows** — the PRD must define first-open/empty state, populated steady-state, attention-needed state (for example a budget nearing its limit), stale/offline snapshot state, and the transition into and back from a child feature.
7. **Edge Cases** — the PRD must address a user with zero captured transactions, a user who has disabled auto-capture entirely and relies on manual entry, a user with data spanning multiple accounts or currencies, and a user who revokes SMS permission mid-session while home is open.
8. **Failure Scenarios** — the PRD must define what happens when a child feature's data is unavailable (for example the budget service is down) and home must degrade without blocking, when snapshot aggregation itself fails, and when two or more features' signals compete for the single "needs attention" slot at once.
9. **AI Behaviors** — the PRD must define how the home surface's tone and amount of editorializing reflect the user's current Proactivity Ladder rung, and how the system selects the single most important item to surface first using the Predict and Suggest verbs rather than showing everything at once.
10. **Notification Behaviors** — the PRD must define home as the landing target for taps on any Finance notification and require correct deep-link reflection of the state that triggered the notification, while explicitly prohibiting home itself from originating notifications, since arbitration is owned entirely by the Notification System.
11. **Success Criteria** — a user can state their financial standing within seconds of opening Finance, and no entry point ever dead-ends without a clear next action.
12. **Metrics** — the PRD must define a time-to-first-glance-understanding proxy, the percentage of Finance sessions that navigate into a child feature, home surface load latency, and empty-state-to-first-capture conversion rate.
13. **Open Questions** — how much predictive content belongs on home versus being reserved for the Spend Prediction surface; whether multi-account or multi-currency users are in scope for the first release; whether an Investments entry point appears on home before the Investments PRD ships.

## Deliverables
* Approved Finance Tracker (Home) PRD.
* Home summary composition and entry-point requirements reference, at a product-requirement (not visual-design) level.
* A "needs attention" arbitration rule set defining precedence among the six child Finance Suite features.

## Dependencies
Finance Experience Overview, Dashboard System, Navigation Philosophy, Automation Philosophy, Notification System (Phase 2), plus the sibling Finance Suite PRDs: Expense Capture PRD, Subscription Manager PRD, Budget Planner PRD, Spend Prediction PRD, Bills PRD, Investments (Future) PRD.

## Teams Using This
Product, Design, Engineering (Finance Feature Team), Data Science/ML, Trust & Safety, QA

## Completion Criteria
- [ ] The home summary is defined with no duplication of ledger, budget, bill, or subscription detail owned by sibling PRDs.
- [ ] The "needs attention" arbitration rule set has been validated against at least one scenario where two child features compete for the slot simultaneously.
- [ ] Offline/stale-snapshot behavior has been reviewed to ensure no financial figure is ever shown without a freshness indicator when it is not live.
- [ ] Every entry point has a defined empty state for a user with no data in that feature yet.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Engineering Lead (required).
