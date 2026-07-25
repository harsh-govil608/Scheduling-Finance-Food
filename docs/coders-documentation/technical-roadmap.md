# Technical Roadmap — What's Left

**Last updated:** 2026-07-26, after Day 5 (Notes/Journal/Shopping/Timeline, the real Room
migration, and the proactivity batch). This is a living document - update it whenever a PRD
moves from "not started" to "built," or when a new cross-cutting technical gap is identified.
Polishing/verification of already-built features is tracked separately (see
docs/coders-documentation/day-*.md's "Known Gaps" sections) - this file is strictly about
**new implementation**: PRDs with zero code behind them yet.

## Current State

**31 of 47 Phase 3 PRDs built** to a genuine, honestly-scoped-down, working implementation
(see docs/coders-documentation/day-1.md through day-4.md for exactly what was cut and why in
each). Two pillars exist: Finance (complete, 7/7 PRDs) and Home (8/10 Productivity Suite PRDs,
plus Notes/Journal/Shopping/Context Timeline from the adjacent Life Utility / Intelligence
groups). Zero PRDs exist yet for Health (Food) - an explicit, deliberate deferral, not an
oversight (see day-4.md Section 1 for the reasoning: no free automatic-capture hook like
Finance's bank SMS, and meal tracking needs real photo/ML recognition, which nothing else in
this app needs).

| Group | Docs | Built | Remaining |
|---|---|---|---|
| Daily Experience Core Surfaces | 01-06 | 6 | 0 |
| Productivity Suite (Home) | 07-16 | 8 | 2 |
| Finance Suite | 17-23 | 7 | 0 |
| Health Suite (Food) | 24-30 | 0 | 7 |
| Intelligence Layer | 31-34 | 2 | 2 |
| Life Utility Features | 35-39 | 3 | 2 |
| Account & Access Platform | 40-47 | 5 | 3 |
| **Total** | | **31** | **16** |

**Honest read on this ratio:** the 16 remaining are concentrated exactly where you'd expect -
7 are the entire deliberately-deferred Health pillar, 2 are AI-blocked with no honest reduced
scope (AI Memory, AI Coach), 3 need a backend/payments/multi-user that doesn't exist yet. Only
4 remaining PRDs (Calendar Intelligence at read-only scope, Travel, Knowledge Vault, plus
whichever Account/Access items don't need a backend) are actually "just build it" work at this
point. Feature breadth is not the bottleneck anymore - see the hardening list below for what is.

---

## Remaining Work, Grouped by What's Actually Buildable Right Now

### Group A — DONE (2026-07-26)

Notes, Journal, and Shopping shipped as plain Room CRUD (title+body+timestamp / item+quantity+
checked, same shape as Goals/Tasks). Context Timeline shipped as a rule-based, no-inference
chronological merge of today's transactions/completed-tasks/habit-completions across both
pillars. All four verified against real on-device data. Nothing left in this group.

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

1. **`fallbackToDestructiveMigration()` is only actually safe for the 7→8 gap now.**
   `MIGRATION_7_8` (Notes/Shopping tables) is the project's first real, verified, non-destructive
   migration - but the destructive fallback still fires for every *other* version gap. Nothing
   stops the next schema change from being written the old (destructive) way again unless this
   becomes a standing rule, not a one-off fix. **Partially addressed, not solved.**
2. **Zero automated test coverage beyond 4 SMS-parser unit tests.** No tests for DAOs,
   ViewModels, the migration itself, or any Compose UI. Every feature so far has been verified
   by manual on-device tapping in a live session - that doesn't scale past one person doing it,
   and it means regressions can only be caught by luck.
3. **No crash reporting.** Once a build leaves this machine, there is no visibility into what
   breaks for Sohom or anyone else - verification has entirely depended on live `adb logcat`
   greps during a session with the device in hand.
4. **Bank template coverage.** Only ICICI and SBI (one format each) are verified. The core
   "automatic capture" differentiator only works for these two banks' exact SMS wording today -
   every other bank falls into "unparsed." This is ongoing work, not a one-time fix.
5. **Only ever tested on one physical device**, shared with its actual owner (not a dedicated
   test rig) - zero coverage of other Android versions/OEMs. Samsung's Auto Blocker already
   showed real install friction that a different device wouldn't have surfaced.
6. **No Play Store listing.** Directly blocks reaching beyond a small personal network (see the
   "500-1000 users" conversation) - not urgent during the current 2-pillar hardening phase, but
   the real unlock once ready to widen distribution.
7. **No release-signing pipeline, no CI.** Currently everything ships as a manually-built,
   debug-signed APK from one machine. Fine for 2 pilot testers; would need a real release
   keystore and automated builds before any Play Store submission or larger tester group.
8. **No security/privacy hardening pass.** The "100% on-device" architecture is a genuinely
   strong privacy story, but nothing has verified it end-to-end yet - no encrypted-storage audit,
   no data export/delete flow, for an app that reads SMS and financial data.

---

## Before Widening Past the 2-Person Test (priority order)

This is the practical checklist for the gap between "works for me and Sohom" and "safe to hand
to a third person," per the agreed staged-rollout plan:

1. **Generalize the real-migration discipline (debt #1).** Not a new feature - a rule: every
   future schema bump gets a hand-written `Migration`, the same rigor as `MIGRATION_7_8`, before
   it ships. This is the one item that directly protects data a wider group of testers would
   start accumulating.
2. **Crash reporting (debt #3).** Cheapest visibility win available - without it, every future
   bug report from a tester who isn't sitting next to you starts from "it doesn't work" with no
   further information.
3. **A minimal automated test layer (debt #2).** Doesn't need to be comprehensive - DAO tests for
   the Room entities and a test for the migration itself would catch the two classes of bug
   (data loss, duplication) that have actually happened so far.
4. **Bank template coverage (debt #4)**, in proportion to who's actually being onboarded next -
   check what bank each new tester uses before adding them, not after.
5. Everything else (Play Store listing, release signing/CI, second device coverage, privacy
   audit) matters more the wider the rollout gets, but isn't blocking the *next few* testers the
   way 1-3 are.

## Suggested Build Order (net-new features, once the above hardening is underway)

1. Revisit Calendar Intelligence at the read-only scope (Group B).
2. Travel, Knowledge Vault (Group B) - lower priority, weaker standalone value.
3. Everything else (Groups C, D, E) stays blocked/deferred until their respective prerequisites
   (an AI/ML integration decision, a backend decision, or the Food decision) actually change.
