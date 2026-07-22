# Document 15: Data Ownership

## Document Name
Data Ownership

## Purpose
Define, for every data class, who owns it — the user, the company acting as steward, or a third party — and translate the Phase 1 Guiding Principles' commitment that "the user owns their data; the company is a steward, not an owner" into concrete, operational rules: what rights the user retains, what the company may and may not do with data absent explicit consent, and how ownership is represented in access-control decisions.

## Why It Exists
A principle stated once in a foundational document has no force on its own; without an operational ownership model, "the company is a steward, not an owner" is a sentence engineers and product managers can agree with in the abstract while still building features that treat user data as a company asset by default — an internal dashboard that surfaces raw transaction data without a logged purpose, or a data-sharing partnership evaluated purely on revenue terms. This document exists to give that principle teeth: a data class ownership map that Authorization (Phase 4 Doc 08) can enforce against, and a rulebook that Product and Business Development can be held to when a new monetization or partnership idea is proposed.

## Approximate Page Count
7-9 pages

## Sections
1. **Ownership Model Definition** — the three-way ownership taxonomy (user-owned, company-held-as-steward, third-party-sourced) and what rights and obligations attach to each category.
2. **Steward-Not-Owner Operationalization** — concrete rules translating the Phase 1 principle into engineering and business practice (e.g., user data is never sold; company use beyond stated consent purpose requires a new consent event; internal access requires a logged, legitimate purpose).
3. **Data Class Ownership Map** — every canonical entity in the Phase 4 Data Architecture (Doc 56) assigned an ownership category, cross-referenced against the sensitivity tiers in Data Classification (Doc 14).
4. **Derived & AI-Generated Data Ownership** — how the stewardship principle applies to data with no direct user-authored source — AI memory entries, predictions, embeddings — and who owns an inference the AI makes about a user that the user never explicitly stated.
5. **Company-Retained Aggregate & De-Identified Data** — the boundary between user-owned personal data and company-owned aggregate or de-identified analytics derived from it, and the de-identification bar a dataset must clear before it is treated as company-owned.
6. **Third-Party-Sourced Data Ownership** — the ownership treatment for data the product receives from a third party on the user's behalf (a banking aggregator, a wearable, a calendar provider), and which obligations flow through from that third party's own terms.
7. **Ownership-to-Access-Control Enforcement** — how an ownership category maps to concrete authorization rules (Phase 4 Doc 08) so that a user-owned record cannot be accessed internally without a legitimate, logged, and auditable purpose.
8. **Ownership Disputes & Shared Data** — how ownership is handled for shared/family-mode accounts (Phase 3 Doc 42) where more than one user has a stake in the same record, such as joint household financial data.

## Deliverables
- Published ownership taxonomy with rights and obligations per category.
- Data class ownership map covering every canonical entity.
- Steward-not-owner operational rulebook for Product and Business Development.
- Ownership-to-authorization enforcement mapping, validated against Phase 4 Doc 08.

## Dependencies
Phase 1 Guiding Principles Document (Doc 7); Phase 4 Data Architecture & Canonical Data Model (Doc 56); Phase 4 Authorization (Doc 08); Phase 3 Shared/Family Mode PRD (Doc 42); Phase 6 Data Classification (Doc 14).

## Teams
Legal, Privacy/DPO, Product, Engineering, Security, Executive Leadership

## Completion Criteria
- [ ] Ownership map covers every canonical entity with no unassigned category.
- [ ] Steward-not-owner rulebook tested against at least two hypothetical business-pressure scenarios (e.g., a proposal to sell aggregate data, a proposed ad-partnership use of transaction data).
- [ ] Ownership-to-authorization enforcement mapping validated with the Authorization architecture (Phase 4 Doc 08).
- [ ] Shared/family account ownership edge cases resolved and documented.
- [ ] Signed off by: Head of Privacy/DPO (required), General Counsel (required), CEO/Executive Sponsor (required), CISO (required).
