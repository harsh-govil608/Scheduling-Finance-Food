# Document 32: Permissions & Consent UX

## Document Name
Permissions & Consent UX

## Purpose

Define the user-facing experience of requesting, explaining, granting, and revoking every sensitive permission the product relies on — SMS access, location, health/food photos, and financial data — in plain language a non-technical user can act on. This document covers the experience of consent only; backend compliance, data-handling implementation, and security architecture are explicitly out of scope.

## Why It Exists

This product asks for an unusually sensitive combination of permissions for a consumer app — reading SMS to parse UPI transactions, continuous location, photos of food and potentially of the user's body or surroundings, and full financial transaction data — and it does so specifically so the AI can act with less friction than a user would tolerate from any ordinary app. Consent is the foundation the whole Proactivity Ladder is built on: if a user does not clearly understand what they are granting and does not trust that they can revoke it just as easily, they will either refuse the permission (starving the AI of the input it needs to be proactive) or grant it resentfully (which erodes trust the moment the AI visibly uses that data). Without one unified consent experience, each pillar will phrase and time its permission asks differently, and the product will feel invasive in some moments and trustworthy in others.

## Approximate Page Count

7-9 pages.

## Sections

1. **Sensitive Data Inventory (Product-Facing)** — the enumerated list of sensitive permission categories (SMS content, location, health/food photos, financial/UPI data) described in plain user-facing terms, independent of technical implementation.
2. **Plain-Language Explanation Standard** — the required structure for every permission explanation: what is accessed, why, what the user gains, and what happens if they decline, written so a non-technical user understands it in one read.
3. **Permission Request Timing & Sequencing** — the rules for when each permission is asked relative to onboarding and first value, referencing the Onboarding Experience document for overall sequencing.
4. **Granular vs. Bundled Permissions** — the product's stance on whether related permissions (e.g., SMS parsing for Finance) are requested as one bundled ask or split into narrower, separately revocable grants.
5. **Revocation Experience** — how a user turns off any one sensitive permission independently, without being forced to abandon the pillar entirely, and where revocation controls live.
6. **Consequences-of-Denial Communication** — how the product explains, without guilt-tripping or nagging, what functionality is reduced or disabled when a permission is denied or revoked.
7. **Re-Request & Permission Nudges** — the rules for if and when the product may ask again after an initial denial, including frequency limits and required framing.
8. **Visual & Copy Consistency Requirements Across Pillars** — the shared pattern (layout, iconography, tone) every permission prompt across all three pillars must follow so consent requests feel like one system, not three.

## Deliverables

* Approved Permissions & Consent UX document.
* A plain-language explanation template for each sensitive data category.
* A consent-state matrix (granted / denied / revoked) crossed against pillar-level functional impact.
* A re-request frequency and framing ruleset.

## Dependencies

Requires the Guiding Principles Document and Product (Behavioral) Philosophy Document (Phase 1) for the centrality of trust and consent; requires the Problem Statement Document (Phase 1) for the specific trust barriers this product faces due to its data sensitivity; depends on the Onboarding Experience document (Document 31) for request timing; feeds directly into the User Control Model (Document 34) for how revocation connects to broader override mechanisms.

## Which Teams Use This

Product, Design, Content/Copy, Trust & Safety, Legal/Compliance (as downstream consumers), Engineering (as downstream consumers).

## Completion Criteria

- [ ] Every sensitive data category has an approved plain-language explanation covering what, why, benefit, and consequence-of-decline.
- [ ] The consent-state matrix covers all three states (granted, denied, revoked) for every permission and every affected pillar feature.
- [ ] Revocation has been validated to work independently per permission without forcing full pillar abandonment.
- [ ] Re-request frequency and framing rules are specific enough to be tested against (no vague "occasionally" language).
- [ ] Confirmed this document describes only user-facing consent experience, with no backend compliance or security implementation content.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Trust & Safety Lead (required).
