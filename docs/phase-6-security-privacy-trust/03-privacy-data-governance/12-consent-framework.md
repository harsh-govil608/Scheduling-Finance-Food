# Document 12: Consent Framework

## Document Name
Consent Framework

## Purpose
Define the technical and legal architecture underlying every consent capture, storage, versioning, and withdrawal event in the product — what constitutes valid consent per data category, the consent record schema, how a change to policy or purpose creates a new consent version, and the enforcement contract that guarantees a withdrawal actually stops data use everywhere it occurs. This document is the backend/legal engine beneath Phase 2's Permissions & Consent UX and Phase 3's Permissions & Consent PRD; it does not define request screens, copy, or on-screen sequencing.

## Why It Exists
Phase 2's Permissions & Consent UX (Doc 32) defines how consent is explained and experienced, and Phase 3's Permissions & Consent PRD (Doc 41) defines the buildable request/revoke feature and a product-behavior consent-state schema — but neither owns the harder question of what makes a consent record legally and operationally valid, how consent survives a policy change, or what "propagation" must mean at a systems level once a user revokes access mid-use. For a product that reads SMS content, tracks location, and stores financial and health data specifically so an AI can act with reduced friction, a consent architecture that is merely a UI toggle backed by an unversioned boolean is a trust and compliance failure waiting to happen. This document exists to give every service in the platform one authoritative, auditable answer to "is this specific use of this specific data currently consented to, and can I prove it."

## Approximate Page Count
9-11 pages

## Sections
1. **Consent Validity Model** — the conditions under which a consent record counts as valid (specific, informed, freely given, revocable, timestamped, tied to a specific policy version), independent of the UI or copy used to capture it (Phase 2 Doc 32).
2. **Consent Record Schema** — the canonical fields for every consent grant (user id, data category, purpose, legal basis, policy version, granted timestamp, revoked timestamp, capturing surface), extending the Permissions & Consent PRD's product-behavior schema (Phase 3 Doc 41) with the fields required for legal defensibility.
3. **Consent Versioning** — how a change to a privacy policy, terms of service, or a permission's stated purpose creates a new consent version, whether prior consents carry forward or require re-consent, and how the system distinguishes "consented to v3" from "has never seen v4."
4. **Purpose Limitation Enforcement** — the technical control tying a consent grant to the specific purpose(s) it was given for, preventing a service from repurposing consented data for an unconsented secondary use (e.g., SMS access granted for transaction parsing being used for ad targeting).
5. **Withdrawal Propagation Contract** — the hard technical guarantee, extending the Permissions & Consent PRD's product-level propagation requirement (Phase 3 Doc 41), that a withdrawal event reaches every downstream consumer — services, caches, AI memory, embeddings, batch jobs — within a defined bound, with a named owning team per consumer.
6. **Consent-to-Processing Enforcement Point** — where in the request path (API gateway, service layer, AI platform boundary) consent is actually checked before data is read or processed, and the fail-closed default applied when consent state cannot be verified.
7. **Third-Party & Downstream Consent Inheritance** — how consent is represented and honored when data is shared with a sub-processor or third-party integration (e.g., a banking aggregator, a cloud AI provider), and what must happen to that data at the third party when consent is withdrawn.
8. **Special-Category & Shared-Account Consent Handling** — additional validity requirements for higher-sensitivity data (health) and for shared/family-mode accounts (Phase 3 Doc 42) where consent may need to be captured per family member rather than per account.
9. **Consent Audit Trail** — the immutable record retained to prove, after the fact, what a user consented to and when, and how it is produced in response to a regulator, auditor, or user dispute.
10. **Legal Basis Mapping & Counsel Review Checkpoint** — the framework for mapping each data category/purpose pair to a stated legal basis, with an explicit requirement that final legal-basis determinations and jurisdiction-specific validity rules are reviewed and approved by qualified counsel rather than finalized by this document alone.

## Deliverables
- Canonical consent record schema, published for engineering adoption across all services.
- Consent validity checklist per data category.
- Withdrawal propagation SLA table with a named owning team per downstream consumer.
- Consent versioning and re-consent decision tree.
- Draft legal basis mapping table, routed for qualified counsel review prior to finalization.

## Dependencies
Phase 1 Guiding Principles Document (Doc 7); Phase 2 Permissions & Consent UX (Doc 32); Phase 3 Permissions & Consent PRD (Doc 41); Phase 3 Shared/Family Mode PRD (Doc 42); Phase 6 Security Program & Governance (Doc 01); Phase 6 Data Classification (Doc 14); Phase 6 Data Ownership (Doc 15).

## Teams
Legal, Privacy/DPO, Security, Engineering, Data Platform, Product, Compliance

## Completion Criteria
- [ ] Every data category has a documented validity checklist confirming the specific/informed/freely-given/revocable conditions.
- [ ] Withdrawal propagation bound is defined and agreed with every downstream consumer team.
- [ ] Consent versioning rules are tested against at least one hypothetical policy-language change.
- [ ] Legal basis mapping is reviewed by qualified counsel and not treated as final until that review completes.
- [ ] Signed off by: Head of Privacy/DPO (required), General Counsel (required), CISO (required), Head of Engineering (required).
