# Document 41: In-Product Help & Support Experience

## Document Name

In-Product Help & Support Experience

## Purpose

Define how a user gets unstuck inside the product itself — finding an explanation, contacting a human, or understanding why the AI did something — without that need ever being treated as a failure state to be minimized rather than designed.

## Why It Exists

A proactive AI that "remembers, predicts, and acts" will inevitably do something a user doesn't understand or disagrees with; without a designed help surface, that moment gets handled ad hoc per-team (a Finance-team tooltip here, a Health-team FAQ there), producing a support experience as fragmented as the "three apps" problem this product exists to solve. This document exists so that "why did it do that" and "I need a human" have one consistent, cross-pillar answer.

## Approximate Page Count

5–7 pages.

## Sections

1. **Help Philosophy** — the stance that asking for help is not a failure state; how this connects to the Error Recovery Experience document without duplicating it.
2. **"Why Did It Do That" Pattern** — the standard, reusable in-context explanation pattern for any automated action, tied to the Automation Philosophy document's explainability requirement.
3. **Contextual Help Surfaces** — where help is accessible from (in-flow vs. a dedicated help section) and how it's triggered without adding to notification volume.
4. **Human Support Escalation** — when and how a user reaches a human, and what information carries over from the AI context so the user never has to re-explain from scratch.
5. **Self-Serve Knowledge Surfaces** — FAQ/knowledge base scope and how it's kept distinct from in-context explanations (reference vs. moment-of-need).
6. **Support Across Pillars** — how support stays coherent when an issue spans pillars (e.g., a finance question that started from a notification triggered by a schedule change).
7. **Feedback Capture** — how users flag "this suggestion was wrong" in a way that feeds learning (behavioral hook only; ML handling is out of scope here).

## Deliverables

* Approved In-Product Help & Support Experience document.
* Standard "Why did it do that" explanation pattern, reusable across every automated feature.
* Support handoff context checklist (what must carry over to a human agent).

## Dependencies

Requires Automation Philosophy Document (explainability baseline), Error Recovery Experience Document (adjacent but distinct scope), and Notification System Document (to avoid help surfaces becoming another interruption channel).

## Which Teams Use This

Product, Design, Customer Support/Success, Trust & Safety.

## Completion Criteria

* [ ] The "why did it do that" pattern has been validated against at least one worked example per pillar.
* [ ] Boundary with Error Recovery Experience is explicit and non-duplicative.
* [ ] Signed off by: Head of Product (required), Head of Design (required), Head of Support (once hired).
