# Document 39: Gamification Philosophy

## Document Name
Gamification Philosophy

## Purpose

Establish whether, and under what exact constraints, gamification mechanics — streaks, badges, progress bars, milestones, levels — are used anywhere in the product, and reconcile that decision explicitly against the anti-dark-pattern, anti-guilt-based-engagement, anti-streak-shaming anti-goals stated in Phase 1. This document produces the single rule set every feature team must check a proposed mechanic against before shipping it.

## Why It Exists

Habit tracking, budget discipline, and goal planning are naturally magnetic to conventional gamification — streaks and badges are the industry-default answer to "how do we get people to keep doing the thing" — but the company's Phase 1 anti-goals explicitly reject guilt-based engagement loops and streak-shaming as a growth lever. The Habit Tracking Experience Document surfaces this tension directly: habit formation benefits from some sense of continuity and momentum, yet the most common mechanic for representing that (a breakable streak counter) is also the most common vector for guilt and anxiety in health and productivity apps. Without a company-wide ruling on this, individual feature teams will resolve the tension differently — one pillar shipping compassionate progress framing while another ships a punitive streak counter — producing an assistant that feels supportive in one pillar and shaming in another, directly undermining the "encourage, never overwhelm" behavioral loop from the Product (Behavioral) Philosophy Document.

## Approximate Page Count

7-9 pages.

## Sections

1. **Position on Gamification** — the company's explicit stance on the spectrum from "no gamification at all" to "full points/levels/leaderboards," stated as a ruling, not a menu of options left to each team.
2. **Permitted Mechanics Taxonomy** — the specific mechanic types allowed (e.g., non-punitive progress reflection, one-time milestone celebration, momentum framing that survives a missed day) versus the specific types explicitly banned (loss-framed streak counters, guilt-toned reminder copy, competitive leaderboards, badge scarcity pressure).
3. **The Streak Problem — Direct Resolution** — the specific reconciliation with the tension flagged in the Habit Tracking Experience Document: how the product represents consistency and momentum without using a breakable-streak mechanic that punishes a single missed day, including the reframing principle that replaces "don't break the streak" with a forgiving, resumable model of progress.
4. **The Encouragement-vs-Guilt Test** — a concrete, repeatable heuristic (a short set of test questions) that any proposed gamification mechanic must pass before a feature team is allowed to ship it, designed to be applied the same way the Guiding Principles Document's principles are applied in design review.
5. **Failure and Recovery Experience** — how the product responds, in tone and mechanic, when a user misses a day, breaks a habit, overspends a budget, or abandons a goal; states explicitly that recovery framing is never gated behind premium (cross-referenced against the Premium Experience Document).
6. **Social Comparison and Visibility Boundaries** — rules on whether any progress, streak, or achievement is ever visible to another person, defaulting to private-by-default and opt-in-only for any comparative or social feature, tied to the "no dark patterns, ever, including for retention" principle.
7. **Where Gamification Is Explicitly Banned Outright** — a pillar-by-pillar list of specific situations where no gamification mechanic of any kind is permitted (e.g., overspending in Finance, missed sleep targets in Health), because the risk of the mechanic reading as shame outweighs any motivational benefit.
8. **Governance — Reviewing New Mechanic Proposals** — the process by which a team proposing a new streak/badge/progress mechanic gets it checked against this document before build, and who has authority to reject a proposal on gamification-philosophy grounds.

## Deliverables

* Approved Gamification Philosophy document.
* The Encouragement-vs-Guilt Test as a standalone one-page checklist for design and product review.
* A pillar-by-pillar "gamification permitted / banned" reference table.

## Dependencies

Requires the Guiding Principles Document (Phase 1) for the anti-dark-pattern, no-guilt-based-retention stance this document must operationalize, and the Product (Behavioral) Philosophy Document (Phase 1) for the "encourage" and "never overwhelm" verbs a mechanic must satisfy simultaneously. Directly resolves the tension flagged in the Habit Tracking Experience Document between habit-formation momentum and streak-shaming risk. Cross-references the Premium Experience Document (Document 38) to confirm compassionate failure-recovery is never a paywalled feature.

## Which Teams Use This

Product, Design, Data Science/ML, Trust & Safety, Content/Copy, Growth.

## Completion Criteria

- [ ] The Habit Tracking Experience Document's flagged streak-vs-shaming tension is addressed with a named, specific resolution (not a general statement of good intentions).
- [ ] The Encouragement-vs-Guilt Test has been run against at least three real candidate mechanics (e.g., a streak counter, a badge system, a leaderboard) with a documented pass/fail outcome for each.
- [ ] Pillar-by-pillar banned-mechanic list covers all three pillars with at least one concrete banned example per pillar.
- [ ] Confirmed no gamification mechanic in this document is gated behind the premium tier in a way that withholds compassionate failure/recovery framing from free users.
- [ ] Governance process names a specific role with authority to reject a gamification proposal.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Trust & Safety lead (required).
