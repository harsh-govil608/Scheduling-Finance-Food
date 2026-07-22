# Document 14: Data Classification

## Document Name
Data Classification

## Purpose
Define the sensitivity tiers every piece of data in the system is classified into (e.g., public, internal, sensitive-financial, sensitive-health, AI-memory/derived) and the handling rules that follow from each tier — the single reference every other privacy and security document points to when it says "per data classification."

## Why It Exists
"Handle sensitive data carefully" is not actionable without a shared taxonomy; without this document, Finance and Health engineering teams independently decide what counts as sensitive, producing inconsistent encryption, retention, and access rules for data that should be governed identically. Because this product spans financial transactions, health photos and logs, SMS content, precise location, and AI-formed memory in a single account, a missing or ambiguous tier assignment on any one of them creates a control gap that is invisible until an incident exposes it. This document exists to be the mechanical lookup table — "this field is Tier X, therefore these controls apply" — that removes judgment calls from individual engineers and gives Security, Privacy, and Legal one place to audit against.

## Approximate Page Count
7-9 pages

## Sections
1. **Classification Tiers** — the tier definitions (e.g., Tier 0 Public, Tier 1 Internal, Tier 2 Sensitive-Personal, Tier 3 Sensitive-Regulated covering financial and health data, Tier 4 AI-Memory/Derived) and the handling implications that follow from each.
2. **Data Class Inventory** — every entity in the Phase 4 Canonical Data Model (Doc 56) mapped to a tier, maintained as a living table rather than a one-time exercise.
3. **Tier-to-Control Mapping** — the encryption, access, retention, and audit-logging requirements that attach to each tier, cross-referenced to the Phase 6 Secrets, Encryption & Session group and to Authorization (Phase 4 Doc 08).
4. **Cross-Pillar & Derived Data Classification** — the rule that a derived artifact (an AI memory entry, an embedding, a prediction) inherits at minimum the tier of its most sensitive source input, and how mixed-sensitivity aggregates are classified.
5. **Classification Ownership & Assignment Process** — who is responsible for assigning a tier to a newly introduced data field, and the required checkpoint (tied to the Privacy Review Gate, Doc 13) that prevents a field from shipping unclassified.
6. **Re-Classification Triggers** — the events that force a data class's tier to be reassessed (a new regulatory category, a new AI use of previously low-sensitivity data, a product change that combines fields into a more sensitive whole).
7. **Technical Labeling & Enforcement** — how tier metadata is represented in schemas, storage, and logging pipelines so that classification is machine-enforceable (e.g., automated redaction, access gating) rather than a documentation-only label.
8. **Classification in Third-Party Sharing** — the rule that a data class's tier travels with it when shared with a sub-processor or integration partner, and the minimum contractual/technical controls required before a given tier may leave the platform's own infrastructure.
9. **Exceptions & Mixed-Sensitivity Records** — how a single record containing fields of different tiers (e.g., a note that mentions both a task and a health detail) is classified and handled without either over- or under-protecting the whole record.

## Deliverables
- Published tier definitions with handling implications per tier.
- Full data class inventory mapping every canonical entity to a tier.
- Tier-to-control matrix (encryption, access, retention, audit) for engineering reference.
- Technical labeling/tagging specification for schemas and logging pipelines.
- Re-classification trigger checklist.

## Dependencies
Phase 4 Data Architecture & Canonical Data Model (Doc 56); Phase 4 Authorization (Doc 08); Phase 6 Security Program & Governance (Doc 01); Phase 6 Privacy Architecture (Doc 13).

## Teams
Security, Privacy/DPO, Engineering, Data Platform, Legal, Compliance

## Completion Criteria
- [ ] Every entity in the Canonical Data Model has an assigned tier.
- [ ] Tier-to-control matrix reviewed and confirmed implementable by Engineering.
- [ ] Technical labeling mechanism piloted on at least one service.
- [ ] Re-classification triggers documented and tied to the Privacy Review Gate (Doc 13).
- [ ] Signed off by: CISO (required), Head of Privacy/DPO (required), Legal (required).
