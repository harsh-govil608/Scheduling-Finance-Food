# Document 13: Community

## Document Name
Community

## Purpose
Define whether, where, and how a user community is built around a product whose core content is deeply personal and often sensitive (financial balances, health conditions, life circumstances) — establishing the topics, formats, and moderation model for peer-to-peer connection (tips, encouragement, shared workflows) without ever requiring or implying that a user must expose the private life data the AI itself was built to keep private. This document sets the scope boundary between "community" and "the product," and the hard rule that community participation is optional and never a precondition for core value.

## Why It Exists
Most consumer products build community because it drives retention and word-of-mouth cheaply — but this product's mission depends on users trusting it with the most sensitive data they have, and a careless community feature (a public leaderboard, a shared feed, an auto-suggested post) is one of the fastest ways to leak or imply private financial or health information and break that trust irreversibly. At the same time, users managing debt, illness, or major life transitions often want to talk to peers going through the same thing, and a total absence of community leaves that need to unmoderated, unaffiliated spaces (Reddit, Facebook groups) where the company has no ability to keep the conversation safe or on-brand. This document exists to make the community decision deliberate — with an explicit reconciliation against the Phase 6 privacy commitments — rather than something a growth team bolts on unilaterally to chase an engagement metric the Phase 1 Success Metrics Document already warns against optimizing for.

## Approximate Page Count
6-8 pages

## Sections
1. **Community Thesis & Non-Goals** — why a community may be warranted (peer encouragement, shared tips, belonging in a hard moment) stated alongside explicit non-goals (no social feed of personal data, no gamified public comparison, no requirement to participate to get value from the AI).
2. **Topic & Format Boundaries** — what a community conversation is allowed to be about (general strategies, product tips, emotional support in generic terms) versus what it must never surface (specific balances, diagnoses, transaction detail, anything the AI holds in the user's private memory).
3. **Privacy Reconciliation** — the explicit, documented walkthrough of how every proposed community surface was checked against the Phase 6 Privacy Architecture and Consent Framework, including a standing rule that no user data auto-populates into a community-facing surface without a distinct, separately scoped consent.
4. **Anonymity & Identity Model** — whether community identity is pseudonymous, semi-anonymous, or real-name, and why, given that a real-name model paired with sensitive topics (debt, illness) materially raises the stakes of any leak.
5. **Moderation Model** — human moderation staffing, moderation guidelines specific to financial/health topics (e.g., no medical or financial advice presented as fact, escalation path for users expressing crisis-level distress), and the tooling required to enforce them.
6. **Community Formats & Channels** — candidate formats (in-app forum, curated tip library, cohort-based groups, none-of-the-above) evaluated against the privacy and moderation constraints above, with a recommendation and phased rollout stance.
7. **Relationship to Support & Customer Success** — how community is explicitly kept distinct from the reactive Support function (Doc 15) and the proactive Customer Success function (Doc 14), including the rule that community is never used as a substitute for either.
8. **Crisis & Sensitive-Disclosure Protocol** — what happens when a user discloses something in a community context that indicates financial or health crisis (e.g., self-harm risk, imminent eviction), and how that hands off to a human without becoming a surveillance mechanism over ordinary community use.
9. **Go/No-Go Decision Framework** — the criteria under which the company decides to build, defer, or explicitly not build a community feature, including minimum trust/safety readiness bars that must be met first.
10. **Success Measurement** — how community health is measured if built (qualitative trust signals and opt-in participation, not raw engagement volume), consistent with the Phase 1 Success Metrics Document's guardrails against vanity engagement metrics.

## Deliverables
- A Community Thesis document with explicit non-goals and a stated go/no-go recommendation.
- A Privacy Reconciliation checklist mapping every proposed community surface to the relevant Phase 6 privacy/consent controls.
- Moderation guidelines specific to financial- and health-adjacent peer content, including a crisis-disclosure escalation protocol.
- A phased rollout plan (or explicit deferral rationale) for community formats.

## Dependencies
Requires Guiding Principles Document (Phase 1), Success Metrics Document (Phase 1), Privacy Architecture (Phase 6, Doc 13), Consent Framework (Phase 6, Doc 12), Data Classification (Phase 6, Doc 14), Abuse Prevention (Phase 6, Doc 23). Coordinates with Customer Success (Phase 8, Doc 14) and Support (Phase 8, Doc 15).

## Teams
Product, Trust & Safety, Legal/Privacy, Customer Success, Marketing, Design

## Completion Criteria
- [ ] Every proposed community surface has a documented privacy reconciliation with no open objections from Legal/Privacy.
- [ ] Moderation guidelines reviewed and approved by Trust & Safety, including the crisis-disclosure escalation protocol.
- [ ] Go/no-go recommendation made explicit, with rationale, rather than left implicit.
- [ ] Boundary with Customer Success and Support functions confirmed non-overlapping.
- [ ] Signed off by: Head of Product (required), Head of Trust & Safety (required), General Counsel/Privacy Lead (required), CEO (required for go decision).
