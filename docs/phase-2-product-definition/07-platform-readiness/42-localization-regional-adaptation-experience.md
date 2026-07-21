# Document 42: Localization & Regional Adaptation Experience

## Document Name

Localization & Regional Adaptation Experience

## Purpose

Define what must adapt to a user's language, region, and local financial/cultural conventions for the product to feel native rather than translated — and, just as importantly, what must stay identical everywhere so "one intelligent assistant" doesn't fragment into inconsistent regional behavior.

## Why It Exists

The product's Finance pillar is explicitly anchored to region-specific mechanics (UPI transaction tracking, SMS-based parsing conventions that differ by carrier and banking format), and the Market Definition Document already commits to geographic prioritization — without a dedicated document, localization gets treated as a translation afterthought instead of a product requirement, and the first international market becomes a rebuild instead of a rollout.

## Approximate Page Count

6–8 pages.

## Sections

1. **What Localizes vs. What Doesn't** — language, currency, date/time, and cultural tone adapt; core philosophy, the Proactivity Ladder, and trust/consent rules never do, regardless of region.
2. **Financial Localization** — region-specific transaction parsing conventions (UPI, SMS formats), currency display, and budget norms, scoped to product experience (parsing implementation itself is a later phase).
3. **Language & Tone Adaptation** — how the AI's encouraging, non-judgmental voice (per Phase 1 Product Philosophy) translates across languages without losing its character.
4. **Cultural Calendar & Scheduling Norms** — week start day, holidays, work-week conventions affecting the Scheduling System and Calendar Experience.
5. **Health & Nutrition Regional Adaptation** — regional food databases and units (metric vs. imperial), without altering the underlying nutrition-goal experience.
6. **Rollout Sequencing Model** — how a new region is added to the product (what must exist before a market can be supported), referencing the Market Definition Document's prioritization.
7. **Non-Negotiables Checklist** — a standing list product and design teams check new features against before shipping regionally exclusive behavior.

## Deliverables

* Approved Localization & Regional Adaptation Experience document.
* "What Localizes vs. What Doesn't" reference table, reusable across every future PRD.
* Region-readiness checklist gating new-market launches.

## Dependencies

Requires Market Definition Document (Phase 1, geographic prioritization), Transaction Capture Experience Document, Product (Behavioral) Philosophy Document (Phase 1, tone of voice principles).

## Which Teams Use This

Product, Design, Localization/Internationalization, Business Development/Partnerships (regional financial integrations), Engineering leadership (for later architecture planning).

## Completion Criteria

* [ ] "What Localizes vs. What Doesn't" table reviewed and approved — no feature ships regionally-inconsistent core behavior without an explicit exception logged here.
* [ ] At least one full regional rollout scenario (the Market Definition Document's first-priority market) walked through end-to-end against this document.
* [ ] Signed off by: Head of Product (required), Head of Design (required).
