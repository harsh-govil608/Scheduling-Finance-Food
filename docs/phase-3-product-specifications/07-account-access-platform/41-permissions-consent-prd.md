# Document 41: Permissions & Consent PRD

## Document Name
Permissions & Consent PRD

## Purpose
This PRD will define the concrete feature that requests, explains, records, and allows revocation of every sensitive permission the product needs — SMS, location, health/food photos, and financial data. It defines the actual request screens, the consent record and how it is queried by the rest of the product, the revocation flow, and the rules for re-requesting a previously denied permission.

## Why It Exists
Phase 2's Permissions & Consent UX document established the plain-language explanation standard and the experiential principles for consent, but stopped short of specifying the buildable feature: what a permission-request screen actually contains, how a granted/denied/revoked state is stored and checked by every pillar that depends on it, and what exactly happens the instant a user revokes access mid-use. Because this product touches unusually sensitive data, an ambiguous or inconsistently implemented consent feature is a trust failure with legal and ethical weight, not just a UX rough edge — this PRD must leave engineering and design zero room to improvise consent behavior pillar by pillar.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: permission request UI/timing, the consent record (state per permission per user, queryable by any pillar), revocation flow, and re-request rules after denial. Out of scope: backend compliance/legal implementation, encryption/storage architecture for consent records, and the plain-language copy content itself (content is authored per the Permissions & Consent UX standard but is a Content/Copy deliverable, not this PRD's).
2. **User Stories** — As a user, I want to understand in plain language why the app needs SMS access before I grant it, not after; as a user, I want to revoke location access for Health without losing my Finance or Productivity functionality; as a user who denied a permission once, I want the app to respect that decision and not nag me every session; as a user, I want to see, in one place, every permission I've granted and what each one currently unlocks; as a user who revokes a permission mid-use, I want to immediately understand what stopped working and why.
3. **Functional Requirements** — Define the request-screen composition (what/why/benefit/consequence-of-decline, per the Permissions & Consent UX standard), the consent-record schema at the product-behavior level (permission type, state, timestamp, requesting pillar), the revocation flow and its immediate propagation requirement to every dependent feature, the re-request eligibility rules (cooldown period, allowed framing, maximum attempts), and the granular-vs-bundled grouping of related permissions (e.g., SMS parsing for Finance).
4. **Non-Functional Requirements** — Define the latency ceiling between a revocation action and every dependent feature respecting it, the requirement that consent state be available even in degraded/offline conditions (fail closed, not open), and the constraint that no permission is ever silently re-enabled without a fresh explicit grant.
5. **UX Requirements** — This feature must conform to the Permissions & Consent UX and Onboarding Experience (Phase 2) documents for explanation structure, timing, and visual/copy consistency across pillars; feature-specific UX rules must define the layout of the centralized permissions review screen and how a "why is this greyed out" state is explained inline wherever a revoked permission disables a feature.
6. **States & Flows** — Enumerate the lifecycle per permission: not yet requested → requested → granted / denied → [granted → revoked] / [denied → re-requested → granted/denied] → (any state) → reviewed on demand via the permissions screen.
7. **Edge Cases** — Cover a permission granted at the OS level but denied inside the app's own consent flow (or vice versa), a bundled permission where the user wants partial access (e.g., SMS for Finance but not for anything else), simultaneous grant/revoke actions from two devices under one account, and a permission required mid-session for a feature the user just triggered (just-in-time request).
8. **Failure Scenarios** — Define behavior when the OS revokes a permission outside the app's knowledge (detected only on next check), when a consent-state check fails or times out and a feature must decide whether to proceed, and when a user's re-installed app has no local consent record and must reconcile against a server-side one.
9. **AI Behaviors** — Minimal: this feature does not itself predict or learn, but the AI's Proactivity Ladder rung for any pillar is gated by the consent state this feature produces — this PRD must define the handoff contract (what consent state values the ladder logic consumes) without specifying the ladder logic itself.
10. **Notification Behaviors** — Define whether a re-request is ever delivered as a push notification (default should lean toward in-context only) versus surfaced purely in-app, how revocation confirmations are communicated, and how re-request nudges are arbitrated against Notification System frequency limits to avoid nagging.
11. **Success Criteria** — State the qualitative bar: a user should always be able to explain, in their own words, what they've granted and why, and should never feel tricked, surprised, or unable to find the revoke control for something they no longer want to share.
12. **Metrics** — Define quantitative targets such as grant rate per permission type, denial-to-later-grant conversion after re-request, revocation rate, time-to-full-propagation after revocation, and rate of users who visit the centralized permissions screen unprompted.
13. **Open Questions** — Capture unresolved questions such as how many re-request attempts are acceptable before a permission is considered permanently declined, and how granular the SMS/location bundling should be given the tradeoff between user clarity and prompt fatigue.

## Deliverables
- Full Permissions & Consent PRD document following the 13-section structure above.
- Consent-state schema (product-behavior level) covering all sensitive permission types.
- Revocation propagation flow diagram.
- Re-request eligibility ruleset.

## Dependencies
Phase 3: Onboarding PRD. Phase 2: Permissions & Consent UX, Onboarding Experience, User Control Model. Phase 1: Guiding Principles Document, Product (Behavioral) Philosophy Document, Problem Statement Document.

## Teams Using This
Product, Design, Engineering, Trust & Safety, Content/Copy, QA, Legal/Compliance (as downstream consumers)

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Revocation propagation is validated to reach every dependent pillar feature within the stated latency ceiling.
- [ ] Re-request rules are specific enough to be tested against (no vague "occasionally" language).
- [ ] Consent-state handoff contract to Proactivity Ladder logic is explicitly defined.
- [ ] Signed off by: Head of Product (required), Head of Trust & Safety (required), Engineering Lead (required).
