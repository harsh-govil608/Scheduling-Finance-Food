# Document 10: Embedding & Vector Store Strategy

## Document Name
Embedding & Vector Store Strategy

## Purpose
Define the technical requirements for generating embeddings and for storing and querying them at scale — the shared substrate underlying both unified Search (Phase 3 Search PRD, Phase 4 Search Service) and Retrieval Architecture (Document 09). This document specifies embedding model selection criteria, indexing and update strategy, versioning rules, and consent-scoped partitioning requirements a future Embedding & Vector Store Strategy document must satisfy; it does not select a final embedding model or vector database vendor.

## Why It Exists
Semantic search and retrieval-augmented generation both depend on turning memories, tasks, transactions, meals, notes, and other content into comparable vector representations, and both Search Service (Phase 4, Document 15) and Retrieval Architecture (Phase 5, Document 09) need that capability — but if each builds its own embedding pipeline and vector store independently, the platform ends up with two incompatible embedding spaces where "similar" means something different in Search than it does in an AI-generated suggestion, doubling infrastructure cost and making it impossible to reuse one system's index for the other. Because a forgotten memory's vector representation is itself sensitive data, this document also exists to give the platform one place that owns the hard guarantee that deleting a memory per the AI Memory PRD actually deletes its vector, not just filters it out of results — a guarantee that is easy to get wrong if embedding infrastructure is built ad hoc per feature team.

## Approximate Page Count
8-10 pages

## Sections
1. **Embedding Scope & Entity Coverage** — the catalog of what gets embedded (memory entries, tasks, transactions, meal/health entries, notes, journal and knowledge-vault content, Context Timeline entries) and at what granularity each is represented as a vector.
2. **Embedding Model Selection Criteria** — the evaluation framework (dimensionality, multilingual support, domain fit across Productivity/Finance/Health vocabulary, cost per embedding, generation latency, upgrade cadence) used to choose an embedding model, consistent with the Build vs. Buy Philosophy established in the AI Platform Overview (Phase 5, Document 01), without naming a final vendor.
3. **Embedding Generation Pipeline** — when and how embeddings are generated (on-write, batch, or on-demand), cross-referencing Event Architecture (Phase 4, Document 04) for the triggering mechanism from source-of-truth writes.
4. **Vector Store Selection Criteria** — the evaluation framework (approximate-nearest-neighbor algorithm support, scale ceiling at 100M+ users, hybrid filtering support, multi-region replication, operational maturity) extending the Databases (Phase 4, Document 20) selection-criteria pattern to vector workloads specifically.
5. **Index Update & Consistency Model** — how vector index freshness tracks source-of-truth and Memory System (Document 07) updates, and the hard requirement that a forgotten memory's vector is purged from the index, not merely excluded by a query-time filter.
6. **Embedding Versioning & Re-Embedding Strategy** — how the platform upgrades to a new embedding model generation without breaking existing indexes, covering dual-write, backfill, and cutover approaches.
7. **Multi-Tenancy & Consent-Scoped Partitioning** — how vectors are partitioned per user and per consent boundary so that retrieval can never cross an authorization boundary, cross-referencing Authorization (Phase 4, Document 08) and Multi-Tenancy (Phase 4, Document 40).
8. **Privacy-Preserving Considerations** — whether and how an embedding itself can leak sensitive information about the underlying content, and the resulting requirements for encryption at rest and access control on the vector store.
9. **Scaling & Cost Characteristics at 100M+ Users** — read/write pattern assumptions, sharding/partitioning strategy, and cost-per-embedding and cost-per-query envelopes at target scale.
10. **Non-Functional Requirements** — latency budgets for embedding generation and vector query on the interactive path, availability targets, and disaster-recovery expectations for the vector store.

## Deliverables
- Embedding scope and entity-coverage catalog with granularity per entity type
- Embedding model selection criteria matrix (not a final vendor selection)
- Vector store selection criteria matrix, extending the Databases (Phase 4, Document 20) framework
- Index update and consistency model, including the deletion-on-forget guarantee
- Embedding versioning and re-embedding runbook template
- Cost and scale projections at 100M+ user scale

## Dependencies
Requires AI Platform Overview (Phase 5, Document 01), Memory System Architecture (Phase 5, Document 07), Databases (Phase 4, Document 20), Authorization (Phase 4, Document 08), and Multi-Tenancy (Phase 4, Document 40); underlies Retrieval Architecture (Phase 5, Document 09) and Search Service (Phase 4, Document 15); must satisfy the forgetting guarantees committed to by the AI Memory PRD (Phase 3, Document 31) at the vector layer.

## Teams
AI/ML Engineering, Data Engineering, Platform/Infrastructure, Security, Privacy/Legal

## Completion Criteria
- [ ] Embedding scope confirmed to cover every entity type required by Search Service (Phase 4, Document 15) and Retrieval Architecture (Phase 5, Document 09) with no gaps.
- [ ] Deletion-on-forget guarantee reviewed jointly with the Memory System Architecture (Document 07) owner and confirmed to close the audit-of-forgetting requirement at the vector layer.
- [ ] Vector store selection criteria reviewed and confirmed achievable at 100M+ user scale by the Principal Architect.
- [ ] Consent-scoped partitioning model reviewed against Authorization (Phase 4, Document 08) and Multi-Tenancy (Phase 4, Document 40) with no boundary gaps.
- [ ] Embedding versioning/re-embedding strategy validated against at least one simulated model-upgrade scenario.
- [ ] Signed off by: Head of AI/ML (required), Principal Architect (required), Head of Privacy (required), Head of Security (required).
