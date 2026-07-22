# Document 34: Tracing

## Document Name
Tracing

## Purpose
Define distributed tracing requirements — trace context propagation, span architecture, and sampling strategy — needed to reconstruct a single user-facing or AI-triggered action as it flows across the event-driven, multi-service, multi-region architecture. This document focuses specifically on tracing across asynchronous event hops, which is materially harder than tracing a synchronous request/response chain.

## Why It Exists
A proactive AI action can traverse many asynchronous event hops across several of the 9 backend services and the AI platform boundary before producing a single user-visible effect — a reminder, a payment nudge, a rescheduled appointment. Without end-to-end tracing that survives the event bus, diagnosing "why did the AI take this action" or "why was this notification five minutes late" becomes guesswork across dozens of services and log stores. This document exists so that any engineer, at 100M+ user scale and across regions, can pull a single trace ID and see the complete path an action took, including through the async event-driven layers where trace context is easiest to lose.

## Approximate Page Count
6-8 pages.

## Sections
1. **Trace Context Propagation Standard** — the required trace/span ID format and propagation rules across both synchronous calls and asynchronous event messages.
2. **Span Architecture & Naming Conventions** — what constitutes a span per service and the required span attributes and naming standard for cross-team consistency.
3. **Event-Driven Trace Continuity** — specific requirements for maintaining trace context across the event bus, including fan-out and fan-in scenarios where one trigger produces many downstream events.
4. **Sampling Strategy at Scale** — head-based vs. tail-based sampling trade-offs, required sampling rates by criticality tier, and guaranteed full sampling of AI-initiated proactive actions and error paths.
5. **Cross-Service and Cross-Region Trace Assembly** — how traces spanning multiple regions are collected and assembled into one coherent view.
6. **Correlation with Logs & Metrics** — the requirement that trace IDs are embedded in logs and that metric exemplars link back to representative traces.
7. **AI-Boundary Trace Handoff** — how a trace crossing into the Phase 5 AI platform is represented at the boundary, without tracing AI internals.
8. **Trace Data Retention & Access** — retention windows for trace data and access controls, given traces may contain sensitive request context.

## Deliverables
* Approved trace context propagation standard covering synchronous and asynchronous paths.
* Span naming and attribute convention document.
* Sampling policy with guaranteed full capture of AI-initiated actions and errors.
* Cross-region trace assembly architecture.

## Dependencies
Requires Observability, Overall System Architecture, Service Decomposition, Event Architecture. Coordinates with Logging and Metrics for cross-signal correlation via shared identifiers.

## Teams
SRE, Platform/Infrastructure, Engineering, AI/ML (boundary handoff only).

## Completion Criteria
- [ ] Trace context propagation validated end-to-end across at least one full asynchronous, cross-service event flow.
- [ ] Sampling policy guarantees 100% capture of AI-initiated proactive actions and all error paths.
- [ ] Cross-region trace assembly demonstrated for a scenario spanning at least two regions.
- [ ] Correlation between traces, logs, and metrics validated for a representative incident scenario.
- [ ] Signed off by: Head of SRE (required), Principal Architect (required).
