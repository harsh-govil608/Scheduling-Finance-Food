# Document 37: Performance

## Document Name
Performance

## Purpose
Define the latency and throughput targets the platform must meet for its critical user journeys — expense capture confirmation, meal photo recognition round-trip, morning dashboard load, proactive notification delivery, calendar reschedule, and similar moments where the assistant's responsiveness is felt directly by the user. This document establishes the budget methodology (how a journey's end-to-end target is decomposed across client, gateway, service, and AI-boundary hops) and the testing and enforcement mechanisms that keep the platform inside those budgets as it grows toward 100M+ users.

## Why It Exists
An AI that "proactively manages a user's life" only earns trust if it feels instantaneous at the moments that matter — a slow expense-capture confirmation or a meal photo that takes ten seconds to recognize reads to the user as the assistant not paying attention, which is the opposite of the mission. Without one authoritative performance document, each of the 9 backend services and each Phase 3 PRD would set its own informal latency expectations, some services would over-invest in speed nobody notices while others silently degrade the journeys users care about most, and there would be no shared basis for saying a release regressed performance versus simply shifted it. This document exists so "fast enough" is a specific, testable number tied to a named user journey rather than a vague engineering aspiration, and so that number survives contact with 100M+ users and multi-region deployment.

## Approximate Page Count
8-10 pages.

## Sections
1. **Latency Budget Philosophy** — the methodology for setting end-to-end p50/p95/p99 targets per journey and decomposing them into per-hop budgets (client, gateway, service, event bus, AI-platform boundary, data store).
2. **Critical User Journey Catalog** — the enumerated list of journeys that receive an explicit performance budget (e.g., expense capture confirmation, meal photo recognition round-trip, morning dashboard load, proactive notification delivery, calendar reschedule), each traced to its owning Phase 3 PRD.
3. **AI Inference Latency Budget (Interface Only)** — the portion of a journey's budget allocated to the call across the AI platform boundary defined in Document 01, specified as a contract latency ceiling only; AI/ML internals that meet that ceiling are Phase 5 scope.
4. **Client-Perceived vs. Backend Latency** — the distinction between raw backend response time and perceived performance (optimistic UI, skeleton states, prefetching), and which techniques are a client concern versus a genuine backend budget.
5. **Throughput & Concurrency Targets** — peak request-per-second and event-per-second targets per service at 100M+ user scale, distinguishing sustained load from burst load.
6. **Multi-Region Latency Considerations** — how regional routing and data locality affect achievable latency budgets, cross-referencing the Infrastructure & Observability document group for deployment topology.
7. **Performance Testing & Load Testing Requirements** — the required load-test scenarios (sustained, burst, soak) and the vendor-neutral criteria a load testing approach must satisfy before a service ships at scale.
8. **Performance Regression Prevention** — the requirement for automated, per-release performance budget checks and the escalation path when a budget is breached.
9. **Degraded-Mode Performance** — the performance bar the system must still meet when operating in a degraded state, cross-referencing the Phase 2 Error Recovery Experience for the user-facing behavior a slow or failing dependency should trigger.
10. **Performance Ownership & Escalation** — who owns a journey's budget, how a budget dispute between two services sharing a journey is resolved, and the escalation path for a sustained SLA breach.

## Deliverables
* Approved Performance document with a named latency/throughput budget for every catalogued critical journey.
* Per-hop latency budget waterfall template usable by any service team.
* Load testing scenario catalog (sustained, burst, soak) with pass/fail criteria.
* Performance regression-gate policy for CI/release pipelines.

## Dependencies
Requires Overall System Architecture, Service Decomposition, API Architecture, Event Architecture (Phase 4); informed by the Infrastructure & Observability document group for deployment topology and by Capacity Planning for the growth assumptions the throughput targets must hold under. Traces to Phase 3 PRDs that define the specific journeys (e.g., Expense Capture, Meal Recognition, Morning Dashboard); the degraded-mode section is governed by the Phase 2 Error Recovery Experience.

## Teams
Engineering, Platform/Infrastructure, SRE, AI/ML, QA, Product.

## Completion Criteria
- [ ] Every catalogued critical journey has an explicit p50/p95/p99 end-to-end target traced to its owning Phase 3 PRD.
- [ ] Every journey's end-to-end budget is decomposed into per-hop budgets with no unallocated "slack."
- [ ] AI-boundary latency budget is defined as an interface contract only, with no AI/ML internal implementation detail present.
- [ ] Load testing scenarios cover sustained, burst, and soak conditions for at least the three highest-traffic services.
- [ ] Degraded-mode performance bar is reconciled with the Phase 2 Error Recovery Experience with no contradictory user-facing behavior.
- [ ] Signed off by: VP Engineering (required), Principal Architect (required), Head of SRE (required).
