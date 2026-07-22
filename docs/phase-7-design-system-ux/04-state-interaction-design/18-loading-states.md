# Document 18: Loading States

## Document Name
Loading States

## Purpose
Define the visual patterns for every loading state in the product — skeleton screens, spinners, and progressive content reveal — and specify how a state where the AI is actively generating a response, prediction, or plan is visually distinguished from a state where the app is simply waiting on a routine network request.

## Why It Exists
Loading states are among the most frequent visual moments in the product, appearing every time a dashboard, transaction feed, or meal log fetches data, and also every time the AI itself is reasoning about a suggestion, plan, or prediction — two situations that are functionally different but easy to render identically as a generic spinner. If the product cannot visually distinguish "the AI is thinking" from "the app is waiting on the network," it forfeits the chance to communicate that something more valuable than a routine fetch is happening, and it also sets the wrong patience expectation when AI generation legitimately takes longer than a data fetch. This document exists so loading feels purposeful rather than dead time, and so the product's proactive, reasoning moments are visually legible as such across all three pillars.

## Approximate Page Count
6-8 pages.

## Sections
1. **Loading State Taxonomy** — the enumerated categories of loading (initial app load, per-surface data fetch, AI reasoning/generation, background sync) that require distinct visual treatments.
2. **Skeleton Screen System** — the standard skeleton shapes, shimmer animation, and timing rules used for populated-content surfaces while their data loads.
3. **"AI Is Thinking" Visual Language** — the distinct motion, iconography, and color treatment reserved for moments the AI is actively reasoning, predicting, or generating, so it reads as cognitive work rather than a stalled network call.
4. **Progressive & Partial Content Reveal** — the requirement for how AI-generated content streams in as it becomes available (e.g., a generated plan appearing step by step) rather than appearing all at once after a blank wait.
5. **Duration Thresholds & Escalating Feedback** — the rules for when a loading state must change its messaging or visual treatment the longer it persists, distinguishing a normal short wait from an unusually long AI generation.
6. **Skeleton-to-Content Transition** — the transition animation and timing rules for the moment loaded content replaces its skeleton, avoiding jarring layout shifts.
7. **Loading States on Low-Bandwidth / Offline Transitions** — how loading visuals differ when the product suspects a connectivity issue rather than a normal fetch, coordinated with the Offline Experience document.
8. **Cross-Pillar Consistency Requirements** — the shared loading-state grammar applied identically across Productivity, Finance, and Health surfaces so a user recognizes "the AI is thinking" the same way in every pillar.

## Deliverables
* Approved Loading States document.
* A skeleton-screen component specification per major surface type.
* A distinct "AI is thinking" motion and visual spec, separate from generic loading spinners.
* A duration-threshold table defining when loading messaging escalates.

## Dependencies
Requires Motion & Animation Principles (Phase 7) for base timing and easing curves; requires Offline Experience (Phase 2, Doc 30) for connectivity-related loading behavior; requires Component System (Phase 7) for skeleton component tokens; requires Error States (Phase 7) for the visual handoff when a loading state resolves into an error.

## Teams
Design, Engineering (Frontend), Product, Data Science/ML

## Completion Criteria
- [ ] Every loading category in the taxonomy has an approved skeleton or motion treatment.
- [ ] The "AI is thinking" state is visually distinguishable from generic network loading in a side-by-side review.
- [ ] Duration thresholds have been validated against real latency data for at least one AI-generation flow per pillar.
- [ ] Skeleton-to-content transitions have been reviewed for layout-shift issues.
- [ ] Signed off by: Head of Design (required), Head of Engineering (required).
