# Document 12: Performance Testing

## Document Name
Performance Testing

## Purpose
Define the recurring practice of validating actual measured latency and throughput against the per-journey budgets set in Phase 4's Performance document (Doc 37) — when performance tests run, what they measure, how a regression is detected and blocked before release, and how this practice differs from Load Testing (Doc 13), which validates behavior under volume and stress rather than budget adherence under normal conditions.

## Why It Exists
Doc 37 sets the numbers — a p95 latency ceiling for meal photo recognition, a throughput target for the notification service — but a target that is never re-measured against reality is just an aspiration. Without a recurring performance testing practice tied to CI/release, latency regresses silently release over release (a phenomenon often called "death by a thousand cuts") until a journey that once felt instantaneous quietly no longer does, undermining the core promise that the assistant is paying attention. This document exists to make performance budget adherence a continuously verified, release-blocking property rather than a one-time architectural target.

## Approximate Page Count
6-8 pages

## Sections
1. **Performance Test Scope & Relationship to Load Testing** — the explicit boundary between this document (does the system meet its latency/throughput budget under expected conditions) and Load Testing/Doc 13 (does the system survive and degrade gracefully under volume/stress), so the two practices are not conflated.
2. **Per-Journey Performance Test Suite** — how each critical user journey catalogued in Doc 37 gets a corresponding automated performance test, and how that suite is kept in sync as journeys are added or change.
3. **Performance Test Cadence** — when performance tests run (per-PR smoke checks, nightly full suite, pre-release gate), balancing signal freshness against CI runtime cost.
4. **Regression Detection & Budget Gates** — the statistical method for detecting a real regression versus normal variance, and the CI gate that blocks a release when a journey breaches its Doc 37 budget.
5. **AI-Boundary Latency Verification** — how the AI inference latency ceiling defined in Doc 37 (interface-only, per the AI platform boundary) is verified in practice without requiring visibility into AI/ML internals.
6. **Multi-Region Performance Validation** — how performance tests account for the multi-region latency considerations in Doc 37, including which regions are tested and how regional results are reconciled.
7. **Performance Test Environment Requirements** — what test environment fidelity (data volume, infrastructure parity with production) is required for a performance test result to be trustworthy, cross-referencing Doc 52's environment tiering.
8. **Regression Triage & Escalation** — the process when a budget breach is detected: who is paged, how the responsible service/team is identified, and the resolution SLA before the breach becomes release-blocking beyond a grace window.
9. **Performance Trend Reporting** — the recurring report of per-journey latency/throughput trends over time, distributed to engineering leadership, distinct from a single pass/fail gate result.

## Deliverables
- Automated per-journey performance test suite mapped 1:1 to Doc 37's critical journey catalog
- Regression-detection methodology and CI budget-gate configuration
- AI-boundary latency verification harness (interface-only, no AI internals)
- Performance regression triage runbook with paging and SLA
- Recurring performance trend report template

## Dependencies
Requires Performance (Phase 4 Doc 37) for the budgets being validated and Capacity Planning (Phase 4 Doc 38) for the scale assumptions test environments must approximate. Requires Testing Strategy (Phase 4 Doc 52) and Testing Strategy Process Layer (Doc 10) for how performance tests fit into the broader pyramid and CI process. Distinct from, but coordinates with, Load Testing (Doc 13) on shared test infrastructure.

## Teams
Platform Engineering, SRE, QA/Test Engineering, Backend Service Teams, AI Platform Team (boundary interface only)

## Completion Criteria
- [ ] Every critical journey in Doc 37's catalog has a corresponding automated performance test.
- [ ] Regression-detection method validated against at least one historical real regression to confirm it would have caught it.
- [ ] AI-boundary latency verification confirmed testable without requiring AI/ML internal access.
- [ ] Regression triage runbook includes a named paging target and resolution SLA.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required), Principal Architect (required).
