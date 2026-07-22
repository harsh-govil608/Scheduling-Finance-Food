# Document 10: Referral

## Document Name
Referral

## Purpose
Define the design reasoning behind the referral program — why users would refer, what incentive structures are being considered and why, and how each candidate structure is checked against the anti-dark-pattern principles from Phase 1 — without committing to final incentive amounts, reward tiers, or payout figures.

## Why It Exists
Referral is the most direct and most easily abused of the growth loops named in the Growth Loops Document: done well, it turns genuine user satisfaction into new users at a low blended CAC; done poorly, it turns into spam-inviting contacts, pay-to-recruit schemes, or guilt-tripped "your friend is waiting" nags that violate the company's explicit "no dark patterns, ever, including for retention" principle. Because this product handles sensitive Finance and Health data, a referral mechanic also has to be designed so the incentive to refer never creates pressure to over-share personal outcomes (e.g., a friend's weight-loss result) as a side effect of claiming a reward. This document exists so the referral program is designed deliberately against these specific failure modes before any incentive structure is built, not patched after a growth hack goes wrong.

## Approximate Page Count
7-9 pages

## Sections
1. **Referral Loop Position** — where the referral loop sits within the Growth Loops Document's taxonomy, and its expected relative contribution compared to other named loops at each company stage.
2. **Referral Motivation Model** — the reasons a genuinely satisfied user would refer someone (time saved, a specific outcome achieved, a moment of delight) versus reasons the program must not rely on (reward chasing, social pressure, gamified point-scoring), grounded in the value creation thesis from the Business Model Document.
3. **Incentive Structure Candidates** — the shapes of incentive being evaluated (e.g., give-get credit, one-sided referrer reward, milestone-based unlocks, non-monetary recognition), with the reasoning for and against each — explicitly not final dollar amounts or reward tiers.
4. **Two-Sided Value Reasoning** — how the incentive design keeps value proportionate for both referrer and referee so the mechanic reads as "we want to reward you for something good" rather than "we're paying you to recruit," including guardrails against structures that would incentivize spam-inviting low-intent contacts.
5. **Eligibility, Timing & Friction Design** — when in the user's journey a referral ask can appropriately surface (e.g., only after a moment of demonstrated value, never during onboarding before trust is established), and what friction is deliberately kept in the flow to prevent low-quality mass-inviting.
6. **Anti-Dark-Pattern Review** — an explicit, itemized check of the referral design against the Phase 1 Guiding Principles and the Gamification Philosophy Document's Encouragement-vs-Guilt Test, naming specific tactics ruled out (artificial urgency on reward expiry designed to manipulate rather than inform, guilt-toned "you're letting your friend down" copy, leaderboards of top referrers, dark-pattern contact-list scraping).
7. **Privacy & Cross-Pillar Sensitivity Boundaries** — rules preventing the referral mechanic from ever surfacing or implying another person's Finance or Health data as part of the referral flow, cross-referenced against Phase 6's privacy commitments.
8. **Fraud & Abuse Resistance Reasoning** — the categories of referral fraud the program must be resistant to (self-referral, fake accounts, incentive farming) at a structural-reasoning level, feeding detailed rules into Phase 6's fraud/monitoring documents.
9. **Referral Funnel Instrumentation** — the funnel stages (invite sent → invite accepted → referee activated → referee retained) that must be measured, and how referee quality (not just volume) is tracked so the program cannot be gamed by volume-only metrics.
10. **Program Evolution & Sunset Conditions** — the reasoning for how the referral program is expected to change as the company scales, and the conditions under which an incentive structure would be reduced or retired rather than escalated indefinitely.

## Deliverables
- A referral motivation model distinguishing genuine-value referrals from reward-chasing referrals.
- A shortlist of candidate incentive structures with pros/cons reasoning (no final figures).
- A written anti-dark-pattern review of the referral design, itemized against Phase 1 principles.
- A referee-quality funnel instrumentation plan.
- A fraud/abuse risk list handed off to Phase 6 for detailed control design.

## Dependencies
Requires the Guiding Principles Document (Phase 1), the Growth Loops Document (Phase 8, Document 09), the Business Model Document (Phase 8, Document 01), the Gamification Philosophy Document (Phase 2, Document 39), and the Security/Privacy/Trust framework (Phase 6) for fraud and data-sensitivity boundaries. Feeds fraud-control requirements into Phase 6's Monitoring, Incident & Fraud Document.

## Teams
Growth, Product, Design, Data/Analytics, Trust & Safety, Legal

## Completion Criteria
- [ ] Every candidate incentive structure is evaluated for its potential to encourage spam-inviting or reward-chasing behavior, with a stated mitigation for each.
- [ ] Anti-dark-pattern review section completed as an itemized checklist, not a general statement of intent.
- [ ] Cross-pillar privacy boundary confirmed: no referral flow surfaces another person's Finance or Health data.
- [ ] Referee-quality metrics (activation, retention) are specified alongside referral volume, so the program cannot be optimized on volume alone.
- [ ] Fraud/abuse risk list reviewed by Trust & Safety and handed off to the Phase 6 monitoring/fraud document.
- [ ] Signed off by: Head of Growth (required once hired), Head of Product (required), Trust & Safety lead (required).
