# Document 39: Rate Limiting

## Document Name
Rate Limiting

## Purpose
Define rate-limiting requirements across the API surface — per-user, per-endpoint, and per-integration-partner limits — protecting the platform from abuse and runaway automated behavior without degrading legitimate proactive AI actions. This document specifies where limits are enforced, how they scale with account tier, and how a limit being hit is surfaced back to the user, without prescribing a specific rate-limiting product or algorithm implementation.

## Why It Exists
A platform whose AI is designed to act proactively on a user's behalf creates a rate-limiting problem ordinary consumer apps don't have: the system itself, not just external attackers or misbehaving clients, is a potential source of runaway request volume, since a bug in proactive logic could fire the same action repeatedly across 100M+ accounts before a human notices. Without explicit, infrastructure-level rate limits tied back to the Phase 1 "Never Overwhelm" principle, that principle remains a UX aspiration with no technical backstop, and the platform has no defense against either abusive external traffic or its own AI acting outside its intended bounds. This document exists so rate limiting is a designed, tiered, observable control — not an incident-response afterthought bolted onto the gateway after the first outage it could have prevented.

## Approximate Page Count
6-8 pages.

## Sections
1. **Rate Limit Tiers** — per-user default limits versus elevated limits for premium tiers, cross-referencing the Phase 3 Premium Features PRD for which tier a given account is on.
2. **AI-Action Rate Limits** — specific limits on autonomous/proactive AI actions per user per day, tying back to the Phase 1 "Never Overwhelm" principle at the infrastructure enforcement level, distinct from and in addition to any behavioral pacing the AI/ML layer applies internally (Phase 5).
3. **Integration Partner Limits** — limits for third-party integrations (banking, calendar providers, SMS/notification providers) that protect both the platform and the partner relationship from excess call volume.
4. **Limit Enforcement Point** — gateway-level versus service-level enforcement, and the criteria for choosing each, cross-referencing API Architecture's internal/external API boundary.
5. **Graceful Degradation** — the user-facing behavior when a limit is hit, ensuring it reads as a considerate pause rather than a broken feature, cross-referencing the Phase 2 Error Recovery Experience.
6. **Abuse Detection Escalation** — the threshold and process for when repeated rate-limit violations escalate from automatic throttling to a security review.
7. **Rate Limit Observability** — the requirement that every enforced limit emits metrics and alerts distinguishing legitimate-tier exhaustion from suspected abuse, feeding the Infrastructure & Observability document group.
8. **Limit Configuration & Change Management** — how limits are defined, reviewed, and changed without a full deployment, and the approval bar for raising a limit that gates a proactive AI action.

## Deliverables
* Approved Rate Limiting document with the tiered limit structure per endpoint category and account tier.
* AI-action rate limit table explicitly reconciled against the Proactivity Ladder's autonomy levels.
* Enforcement-point decision matrix (gateway vs. service level).
* Abuse-escalation runbook trigger criteria.

## Dependencies
Requires API Architecture, Overall System Architecture, Service Decomposition (Phase 4); enforcement of AI-action limits is informed by Phase 1 Product Philosophy's "Never Overwhelm" principle and the Proactivity Ladder; graceful-degradation behavior is governed by the Phase 2 Error Recovery Experience; tier-based limits are informed by the Phase 3 Premium Features PRD.

## Teams
Engineering, Platform/Infrastructure, Security, SRE, AI/ML.

## Completion Criteria
- [ ] AI-action rate limits explicitly reconcile with the Proactivity Ladder's autonomy levels, with no autonomy level left unbounded.
- [ ] Every limit tier is mapped to a Premium Features PRD account tier with no ambiguous tier assignment.
- [ ] Graceful-degradation behavior for a hit limit is validated against the Phase 2 Error Recovery Experience with no contradictory messaging.
- [ ] Abuse-escalation thresholds and the security review handoff process are explicitly defined.
- [ ] Signed off by: CTO/VP Engineering (required), Head of Security (required).
