# Document 3: Problem Statement Document

## Purpose

State, with evidence rather than assertion, exactly what is broken about how people currently manage their productivity, finances, and health — and why fixing all three together (not separately) is the actual opportunity. This document is what justifies the company's existence to a skeptical engineer, investor, or journalist.

## Why It Exists

"Current productivity apps are reactive" (from `requirements.md`) is a claim, not yet an argument. Without evidence and specificity, it invites the obvious rebuttal: "then why hasn't an existing app fixed this?" This document exists to pressure-test the premise before a single line of code is written, and to give every future document (Market Definition, Personas, PRDs) a shared, specific villain to design against.

## Approximate Page Count

6–9 pages.

## Sections

1. **The Reactive Trap** — formal definition of "reactive" software (waits for input, requires manual logging, forgets context between sessions) vs. "proactive" software, with named examples of reactive tools per pillar (generic categories, not necessarily naming competitors here — that's Market Definition's job).
2. **Problem Evidence Per Pillar**:
   * Productivity: cognitive load of manual planning, task/reminder abandonment rates, context-switching cost.
   * Finance: manual expense entry abandonment, the "SMS/UPI blind spot" (transactions happen but are never logged), reactive budgeting (finding out you overspent after the fact).
   * Health: photo/voice logging friction, why nutrition tracking apps have high day-7 churn, the gap between intention (log every meal) and behavior.
3. **The Fragmentation Problem** — the specific cost of managing life across 3+ disconnected apps: duplicated context, no shared memory, no single place that "knows" the user holistically (e.g., a financial stressor affecting sleep, or a schedule change affecting meal timing) — this is the case for *why one system*, not three.
4. **The Manual-Entry Tax** — quantified (even if estimated) cost in minutes/week the average user spends on data entry across these three domains, and the compounding effect of that friction on abandonment.
5. **Why Existing Solutions Don't Solve This** — structural reasons (not just "they're missing a feature"): single-domain focus, business models that reward engagement over reduced effort, lack of ambient data capture, no long-term memory/learning.
6. **Cost of the Problem, Left Unsolved** — what happens to the user (financial stress, missed health goals, burnout) and to the market (why this stays a recurring, growing pain as digital life gets more fragmented, not less).
7. **The Enabling Insight** — the specific belief that makes this solvable now (e.g., LLMs + passive signal capture can finally infer intent well enough to act proactively without constant correction).

## Deliverables

* Approved Problem Statement document.
* A one-page "Problem on a Page" summary (derived artifact) for pitch decks and onboarding.
* A running list of evidence sources (survey data, third-party research, informal user interviews) as an appendix/bibliography, kept live and re-cited in the Market Definition Document.

## Dependencies

Loosely depends on **Vision & Mission** (must not contradict the stated mission) but can be authored in parallel with it. Feeds directly into **Market Definition Document** and **User Personas Document**.

## Which Teams Use This

Product (problem prioritization), Research/User Research (what to validate first with real users), Fundraising/IR (narrative), Market Definition authors, Engineering leadership (understanding why architecture must support cross-domain memory, not siloed data).

## Completion Criteria

* [ ] Every claim in "Problem Evidence Per Pillar" is backed by at least one cited source or explicitly marked as a hypothesis pending user research (no unlabeled assumptions presented as fact).
* [ ] The Fragmentation Problem section includes at least one concrete cross-domain example (e.g., finance → health, schedule → finance) demonstrating why unification beats point solutions.
* [ ] Reviewed against at least 5 informal user interviews or equivalent evidence before being marked final.
* [ ] Signed off by: Head of Product (required), Head of Research/User Research (required once hired).
