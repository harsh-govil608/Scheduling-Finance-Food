# Document 5: User Personas Document

## Purpose

Define, in concrete behavioral (not demographic) detail, the specific people the product is built for — so that every future product decision can be tested against "would this help [Persona] specifically" instead of a vague "the user."

## Why It Exists

A product spanning Productivity, Finance, and Health for "tens of millions of users" risks designing for an imaginary universal user who doesn't exist, resulting in a product that's mediocre for everyone. This document exists to force specificity early: which real pattern of life this product optimizes for first, what tradeoffs that implies, and which users are explicitly *not* the initial target (without excluding them from the long-term vision).

## Approximate Page Count

10–12 pages.

## Sections

1. **Persona Methodology** — how personas were derived (user interviews, market research from Problem Statement/Market Definition, or founder-hypothesis pending validation — labeled honestly).
2. **Primary Persona(s)** (2–3 max, to avoid dilution) — each with:
   * Life snapshot (role, life stage, daily rhythm — enough to simulate their day, not just an age/income label).
   * Current tool stack across Productivity/Finance/Health and where it fails them (ties to Problem Statement).
   * Trigger moment — what causes them to seek/adopt this product.
   * Cross-domain moment — a specific scenario where the three pillars intersect for this person (e.g., a schedule change causing a missed meal causing an impulse expense) — this is the persona-level proof of "why unified, not three apps."
   * What "manual work approaching zero" looks like specifically for them.
   * Trust threshold — how much autonomy (per the Proactivity Ladder in the Philosophy doc) they'd accept, and when.
3. **Secondary Persona(s)** (1–2) — briefer, capturing meaningful variation (e.g., different risk tolerance for financial autonomy, different data-sharing comfort for health).
4. **Explicit Anti-Persona** — who this is *not* built for at launch (e.g., users wanting enterprise/team features, users wanting manual full-control power-tools) and why excluding them is a deliberate focus decision, not an oversight.
5. **Persona-to-Pillar Mapping** — a matrix showing which pillar matters most to which persona and in what sequence, to inform later feature prioritization (Phase 3).
6. **Persona Validation Plan** — how/when these personas will be validated or revised against real user data post-launch (personas are hypotheses until proven).

## Deliverables

* Approved User Personas document.
* One-page persona cards (derived artifact) per primary/secondary persona for use in design and PRD reviews.
* Persona-to-Pillar prioritization matrix (feeds Phase 3 PRD sequencing).

## Dependencies

Requires **Problem Statement Document** (personas must experience the stated problems) and **Market Definition Document** (personas must sit inside the sized, prioritized market/geography).

## Which Teams Use This

Product (feature prioritization, roadmap sequencing), Design (UX decisions, tone), Research (validation targets), Marketing (messaging and acquisition targeting), Data Science (defining what signals matter for prediction per persona).

## Completion Criteria

* [ ] Each primary persona includes a concrete cross-domain scenario spanning at least 2 of the 3 pillars — abstract personas without a lived scenario are rejected.
* [ ] Anti-persona section explicitly approved by founders (prevents silent scope creep back toward "building for everyone").
* [ ] Persona claims are labeled as either "validated by interview/data" or "hypothesis pending validation" — no unlabeled claims.
* [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Research (required once hired).
