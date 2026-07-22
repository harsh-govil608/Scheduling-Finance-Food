# Document 25: AI Safety

## Document Name
AI Safety

## Purpose
Define the umbrella AI safety policy that governs what content and advice the AI is permitted to generate regardless of who is asking or how the request is phrased — the harmful-content refusal taxonomy, the safe-completion boundaries for the AI Coach's advice-giving across finance, health, and relationship domains, and the crisis-detection and escalation protocol for users in distress. This document specifies what the completed AI Safety Policy artifact must contain and how it is enforced, tested, and governed, not the finished policy prose itself.

## Why It Exists
Phase 5's AI Quality & Safety group (Documents 22-25) built the machinery that keeps the AI accurate and reliable — grounding, confidence calibration, abstention, human escalation — but accuracy is not the same property as safety: a perfectly well-grounded, high-confidence response can still be actively harmful (specific self-harm method detail, illegal financial guidance, a diagnosis the AI has no business making) or can be exactly the kind of response a distressed or vulnerable user should never receive from a system positioned as a trusted daily companion. Without a dedicated safety policy, "should the AI say this at all" is left to ad hoc judgment inside each pillar's prompt engineering, which is unacceptable for a product that proactively inserts itself into a user's finances, health, and family life and that explicitly ingests SMS, email, voice, and photo content it does not control. This document exists to make refusal and redirection boundaries explicit, testable, and consistent across every surface the AI speaks through.

## Approximate Page Count
9-11 pages

## Sections
1. **Scope & Boundary with AI Quality/Reliability (Phase 5, Group 06)** — an explicit statement that this document governs harmful-but-plausible AI behavior (content the AI should never produce regardless of accuracy), while Phase 5 Documents 22-25 govern organic error, hallucination, and explainability; the two boundaries are cross-referenced so engineering never has to guess which policy applies.
2. **Harmful Content Taxonomy** — the categorized list of content the AI must refuse to generate under any framing, including self-harm facilitation detail, violence facilitation, illegal financial instruction (e.g. money laundering, tax fraud), and content that discriminates against or targets a protected class.
3. **Safe-Completion Policy for the AI Coach** — the decision framework the AI Coach (Phase 3, Document 33) uses per sensitive domain — answer directly, answer with disclaimer, redirect to a qualified resource, or refuse — for financial advice, health guidance, mental health topics, and relationship coaching.
4. **Domain-Specific Safety Boundaries** — the mapping of the harmful content taxonomy onto each product pillar's specific risk surface: Finance (no tax evasion or money-laundering guidance, no guaranteed-return investment claims), Health (no diagnosis, no prescribing, no contraindication overrides), Productivity/Family (no guidance that facilitates surveillance or coercion of another Shared Family Mode member).
5. **Refusal & Redirection UX Contract** — how a safety refusal is communicated to the user so it preserves trust rather than reading as an arbitrary or preachy block, coordinated with the Error Recovery Experience (Phase 2, Document 35) and User Control Model (Phase 2, Document 34).
6. **Self-Harm & Crisis Detection Protocol** — the signals, across every input modality (chat, voice, SMS content the AI observes, journal/notes content), that trigger crisis handling, and the mandatory escalation path into Human-in-the-Loop Escalation Architecture (Phase 5, Document 25) and, where warranted, real-world crisis resources.
7. **Vulnerable User Protections** — additional safety constraints for minors under Shared Family Mode (Phase 3, Document 42), elderly users, and users showing signs of financial distress or health crisis, including what the AI must never do autonomously for these users regardless of Proactivity Ladder tier.
8. **Safety Evaluation & Red-Teaming Program** — the structured pre-release testing program for safety behavior specifically (adversarial prompt suites targeting the harmful content taxonomy), distinct from the general quality evals defined in the AI Evaluation & Quality Framework (Phase 5, Document 22).
9. **Policy Update & Model Change Governance** — the required process for re-validating safety policy whenever the underlying model, prompt architecture, or a pillar's capability set changes, so safety review is a gate on model upgrades rather than an afterthought.
10. **Incident Response for Safety Failures** — what happens when the AI produces harmful output despite safeguards: the reporting chain into Trust & Safety and the Security Incident Response process (Phase 6, group 04), required user-facing remediation, and root-cause capture feeding back into section 8's test suite.

## Deliverables
- Harmful content taxonomy with per-category refusal rules.
- Safe-completion decision framework for the AI Coach, one worked example per sensitive domain.
- Domain-specific safety boundary checklist for Finance, Health, and Family/Productivity pillars.
- Refusal and redirection UX contract reviewed against the User Control Model.
- Crisis detection protocol with a documented escalation handoff to Human-in-the-Loop Escalation.
- Vulnerable user protection ruleset, including Shared Family Mode minor-specific constraints.
- Safety-specific adversarial prompt test suite and red-team report template.
- Safety incident response playbook.

## Dependencies
Requires Human-in-the-Loop Escalation Architecture (Phase 5, Document 25) as the crisis escalation target and Hallucination & Error Mitigation Architecture (Phase 5, Document 23) for the accuracy/safety boundary. Implements the AI behavioral commitments in the Guiding Principles Document (Phase 1, Document 7). Scoped against the AI Coach PRD (Phase 3, Document 33) and Shared Family Mode PRD (Phase 3, Document 42). Coordinates with Monitoring, Incident & Fraud (Phase 6, group 04) for incident reporting and with AI Abuse Prevention (Phase 6, Document 26) at the boundary between harmful-content refusal and insider misuse of a legitimately authorized capability.

## Teams
Trust & Safety, AI/ML Engineering, Legal/Compliance, Health domain team, Finance domain team, Security

## Completion Criteria
- [ ] Harmful content taxonomy reviewed against every sensitive domain the AI Coach operates in (finance, health, mental health, family/relationship).
- [ ] Crisis detection protocol validated end-to-end with Human-in-the-Loop Escalation, including at least one tabletop exercise.
- [ ] Vulnerable user protections reviewed jointly with the Shared Family Mode product owner.
- [ ] Safety-specific red-team test suite executed with zero unresolved critical findings before initial launch.
- [ ] Signed off by: Head of Trust & Safety (required), Head of AI/ML (required), General Counsel (required).
