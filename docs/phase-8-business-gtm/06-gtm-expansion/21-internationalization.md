# Document 21: Internationalization

## Document Name
Internationalization

## Purpose
Define the business investment case for internationalization — which markets justify building durable multi-language, multi-currency, multi-regulatory infrastructure, and when that investment should be made relative to expansion timing. This document is the business-case/investment-decision layer; it is explicitly distinct from Phase 2's Localization & Regional Adaptation Experience (Doc 42), which defines the product-execution layer (what localizes vs. what doesn't, financial/language/cultural adaptation at the UX level). Doc 42 assumes the investment decision has been made; this document is what produces that decision.

## Why It Exists
Internationalization infrastructure — string externalization, currency and number formatting frameworks, right-to-left layout support, translation pipelines, locale-aware date/time handling — is expensive to build and, once built for a market that never justifies the spend, expensive to carry as ongoing maintenance debt. Without a dedicated business case, internationalization tends to be approved either too early (built speculatively "for scale" before any market demands it, burning engineering time the roadmap needed elsewhere) or too late (bolted on reactively after a market opportunity is already being lost to a localized competitor). This document exists to make the internationalization investment decision explicit, criteria-driven, and owned at the executive level — answering "which markets justify this spend, and at what point in their lifecycle" — so that Phase 2's product-execution localization work only begins once the business case actually supports it.

## Approximate Page Count
6-8 pages

## Sections
1. **Internationalization vs. Localization Boundary** — an explicit statement of scope distinguishing this document (the business case: whether and when to invest) from Phase 2's Localization & Regional Adaptation Experience, Doc 42 (the product execution: what changes at the UX level once the investment is approved).
2. **i18n Investment Trigger Framework** — the revenue, user-count, or strategic-partnership thresholds a market must show (or credibly project, per the Market Definition Document's sizing methodology) before internationalization infrastructure spend is approved for it.
3. **Cost Model for Internationalization** — the categorized cost buckets of building i18n infrastructure (engineering time for string externalization and locale frameworks, translation vendor cost, ongoing linguistic QA, locale-specific compliance overhead) at a methodology level, not final budget figures.
4. **Market Sequencing for i18n Investment** — how internationalization investment timing is coordinated with the Market Expansion (Phase 8, Doc 20) sequencing model, including the case for investing ahead of expansion (to de-risk entry) versus behind it (to avoid speculative spend).
5. **Build vs. Buy / Vendor Strategy** — the criteria for when translation/localization work is done via external vendors or platforms versus an in-house linguistic and engineering function, and how that choice shifts as market count grows.
6. **Regulatory & Compliance Cost Factors** — how market-specific regulatory requirements (data residency, financial-services localization mandates, consumer-protection disclosure language) factor into the i18n cost model beyond pure translation cost.
7. **ROI Model & Payback Threshold** — the methodology (consistent with the Unit Economics document's, Phase 8 Doc 06, standard time windows and cohorting rules) for weighing internationalization cost against projected revenue lift in the target market, and the minimum payback threshold required for approval.
8. **Minimum Viable Internationalization Bar** — the floor of i18n readiness (e.g., currency and date formatting at minimum, even without full language translation) required before any non-home-market user is served at all, distinct from the full-investment bar.
9. **Governance & Decision Rights** — who has approval authority over internationalization spend (CEO/CFO-level, given its infrastructure and multi-year maintenance implications), and how the decision is documented and revisited.

## Deliverables
* Approved Internationalization business-case framework, including the investment trigger criteria and cost model methodology.
* An ROI/payback model template applied to each candidate market before i18n spend is approved.
* A documented minimum-viable-internationalization bar distinguishing "servable" from "fully invested" markets.
* A governance record of internationalization investment decisions and their rationale, maintained over time.

## Dependencies
Requires Market Definition Document (Phase 1, Doc 4), Market Expansion (Phase 8, Doc 20), Localization & Regional Adaptation Experience (Phase 2, Doc 42 — the product-execution counterpart this document funds), Unit Economics (Phase 8, Doc 06). Directly informs Localization Strategy — Business Layer (Phase 8, Doc 22).

## Teams
CEO/CFO, Engineering Leadership, Product, Finance, Localization/Internationalization, Legal

## Completion Criteria
- [ ] Internationalization vs. localization boundary explicitly reviewed against Phase 2 Doc 42 to confirm no scope overlap or gap between the two documents.
- [ ] i18n investment trigger framework is quantified (named thresholds), not left as a qualitative "when it makes sense" judgment call.
- [ ] Cost model reviewed by Engineering Leadership for completeness against real i18n infrastructure cost categories (not translation cost alone).
- [ ] ROI/payback model methodology is consistent with the Unit Economics document's standard time windows and cohorting rules.
- [ ] Minimum viable internationalization bar reviewed and approved as the non-negotiable floor before any non-home-market launch.
- [ ] Signed off by: CEO (required), CFO/Finance Lead (required once hired), Head of Engineering (required).
