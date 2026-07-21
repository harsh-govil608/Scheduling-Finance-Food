# Document 01: Product Architecture Overview

## Document Name
Product Architecture Overview

## Purpose
Define, from a product (not engineering) perspective, how the three pillars, the context engine, the memory model, personalization, and the automation/notification layer combine into a single experienced assistant. This document is the map-of-maps for Phase 2: it establishes the vocabulary, the component boundaries, and the "where does X live" answer that every other Product Definition document will point back to.

## Why It Exists

Without a single authoritative structural map, feature teams working on Finance, Health, and Productivity will each invent their own mental model of how notifications, memory, and context relate to their pillar — and those models will silently diverge. Because the mission is "one intelligent assistant, not three apps," the cost of divergence is existential to the product's identity: a user who feels three different logics operating behind one UI will stop trusting all three. This document exists so that when a designer asks "does this suggestion come from the context engine or the memory model," or an engineer asks "which document defines the rules for this," there is exactly one place to look, and every subsequent Phase 2 document can assume this shared map instead of re-litigating it.

## Approximate Page Count
8-10 pages.

## Sections

1. **The One-Assistant Principle, Structurally Defined** — states in product terms what makes this a single system rather than three bundled apps, and the concrete structural commitments (shared memory, shared notification queue, shared context signals) that enforce it.
2. **The Five Core Components** — introduces the three pillars plus the Context Engine and the Memory Model as the five building blocks referenced throughout Phase 2, with a one-paragraph product-level definition of each.
3. **Component Interaction Map** — a diagram and accompanying description of how a single user moment (e.g., a suggestion appearing) flows across components: which component observes, which decides, which surfaces, which remembers the outcome.
4. **The Automation/Notification Layer as a Shared Surface** — explains why suggestions, reminders, and confirmations are issued through one shared layer rather than per-pillar, and what that implies for consistency of tone, timing, and priority.
5. **Personalization as a Cross-Cutting Layer** — clarifies that personalization is not a sixth component but a lens applied across all five, and previews where personalization rules are detailed.
6. **Where Cross-Pillar Moments Live** — explicitly scopes out detailed cross-pillar UX choreography, pointing to the Cross-Pillar Coordination Experience Document as the owner of that detail.
7. **Document Map for Phase 2** — a table listing every planned Phase 2 document, which of the five components it primarily concerns, and its dependency relationship to this document.
8. **Out of Scope** — explicit list of what this document does not cover (technical architecture, ML models, data schemas, infrastructure) and where that content will eventually live.

## Deliverables

* Approved Product Architecture Overview document.
* A one-page component interaction diagram (the "five-component map") reusable across all Phase 2 and later documents.
* A Phase 2 document dependency table used to sequence and validate authoring order for the remaining seven documents in this group and beyond.

## Dependencies

Requires the Vision & Mission Document and the Product (Behavioral) Philosophy Document (Phase 1) — the five-component structure must express the Behavioral Loop and Proactivity Ladder already defined there, not reinterpret them.

## Which Teams Use This

Product, Design, Engineering leadership, Data Science/ML, QA, Documentation.

## Completion Criteria

- [ ] Each of the five core components has a single, unambiguous product-level definition with no overlapping ownership.
- [ ] The component interaction map has been walked through against at least one real scenario from each pillar (Productivity, Finance, Health).
- [ ] The Phase 2 document map lists all 8 documents in this group plus placeholders for known future groups, with no orphaned or duplicate ownership.
- [ ] Reviewed against the Product Philosophy Document to confirm no contradiction with the Behavioral Loop or Proactivity Ladder.
- [ ] Signed off by: Head of Product (required), Head of Engineering (required, structural feasibility only — no implementation detail).
