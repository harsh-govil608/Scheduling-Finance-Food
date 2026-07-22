# Document 42: Shared Family Mode PRD

## Document Name
Shared Family Mode PRD

## Purpose
This PRD will define the multi-user/shared-household feature — shared budgets, shared task lists, and any visibility one account grants another — for households that want to coordinate through the product rather than use it purely single-user. It defines what is shared, what remains private by default, and how a shared space is created, joined, and left.

## Why It Exists
Every other Phase 1 and Phase 2 document was written for a single-user relationship between one person and their AI; Shared Family Mode introduces a second class of relationship — person-to-person coordination mediated by the AI — that was never scoped as a launch commitment in the Phase 1 User Personas Document. Building it without an explicit go/no-build decision risks quietly expanding surface area (permission models, privacy boundaries, conflicting-suggestion logic) far beyond what was budgeted, so this PRD exists both to specify the feature *if* it is greenlit and to force that scope decision to be made deliberately rather than by default.

**Scope flag: this PRD describes a feature that was not committed to as a launch requirement in Phase 1. Product leadership must explicitly confirm build/no-build and target release before this PRD proceeds past a draft.**

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: shared-space creation/invite/join/leave, shared budget and shared task list mechanics, per-item and per-category visibility controls. Out of scope: individual-user budgeting and task management themselves (owned by Budget & Spend Intelligence PRD and Task Management PRD respectively) — this PRD owns only the sharing layer on top of them, and it explicitly excludes any AI arbitration logic for conflicting household preferences beyond basic visibility rules.
2. **User Stories** — As a user setting up a shared household budget, I want to invite a partner and agree on what categories are visible to both of us; as a user, I want my personal health data to stay private by default even inside a shared family space; as a user, I want to see a shared task list update in real time when my partner completes an item; as a user leaving a shared space, I want a clear description of what happens to previously shared data; as an admin of a shared space, I want to remove a member without losing the shared history I'm still entitled to.
3. **Functional Requirements** — Define the shared-space creation and invite/accept flow, the default visibility posture per pillar (what is shared-by-default vs. private-by-default, with Health defaulting to private), per-item override controls, the shared budget aggregation and attribution model (who spent what), the shared task list assignment/completion model, and the leave/remove-member flow and its data-retention consequences for each party.
4. **Non-Functional Requirements** — Define the propagation latency for shared-state updates (e.g., a shared budget entry visible to all members), the privacy boundary guaranteeing no pillar defaults to shared without explicit opt-in, and the requirement that leaving a shared space never silently deletes an individual member's own data.
5. **UX Requirements** — This feature must conform to the User Control Model and Permissions & Consent UX (Phase 2) for how sharing-scope changes are consented to and reversible; feature-specific UX rules must define how shared vs. private items are visually distinguished throughout Productivity and Finance surfaces, and how an invite/join flow is presented without implying more sharing than the user has actually configured.
6. **States & Flows** — Enumerate the lifecycle: no shared space → space created → invite sent → invite accepted/declined/expired → active member → [visibility configured/reconfigured] → member left/removed → space archived or continues with remaining members.
7. **Edge Cases** — Cover a shared budget category that one member wants private after previously sharing it, an invite sent to someone already in another shared space, a member removed while they have unresolved shared task assignments, and a shared space where all members leave (space dissolution).
8. **Failure Scenarios** — Define behavior when the core assumption — that all members can see a consistent shared state — breaks: two members editing the same shared budget category simultaneously, a member's account being deleted while shared items still reference them, and an invite accepted on one device while declined on another for the same user.
9. **AI Behaviors** — Minimal for the sharing mechanics themselves (creation, invite, visibility are user-driven, not AI-driven); the Proactivity Ladder angle is limited to whether the AI is ever permitted to surface a suggestion informed by another member's shared data (e.g., a joint budget insight) — this must default to off unless both members' consent states explicitly allow it.
10. **Notification Behaviors** — Define notifications for invite sent/accepted/declined, shared-item changes (new shared expense, task completed by another member), and member-left/removed events, all arbitrated through the Notification System to avoid one household member's activity flooding another's notification stream.
11. **Success Criteria** — State the qualitative bar: household members should feel coordination is easier without feeling surveilled, and no member should ever discover their data was shared more broadly than they explicitly configured.
12. **Metrics** — Define quantitative targets such as shared-space creation rate, invite acceptance rate, active shared-item usage (shared budget entries, shared task completions per week), and unshare/leave rate as a signal of sharing-model friction.
13. **Open Questions** — Capture unresolved questions such as whether Shared Family Mode is in scope for initial launch at all (pending leadership decision), what the maximum household size is, whether children/dependents require a distinct account type, and how disputes over shared financial data are handled at the product level.

## Deliverables
- Go/no-build decision memo requesting explicit leadership sign-off before full PRD development proceeds.
- Full Shared Family Mode PRD document following the 13-section structure above (contingent on go decision).
- Default visibility matrix (pillar × data type × default shared/private state).
- Shared-space lifecycle-state diagram.

## Dependencies
Phase 3: Permissions & Consent PRD, Account & Profile Management PRD, Budget & Spend Intelligence PRD, Task Management PRD (for the individual-user mechanics being shared). Phase 2: User Control Model, Permissions & Consent UX, Product Pillars Overview. Phase 1: User Personas Document (for confirmation this was not a scoped launch persona), Guiding Principles Document.

## Teams Using This
Product, Design, Engineering, Trust & Safety, Data Science/ML, QA, Executive Leadership (scope decision)

## Completion Criteria
- [ ] Explicit build/no-build/timing decision obtained from product leadership and recorded before detailed spec work continues.
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Default visibility matrix defaults every sensitive category (especially Health) to private with explicit opt-in required to share.
- [ ] Leave/remove-member data-retention consequences are unambiguous for both the leaving/removed member and remaining members.
- [ ] Signed off by: Head of Product (required), Head of Trust & Safety (required), CEO/Founder (required, given out-of-original-scope status).
