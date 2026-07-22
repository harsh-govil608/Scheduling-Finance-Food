# Document 20: Databases

## Document Name
Databases

## Purpose
Define the selection criteria and decision framework each backend service uses to choose its structured/queryable data store based on access pattern — not a final vendor or engine pick. This document covers operational (transactional) data stores as distinct from the blob/media storage covered in Document 19, and establishes the multi-region data residency implications of each access pattern class.

## Why It Exists
The nine backend services have fundamentally different data access patterns — Finance needs strict transactional consistency for balances and ledger entries, Health needs high-write time-series ingestion from wearables, Search needs full-text and semantic query capability, Notification needs high-throughput low-latency key lookups — and forcing every service onto one database engine "for consistency" produces services fighting their storage layer instead of being served by it. Conversely, letting each team pick a database with no shared framework produces an unauditable sprawl of engines at 100M+ user scale, each requiring its own operational expertise, backup strategy, and on-call runbook. This document exists to give every service owner a repeatable, defensible decision process — matching access pattern to data store category — while keeping the resulting portfolio small enough for the platform team to operate responsibly.

## Approximate Page Count
9-11 pages.

## Sections
1. **Access Pattern Taxonomy** — the classes of data access the platform must support (strongly-consistent transactional, high-throughput key-value, time-series, full-text/semantic search, graph/relationship, analytical/columnar) and which services exhibit each.
2. **Selection Criteria Framework** — the decision matrix (consistency requirements, read/write ratio, query complexity, latency SLA, scale ceiling, operational maturity of the org) used to map an access pattern to a database category without prescribing a specific engine.
3. **Per-Service Access Pattern Mapping** — for each of the 9 backend services, its dominant access pattern(s) and the resulting data store category, cross-referencing Service Decomposition.
4. **Consistency & Transaction Boundaries** — where strict ACID transactions are required (e.g., Finance ledger writes) versus where eventual consistency is acceptable, and how service boundaries keep transactions from spanning multiple data stores.
5. **Polyglot Persistence Governance** — the approval process and operational bar a service must clear to introduce a new database category into the platform portfolio, preventing unchecked engine sprawl.
6. **Multi-Region Data Residency** — how each data store category behaves under multi-region deployment (single-region-with-replicas vs. multi-region-write), and the resulting constraints on where a given user's data can be written and read.
7. **Schema Ownership & Evolution** — the principle that each service owns and evolves its own schema with no cross-service direct database access, and how schema migrations are coordinated safely at scale.
8. **Backup, Point-in-Time Recovery & Disaster Recovery** — required recovery point/time objectives per data store category, especially for Finance and Health data.
9. **Scaling Strategy per Category** — read replicas, sharding/partitioning, and connection pooling approaches expected as each category approaches 100M-user scale, cross-referencing the Scalability document group.

## Deliverables
* Approved Databases document with the access pattern taxonomy and selection criteria framework.
* Per-service data store category mapping for all 9 backend services.
* Data residency decision matrix by region and data store category.
* Polyglot persistence governance/approval process.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Domain Boundaries, Storage.

## Teams
Platform/Infrastructure, Data Engineering, Engineering, Security, Privacy/Legal.

## Completion Criteria
- [ ] Every backend service has a documented access pattern and resulting data store category with rationale.
- [ ] No cross-service direct database access is permitted anywhere in the mapping.
- [ ] Consistency/transaction boundaries reviewed against at least one Finance and one Health scenario.
- [ ] Data residency implications validated for at least two regions with differing regulatory requirements.
- [ ] Signed off by: Principal Architect (required), Head of Data Engineering (required), VP Engineering (required).
