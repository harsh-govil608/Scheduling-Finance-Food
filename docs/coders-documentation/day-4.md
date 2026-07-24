# Coder's Documentation — Day 4

**Scope of Day 4:** the app's first second pillar. Every day before this was entirely inside Finance; today adds "Home" (the Productivity Suite, Phase 3 Docs 07-16) as a real second tab, not a mockup - Task Management, Habits, Goals, Projects, Smart Reminders, and Weekly/Monthly Review, all genuinely working end-to-end against a local database, using the exact same "rules first, prove the need before reaching for ML" discipline the Finance pillar was built on. Food (the Health Suite) is explicitly still untouched - see Section 4.

- [1. Why Home, and Not Food](#1-why-home-and-not-food)
- [2. What Was Built](#2-what-was-built)
- [3. Multi-Pillar Navigation](#3-multi-pillar-navigation)
- [4. Explicitly Not Started](#4-explicitly-not-started)
- [5. Verification Status](#5-verification-status)
- [6. PRD Tally](#6-prd-tally)

---

## 1. Why Home, and Not Food

Asked directly why Health/Food (nutrition tracking, meal recognition) hadn't been touched, the
honest answer given before writing any code: Finance had a structural advantage no other pillar
has for free - bank SMS gives automatic data capture with zero user effort. Food has no
equivalent; the two realistic options are manual logging (high-friction, historically the reason
food-tracking apps see steep day-2 drop-off) or photo/AI meal recognition (a real ML build, not a
rule-based one - the first genuinely AI-dependent feature this project would need). Productivity's
Task Management and Habits PRDs, by contrast, are buildable with the exact same toolset already
proven out in Finance: local Room storage, manual CRUD, rule-based derived state (streaks, due
dates), no AI, no new permissions, no backend. The user's own call: build out Home fully, prove
Finance and Home before reconsidering Food.

---

## 2. What Was Built

Six PRDs from the Productivity Suite (Phase 3 Docs 07-16), each scoped down the same way every
Finance PRD was - full spec reviewed, AI/backend-dependent sections named and cut, the remaining
buildable core implemented for real:

| PRD | Doc | What's real | What's cut |
|---|---|---|---|
| Task Management | 10 | Manual create/complete/delete, user-set priority, optional due date | No AI-inferred priority/duration, no recurrence, no subtasks |
| Habits | 13 | Habit CRUD, daily check-off, streak tracking | No AI calibration, no flexibility windows, no goal linkage |
| Goals | 12 | Title, optional target date, completed flag | No AI suggestions, no automatic progress inference |
| Projects | 11 | Named groups tasks can be tagged into | No dependency graphs, no milestones |
| Smart Reminders | 9 | Notification when a task's due date arrives | No AI-driven timing/re-prioritization |
| Daily Planning | 14 | Folded into the Home pillar's landing screen itself (today's due tasks + pending habits, inline) | No AI-generated focus suggestions, no cross-pillar time-blocking |
| Weekly/Monthly Review | 15/16 | One screen, a period toggle, real completed-task/habit-maintenance numbers | No AI-generated narrative insight |

**The one requirement that mattered most and wasn't just a scope cut**: the Habits PRD names its
own streak/momentum model as a high-risk area for accidentally becoming punitive - the "encourage,
never guilt" principle the Budget screen already had to be fixed for on Day 3. `HabitsViewModel`'s
`currentStreak()` never reports a lapsed streak as "broken"; a miss just returns to a "ready when
you are" state, and it doesn't even count "today" as missed until the day has actually passed
(finishing a habit at 11pm shouldn't have felt like recovering from a failure that never happened).

**Daily Planning was folded into the Home dashboard rather than built as its own screen** - the
same consolidation call made for Spend Prediction inside Budget on Day 2: the PRD's core idea
("what should I focus on today") IS what a pillar landing screen should already be showing, not a
separate surface competing for attention with it.

---

## 3. Multi-Pillar Navigation

Until today, the app was implicitly single-pillar - the TopAppBar on Home said "Finance" outright,
and there was no concept of switching contexts. `PillarBottomBar` is the first real multi-pillar
navigation element: a two-tab bottom bar (Finance / Home) shown only on each pillar's own landing
screen, not on drill-down detail screens (Ledger, Tasks, Goals, etc.) - standard tab-navigation
practice, and it avoids needing a nested-NavHost rewrite of the existing, already-tested Finance
navigation graph for a 2-tab pilot. Switching tabs is a plain `navController.navigate(...)` call
with `launchSingleTop = true`, not full back-stack state preservation per tab - a deliberate
simplification, noted in `PillarBottomBar`'s own kdoc, worth revisiting if the app grows past two
pillars or usage shows people expect their scroll position preserved when switching back.

---

## 4. Explicitly Not Started

Two Productivity Suite PRDs were surveyed and deliberately NOT built, for reasons worth recording
so a future session doesn't waste time re-deriving them:

- **AI Scheduler (Doc 07)** - the PRD's entire premise is AI-driven time-slotting. There's no
  honest way to scope this down without it just becoming a duplicate of Smart Reminders.
- **Calendar Intelligence (Doc 08)** - needs Android's Calendar Provider (a new runtime
  permission and real read/write integration with the system calendar), plus the "intelligence"
  half is AI-dependent the same way AI Scheduler is.

Food (the entire Health Suite, Docs 24-30) remains fully untouched, per the explicit decision in
Section 1 - not a PRD-by-PRD skip, a whole-pillar deferral.

---

## 5. Verification Status

Being honest about what "done" means here, per this project's established standard: this batch
compiled cleanly on the first attempt for the Goals/Projects/Review/Smart-Reminders half, and
after one missing-import fix for the earlier Tasks/Habits half - both confirmed via
`gradle testDebugUnitTest assembleDebug`. Installed on the dev device across the resulting schema
v6 and v7 migrations with no crash in either case, confirmed via `adb logcat` showing no
`FATAL EXCEPTION`/`AndroidRuntime` entries.

What's confirmed by genuine interactive use, not just "it compiled": the app's actual owner
started exploring the new Tasks screen live on the device mid-session (unprompted) - opened the
"New task" dialog and began typing a real task title, with the priority selector and due-date
quick-picks visibly rendering and responding. That's real signal the core Task Management flow
works, independent of anything I drove myself.

What's NOT yet independently verified: Habits' streak display after an actual day boundary
passes, Goals/Projects/Review screens beyond a static render check, and the Smart Reminders
notification actually firing for a real overdue task (its logic mirrors the already-verified
Bills/Budget notification checks exactly, but hasn't been observed firing live). Per the explicit
instruction for this session - build first, do a full pass of testing together afterward - this
is recorded as the honest starting point for that pass, not claimed as already confirmed.

One recurring, non-app-related complication worth noting for whoever debugs a "why didn't my tap
register" report later: this device is the owner's actual daily-use phone, not a dedicated test
device, and several verification attempts this week were interrupted by real concurrent use
(WhatsApp, LinkedIn, a floating YouTube video overlay intercepting taps meant for the app's own
bottom nav bar). Not a bug; a real constraint on how much can be automated against a shared
personal device.

---

## 6. PRD Tally

25 PRDs now touched across two pillars (19 from Finance + Daily Experience/Account-Access after
Day 3, +6 new from Home today), out of 47 in the Phase 3 corpus. Health Suite (7 PRDs) and two
Intelligence Layer / Life Utility groups remain fully untouched; AI Scheduler and Calendar
Intelligence are named, surveyed, and deliberately deferred (Section 4) rather than silently
skipped.
