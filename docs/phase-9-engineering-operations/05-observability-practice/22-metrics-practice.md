# Document 22: Metrics Practice

## Document Name
Metrics Practice

## Purpose
Define the operational discipline of defining, reviewing, and retiring metrics and the dashboards built on them — including who can create a new metric, how metric quality is reviewed, and when a metric or dashboard should be deprecated — layered on top of the Metrics architecture defined in Phase 4 (Doc 33). This document specifies requirements for the eventual Metrics Practice document, not the metrics pipeline itself.

## Why It Exists
Phase 4's Metrics architecture (Doc 33) defines how metrics are emitted, aggregated, and stored at scale, but without a governing practice, metric proliferation is inevitable: at 100M+ users across multi-region, event-driven services, every team's temptation to add "just one more counter" compounds into cardinality blowups, redundant dashboards, and metrics nobody trusts because nobody owns their correctness. This document exists to keep the metrics surface area meaningful and affordable — ensuring every metric in production has a clear owner, a reason to exist, and a defined lifecycle from creation to retirement.

## Approximate Page Count
6-8 pages

## Sections
1. **Scope and Relationship to Phase 4 Metrics** — clarifies this document governs metric lifecycle and dashboard practice; Doc 33 owns the metrics pipeline, aggregation engine, and cardinality architecture.
2. **Metric Proposal and Review Process** — the lightweight review a team follows before adding a new metric to production, including cardinality-impact estimation.
3. **Metric Ownership** — the requirement that every metric has a named owning team responsible for its correctness and continued relevance.
4. **Dashboard Design Practice** — practice-level guidance (not visual design standards) for building dashboards that answer specific operational questions rather than displaying data for its own sake.
5. **Metric Review Cadence** — periodic review of existing metrics and dashboards to confirm they are still queried, still accurate, and still tied to an active decision or alert.
6. **Metric Retirement Process** — the practice for deprecating and removing unused or redundant metrics, including a grace period and stakeholder notification.
7. **Business and Product Metric Alignment** — practice for keeping engineering-owned reliability metrics traceable to the product and business metrics defined in earlier phases, so on-call engineers understand user impact.
8. **Cardinality and Cost Stewardship** — team-level practice for monitoring the cost/cardinality footprint of their own metrics and responding to platform-team flags.
9. **Metrics in Postmortems and Reviews** — expectation that postmortems and quarterly reliability reviews cite specific metrics as evidence, and the practice for building ad hoc investigation dashboards.
10. **Metrics Practice Audit** — periodic audit of metric ownership records and dashboard usage analytics, with stale or ownerless metrics flagged for retirement.

## Deliverables
- Metric proposal and review template, including cardinality-impact worksheet.
- Metric ownership registry.
- Dashboard design practice guide with before/after examples.
- Metric retirement checklist and stakeholder-notification template.
- Quarterly metrics/dashboard usage audit report template.

## Dependencies
Requires Metrics (Phase 4 Doc 33), Observability (Phase 4 Doc 31), Monitoring Practice (Phase 9 Doc 20), Alerting (Phase 9 Doc 24), Product Metrics Framework (Phase 3).

## Teams
SRE, Platform Engineering, Service Owning Teams, Data/Analytics Engineering, Engineering Leadership

## Completion Criteria
- [ ] Metric proposal process piloted on at least three newly proposed metrics.
- [ ] Metric ownership registry populated for all Tier-1 service metrics.
- [ ] Retirement process exercised on at least one deprecated metric end-to-end.
- [ ] Cardinality/cost stewardship practice validated against current platform cost data.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required), Head of Data/Analytics Engineering (required).
