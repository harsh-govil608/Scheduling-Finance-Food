# Document 56: Data Architecture & Canonical Data Model

## Document Name
Data Architecture & Canonical Data Model

## Purpose
Define the cross-service canonical entity model — the company-wide, authoritative representation of core entities like "a task," "a transaction," and "a meal" — along with the data dictionary and ownership map that establish which service is the system of record for each entity and how every other service must reference it. This is distinct from any individual service's own internal data model, which may store additional service-local fields.

## Why It Exists
In a microservices architecture spanning Productivity, Finance, and Health pillars, multiple services inevitably need to read or reference the same real-world concept — a "task" is touched by the scheduling service, the notification service, and the AI suggestion engine; a "transaction" is touched by the banking integration, the budgeting service, and the AI spending-insight engine. Without a canonical model and clear ownership, each service invents its own shape for these entities, and the platform ends up with silently divergent definitions of the same thing — a class of bug that is subtle, hard to detect, and was explicitly flagged in self-review as missing from the original document scope (each service's Data Model section covers only that service's internal schema, not the cross-service picture).

## Approximate Page Count
8-11 pages

## Sections
1. **Canonical Entity Catalog** — the authoritative list of cross-service entities (task, transaction, meal, health metric, goal, notification, etc.) with their canonical field definitions.
2. **System-of-Record Ownership Map** — for each canonical entity, which service is authoritative, and which services hold read-only or derived copies.
3. **Entity Relationship Model** — how canonical entities relate to one another across pillars (e.g., how a Finance transaction can relate to a Productivity task like "pay rent").
4. **Data Dictionary Governance** — the process for proposing, reviewing, and approving changes to a canonical entity definition, and how that process prevents silent divergence.
5. **Local Extension Policy** — the rule for how a service may extend a canonical entity with service-local fields without corrupting the shared definition for other consumers.
6. **Data Consistency & Propagation Model** — how updates to a canonical entity propagate to services holding derived copies (event-driven sync, read-through, etc.), and the acceptable staleness window per entity type.
7. **Cross-Pillar Identity Resolution** — how the same real-world concept is kept identifiable across Productivity, Finance, and Health boundaries without forcing premature coupling between pillar services.
8. **Canonical Model Versioning** — how the shared entity definitions themselves are versioned as the product evolves, coordinated with Versioning (Doc 51).
9. **Data Lineage & Discoverability** — how engineers and the AI platform can discover "where does this entity actually come from" across the service graph.

## Deliverables
- Canonical entity catalog with field-level definitions for all cross-service entities
- System-of-record ownership map
- Entity relationship diagram spanning Productivity, Finance, and Health pillars
- Data dictionary governance process specification
- Consistency/propagation model with staleness targets per entity

## Dependencies
Requires each pillar's Backend Service Data Model sections (Phase 4 Docs in 02-services-and-clients / 03-platform-and-data); requires Event Architecture for propagation mechanics; informs AI Platform Integration Boundary (Doc 57), since the AI platform consumes and reasons over these canonical entities; works with API Contracts (Doc 50) to enforce canonical shapes at service boundaries.

## Teams
Platform Engineering, Data Architecture, Backend Service Teams (Productivity, Finance, Health), AI Platform Team (consumer of canonical entities)

## Completion Criteria
- [ ] Canonical entity catalog covers at least one core entity from each of Productivity, Finance, and Health pillars.
- [ ] System-of-record ownership map reviewed and confirmed with each owning service team.
- [ ] Local extension policy validated against at least one real case of a service needing extra fields.
- [ ] Consistency/propagation model reviewed against Event Architecture's actual delivery guarantees.
- [ ] Signed off by: VP Engineering (required), Head of Data Architecture (required), AI Platform Lead (required as primary cross-cutting consumer).
