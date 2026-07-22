# Phase 8 — Business & Go-To-Market Requirements

Per `phase8.md`, this document defines every business documentation artifact required before commercialization. As with prior phases, this is the requirements specification, not the business plan itself — no final prices, dates, or numbers are invented anywhere in this phase; every document defines a framework/methodology.

---

## Document Set

### Group 1 — Business Model Core (`01-business-model-core/`)

| # | Document | Pages | File |
|---|---|---|---|
| 1 | Business Model | 8–10 | [`01-business-model.md`](01-business-model-core/01-business-model.md) |
| 2 | Pricing Strategy | 6–8 | [`02-pricing-strategy.md`](01-business-model-core/02-pricing-strategy.md) |
| 3 | Subscription Plans | 6–8 | [`03-subscription-plans.md`](01-business-model-core/03-subscription-plans.md) |
| 4 | Premium Features (Business Layer) | 6–8 | [`04-premium-features-business-layer.md`](01-business-model-core/04-premium-features-business-layer.md) |
| 5 | Monetization Strategy | 7–9 | [`05-monetization-strategy.md`](01-business-model-core/05-monetization-strategy.md) |

### Group 2 — Unit Economics & Metrics (`02-unit-economics-metrics/`)

| # | Document | Pages | File |
|---|---|---|---|
| 6 | Unit Economics | 6–8 | [`06-unit-economics.md`](02-unit-economics-metrics/06-unit-economics.md) |
| 7 | CAC | 6–8 | [`07-cac.md`](02-unit-economics-metrics/07-cac.md) |
| 8 | LTV | 6–8 | [`08-ltv.md`](02-unit-economics-metrics/08-ltv.md) |

### Group 3 — Growth (`03-growth/`)

| # | Document | Pages | File |
|---|---|---|---|
| 9 | Growth Loops | 7–9 | [`09-growth-loops.md`](03-growth/09-growth-loops.md) |
| 10 | Referral | 6–8 | [`10-referral.md`](03-growth/10-referral.md) |
| 11 | Virality | 6–8 | [`11-virality.md`](03-growth/11-virality.md) |
| 12 | Retention | 6–8 | [`12-retention.md`](03-growth/12-retention.md) |

### Group 4 — Community & Support (`04-community-support/`)

| # | Document | Pages | File |
|---|---|---|---|
| 13 | Community | 6–8 | [`13-community.md`](04-community-support/13-community.md) |
| 14 | Customer Success | 6–8 | [`14-customer-success.md`](04-community-support/14-customer-success.md) |
| 15 | Support | 6–8 | [`15-support.md`](04-community-support/15-support.md) |

### Group 5 — Partnerships (`05-partnerships/`)

| # | Document | Pages | File |
|---|---|---|---|
| 16 | Partnerships (General Strategy) | 6–8 | [`16-partnerships-general-strategy.md`](05-partnerships/16-partnerships-general-strategy.md) |
| 17 | Banking Partnerships | 7–9 | [`17-banking-partnerships.md`](05-partnerships/17-banking-partnerships.md) |
| 18 | Wearable Partnerships | 6–8 | [`18-wearable-partnerships.md`](05-partnerships/18-wearable-partnerships.md) |

### Group 6 — GTM & Expansion (`06-gtm-expansion/`)

| # | Document | Pages | File |
|---|---|---|---|
| 19 | GTM Strategy | 8–10 | [`19-gtm-strategy.md`](06-gtm-expansion/19-gtm-strategy.md) |
| 20 | Market Expansion | 6–8 | [`20-market-expansion.md`](06-gtm-expansion/20-market-expansion.md) |
| 21 | Internationalization | 6–8 | [`21-internationalization.md`](06-gtm-expansion/21-internationalization.md) |
| 22 | Localization Strategy (Business Layer) | 6–8 | [`22-localization-strategy-business-layer.md`](06-gtm-expansion/22-localization-strategy-business-layer.md) |

### Group 7 — Competitive & Planning (`07-competitive-planning/`)

| # | Document | Pages | File |
|---|---|---|---|
| 23 | Competitive Strategy | 7–9 | [`23-competitive-strategy.md`](07-competitive-planning/23-competitive-strategy.md) |
| 24 | Roadmap | 7–9 | [`24-roadmap.md`](07-competitive-planning/24-roadmap.md) |
| 25 | OKRs | 6–8 | [`25-okrs.md`](07-competitive-planning/25-okrs.md) |

### Group 8 — Brand & Feedback (`08-brand-and-feedback/`)

| # | Document | Pages | File |
|---|---|---|---|
| 26 | Brand & Positioning Strategy | 6–8 | [`26-brand-positioning-strategy.md`](08-brand-and-feedback/26-brand-positioning-strategy.md) |
| 27 | Customer Feedback & Voice-of-Customer Program | 6–8 | [`27-customer-feedback-voice-of-customer-program.md`](08-brand-and-feedback/27-customer-feedback-voice-of-customer-program.md) |

---

## Dependency Graph

```
PHASE 1 (Vision & Mission, Market Definition, Personas, Success Metrics, Guiding Principles)
        │
        ▼
01 Business Model ──┬──> 02 Pricing Strategy ──> 03 Subscription Plans ──> 04 Premium Features (Biz)
                     └──> 05 Monetization Strategy (synthesizes 02-04)
        │
        ▼
06 Unit Economics ──> 07 CAC / 08 LTV
        │
        ▼
09 Growth Loops ──> 10 Referral / 11 Virality / 12 Retention
        │
        ▼
13 Community / 14 Customer Success / 15 Support
        │
        ▼
16 Partnerships (General) ──> 17 Banking Partnerships / 18 Wearable Partnerships
        │
        ▼
19 GTM Strategy ──> 20 Market Expansion ──> 21 Internationalization ──> 22 Localization (Biz)
        │
        ▼
23 Competitive Strategy ── 24 Roadmap ── 25 OKRs
        │
        ▼
26 Brand & Positioning Strategy ── 27 Voice-of-Customer Program
```

## Writing Order

Group 1 (business model, blocking for everything monetization-adjacent) → Group 2 (economics, needs Group 1) → Group 3 (growth, needs Group 2's CAC/LTV) → Group 4 (support functions) → Group 5 (partnerships, needs Group 1) → Group 6 (GTM, needs Groups 1–2 and Phase 1) → Group 7 (competitive/planning, synthesizes everything above) → Group 8 (brand/feedback, closes the phase).

## Critical Path

1. **01 Business Model** — the root; reconciles the entire phase against Phase 1's anti-data-monetization guiding principle before any revenue mechanic is defined.
2. **06 Unit Economics** — CAC (07) and LTV (08) are meaningless in isolation; this document's shared cohorting/time-window conventions must exist first.
3. **19 GTM Strategy** — the single document synthesizing Phase 1's Market Definition + Personas with Phase 8's own CAC/Business Model into an actual launch plan.
4. **24 Roadmap** — the document that turns 8 phases and roughly 300 other documents into a sequenced, resourced execution plan; effectively the capstone of the entire documentation program to date.

## Estimated Total Documents

**27 documents** (+ this specification = 28 files in the phase).

## Estimated Total Pages

**~180–210 pages** across 27 documents.

---

## CEO/CRO Self-Review

**Coverage assessment: ~99% complete against `phase8.md`'s 24-item required coverage list, all covered, plus 2 gap-closing additions** (Brand & Positioning Strategy, Customer Feedback & Voice-of-Customer Program — the original list covered monetization, growth, partnerships, and GTM extensively but had no dedicated external-brand document distinct from Phase 1's internal mission, and no structured proactive feedback program distinct from reactive Support).

What remains open, honestly:

* **Fundraising materials** (pitch deck, cap table strategy, investor updates) are intentionally out of scope — `phase8.md` frames this phase as "how the company becomes a sustainable business" via product/growth/monetization, not capital-raising mechanics, consistent with Phase 2's original exclusion of fundraising from product scope.
* **Sales motion / enterprise GTM** is not separately documented — this product and its personas (Phase 1) are consumer-first; if a B2B/enterprise motion is ever pursued (e.g. selling to employers as a benefit), it would warrant a dedicated document at that time.
* **Financial modeling / projections** (actual multi-year P&L) is deliberately not a named document — Unit Economics (06) defines the *methodology* a financial model would run on, but the model itself is a living financial-planning artifact outside a documentation phase's scope.

No other item from `phase8.md`'s required coverage list, and no additional business-strategy concern the reviewer could identify as necessary before commercialization, remains undocumented. Phase 8 is ready to move to detailed drafting, with Business Model (01) and Roadmap (24) recommended for earliest review — the former because everything else depends on it, the latter because it's the phase's ultimate synthesis point.
