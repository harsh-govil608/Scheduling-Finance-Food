# Document 43: Premium Features PRD

## Document Name
Premium Features PRD

## Purpose
This PRD will define the actual entitlement and gating feature that implements the free/premium contrast described conceptually in Phase 2's Premium Experience document — the mechanism by which the product knows a user's tier, enforces which Proactivity Ladder rungs and depth-of-foresight capabilities are available at each tier, and presents an upgrade path. It defines the feature, not the price points, packaging, or business-model economics behind it.

## Why It Exists
Phase 2's Premium Experience document established the philosophy that premium expands depth and autonomy of the same assistant rather than hobbling the free tier, but a philosophy cannot enforce itself in a running product — something has to actually check a user's tier before allowing a higher-autonomy AI action, and something has to render the upgrade moment consistently across three pillars. Without a single entitlement feature, each pillar risks building its own ad hoc tier check, producing exactly the fractured, inconsistently-gated experience the Premium Experience document warned against, and risking a free tier that degrades below its committed completeness bar by accident.

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: the entitlement/tier-state feature (what tier a user is on, how a capability check is performed), the enforcement points that gate Proactivity Ladder rungs and depth-of-foresight features per pillar, and the upgrade-moment surface (where and how an upgrade prompt appears). Out of scope: price points, packaging/SKU definitions, billing, payment processing, trial mechanics, and discounting — all reserved for a later business-model phase.
2. **User Stories** — As a free-tier user, I want every core capability of the assistant to work without nagging, so the free tier feels complete rather than a demo; as a user approaching a premium-gated capability, I want to understand what I'd gain in that specific moment, not through a generic ad; as a premium user, I want the higher-autonomy behavior I'm entitled to at every pillar, not just the one I upgraded for; as a downgrading user, I want to know exactly what will change and keep everything I've already built with the AI; as a user, I want a single place to see what tier I'm on and what it unlocks.
3. **Functional Requirements** — Define the tier-state feature (current tier, effective date, source of truth check), the capability-gating map (which specific Proactivity Ladder rungs and depth-of-foresight features are tier-gated, per the Premium Experience document's mapping), the upgrade-moment trigger conditions and placement rules, and the downgrade/cancellation flow's effect on previously-unlocked data, history, and AI-learned context (which must persist, not be stripped).
4. **Non-Functional Requirements** — Define the latency ceiling for a tier-change to take effect across all pillars simultaneously, the requirement that a tier check never blocks core (non-gated) functionality even if the entitlement check itself is temporarily unavailable (fail toward the safer, already-granted tier), and the consistency requirement that the same capability is gated identically regardless of which pillar surface it's accessed from.
5. **UX Requirements** — This feature must conform to the Premium Experience document (Phase 2) for tone and trigger rules on upgrade moments and to the Guiding Principles Document's anti-dark-pattern stance; feature-specific UX rules must define how a gated capability is visually indicated before a user attempts to use it (versus failing silently or after the fact) and how the tier/entitlement summary screen is laid out.
6. **States & Flows** — Enumerate the lifecycle: no tier assigned (new account) → free tier (default) → [upgrade initiated → tier active: premium] → [downgrade/cancellation initiated → tier reverts: free, data/history preserved] → re-upgrade at any point restores premium capability access without re-onboarding.
7. **Edge Cases** — Cover a capability that was used under premium and then becomes gated after downgrade (what happens to in-progress state), a tier change occurring mid-session on one device while another device is active, a lapsed payment causing an unintended downgrade, and a user who qualifies for premium via a promotional grant rather than direct purchase.
8. **Failure Scenarios** — Define behavior when the entitlement check fails or times out (must fail toward not falsely blocking access the user is entitled to), when a tier-change event is received out of order across devices, and when the capability-gating map itself is misconfigured such that a free-tier user is inadvertently granted a premium capability (detection and safe correction, not punitive rollback).
9. **AI Behaviors** — This feature directly gates the Proactivity Ladder: it must define the explicit mapping from tier to maximum allowed ladder rung per pillar (per the Premium Experience document), and confirm that trust/learning data continues to accumulate even for capabilities a free user cannot yet access, so nothing is lost if they later upgrade.
10. **Notification Behaviors** — Define whether upgrade-moment prompts are ever delivered as push notifications (should default to strongly disfavored, in-context only) versus surfaced in-app at the moment of relevance, and how tier-change confirmations (upgrade successful, downgrade effective) are communicated, arbitrated through the Notification System.
11. **Success Criteria** — State the qualitative bar: a free user should never feel deliberately hobbled, and a premium user should feel the upgrade as a natural deepening of the same assistant rather than a different product.
12. **Metrics** — Define quantitative targets such as capability-gate encounter-to-upgrade conversion rate, free-tier task completion rate (as a proxy for free-tier completeness), downgrade rate and its correlation with any specific gated capability, and re-upgrade rate after a prior downgrade.
13. **Open Questions** — Capture unresolved questions such as how quickly a tier downgrade should take effect versus allowing a grace period, and whether any capability should ever be temporarily unlocked as a trial/preview outside formal trial mechanics (which are out of scope here but touch this feature's gating logic).

## Deliverables
- Full Premium Features PRD document following the 13-section structure above.
- Capability-gating map (capability × pillar × required tier × mapped Proactivity Ladder rung).
- Tier-state lifecycle diagram.
- Downgrade/cancellation data-preservation checklist.

## Dependencies
Phase 3: Onboarding PRD, Account & Profile Management PRD. Phase 2: Premium Experience, User Control Model, Product Pillars Overview. Phase 1: Product (Behavioral) Philosophy Document (Proactivity Ladder), Guiding Principles Document (anti-dark-pattern stance).

## Teams Using This
Product, Design, Engineering, Growth, Data Science/ML, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Capability-gating map is checked against the Premium Experience document's Free Tier Completeness Bar with no free-tier regression.
- [ ] Every gated capability is mapped to a specific Proactivity Ladder rung with no ungated ambiguity.
- [ ] Downgrade/cancellation flow confirmed to never strip or degrade previously-built user data or history.
- [ ] Confirmed this document contains no pricing figures, packaging/SKU definitions, or business-model economics.
- [ ] Signed off by: Head of Product (required), Head of Growth (required), Engineering Lead (required).
