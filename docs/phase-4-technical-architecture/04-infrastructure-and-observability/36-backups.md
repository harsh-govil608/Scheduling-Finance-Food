# Document 36: Backups

## Document Name
Backups

## Purpose
Define the backup strategy per data class — backup frequency, mechanism, point-in-time recovery granularity, and restore validation — required to reliably recover a specific point-in-time copy of data. This document is distinct from Disaster Recovery, which governs regional failover and service continuity rather than the mechanics of restoring a particular data copy.

## Why It Exists
Because the product stores irreplaceable personal data spanning financial transactions, health records, and years of cumulative AI memory, "restore this user's data to how it was an hour ago" must be achievable independent of any regional disaster — recovering from a bad migration, an accidental deletion, corruption introduced by a bug, or a user-initiated correction all require reliable backups even when no region has failed and no disaster has been declared. At 100M+ users, backups also become one of the platform's largest aggregations of sensitive data in one place, so this document treats backup storage itself as a security-critical asset, not merely an operational safety net. It exists so that recoverability is guaranteed at the granularity each data class actually needs, and so that backups are periodically proven restorable rather than assumed to be.

## Approximate Page Count
6-8 pages.

## Sections
1. **Backup Strategy per Data Class** — the differentiated approach for operational transactional stores, AI memory stores, analytical stores, and media/object storage, with ephemeral caches explicitly excluded from scope.
2. **Backup Frequency & Point-in-Time Recovery Granularity** — required recovery granularity per data class, with continuous or near-continuous point-in-time recovery for Finance and AI memory data versus periodic snapshots for lower-tier data.
3. **Backup Storage & Redundancy** — geographic redundancy of backup storage independent of the primary data region, and immutability/write-once requirements protecting against ransomware or malicious deletion.
4. **Restore Procedures & Granularity** — full-store restore versus single-record or single-user restore capability, required to support user-initiated correction or deletion-related recovery requests.
5. **Backup Validation & Restore Testing** — mandatory periodic restore drills proving backups are actually restorable, not merely captured on schedule.
6. **Encryption & Access Control for Backups** — encryption-at-rest requirements for backup data and access restrictions reflecting that backups aggregate the platform's most sensitive data in one place.
7. **Retention Windows & Deletion Compliance** — how backup retention interacts with user deletion and right-to-be-forgotten requests, ensuring deleted user data does not persist indefinitely in backup copies.
8. **Relationship to Disaster Recovery** — an explicit boundary statement cross-referencing Disaster Recovery, clarifying that backups are a building block of recovery but not a substitute for failover architecture.

## Deliverables
* Approved backup strategy matrix per data class.
* Restore SLA defined per recovery granularity tier (full-store vs. single-user).
* Backup validation and restore-drill schedule.
* Backup retention policy reconciled with deletion/right-to-be-forgotten compliance requirements.

## Dependencies
Requires Overall System Architecture, Service Decomposition. Coordinates with Data Architecture for per-store backup mechanics. Feeds, and is consumed by, Disaster Recovery.

## Teams
Data Engineering, Platform/Infrastructure, SRE, Security, Legal/Compliance.

## Completion Criteria
- [ ] Backup strategy and point-in-time recovery granularity defined for every data class, including AI memory.
- [ ] At least one restore drill executed successfully at single-user granularity.
- [ ] Backup storage encryption and access control reviewed and approved by Security.
- [ ] Backup retention policy reconciled with user deletion and right-to-be-forgotten requirements.
- [ ] Signed off by: Head of Data Engineering (required), Head of SRE (required), DPO/Compliance Lead (required).
