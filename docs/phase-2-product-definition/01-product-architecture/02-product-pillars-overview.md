# Document 02: Product Pillars Overview

## Document Name
Product Pillars Overview

## Purpose
Define the Productivity, Finance, and Health pillars as product surfaces: what capabilities live inside each, how each pillar presents itself to the user, and the high-level relationship model between the three. This document establishes the boundaries of each pillar so downstream feature-specific documents have a stable frame of reference.

## Why It Exists

Without a shared pillar definition, feature teams risk duplicating capabilities (e.g., both Productivity and Health teams independently designing a "reminder" pattern) or leaving gaps at the seams (e.g., no one owning a budget-driven task suggestion). Because the product is meant to feel like one assistant with three areas of competence rather than three apps, ambiguity about what belongs to which pillar directly threatens the "one assistant" experience the mission promises. This document exists so every future feature can be placed unambiguously into a pillar (or flagged as genuinely cross-pillar) before it is designed.

## Approximate Page Count
8-10 pages.

## Sections

1. **Pillar Definition Criteria** — the test used to decide what belongs inside a pillar versus what is a cross-pillar concern, applied consistently across all three.
2. **Productivity Pillar Surface** — what capabilities the user experiences under Productivity (smart scheduling, dynamic reminders, goal planning, daily prioritization, adaptive rescheduling, context-aware notifications) and how they present as a coherent surface.
3. **Finance Pillar Surface** — what capabilities the user experiences under Finance (automatic expense capture, SMS parsing, UPI transaction tracking, location-aware expense prompts, spend prediction, budget intelligence, subscription tracking) and how they present as a coherent surface.
4. **Health Pillar Surface** — what capabilities the user experiences under Health (nutrition tracking, photo-based food logging, voice logging, water tracking, protein goals, habit tracking, sleep insights) and how they present as a coherent surface.
5. **Cross-Pillar Relationship Model (High-Level)** — the general pattern by which pillars are allowed to reference or influence one another (e.g., a Finance signal informing a Productivity suggestion), without detailing specific UX moments.
6. **Shared Vocabulary Across Pillars** — terms and concepts (e.g., "goal," "reminder," "prediction") that must mean the same thing regardless of which pillar uses them.
7. **Pillar Ownership Boundaries** — how responsibility is assigned when a feature idea could plausibly sit in more than one pillar, and the escalation path for resolving ambiguity.
8. **Explicit Non-Scope: Cross-Pillar UX Choreography** — states plainly that detailed cross-pillar experience moments (e.g., exactly how a Finance alert visually interrupts a Health flow) are owned by the Cross-Pillar Coordination Experience Document and are not defined here.

## Deliverables

* Approved Product Pillars Overview document.
* A one-page "pillar surface" reference chart listing every capability under its owning pillar.
* A shared glossary of cross-pillar terms, contributed to the master product glossary.

## Dependencies

Requires the Product Architecture Overview (Document 01) for the five-component frame, and the Product (Behavioral) Philosophy Document (Phase 1) for the behavioral loop each pillar surface must express.

## Which Teams Use This

Product, Design, Engineering (Productivity/Finance/Health feature teams), Data Science/ML, Content/Copy.

## Completion Criteria

- [ ] Every listed capability (all 18 across the three pillars) is assigned to exactly one owning pillar with no duplication.
- [ ] Pillar definition criteria have been applied as a test against at least three ambiguous/edge-case feature ideas to confirm they resolve cleanly.
- [ ] Shared vocabulary terms are consistent with terms already used in Phase 1 documents (no silent redefinition).
- [ ] Confirmed this document does not attempt to resolve cross-pillar UX sequencing (that remains reserved for the Cross-Pillar Coordination Experience Document).
- [ ] Signed off by: Head of Product (required), one Lead from each of the three pillar feature teams (required).
