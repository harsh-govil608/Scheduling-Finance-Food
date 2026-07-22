# Document 45: Calendar Providers

## Document Name
Calendar Providers

## Purpose
Define the architecture for two-way synchronization between the platform's internal scheduling data and external calendar providers (Google/Outlook/Apple-class), including conflict resolution when external and internal calendar state diverge. This document specifies the sync protocol, data model mapping, and reconciliation logic required for the Productivity pillar's smart scheduling to operate against a user's real, externally-managed calendar.

## Why It Exists
Smart scheduling cannot proactively manage a user's time in isolation — it must read and write against the calendar(s) the user actually uses day to day, which live on external providers outside the platform's control. This creates a distributed-state problem: the same event can be created, edited, or deleted independently on either side, and the architecture must define, deterministically, how conflicts are detected and resolved without silently dropping user data or creating duplicate events. This document exists to specify that reconciliation model up front, because getting it wrong erodes user trust in the scheduling feature faster than almost any other failure mode in the product.

## Approximate Page Count
7-9 pages

## Sections
1. **Provider Account Linking** — OAuth-based connection architecture for linking one or more external calendar accounts per user, including scope minimization requirements.
2. **Sync Protocol & Change Detection** — how the architecture detects changes on the provider side (webhook/push notification channels vs. incremental sync tokens vs. polling) and on the internal side.
3. **Data Model Mapping** — how internal event/task representations map to and from each provider's calendar event schema, including handling of fields with no direct equivalent.
4. **Conflict Detection & Resolution** — the rules architecture for identifying divergent state (same event edited on both sides, deleted on one side and modified on the other) and the resolution strategy (last-write-wins, merge, user-prompted).
5. **Multi-Calendar & Multi-Account Handling** — architecture for users who link multiple calendar accounts or maintain multiple calendars within a single account, including how the "primary" scheduling surface is determined.
6. **Write-Back Safety** — safeguards preventing the platform's proactive scheduling actions from silently overwriting user-authored external calendar content.
7. **Sync Failure & Recovery** — behavior when provider APIs are unavailable, rate-limited, or return partial results, and how sync state is reconciled on recovery.
8. **Revocation & Unlink Handling** — architecture for what happens to internally-derived scheduling data when a user disconnects a calendar account.

## Deliverables
- Sync architecture diagram covering push/webhook and polling paths per provider.
- Data model mapping table (internal schema ↔ each provider's event schema).
- Conflict resolution decision matrix covering all divergence scenarios (edit/edit, edit/delete, delete/edit, create/create duplicate).
- Multi-account/multi-calendar handling specification.
- Failure and recovery state diagram for sync interruptions.

## Dependencies
Requires Event Architecture, the Productivity/Scheduling Service, and Notification Service (Phase 4 Doc 14) for conflict alerts; informed by Phase 1 Trust & Data Stewardship, Phase 2 Permissions & Consent UX, and the Phase 3 Travel PRD.

## Teams
Backend Engineering (Productivity Service), Mobile Engineering, Data Platform, Product (Productivity pillar), QA

## Completion Criteria
- [ ] Conflict resolution matrix reviewed and validated against real-world edit/edit and edit/delete scenarios.
- [ ] Data model mapping reviewed for completeness against each targeted provider's API.
- [ ] Write-back safety mechanism reviewed by Product to confirm no silent overwrite of user-authored events.
- [ ] Sync failure/recovery behavior validated by QA under simulated provider outages.
- [ ] Signed off by: CTO/VP Engineering (required), Head of Product (required).
