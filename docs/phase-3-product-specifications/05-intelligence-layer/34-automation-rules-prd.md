# Document 34: Automation Rules PRD

## Document Name
Automation Rules PRD

## Purpose
This PRD will define the user-facing feature that lets a user explicitly define, review, and approve their own automation rules — e.g., "always auto-categorize Swiggy as Food" or "always auto-log this recipe as breakfast." It defines the rule authoring surface, the rule lifecycle, and how a user-authored rule coexists with the AI's own default, learned automation behavior, without redefining that default behavior itself.

## Why It Exists
The Automation Philosophy (Phase 2) defines how the AI's own inferred automation climbs the Proactivity Ladder through demonstrated reliability, but it does not cover the separate, user-initiated case where a user wants to hand-author a rule instead of waiting for the AI to learn it — a materially different trust relationship because the user, not the AI, is asserting the pattern is true. Without this PRD, engineering has no spec for where user-defined rules live, how they interact with the AI's own automation for the same category, or what happens when the two disagree, risking a product where a user's explicit rule is silently overridden by the AI's own inference and trust collapses instantly. This PRD exists to make user-authored automation a first-class, predictable feature clearly separated from the AI's default proactive behavior.

## Approximate Page Count
8-11 pages

## Sections
1. **Feature Scope** — In scope: the rule authoring UI (trigger/condition/action definition), rule management (view, edit, pause, delete, reorder when rules conflict), and the precedence relationship between a user-authored rule and the AI's own default automation for the same category. Out of scope: the AI's own default, learned automation behavior and its Ladder progression (owned by the Automation Philosophy, Phase 2), and pillar-specific rule trigger vocabularies beyond the generic rule-building primitives (owned by each pillar's respective PRDs, e.g., Transaction Management).
2. **User Stories** — As a user, I want to create a rule that always categorizes a specific merchant the same way so I never have to correct it again; as a user, I want to see all my active automation rules in one place and understand what each one does; as a user, I want to pause a rule temporarily without deleting it, in case my situation changes; as a user, I want to know what happens if my rule and the AI's own judgment disagree on a given transaction; as a user, I want to be warned before a new rule I'm creating would silently override an existing one.
3. **Functional Requirements** — Define the rule structure (trigger condition, scope, resulting action), the rule creation flow including any AI-assisted rule suggestion (e.g., "you've corrected this three times — want to make it a rule?"), the precedence order when a user-authored rule and the AI's own inferred automation both apply to the same event, and the management operations (edit, pause, delete, duplicate, reorder for conflicting rules).
4. **Non-Functional Requirements** — Define the latency ceiling between creating a rule and it applying to the next matching event, the limit (if any) on the number of active rules per user before performance or comprehensibility degrades, and the requirement that a user-authored rule's action always remains at least as reversible/undoable as the equivalent AI-inferred action per the User Control Model.
5. **UX Requirements** — This feature must conform to the Automation Philosophy for how a rule-driven action is visually represented on-screen relative to the canonical Ladder-rung patterns (a user-authored rule executing looks distinct from, but no less transparent than, an AI-autonomous action), and to the User Control Model for how pause/undo/override apply to rule-driven actions; feature-specific UX rules must cover the rule-authoring form itself and the rule-list management view.
6. **States & Flows** — Enumerate the lifecycle a rule moves through: drafted → active → [matched and executed → logged] → [edited / paused / resumed] → deleted, including the AI-suggested-rule flow where a candidate rule is proposed to the user before ever becoming active.
7. **Edge Cases** — Cover two user-authored rules that conflict with each other, a rule whose trigger condition becomes ambiguous as new data types are introduced, a rule that was correct when created but is now stale (e.g., a merchant changed categories), and a user deleting a rule mid-execution of a matching event.
8. **Failure Scenarios** — Define behavior when the core assumption — that a rule's trigger reliably and unambiguously matches — breaks: a trigger condition that matches unintended events (over-broad rule), a rule that silently stops firing due to an upstream data change, and a conflict between a user rule and an AI-inferred automation that produces a visibly wrong action.
9. **AI Behaviors** — Detail how the AI is permitted to proactively suggest a candidate rule after observing a repeated user correction pattern (an active-suggestion-rung behavior, never auto-created), how a user-authored rule interacts with and is expected to take precedence over the AI's own inferred automation for the same trigger, and how the existence of a user rule should suppress the AI from re-learning or re-suggesting an already-covered pattern.
10. **Notification Behaviors** — Define whether a rule's first few executions generate a confirmation notification before settling into silent execution, how an AI-suggested candidate rule is presented (as a suggestion notification versus a passive in-app prompt), and how this integrates with the Notification System's arbitration so rule-related notifications don't compete unfairly with other pillar alerts.
11. **Success Criteria** — State the qualitative bar: a user should feel that a rule they wrote does exactly what they said, every time, with no surprises from the AI's own separate judgment.
12. **Metrics** — Define quantitative targets such as rule creation rate, rule-suggestion acceptance rate, rule edit/pause/delete frequency (a proxy for rules going stale or being wrong), rate of rule-vs-AI-automation conflicts detected, and reduction in manual correction rate for categories covered by a rule.
13. **Open Questions** — Capture unresolved questions such as whether rules should be shareable/exportable across users in a household context, how many active rules is too many before the feature needs a simplification pass, and whether rule precedence over AI automation should ever be user-configurable rather than fixed.

## Deliverables
- Full Automation Rules PRD document following the 13-section structure above.
- Rule lifecycle state diagram.
- Rule-vs-AI-automation precedence decision table.
- Rule authoring and management UX requirements list for Design.

## Dependencies
Phase 2: Automation Philosophy, User Control Model, Personalization, Notification System. Phase 1: Product (Behavioral) Philosophy Document, Guiding Principles Document. Phase 3: AI Memory PRD (for how repeated corrections feed rule suggestions).

## Teams Using This
Product, Engineering (AI Platform), AI/ML, Design, QA

## Completion Criteria
- [ ] Every functional requirement has at least one explicit edge case and failure scenario.
- [ ] Rule lifecycle states are fully enumerated with no unreachable or dead-end states.
- [ ] AI Behaviors section reviewed against the Automation Philosophy for consistency, confirming AI-suggested rules never auto-activate above the active-suggestion rung.
- [ ] Precedence rules between user-authored rules and AI-inferred automation validated against at least one worked conflict scenario per pillar.
- [ ] Scope boundary against the Automation Philosophy (Phase 2) confirmed with no functional overlap on default AI automation behavior.
- [ ] Signed off by: Head of Product (required), Engineering Lead (required), Head of AI/ML (required).
