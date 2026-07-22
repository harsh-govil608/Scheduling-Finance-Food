# Document 02: Coding Standards — Practice Layer

## Document Name
Coding Standards — Practice Layer

## Purpose
Define the human practice of code review culture, style enforcement, and mentorship around code quality that sits on top of Phase 4's Code Standards (Doc 53). Where Doc 53 defines the automated gates, minimum reviewer counts, and elevated-review triggers, this document defines how humans actually behave inside that system day to day — the tone of a review, how quality gets taught rather than just enforced, and how the standards themselves evolve.

## Why It Exists
Rules alone do not produce a healthy engineering culture: a team can satisfy every automated gate and required-reviewer-count in Doc 53 while still running reviews that are slow, adversarial, or purely rubber-stamped, and while treating "standards" as a weapon rather than a shared craft. This document exists because the gap between "a rule exists" and "engineers actually experience code review as fair, fast, and useful" is a culture problem, not an architecture problem, and it needs its own owner distinct from the architecture document it sits on top of.

## Approximate Page Count
6-8 pages

## Sections
1. **Scope: Practice vs. Architecture** — an explicit boundary statement against Code Standards (Phase 4, Doc 53): this document does not redefine linters, static analysis gates, or minimum reviewer counts; it defines the human behavior around them.
2. **Code Review Culture** — expected reviewer tone, the norm of assuming good intent, how to give and receive critical feedback without it reading as personal, and the blameless framing carried over from the Guiding Principles.
3. **Review Turnaround & Unblocking Norms** — target response times for a first review pass, what to do when a PR stalls, and the escalation path for getting an unresponsive review unstuck without going around the reviewer.
4. **Mentorship Through Review** — how senior engineers use code review as a teaching moment rather than a gate to clear, and the structure of a lightweight pairing/mentorship program focused specifically on code quality for engineers early in their growth.
5. **Standards Evolution Process** — the lightweight proposal-and-ratification process by which engineers can propose a change to the automated rules defined in Doc 53, so the standard evolves by consensus rather than by unilateral edit or silent drift.
6. **Handling Non-Compliance** — the coaching-first escalation ladder for code that repeatedly fails standards, distinguishing routine coaching from the rarer case where a pattern of non-compliance becomes a formal performance conversation.
7. **Readability & Ownership Norms** — practical norms like "leave code better than you found it," the boy-scout rule, and guardrails against drive-by rewrites that expand PR scope without context or reviewer buy-in.
8. **Review Depth Calibration by Risk** — guidance on how deep a review should go depending on the risk tier of the code being touched, operationalizing Doc 53's elevated-review triggers for financial, health, and location-sensitive paths with concrete examples.
9. **Recognition & Incentives** — how consistently strong review practice and quality mentorship are expected to surface in performance conversations, pointing to the People & Growth group (Phase 9) rather than duplicating its content.

## Deliverables
- Code review culture guide covering tone, response-time targets, and escalation ladder
- Mentorship/pairing program structure focused on code-quality growth
- Lightweight proposal process for evolving Doc 53's automated rules over time
- Non-compliance coaching ladder, including the threshold for escalation to a formal conversation

## Dependencies
Built directly on top of Code Standards (Phase 4, Doc 53) — must not duplicate its automated-gate or minimum-reviewer-count definitions, only the practice layer around them. Depends on the Engineering Handbook (Phase 9, Doc 01) for the RFC/decision-recording mechanism used in Standards Evolution, and on Git Workflow (Phase 9, Doc 04) for PR mechanics. Ties into the People & Growth group (Phase 9) for recognition.

## Teams
Engineering (all), Engineering Managers, Platform Engineering

## Completion Criteria
- [ ] Review culture guide cross-checked against Code Standards (Doc 53) with zero duplication of automated-gate content confirmed.
- [ ] Non-compliance escalation ladder reviewed and approved by Engineering Managers.
- [ ] Mentorship/pairing structure piloted with at least one cohort and feedback incorporated.
- [ ] Standards Evolution process used at least once to propose or ratify a change to a Doc 53 rule.
- [ ] Signed off by: VP Engineering (required), Engineering Managers (required).
