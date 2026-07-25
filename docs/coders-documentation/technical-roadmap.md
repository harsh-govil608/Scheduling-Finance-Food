# Technical Roadmap — What's Left

**Last updated:** 2026-07-26, after Day 4 (Home pillar launch). This is a living document -
update it whenever a PRD moves from "not started" to "built," or when a new cross-cutting
technical gap is identified. Polishing/verification of already-built features is tracked
separately (see docs/coders-documentation/day-*.md's "Known Gaps" sections) - this file is
strictly about **new implementation**: PRDs with zero code behind them yet.

## Current State

**27 of 47 Phase 3 PRDs built** to a genuine, honestly-scoped-down, working implementation
(see docs/coders-documentation/day-1.md through day-4.md for exactly what was cut and why in
each). Two pillars exist: Finance (complete, 7/7 PRDs) and Home (8/10 Productivity Suite PRDs).
Zero PRDs exist yet for Health (Food) - an explicit, deliberate deferral, not an oversight
(see day-4.md Section 1 for the reasoning: no free automatic-capture hook like Finance's bank
SMS, and meal tracking needs real photo/ML recognition, which nothing else in this app needs).

| Group | Docs | Built | Remaining |
|---|---|---|---|
| Daily Experience Core Surfaces | 01-06 | 6 | 0 |
| Productivity Suite (Home) | 07-16 | 8 | 2 |
| Finance Suite | 17-23 | 7 | 0 |
| Health Suite (Food) | 24-30 | 0 | 7 |
| Intelligence Layer | 31-34 | 1 | 3 |
| Life Utility Features | 35-39 | 0 | 5 |
| Account & Access Platform | 40-47 | 5 | 3 |
| **Total** | | **27** | **20** |

---

## Remaining Work, Grouped by What's Actually Buildable Right Now

### Group A — Buildable now, same toolkit as everything else (rule-based, no AI, no backend, no new permissions)

These can be built exactly the way Tasks/Habits/Goals were: real CRUD against Room, honest
scope cuts where the full PRD wants AI, no new infrastructure required.

- **Notes (Doc 37)** — a plain note-taking feature. Full PRD likely wants AI tagging/search;
  buildable version is title+body+timestamp CRUD, same shape as Goals.
- **Journal (Doc 38)** — daily free-text entries. Same shape as Notes; the two could plausibly
  share a data model with a `type` flag rather than being fully separate features.
- **Shopping (Doc 36)** — a shopping list. Same shape as Tasks (item, quantity, checked-off),
  arguably simpler.
- **Context Timeline (Doc 32)** — the full PRD implies AI-driven cross-pillar inference, but a
  **rule-based version is honestly buildable**: a plain chronological feed of what actually
  happened today across both pillars (transactions captured, tasks completed, habits checked
  off), no inference, just a merged, sorted list. Worth doing since the data already exists in
  both pillars' tables - this is the same "connect what's already built" move as the Morning
  Briefing enhancement.

### Group B — Buildable, but needs a new permission or platform integration (moderate lift, no AI)

- **Calendar Intelligence (Doc 08)** — the "intelligence" half is AI-dependent and should stay
  cut, but a **read-only calendar view** (Android's `CalendarContract` provider, a new runtime
  permission) showing today's/upcoming events alongside Home's task list is honestly buildable
  without any ML. This is the one PRD in the "genuinely blocked" list from Day 4 worth
  reconsidering at this narrower scope.
- **Travel (Doc 35)** — likely wants itinerary structuring; a scoped version (manually logged
  trips with dates, similar shape to Goals) is buildable, though lower priority than Notes/
  Journal/Shopping since travel data doesn't connect to anything else in the app yet.
- **Knowledge Vault (Doc 39)** — full PRD likely wants tagging/linking/search across saved
  content; a scoped version (a flat list of saved text/links) is buildable, but the value
  proposition is weak without real search - lowest priority in this group.

### Group C — Blocked on AI/ML (no honest way to scope down further)

These all have the same problem: the PRD's entire value proposition IS the AI, and there's no
honest reduced version that isn't just a duplicate of something else already built.

- **AI Scheduler (Doc 07)** — becomes a duplicate of Smart Reminders (already built) without
  real time-slotting intelligence.
- **AI Memory (Doc 31)** — a persistent, retrievable memory of user patterns/preferences across
  pillars; without ML this is just... more database tables with no product behind them.
- **AI Coach (Doc 33)** — explicitly a coaching/insight-generation feature; nothing to build
  without a real model.

**These stay blocked until there's an actual reason to integrate an LLM/ML service** - at that
point this becomes the first genuinely new category of technical work (API integration, prompt
design, a privacy/data-handling review since it'd mean SMS-derived data leaving the device for
the first time, which breaks the "100% on-device" privacy story the pilot has had since Day 1).

### Group D — Blocked on backend/accounts infrastructure (none exists)

- **Shared/Family Mode (Doc 42)** — needs a multi-user concept; there is no concept of "another
  user" anywhere in this app.
- **Premium Features (Doc 43)** — needs payment processing (Google Play Billing at minimum) and
  a concept of subscription/entitlement state. Directly relevant to the "how do we earn money"
  conversation - this is the PRD that would need to exist before there's any way to charge
  anyone anything.
- **Cross-Device Sync (Doc 45)** — needs a real backend (the pilot has explicitly stayed
  100% local-only since Day 1's architecture doc). This one PRD is the actual gateway to needing
  a backend at all - everything else so far has deliberately avoided it.

### Group E — Explicitly deferred by your own decision, not a technical blocker

- **All 7 Health Suite PRDs (Docs 24-30)**: Nutrition Tracking, Meal Recognition, Workout
  Tracking, Sleep, Hydration, Medicine, Health Goals. Meal Recognition specifically needs real
  photo/ML - the others (Sleep, Hydration, Medicine, Health Goals, Workout Tracking) are
  actually closer to Group A (rule-based, manual-entry buildable) if/when Food gets
  reconsidered - worth remembering that "Food" as a whole pillar isn't uniformly AI-blocked,
  only Meal Recognition specifically is.

---

## Cross-Cutting Technical Debt (not tied to any single PRD)

These aren't features from the docs, but they're real technical work that will eventually block
progress if left too long:

1. **`fallbackToDestructiveMigration()` is still in place.** Every schema change wipes
   non-SMS-derived data (manual entries, tasks, habits, goals, projects, budgets, rules,
   consent, notification read-state). Self-healing exists for SMS-derived data only (the
   `onDestructiveMigration` -> scan-reset callback from Day 3). This has been an accepted
   pilot-stage tradeoff since Day 1; it stops being acceptable the moment real users have
   Tasks/Habits/Goals data worth protecting across an update - which is now, since Home exists.
   **This is worth prioritizing soon, probably before the 2-person test window**, since Sohom
   losing his own manually-entered tasks/habits to a future schema bump would be a much worse
   experience than losing auto-derived transactions (which at least self-heal).
2. **Bank template coverage.** Only ICICI and SBI (one format each) are verified. This is
   ongoing technical work, not a PRD - every new bank needs a real sample, the same
   one-at-a-time rigor as the first two.
3. **No Play Store listing.** Directly blocks reaching beyond a small personal network (see the
   "500-1000 users" conversation) - not urgent during the current 2-pillar hardening phase, but
   the real unlock once ready to widen distribution.
4. **No release-signing pipeline.** Currently everything ships as a debug-signed APK. Fine for
   pilot testers; would need a real release keystore before any Play Store submission.

---

## Suggested Build Order (once the current hardening/verification pass is done)

1. Fix `fallbackToDestructiveMigration()` with a real `Migration` path (cross-cutting debt #1) -
   protects the Home pillar data that now exists before more people rely on it.
2. Notes + Journal + Shopping (Group A) - cheapest remaining PRDs, same proven pattern, rounds
   out the Home pillar further.
3. Context Timeline (Group A) - connects existing data across both pillars, a genuine (if small)
   step toward the actual cross-pillar differentiation story.
4. Revisit Calendar Intelligence at the read-only scope (Group B) once the above is stable.
5. Everything else (Groups C, D, E) stays blocked/deferred until their respective prerequisites
   (an AI/ML integration decision, a backend decision, or the Food decision) actually change.
