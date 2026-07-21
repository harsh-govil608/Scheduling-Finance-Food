# Document 18: Calendar Experience

## Document Name
Calendar Experience

## Purpose

Define the calendar as a visual surface — how a user views, browses, and manually edits their schedule — independent of the automated scheduling intelligence that decides what goes on it. This document specifies what the user sees and can directly manipulate, not why or when the AI proposed or moved anything.

## Why It Exists

The calendar is likely the single most-viewed screen in the product, and it is the place where a user must be able to see, at a glance, what the AI has done on their behalf. If this document does not exist separately from the Scheduling System Experience Document, designers will be left to infer visual and manual-editing behavior from a document about decision logic, and the two concerns will blur — producing a calendar that cannot clearly distinguish "what the AI decided" from "what the view displays," which makes the AI's actions feel opaque rather than transparent.

## Approximate Page Count

6-8 pages.

## Sections

1. **Calendar View Modes** — the set of ways a user can view their schedule (day, week, month, agenda/list) and what each is optimized for.
2. **Visual Language for AI-Originated vs. User-Originated Items** — how the calendar visually distinguishes something the AI proposed or moved from something the user placed directly, so origin is always legible at a glance.
3. **Manual Editing Interactions** — drag, resize, delete, duplicate, and the full set of direct-manipulation actions available, along with the feedback each produces.
4. **Cross-Pillar Item Representation** — how items originating from Productivity, Health, and Finance are represented consistently on one shared calendar surface, as a visual matter only.
5. **Density & Overwhelm Management** — how the calendar avoids visually overwhelming a busy day (collapsing, grouping, "show more" patterns), tying to Never Overwhelm.
6. **Read States & Change Highlighting** — how the calendar shows a user what changed since they last looked (a rescheduled item, a newly AI-added item) so nothing appears to move invisibly.
7. **Navigation & Time Orientation** — how a user moves across time, including the "today" anchor and conventions for browsing past versus future.
8. **Explicit Non-Scope: Scheduling Intelligence** — states plainly that why or when the AI proposes or moves an item is owned entirely by the Scheduling System Experience Document; this document owns only how it is displayed and how a user can directly manipulate it.

## Deliverables

* Approved Calendar Experience document.
* A visual-language reference sheet (origin markers, change highlighting, density rules) for Design to build from.
* A manual-editing interaction inventory listing every direct-manipulation action and its resulting system feedback.

## Dependencies

Requires the Scheduling System Experience Document (explicit boundary — the calendar must faithfully display what that document defines, not reinterpret it), the Product Pillars Overview (cross-pillar item sourcing), and the Product (Behavioral) Philosophy Document (Phase 1, Never Overwhelm operationalization).

## Which Teams Use This

Design, Product, Engineering (Productivity feature team), QA.

## Completion Criteria

- [ ] Every calendar view mode has defined behavior for a day with fifteen or more items (an overwhelm stress-test).
- [ ] The AI-originated vs. user-originated visual distinction has been validated for legibility with at least one non-designer reviewer.
- [ ] The boundary with the Scheduling System Experience Document has been reviewed jointly; no proposal or rescheduling logic is defined here.
- [ ] Change-highlighting behavior has been confirmed to surface every automated action the Scheduling System Experience Document allows to happen without prior confirmation.
- [ ] Signed off by: Head of Design (required), Head of Product (required).
