# Document 25: Human-in-the-Loop Escalation Architecture

## Document Name
Human-in-the-Loop Escalation Architecture

## Purpose
Define the technical system for detecting when an AI decision is low-confidence, high-stakes, or otherwise unsuitable for autonomous action, and for routing that decision to an appropriate human — the user themselves, or a human support agent — instead of the AI guessing. This document implements the Phase 1 Guiding Principles Document's "say I don't know" commitment as an operational escalation pipeline with defined triggers, routing, and handoff contracts; it does not define the on-screen escalation experience itself, which belongs to the relevant Phase 2/3 product documents.

## Why It Exists
The Proactivity Ladder promises users graduated autonomy earned over time, but graduated autonomy is meaningless unless the system also has a reliable way to step back down the ladder — or off it entirely — the moment confidence drops or stakes rise. Without a defined escalation architecture, engineers under pressure to keep the product feeling "proactive" will be tempted to let a low-confidence output ship anyway rather than build the harder path of detecting uncertainty and routing it to a human, and the first time that produces a bad financial or health outcome it validates the exact fear — "bots don't understand nuance and shouldn't be trusted with real decisions" — that the mission exists to overcome.

## Approximate Page Count
9-11 pages

## Sections
1. **Escalation Trigger Taxonomy** — the categories of condition that trigger escalation (confidence below threshold, explicit model abstention, high-stakes action class regardless of confidence, detected internal inconsistency, user-requested human), sourced from Hallucination & Error Mitigation Architecture (Document 23).
2. **Escalation Target Routing** — how the system decides whether an escalation returns to the user (a clarifying question, "I'm not sure, can you confirm?"), goes to a human support agent, or blocks the action entirely pending review.
3. **Confidence Threshold Governance** — how per-pillar, per-action-class confidence thresholds are set and reviewed, and who owns changing them, tying to the Consequence-Weighted Verification Tiers in Document 23.
4. **Escalation UX Contract** — the technical handoff contract (payload, required context, urgency signal) the AI/ML layer passes to the product or support surface, without dictating the final on-screen treatment owned by product-level documents.
5. **Human Agent Handoff & Context Package** — the context bundled for a human support agent (decision inputs, explanation per Document 24, relevant memory, prior escalations) so the agent isn't starting cold, plus the required response SLA.
6. **Escalation State Management & Timeout Behavior** — the safe default state maintained while awaiting a human response, timeout fallback behavior, and re-engagement so an escalated decision never silently stalls a user-facing flow.
7. **Escalation Volume & Cost Governance** — how the system avoids over-escalation, which would defeat the proactive promise and overload support capacity, and under-escalation, which would erode trust, including target escalation-rate bands per pillar.
8. **Escalation Outcome Feedback Loop** — how a human's resolution of an escalated case feeds back into the AI Evaluation & Quality Framework (Document 22) and Learning Systems (Phase 5, group 04) so escalation rates for well-handled case types decrease over time.
9. **High-Stakes Hard-Stop Category** — the defined category of action (irreversible financial transfers, guidance crossing into medical advice) that always escalates regardless of confidence, and the process for governing and expanding this category.
10. **Auditability of Escalation Decisions** — logging requirements so every escalation, and every non-escalation of a borderline case, can be reconstructed for support investigation, regulatory inquiry, or postmortem.

## Deliverables
- Escalation trigger taxonomy with a worked example per pillar
- Escalation target routing logic (user vs. support agent vs. hard block)
- Confidence threshold governance process and ownership model
- Escalation UX contract/payload specification
- Human agent context-package specification and response SLA targets
- Escalation state management and timeout fallback specification
- Escalation volume target bands per pillar and monitoring plan
- High-stakes hard-stop action category list and governance process
- Escalation audit log specification

## Dependencies
Requires Hallucination & Error Mitigation Architecture (Phase 5, Document 23) as the primary trigger source; requires AI Explainability Architecture (Phase 5, Document 24) for the explanation included in human handoff packages; requires AI Evaluation & Quality Framework (Phase 5, Document 22) for the outcome feedback loop. Implements the "say I don't know" principle from the Guiding Principles Document (Phase 1, Document 7) and the Proactivity Ladder from the Product Philosophy Document (Phase 1, Document 2); coordinates with Error Recovery Experience (Phase 2, Document 35) and In-Product Help & Support Experience (Phase 2, Document 41) for the user-facing and support-agent-facing surfaces this architecture feeds.

## Teams
AI/ML Engineering, Customer Support/Help Operations, Product, Trust & Safety, Data Science, Finance domain team, Health domain team

## Completion Criteria
- [ ] Escalation trigger taxonomy validated against at least one worked example per pillar (Productivity, Finance, Health).
- [ ] High-stakes hard-stop action category reviewed and approved by Finance and Health domain leads.
- [ ] Human agent context package piloted with Customer Support to confirm agents aren't starting cold.
- [ ] Escalation volume target bands reviewed against Customer Support capacity planning.
- [ ] Signed off by: Head of AI/ML (required), Head of Customer Support (required), Head of Trust & Safety (required).
