# Document 06: Memory Model — Behavioral Perspective

## Document Name
Memory Model — Behavioral Perspective

## Purpose
Define what the AI is understood to "remember" from the user's point of view: the distinction between short-term and long-term memory as experienced, what surfaces to the user as "the AI remembered this," and the user-visible controls to view, edit, correct, or forget memories. This document describes memory as a user-facing behavior, not as a technical storage or retrieval system.

## Why It Exists

"Remember" is the first verb in the product's behavioral loop and the foundation the other verbs (Predict, Suggest, Learn, Adapt) build on — but without a behavioral definition of memory, teams will disagree about what should persist, for how long, and what a user should be able to see or undo. A user who cannot tell what the AI remembers, or cannot correct a wrong memory, will lose trust in every proactive suggestion the product makes, directly undermining the trust-gated Proactivity Ladder. This document exists to make memory a legible, controllable, user-facing concept before any technical memory system is designed around it.

## Approximate Page Count
6-8 pages.

## Sections

1. **Short-Term vs. Long-Term Memory (As Experienced)** — the user-facing distinction between transient session context and durable, persisted memory, with examples of what belongs in each.
2. **What "The AI Remembered This" Looks Like** — the moments and patterns where a suggestion or reminder explicitly signals it is drawing on a remembered fact, and why that signaling matters for trust.
3. **Categories of Rememberable Facts per Pillar** — the kinds of things the AI is expected to remember in Productivity, Finance, and Health respectively (e.g., a recurring meeting pattern, a subscription due date, a dietary preference).
4. **Memory Visibility Surface** — where and how a user can go to see what the AI currently remembers about them, described as a product capability rather than a specific screen design.
5. **Editing and Correcting Memory** — the user's ability to correct a wrong or outdated memory, and how the product should behave once a correction is made (i.e., corrections must propagate, not just silently accept and continue using the old fact elsewhere).
6. **Forgetting on Request** — the user's ability to explicitly ask the AI to forget a specific fact or category of facts, and what "forgotten" is expected to mean from the user's point of view.
7. **Memory and the Proactivity Ladder** — how the reliability and correction of memory relates to trust level advancement, without duplicating the Ladder's full definition from Phase 1.
8. **Explicit Non-Scope: Technical Memory/Storage Architecture** — states plainly that data retention systems, embeddings, storage schemas, and retrieval algorithms are excluded and reserved for a later phase.

## Deliverables

* Approved Memory Model — Behavioral Perspective document.
* A "rememberable facts" catalogue, one list per pillar, for use by feature-specific PRDs.
* A user-facing memory controls requirements list (view/edit/correct/forget) for Design.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Remember/Learn verbs and Proactivity Ladder, and the Product Architecture Overview (Document 01) for the five-component frame.

## Which Teams Use This

Product, Design, Trust & Safety, Data Science/ML (as consumers), Content/Copy, Customer Support.

## Completion Criteria

- [ ] Short-term and long-term memory have been distinguished with at least two concrete examples each.
- [ ] Every pillar has a documented catalogue of rememberable fact categories.
- [ ] Editing, correcting, and forgetting flows have each been validated against at least one worked user scenario.
- [ ] Confirmed the correction/forget behavior is consistent with the trust principles in the Product (Behavioral) Philosophy Document.
- [ ] Signed off by: Head of Product (required), Head of Trust & Safety (required), Head of Design (required).
