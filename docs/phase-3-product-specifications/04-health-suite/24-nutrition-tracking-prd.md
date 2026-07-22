# Document 24: Nutrition Tracking PRD

## Document Name
Nutrition Tracking PRD

## Purpose
Define the complete specification for the nutrition data model and the daily/weekly nutrition summary experience — how logged food becomes structured nutrient data, and how that data is aggregated, displayed, and corrected over time. This PRD governs storage-adjacent product behavior (what fields exist, how they roll up, how edits propagate) but not how food enters the system in the first place.

## Why It Exists
Nutrition Tracking sits at the center of the Health pillar: nearly every other health feature (Meal Recognition, Health Goals, Sleep & Habit Insights) reads from or writes to the nutrition record it defines. Without a single agreed specification for what a "logged meal" contains, how macros and micros are computed, how daily totals reset, and how retroactive edits ripple through history, downstream features would each invent their own assumptions and drift out of sync. This document exists to give engineering, design, and AI/ML teams one authoritative contract for the nutrition data surface before any capture mechanism or goal-tracking logic is built on top of it.

## Approximate Page Count
9-12 pages

## Sections
1. **Feature Scope** — In scope: the nutrition entry data model (fields, units, macro/micro breakdown), daily and weekly summary views, manual edit/delete of a logged entry, and historical trend rollups. Out of scope: the photo/voice capture flow and confidence scoring (owned by Meal Recognition PRD), and goal thresholds/targets (owned by Health Goals PRD) — this PRD only defines what gets tracked and summarized, not what "good" looks like.
2. **User Stories** — As a user, I want to see today's total calories and protein at a glance so I don't have to add them up myself. As a user, I want to edit a logged meal's portion size after the fact and have my daily total update immediately. As a user, I want to see a weekly nutrition trend so I can tell if a bad day was an outlier. As a user, I want to tap into any past day and see exactly what was logged and when. As a user recovering from a missed log, I want to add a meal retroactively for an earlier time without it appearing as "now."
3. **Functional Requirements** — Define the canonical nutrition entry schema (food item(s), quantity/portion, macros, timestamp, source of entry); define daily summary aggregation rules including timezone and day-boundary handling; define weekly/rolling-window summary computation; define edit and delete behavior and how each propagates to already-computed summaries; define how entries logged from different capture sources (photo, voice, manual, future integrations) normalize into the same schema.
4. **Non-Functional Requirements** — Daily summary must recompute and render within a defined latency budget after any single-entry edit; nutrition history must remain available offline for a rolling window; because nutrition data is health-adjacent, all read/write paths must respect the user's data-sharing consent state defined in Permissions & Consent UX, and summaries must degrade gracefully (not silently fabricate values) when underlying entries are incomplete.
5. **UX Requirements** — Must conform to Nutrition & Goals Experience and Food Logging Experience from Phase 2; daily summary must be scannable in under a few seconds (single glance for calories/protein/water-adjacent macros); edits to a past entry must never be indistinguishable from edits to today's entry — the UI must always disclose which day is being modified.
6. **States & Flows** — Entry states: draft/unconfirmed, confirmed, edited, deleted (soft-delete with recovery window); summary states: partial-day (in progress), complete-day (past midnight boundary), backfilled (entries added after the day closed); flow from raw entry to day summary to week rollup.
7. **Edge Cases** — Logging a meal that spans a day boundary (e.g., a midnight snack); editing an entry after its day's summary has already been used to compute a weekly trend; duplicate entries from a retried capture action; entries with partial nutrient data (e.g., calories known, protein unknown); user changes timezone mid-day (e.g., travel) and day boundaries shift.
8. **Failure Scenarios** — Underlying nutrient values are missing or malformed from an upstream capture source — summary must show the entry as incomplete rather than silently treating missing fields as zero; a batch of entries fails to sync and the daily total is understated — the UI must distinguish "no food logged" from "sync pending"; a correction to an old entry conflicts with a cached weekly summary — recomputation must be triggered rather than serving a stale rollup.
9. **AI Behaviors** — Nutrition Tracking is a passive data surface early on the Proactivity Ladder (Remember, Predict tiers): it should notice patterns (e.g., protein consistently low by evening) and surface them as gentle observations, not directives; it may predict likely repeat meals based on history to speed future logging, but must never auto-log without explicit confirmation at this feature's ladder rung.
10. **Notification Behaviors** — Nutrition Tracking itself does not originate reminders (that belongs to Hydration/Health Goals); any summary-driven nudge (e.g., "protein is trending low today") must be arbitrated through the shared Notification System rather than firing independently, and must respect quiet hours and per-user notification tolerance.
11. **Success Criteria** — Users can understand their nutrition for any given day within seconds of opening the summary; corrections feel safe and reversible; users trust the numbers enough to reference them when making food decisions later in the day.
12. **Metrics** — Daily summary view rate; percentage of logged entries edited within 24 hours (proxy for capture accuracy handled by Meal Recognition, but tracked here as a data-quality signal); weekly summary engagement rate; time-to-render for daily/weekly summaries.
13. **Open Questions** — How far back should retroactive edits be allowed before they're locked? Should weekly summaries be calendar-week or rolling-7-day by default? How should the summary represent days with zero logged entries — silence, or an explicit "nothing logged" state?

## Deliverables
- Canonical nutrition entry and summary data model specification
- Daily and weekly summary UX flow specification
- Edit/delete/backfill behavior specification
- Cross-source entry normalization rules

## Dependencies
Nutrition & Goals Experience, Food Logging Experience, Automation Philosophy, Permissions & Consent UX (Phase 2); Meal Recognition PRD, Health Goals PRD (Phase 3)

## Teams Using This
Product, Engineering (Mobile), Engineering (Backend), Data/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Data model reviewed and approved by Engineering Lead for compatibility with Meal Recognition and Health Goals PRDs.
- [ ] UX flows validated against Nutrition & Goals Experience and Food Logging Experience.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Design Lead (required).
