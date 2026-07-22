# Document 24: Scheduling

## Document Name
Scheduling

## Purpose
Define the infrastructure-level cron/scheduled-task system that triggers time-based work platform-wide — the substrate underlying product features like Daily Planning generation and reminder firing. This document is strictly about the infrastructure primitive that fires work at a given time or interval; the business logic of what the AI Scheduler decides to plan is product/AI decisioning logic defined elsewhere and explicitly out of scope here.

## Why It Exists
Nearly every proactive behavior this product promises has a time dimension — a daily plan must be ready before the user wakes, a reminder must fire at the right moment, a weekly review must compile on schedule — and all of that depends on a scheduling primitive that is accurate, durable, and correct at 100M+ users spread across time zones and regions. Without one shared scheduling infrastructure, services build their own timers or polling loops, which do not survive restarts reliably, do not scale to per-user schedules at this volume, and cannot be reasoned about consistently for missed-fire recovery. This document exists to define that shared, dependable time-triggering substrate so every service that needs "run this at time X" or "run this every interval Y" builds on one correct implementation instead of many fragile ones.

## Approximate Page Count
6-8 pages.

## Sections
1. **Scope Boundary** — explicit statement that this document covers the infrastructure primitive (fire work at a time/interval, reliably, at scale) and not the business logic of what gets scheduled, which lives in the owning service or the AI Scheduler PRD.
2. **Scheduling Model Requirements** — support required for both platform-level fixed schedules (e.g., nightly batch triggers) and per-user, per-timezone dynamic schedules (e.g., a user's personal wake time driving their daily plan).
3. **Timezone & Regional Correctness** — requirements for correct time-triggering across a global, multi-region user base, including daylight saving transitions and users who travel or relocate.
4. **Durability & Missed-Fire Recovery** — guarantees required so a scheduled trigger is not silently lost on worker restart, deploy, or regional failover, and how missed fires are detected and recovered.
5. **Scale Requirements** — the requirement to support per-user schedules (e.g., individualized reminder times) at 100M+ users without a single scheduler becoming a bottleneck, implying a sharded/distributed scheduling design.
6. **Relationship to Background Jobs & Message Queues** — how a fired schedule hands off to the Background Jobs framework (Document 23) for execution, and how that handoff is transported via Message Queues (Document 22).
7. **Idempotent Triggering & Distributed Lock Interaction** — how the scheduler avoids double-firing the same scheduled trigger across redundant scheduler instances, cross-referencing Distributed Locks (Document 25).
8. **Observability & Drift Detection** — required visibility into trigger latency (scheduled time vs. actual fire time) and alerting on scheduling drift.

## Deliverables
* Approved Scheduling document defining the infrastructure-level cron/trigger system requirements.
* Timezone and regional correctness requirements validated against at least one cross-timezone scenario.
* Missed-fire recovery policy.
* Explicit scope boundary separating this document from the AI Scheduler PRD's business logic.

## Dependencies
Requires Overall System Architecture, Service Decomposition, Message Queues, Background Jobs, Distributed Locks.

## Teams
Platform/Infrastructure, Engineering, SRE.

## Completion Criteria
- [ ] Scope boundary explicitly distinguishes this document from the AI Scheduler PRD with no overlapping content.
- [ ] Per-user, per-timezone scheduling requirement validated against at least one daylight-saving-transition scenario.
- [ ] Missed-fire recovery behavior defined for at least one regional failover scenario.
- [ ] Double-firing prevention requirement cross-checked against Distributed Locks.
- [ ] Signed off by: Principal Architect (required), VP Engineering (required), Head of SRE (required).
