# Document 7: Guiding Principles Document

## Purpose

Capture the small set of non-negotiable rules — design, engineering, ethical, and organizational — that resolve ambiguity when no spec, PRD, or manager is available to make the call. This is the synthesis document of Phase 1: everything above (vision, philosophy, problem, market, personas, metrics) compresses into principles a new engineer can internalize in one sitting.

## Why It Exists

At "tens of millions of users" scale, thousands of small autonomous decisions get made daily by engineers, designers, and (eventually) the AI system itself, without a founder in the room. Without written principles, those decisions default to local incentives (ship fast, avoid hard conversations, follow the path of least resistance) rather than the mission. This document exists to be the thing quoted in code review comments, design critiques, and incident postmortems: "this violates Principle #4."

## Approximate Page Count

5–7 pages.

## Sections

1. **Principle List** (10–15 principles max — a longer list stops being memorable), each with: the principle statement, a one-line rationale, and one concrete example of it being applied. Candidate categories to cover:
   * **Product principles** — e.g., "Default to the least-surprising action," "Every automated action must be explainable in one sentence," "Reduce, never relocate, manual effort."
   * **Data & privacy principles** — e.g., "The user owns their data; the company is a steward, not an owner," "No dark patterns, ever, including for retention."
   * **AI behavior principles** — e.g., "The AI must be able to say 'I don't know' rather than guess silently," "Autonomy is earned per-user, not granted by default" (ties to the Proactivity Ladder).
   * **Engineering principles** — e.g., "Cross-pillar data must be architected as shared from day one, not integrated later," "Build for auditability of every automated decision."
   * **Organizational principles** — e.g., "Write it down," "Optimism about the mission, paranoia about the metrics."
2. **Principle Hierarchy / Conflict Resolution** — what happens when two principles conflict (e.g., proactivity vs. never-overwhelm); a stated tie-breaking order.
3. **How Principles Are Enforced** — mechanism, not just aspiration (e.g., referenced explicitly in code review templates, design review checklists, incident postmortem templates).
4. **Principle Change Process** — these are meant to be stable for years, so changing one requires a higher bar than changing a feature spec; state who can amend this document and under what circumstances.
5. **Quick-Reference Card** — single-page, all principles condensed to one line each, meant to be pinned/printed/onboarded on day one.

## Deliverables

* Approved Guiding Principles document.
* Quick-Reference Card (derived artifact, single page) distributed in onboarding and printed/pinned in engineering spaces.
* Principle references embedded into code review and design review templates (cross-team implementation task, tracked outside this doc).

## Dependencies

Synthesizes all prior Phase 1 documents: **Vision & Mission**, **Product Philosophy**, **Problem Statement**, **Market Definition**, **User Personas**, and **Success Metrics**. Must be written last.

## Which Teams Use This

Every team, always — this is the one Phase 1 document every new hire reads in week one regardless of function. Particularly load-bearing for Engineering (code review), Design (design review), Trust & Safety (policy grounding), and Executive leadership (culture and hiring bar).

## Completion Criteria

* [ ] No more than 15 principles listed; each is a single, falsifiable statement (someone can point to a decision and say "this violated Principle #X"), not a vague value like "we care about users."
* [ ] Every principle traces back to at least one of the other 6 Phase 1 documents (no principle invented from nowhere).
* [ ] Conflict-resolution hierarchy tested against at least 2 real hypothetical tradeoffs from the Product Philosophy document (e.g., proactivity vs. never-overwhelm).
* [ ] Signed off by: CEO/Founder(s) (required), Head of Product (required), Head of Engineering (required once hired), Head of Design (required once hired).
