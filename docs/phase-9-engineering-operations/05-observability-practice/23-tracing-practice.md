# Document 23: Tracing Practice

## Document Name
Tracing Practice

## Purpose
Define the operational discipline of using distributed tracing during day-to-day debugging and live incident response — when to reach for a trace instead of logs or metrics, how to read a trace across the proactive-action event chain, and how trace instrumentation quality is maintained — layered on top of the Tracing architecture defined in Phase 4 (Doc 34). This document specifies requirements for the eventual Tracing Practice document, not the tracing infrastructure itself.

## Why It Exists
Phase 4's Tracing architecture (Doc 34) makes it technically possible to follow a single request or event across the many services involved in an autonomous, event-driven AI action, but tracing infrastructure is only useful if engineers actually know how and when to use it; in practice, many engineers default to log-grepping even when a trace would answer the question in seconds, and instrumentation quietly rots when nobody owns keeping spans meaningful. This document exists to make tracing a habitual, trusted tool in debugging and incident response rather than an underused capability that engineering paid to build but rarely opens.

## Approximate Page Count
5-7 pages

## Sections
1. **Scope and Relationship to Phase 4 Tracing** — clarifies this document governs when and how engineers use tracing operationally; Doc 34 owns trace collection, sampling architecture, and storage.
2. **When to Reach for a Trace** — decision guidance distinguishing situations best solved by tracing (cross-service latency, causal ordering) versus logging or metrics.
3. **Trace Reading Practice** — how to interpret a trace waterfall for the AI system's characteristic event-driven, fan-out execution pattern (a single user trigger spawning multiple asynchronous downstream actions).
4. **Tracing During Live Incidents** — the expected workflow for pulling and sharing a relevant trace within an active incident channel, including how it complements dashboards and logs.
5. **Span Instrumentation Ownership** — the practice requiring each service team to own the quality and meaningfulness of the spans it emits, and to review them periodically.
6. **Sampling Practice for Debugging** — how engineers request or trigger elevated trace sampling for a specific user, session, or service when investigating a hard-to-reproduce issue, within the bounds Doc 34 allows.
7. **Trace-to-Log and Trace-to-Metric Correlation Practice** — the workflow for pivoting from a trace span to the corresponding log lines and metric dashboards during an investigation.
8. **Tracing in Postmortems** — expectation that postmortems for cross-service latency or causality incidents include annotated trace excerpts as evidence.
9. **New Engineer Tracing Onboarding** — training practice to bring new engineers to fluency with the tracing UI and the system's fan-out execution model.
10. **Tracing Practice Audit** — periodic sampling of production traces to check span naming and tagging quality, with findings routed to owning teams.

## Deliverables
- Decision guide for choosing tracing versus logging versus metrics during debugging.
- Trace-reading walkthrough for the platform's canonical fan-out execution pattern.
- Live-incident tracing workflow playbook.
- Span ownership and quality review checklist.
- Quarterly trace-instrumentation quality audit report template.

## Dependencies
Requires Tracing (Phase 4 Doc 34), Observability (Phase 4 Doc 31), Logging Practice (Phase 9 Doc 21), Monitoring Practice (Phase 9 Doc 20), Incident Management (Phase 9), Event-Driven Architecture (Phase 4).

## Teams
SRE, Platform Engineering, Service Owning Teams, On-Call Engineers, Engineering Enablement

## Completion Criteria
- [ ] Trace-reading walkthrough validated against at least one real multi-service fan-out incident.
- [ ] Live-incident tracing workflow rehearsed in at least one incident simulation.
- [ ] Span ownership checklist adopted by at least three service-owning teams.
- [ ] Quarterly instrumentation-quality audit completed with findings actioned.
- [ ] Signed off by: VP Engineering (required), Head of SRE (required).
