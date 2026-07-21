# Document 26: Cross-Pillar Coordination Experience

## Document Name
Cross-Pillar Coordination Experience

## Purpose

Define concrete UX patterns for moments where two or more pillars visibly intersect in a single user interaction — for example, a schedule change shifting meal timing, which in turn triggers a budget nudge. This is the document that proves "one assistant, not three apps" in actual interaction terms rather than as an aspiration.

## Why It Exists

The Product Architecture Overview and the Product Pillars Overview both explicitly carve out detailed cross-pillar UX choreography as out of scope and name this document as its owner. Without it being written, that carve-out becomes a permanent gap: every pillar team defaults to building only within its own pillar, and the product quietly re-creates the "three apps stapled together" outcome the mission statement was written to prevent — no single document ever forces the cross-pillar seams into existence.

## Approximate Page Count

8-10 pages.

## Sections

1. **Cross-Pillar Moment Catalog** — an enumerated list of known moments where pillars intersect (a schedule change shifting meal timing and triggering a budget nudge; a health routine changing a daily task's priority) as concrete worked examples.
2. **Trigger-to-Surface Pattern** — the general interaction pattern by which a signal originating in one pillar becomes a visible nudge or suggestion in another, including the requirement that the user can always tell which pillar something came from.
3. **Sequencing & Timing Rules** — how a cross-pillar chain of effects (A triggers B triggers C) is paced so it does not arrive as a burst, tying to Never Overwhelm.
4. **Visual/Tonal Consistency Across Pillars** — how a cross-pillar surface, such as a single notification touching both Health and Finance, maintains one voice rather than reading like two features glued together.
5. **User Control Over Cross-Pillar Linkages** — how a user can view, adjust, or disable a specific cross-pillar connection (for example, "don't let schedule changes affect my budget nudges") without disabling either underlying pillar.
6. **Conflict Cases Between Pillars** — how the experience handles two pillars producing contradictory suggestions at the same time, such as a scheduling suggestion and a budget suggestion that cannot both be honored.
7. **Attribution & Explainability** — how, in any cross-pillar moment, the user can trace why something happened back to its originating trigger, reinforcing trust.
8. **Cross-Pillar Moment Intake Process** — the process by which a new cross-pillar moment identified by a pillar feature team is added to the catalog in Section 1, keeping this document a living registry rather than a one-time list.
9. **Relationship to Product Pillars Overview and Product Architecture Overview** — restates the explicit carve-out from those two documents and confirms this document is the sole owner of detailed cross-pillar UX choreography.

## Deliverables

* Approved Cross-Pillar Coordination Experience document.
* A living Cross-Pillar Moment Catalog with an initial set of worked examples across Productivity/Finance/Health pairs.
* A cross-pillar intake template for feature teams proposing new cross-pillar moments.

## Dependencies

Requires the Product Architecture Overview (Document 01, carve-out in "Where Cross-Pillar Moments Live") and the Product Pillars Overview (Document 02, carve-out in "Explicit Non-Scope: Cross-Pillar UX Choreography"). Draws on the Task Management Experience, Scheduling System Experience, Finance Experience Overview, Budget & Spend Intelligence Experience, Nutrition & Goals Experience, and Sleep & Habit Insights Experience documents as the sources of the individual pillar behaviors being coordinated, and the Product (Behavioral) Philosophy Document (Phase 1, Never Overwhelm and the Behavioral Loop).

## Which Teams Use This

Product, Design, Engineering leadership (cross-pillar), Data Science/ML, Trust & Safety, Content/Copy.

## Completion Criteria

- [ ] The Cross-Pillar Moment Catalog contains at least one worked example for every pairwise pillar combination (Productivity-Finance, Productivity-Health, Finance-Health).
- [ ] Every catalog entry includes an attribution trace confirming the user can identify the originating pillar.
- [ ] The user control mechanism is validated to allow disabling a specific cross-pillar linkage without disabling either underlying pillar.
- [ ] Sequencing and timing rules are cross-checked against the Never Overwhelm operational rules for no compounding notification bursts.
- [ ] Confirmed this document is referenced as the explicit resolution to the carve-outs in the Product Architecture Overview and the Product Pillars Overview, with no remaining cross-pillar ambiguity in either.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Trust & Safety Lead (required).
