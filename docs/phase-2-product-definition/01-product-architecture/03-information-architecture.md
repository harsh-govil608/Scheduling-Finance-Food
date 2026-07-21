# Document 03: Information Architecture

## Document Name
Information Architecture

## Purpose
Define how all content, data, and features are organized and labeled across the product from a user-experience standpoint: the taxonomy of user-facing entities, the core entity model at a UX level, and how information is grouped and surfaced. This document is the single source of truth for "what things exist in this product and what we call them," independent of navigation or visual design.

## Why It Exists

Without one agreed information architecture, the same underlying concept (e.g., a task versus a reminder versus a goal) will be named and structured differently by different feature teams, producing an app that feels inconsistent even when each individual feature is well designed. Because this product spans three pillars unified by shared memory and shared automation, entities like "reminder" or "memory" must behave and be labeled identically everywhere they appear — a reminder created from a Finance insight must look and feel like a reminder created from a Health insight. This document exists to prevent that drift before any screen is designed.

## Approximate Page Count
8-10 pages.

## Sections

1. **Core Entity Model** — the canonical list of user-facing entities (tasks, transactions, meals, goals, reminders, memories, habits, notifications) with a plain-language definition of each at the UX level.
2. **Entity Relationships** — how entities relate to one another as the user would understand them (e.g., a goal can generate tasks, a transaction can generate a budget alert), expressed without any database or schema language.
3. **Taxonomy and Categorization** — the classification system used to group and tag entities (e.g., categories for transactions, tags for tasks, meal types) and the rules for how categorization stays consistent across pillars.
4. **Naming Conventions** — the rules for what user-facing terms are permitted, banned, or reserved, ensuring one concept never has two names across pillars.
5. **Grouping and Surfacing Logic** — the general rules for how entities are grouped for display (e.g., by date, by pillar, by priority) independent of any specific screen's navigation.
6. **Metadata Visible to Users** — what attributes of an entity the user can see and rely on (e.g., "last updated," "source: SMS parsing," "confidence: predicted") versus what stays invisible.
7. **Cross-Entity Search and Retrieval Expectations** — how a user should expect to find any entity regardless of which pillar created it (e.g., searching "rent" surfaces the transaction, the budget line, and any related reminder).
8. **Governance: Adding New Entities** — the process and criteria for introducing a new entity type in the future without breaking the existing taxonomy.

## Deliverables

* Approved Information Architecture document.
* A canonical entity glossary (feeding the master product glossary) covering every named entity type.
* An entity relationship diagram at the UX level.
* A naming conventions reference for Content/Copy and Design teams.

## Dependencies

Requires the Product Architecture Overview (Document 01) and Product Pillars Overview (Document 02) for the component and pillar boundaries that entities must respect.

## Which Teams Use This

Product, Design, Content/Copy, Engineering, QA, Data Science/ML.

## Completion Criteria

- [ ] Every entity named in the product context (tasks, transactions, meals, goals, reminders, memories, habits, notifications) has one canonical definition with no synonyms in circulation.
- [ ] Entity relationships have been diagrammed and validated against at least one real cross-pillar example (e.g., a Finance-triggered reminder).
- [ ] Naming conventions have been checked against all Phase 1 documents for consistency.
- [ ] The "adding a new entity" governance process has been test-run against one hypothetical new entity type.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Content (required).
