# Phase 1: Company Foundation & Vision — Overview

## Purpose

Establish the immutable foundation every later phase, team, and hire will be measured against: why this company exists, what it believes, who it serves, what problem it solves, how big that problem is, and how success is defined. Phase 1 contains no product specs and no architecture — it is the constitution the product is built on top of.

## Why This Phase Exists First

Every subsequent document (PRDs, architecture, GTM plans, hiring plans) implicitly answers "does this serve the mission?" Without Phase 1 written down, that question gets answered inconsistently by whoever is in the room. Writing it down first means:

* Engineering doesn't design systems that optimize for the wrong thing (e.g., engagement over user wellbeing).
* Product doesn't build reactive features in a company whose stated bet is proactivity.
* Hiring, fundraising, and press narrative stay consistent from day one.
* Later disagreements ("should we build X?") get resolved by re-reading these documents, not by re-litigating first principles.

## The 7 Documents in This Phase

1. **Vision & Mission Document** — the 10-year destination and the one-sentence reason the company exists.
2. **Product Philosophy Document** — the behavioral contract the AI makes with the user (remember, predict, suggest, remind, learn, adapt, encourage, never overwhelm).
3. **Problem Statement Document** — why current productivity/finance/health apps fail, evidenced and specific.
4. **Market Definition Document** — TAM/SAM/SOM, category definition, competitive landscape.
5. **User Personas Document** — who we build for, in enough behavioral detail that a PM can make tradeoffs without asking "what would the user want?"
6. **Success Metrics Document** — the North Star metric and the KPI tree beneath it, including guardrail/anti-metrics.
7. **Guiding Principles Document** — the non-negotiable design, engineering, and ethical rules that resolve ambiguity when no spec covers a situation.

## Sequencing & Dependencies

```
Vision & Mission ──┬──> Product Philosophy ──┐
                    │                          ├──> Success Metrics ──> Guiding Principles
Problem Statement ──┴──> Market Definition ──> User Personas ──┘
```

* **Vision & Mission** has no upstream dependency — it is the root document.
* **Problem Statement** is written in parallel with Vision & Mission; both feed **Market Definition**.
* **User Personas** depends on Problem Statement + Market Definition (can't define personas without knowing the problem and the market segment).
* **Product Philosophy** depends on Vision & Mission (philosophy is how the mission behaves day-to-day).
* **Success Metrics** depends on all four above — you cannot define what to measure until you know who you serve and what "good" means for them.
* **Guiding Principles** is written last because it is the synthesis: the rules that fall out of everything above.

## Deliverables of This Phase (as a whole)

* 7 approved markdown/PDF documents (this folder), version-controlled.
* One internal all-hands presentation deck summarizing all 7 (derived artifact, not a new document).
* Sign-off log: named approvers per document (see Completion Criteria in each doc).

## Which Teams Use This Phase's Output

Founders/CEO, Product leadership, Engineering leadership, Design leadership, Fundraising/Investor Relations, People/Hiring (for job descriptions and culture docs), and every future new hire during onboarding.

## Completion Criteria for Phase 1 (Gate to Phase 2)

Phase 1 is "done" — and Phase 2 (Market & Product Strategy) may begin — only when:

* [ ] All 7 documents exist, are internally consistent (no contradicting claims about mission, users, or metrics), and are checked into version control.
* [ ] Each document has named sign-off per its own Completion Criteria section.
* [ ] The North Star metric in the Success Metrics Document is traceable to a specific persona pain point in the User Personas Document and a specific claim in the Problem Statement Document.
* [ ] A read-through by someone outside the founding team (e.g., first engineering hire or advisor) can correctly answer, unprompted: "what does this company do, for whom, why now, and how will we know it's working?"
