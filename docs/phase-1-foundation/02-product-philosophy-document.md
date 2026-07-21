# Document 2: Product Philosophy Document

## Purpose

Translate the mission into an explicit behavioral contract: what the AI does, in what order, in what tone, and where it must stop and defer to the user. This is the document that keeps "proactive" from becoming "intrusive" as hundreds of features get built by dozens of engineers who never met each other.

## Why It Exists

"The AI should remember, predict, suggest, remind, learn, adapt, encourage, and never overwhelm" (from `requirements.md`) is a philosophy, not a spec — it cannot be built directly. Every feature team will interpret it differently unless the interpretation rules are written down once, centrally. This document exists so that Finance, Health, and Productivity feature teams — who may never sit in the same room — build features that feel like one coherent assistant rather than three bots stapled together.

## Approximate Page Count

6–8 pages.

## Sections

1. **The Behavioral Loop** — formal definition of Remember → Predict → Suggest → Remind → Learn → Adapt → Encourage as a repeatable cycle, with a worked example per pillar (Productivity, Finance, Health).
2. **The Proactivity Ladder** — a graded scale (e.g., Level 0: silent observation → Level 1: passive surfacing → Level 2: active suggestion → Level 3: pre-filled action awaiting confirmation → Level 4: autonomous action with notification) defining exactly how much initiative the AI is allowed to take by default, and what unlocks higher levels (trust, explicit permission, track record).
3. **"Never Overwhelm" — Operationalized** — concrete rules: max proactive interruptions per day, quiet hours, batching logic, priority arbitration when Finance/Health/Productivity all want the user's attention simultaneously.
4. **One Assistant, Not Three Apps** — rules for cross-pillar coherence: shared memory model, shared notification queue, shared tone of voice, a single "inbox" mental model instead of three.
5. **Manual Work → Zero: The Effort Curve** — how the system is expected to reduce required user input over time per user (first week vs. month 6 vs. year 1), and what "manual work approaching zero" measurably means.
6. **Trust & Consent Model (Philosophy Level)** — the principle-level stance on autonomy vs. consent (detailed policy lives in Phase 6 Trust & Safety documents; this section states the philosophy those policies must implement).
7. **Tone of Voice Principles** — encouraging vs. nagging, honest vs. sugar-coated, how the AI talks about failures/misses (e.g., missed budget, missed workout).
8. **Anti-Patterns Explicitly Rejected** — dark patterns, guilt-based engagement loops, notification spam, streak-shaming — named and forbidden.

## Deliverables

* Approved Product Philosophy document.
* The Proactivity Ladder as a standalone reference diagram, reusable in every future PRD.
* A short "Philosophy Checklist" (derived artifact) product teams run every feature through before shipping.

## Dependencies

Requires the **Vision & Mission Document** (this is "the mission, operationalized as behavior").

## Which Teams Use This

Product (feature design decisions), Design (interaction and notification design), Engineering (notification/arbitration system architecture in Phase 4), Data Science/ML (what "predict" and "learn" are allowed to act on), Trust & Safety (baseline for consent policy).

## Completion Criteria

* [ ] Every one of the 8 philosophy verbs (remember, predict, suggest, remind, learn, adapt, encourage, never-overwhelm) has at least one concrete, testable rule attached — not just an adjective.
* [ ] The Proactivity Ladder has been validated against at least one worked scenario per pillar (Productivity, Finance, Health).
* [ ] Anti-patterns list reviewed against known dark-pattern taxonomies (e.g., streaks-as-guilt, red-badge notification bait) and explicitly rejected in writing.
* [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Trust & Safety (once hired, required before Phase 6).
