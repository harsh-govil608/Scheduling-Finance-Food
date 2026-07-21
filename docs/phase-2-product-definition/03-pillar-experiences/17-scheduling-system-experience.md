# Document 17: Scheduling System Experience

## Document Name
Scheduling System Experience

## Purpose

Define the intelligent scheduling behavior of the product — how the AI proposes time slots, negotiates conflicts, and automatically reschedules commitments — as distinct from how the schedule is visually displayed and manually edited. This document specifies the automated/intelligent side of time management; it does not define the calendar surface itself.

## Why It Exists

Adaptive rescheduling is one of the Productivity pillar's headline capabilities, and it is also the capability most capable of feeling invasive if built without explicit rules: an AI that silently moves a user's commitments around is exercising exactly the kind of irreversible, unconsented action the Phase 1 philosophy forbids below the appropriate trust level. Without a document that maps specific rescheduling behaviors to specific rungs of the Proactivity Ladder, engineering teams will default to whatever is technically convenient, and the product risks feeling like it is rearranging a user's life behind their back rather than assisting them.

## Approximate Page Count

7-9 pages.

## Sections

1. **Scheduling Proposal Lifecycle** — how the AI proposes a time slot for a task or commitment, what signals it is allowed to draw on at a product level, and how the proposal is presented to the user.
2. **Negotiation & Conflict Resolution UX** — what happens when a proposed time conflicts with an existing commitment, and how trade-offs are surfaced to the user rather than resolved unilaterally.
3. **Automatic Rescheduling Rules by Proactivity Level** — maps rescheduling behaviors (silent hold, suggest, pre-fill awaiting confirmation, auto-move with notice) to specific rungs of the Proactivity Ladder.
4. **Recurring Commitment Handling** — how recurring items (habits, routines, recurring tasks) are treated differently from one-off scheduling requests.
5. **User Override & Correction Flow** — how a user rejects, edits, or permanently overrides a scheduling suggestion, and how that correction is expected to influence future proposals.
6. **Time-Sensitivity & Urgency Signaling** — how urgency or a closing window is communicated without becoming a source of alarm fatigue, in keeping with Never Overwhelm.
7. **Cross-Commitment Awareness** — how scheduling behavior accounts, at a behavioral level, for commitments sourced from other pillars (a health routine, a finance-related deadline) without detailing the full cross-pillar choreography.
8. **Explicit Non-Scope: Calendar Visualization** — states plainly that how the schedule is displayed, browsed, and manually edited is owned entirely by the Calendar Experience Document.

## Deliverables

* Approved Scheduling System Experience document.
* A decision table mapping each Proactivity Ladder rung to its permitted scheduling action and required user confirmation.
* A conflict-resolution flow reference usable by any pillar team whose feature generates time commitments.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1, Proactivity Ladder — must be reused, not reinterpreted), the Product Architecture Overview (context engine, automation layer), and the Task Management Experience Document (the task entity being scheduled). Maintains an explicit boundary with the Calendar Experience Document.

## Which Teams Use This

Product, Design, Engineering (Productivity feature team, scheduling/context engineering), Data Science/ML, Trust & Safety.

## Completion Criteria

- [ ] Every rescheduling behavior is mapped to exactly one Proactivity Ladder rung with no ambiguous default.
- [ ] At least one worked conflict-resolution scenario has been validated per commitment source (Productivity, Health-originated, Finance-originated).
- [ ] Confirmed that no irreversible reschedule can occur below the trust level required by the Phase 1 philosophy.
- [ ] The boundary with the Calendar Experience Document has been reviewed jointly; no visual or manual-editing UX is defined here.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Trust & Safety Lead (required).
