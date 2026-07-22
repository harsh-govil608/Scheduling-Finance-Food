# Document 44: Account & Profile Management PRD

## Document Name
Account & Profile Management PRD

## Purpose
This PRD will define the feature that lets a user create and manage their account, view and edit their profile data, and control cross-pillar preferences from a single, coherent center — the ongoing home base for identity and preference management after onboarding completes. It covers account credentials, profile attributes, and the preference center; it does not cover the permission grants themselves or first-run setup.

## Why It Exists
Onboarding establishes an account once; Account & Profile Management is where a user returns for the rest of their relationship with the product to correct a detail, update a preference, or understand what the assistant currently knows and is configured to do for them. Because Productivity, Finance, and Health are meant to feel like one assistant rather than three apps, preferences (units, notification defaults, communication tone, pillar-level pause states) must live in one coherent center — without this PRD, each pillar risks building its own settings silo, recreating the fragmentation the product's core promise explicitly rejects.

## Approximate Page Count
8-10 pages

## Sections
1. **Feature Scope** — In scope: account creation/credential management, profile data fields (name, contact, household/context attributes relevant across pillars), and the cross-pillar preference center (units, tone, notification defaults, pillar pause states surfaced here per the User Control Model). Out of scope: the permission-grant mechanics themselves (owned by Permissions & Consent PRD), and pillar-specific settings that don't cross pillar boundaries (owned by each pillar's own PRD).
2. **User Stories** — As a user, I want to update my profile details in one place without hunting through pillar-specific menus; as a user, I want to see and adjust my notification and tone preferences once and have them apply consistently across Productivity, Finance, and Health; as a user, I want to change my account credentials (email, password/auth method) securely and be notified of the change; as a user, I want to find the pause/control settings from the User Control Model in an obvious, centralized location; as a user, I want to delete my account and understand exactly what that does before confirming.
3. **Functional Requirements** — Define the account creation and credential-change flows (including re-authentication requirements for sensitive changes), the profile data schema at the product-behavior level and which fields are required vs. optional, the preference center's contents and how a preference change propagates to all three pillars, and the account-deletion flow's confirmation steps and stated consequences.
4. **Non-Functional Requirements** — Define the propagation latency for a preference change to take effect across pillars and devices, the security requirement that credential changes require re-authentication, and the requirement that profile/preference data remain available (read-only if necessary) during degraded connectivity.
5. **UX Requirements** — This feature must conform to the User Control Model (Phase 2) for how pause/override controls surface here, and to the Cross-Device Experience document for how profile/preference changes sync; feature-specific UX rules must define the information architecture of the preference center (grouped by cross-cutting concern, not by pillar) and how account-deletion is presented with appropriately weighted friction for an irreversible action.
6. **States & Flows** — Enumerate the lifecycle: account created (from Onboarding) → active profile → [profile field edited → saved] → [preference changed → propagated] → [credential change requested → re-authenticated → applied] → [deletion requested → confirmation → grace period if applicable → deleted].
7. **Edge Cases** — Cover a profile edit made on one device while another device is offline, a preference change that conflicts with a pillar-specific override already set, a credential-change request interrupted mid-flow, and a deletion request made while a Shared Family Mode space membership is still active.
8. **Failure Scenarios** — Define behavior when a preference-propagation write fails on one pillar but succeeds on others (partial-propagation state), when re-authentication for a credential change fails repeatedly, and when an account-deletion request cannot fully complete due to a dependent state elsewhere in the product (e.g., an active shared space).
9. **AI Behaviors** — Minimal/none for the account and credential mechanics themselves; the preference center is a direct input to personalization (tone, units) and to the User Control Model's per-pillar pause state, so this PRD must define the handoff contract to those systems without specifying their internal logic.
10. **Notification Behaviors** — Define confirmation notifications for security-relevant changes (credential change, account deletion requested), and confirm routine preference changes generate in-app acknowledgment only, not push notifications, per Notification System arbitration and the "Never Overwhelm" principle.
11. **Success Criteria** — State the qualitative bar: a user should always know where to go to change something about themselves or their preferences, and should never encounter a pillar that silently ignores a preference set elsewhere.
12. **Metrics** — Define quantitative targets such as preference-center engagement rate, propagation-success rate across pillars, credential-change completion rate, and account-deletion completion vs. abandonment rate.
13. **Open Questions** — Capture unresolved questions such as how long a deletion grace period should be before data is irreversibly removed, and which profile fields (if any) should be required at account creation versus deferred to later.

## Deliverables
- Full Account & Profile Management PRD document following the 13-section structure above.
- Profile data field schema (product-behavior level).
- Preference-center content map (preference × pillars affected × propagation requirement).
- Account-deletion flow diagram with dependency checks (e.g., Shared Family Mode).

## Dependencies
Phase 3: Onboarding PRD, Permissions & Consent PRD, Cross-Device Sync PRD, Shared Family Mode PRD (for deletion dependency handling). Phase 2: User Control Model, Cross-Device Experience, Personalization. Phase 1: Guiding Principles Document.

## Teams Using This
Product, Design, Engineering, Trust & Safety, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Preference-propagation requirements validated against all three pillars with no silent-ignore case.
- [ ] Credential-change and account-deletion flows require explicit re-authentication/confirmation with no bypass path.
- [ ] Account-deletion dependency on Shared Family Mode membership explicitly resolved.
- [ ] Signed off by: Head of Product (required), Head of Trust & Safety (required), Engineering Lead (required).
