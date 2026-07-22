# Document 06: Voice Assistant PRD

## Document Name
Voice Assistant PRD

## Purpose
Define the complete engineering-facing specification for the voice interaction feature: the enumerated set of supported voice actions across Productivity, Finance, and Health, the no-screen confirmation mechanics required before any action is committed, and the ambiguity-handling and correction pipeline for misheard or partially understood input.

## Why It Exists
The Voice Interaction document (Phase 2) establishes the product philosophy that every voice-capable moment must confirm what was heard and never require a screen to correct, but it does not specify the supported-action registry, the confirmation state machine, or the ambiguity-resolution pipeline an engineering team needs to build it consistently across pillars — without that, each pillar's voice logging (food, tasks, expenses) will invent its own confidence thresholds and confirmation style, and a misheard entry could get silently committed exactly where the Proactivity Ladder demands caution. This PRD exists because voice is this product's highest-risk autonomy surface: it is where "act with increasing autonomy" and "never overwhelm/never act on bad information" are in the most direct tension, with no screen available to catch a mistake in the moment.

## Approximate Page Count
8-11 pages.

## Sections
1. **Feature Scope** — in scope: the supported voice-action registry per pillar, the parse-confirm-commit pipeline, no-screen confirmation mechanics, ambiguity and misrecognition handling, post-hoc correction of voice-logged entries; out of scope: speech-to-text engine selection, wake-word engineering, and underlying NLU/model behavior, explicitly deferred to later engineering and AI-implementation phases per the Voice Interaction document, and the shared result/display contract for voice-originated search queries, owned by the Search PRD (Document 05).
2. **User Stories** — e.g., as a user saying "log a coffee for 150 rupees" while walking, I hear a brief spoken confirmation of what was captured before it's committed as a Finance transaction; as a user whose voice input for a food log was ambiguous between two similar dishes, I'm asked a single clarifying question rather than having a guess silently logged; as a user who realizes an hour later that a voice-logged expense amount was wrong, I can correct it without having needed to watch a screen at logging time.
3. **Functional Requirements** — the registry of supported voice actions per pillar (e.g., food logging, task capture, expense notes, quick reminders) each with a defined parse schema; the parse-confirm-commit pipeline with an explicit confidence-threshold gate below which the system must ask rather than assume; the spoken-confirmation content requirement (what must be read back before commit); the multi-turn clarification limit before falling back to a screen-based flow; and the correction/undo pathway for entries already committed from voice.
4. **Non-Functional Requirements** — a maximum latency budget from end-of-utterance to spoken confirmation so the interaction doesn't feel broken; an offline/degraded-connectivity behavior definition (queue-and-confirm-later vs. reject) consistent with the Offline Experience document; and a privacy requirement for voice processing of sensitive content (e.g., financial amounts) in shared/public listening environments.
5. **UX Requirements** — must conform to the Voice Interaction document's no-screen confirmation model, ambiguity-handling-to-trust-level mapping, voice-tone/persona consistency with written notification tone, and the requirement that every voice action has an equally capable non-voice fallback.
6. **States & Flows** — Listening, Parsed-High-Confidence-Auto-Confirmed (trust-gated), Parsed-Needs-Spoken-Confirmation, Ambiguous-Asking-Clarification, Committed, Committed-Then-Corrected, Fallback-To-Screen (turn limit exceeded), Discarded-Low-Confidence.
7. **Edge Cases** — background noise causing partial recognition of an otherwise clear intent; a single utterance containing two intents for two different pillars (e.g., "remind me to pay rent and log that I ate out"); a user interrupting their own utterance mid-sentence; a voice command referencing an entity that doesn't exist yet (e.g., logging against a budget category not yet created).
8. **Failure Scenarios** — what happens when the core assumption "we can confidently parse intent from speech" breaks: a misrecognized entry is committed before the user notices (post-hoc correction pathway must be exercised), the confirmation audio itself fails to play (silent confirmation gap), or the clarification dialog loops without resolving and must hard-fall-back to screen.
9. **AI Behaviors** — how the confidence threshold for auto-confirm-without-asking scales with the user's current Proactivity Ladder rung (a new user is asked more often; a high-trust user with a strong history of accurate parses for a given action type may see fewer confirmations); how repeated corrections of the same type of misparse feed back into per-user recognition tuning.
10. **Notification Behaviors** — how a voice-logged action that required deferred confirmation (e.g., connectivity was lost mid-flow) surfaces as a pending item through the Notification System rather than silently disappearing, and how a fully autonomous voice-triggered action still generates the "autonomous action with notification" record required at that Proactivity Ladder rung.
11. **Success Criteria** — a user can complete a pillar-crossing voice action end-to-end without looking at the screen, correctly understands what was captured before it commits, and can always fix a mistake after the fact.
12. **Metrics** — voice-action completion rate without fallback-to-screen, average clarification turns per successful action, misrecognition-then-correction rate, and per-pillar voice adoption rate relative to touch/text logging.
13. **Open Questions** — how the confidence threshold should differ between low-stakes actions (a reminder) and high-stakes ones (a large expense) at the same trust level; whether spoken confirmation should be skippable by power users who've demonstrated high parse accuracy, and if so how that's reconciled with the Voice Interaction document's ask-back requirement; how multi-intent single utterances should be scoped for v1 versus deferred.

## Deliverables
* Approved Voice Assistant PRD.
* Supported voice-action registry with parse schemas per pillar.
* A confidence-threshold-to-confirmation-behavior table mapped to Proactivity Ladder trust levels.
* A worked hear-parse-confirm-commit scenario per pillar, including one ambiguous and one misrecognition case.

## Dependencies
Requires the **Voice Interaction** document (Phase 2, Document 27) for philosophy, modality map, and no-screen confirmation model, the **Automation Philosophy** document (Phase 2) for Proactivity Ladder confidence gating, the **Notification System** document (Phase 2, Document 14) for pending/autonomous-action notification integration, the **Offline Experience** document (Phase 2, Document 30) for degraded-connectivity behavior, the **Search PRD** (Document 05) for the shared result contract when voice is used as a search entry point, and the **Product Philosophy Document** (Phase 1).

## Teams Using This
Product, Design (Voice/Conversational UX), Engineering (Voice), Engineering (Client), Data Science/ML (NLU), QA, Content/Copy.

## Completion Criteria
- [ ] Every voice action in the registry has a documented non-voice fallback with equivalent functionality.
- [ ] The no-screen confirmation model validated against at least one worked scenario per pillar.
- [ ] Confidence-threshold-to-confirmation-behavior table mapped to at least two distinct Proactivity Ladder trust levels.
- [ ] Correction/undo flow walked through for a misheard-entry scenario in each pillar.
- [ ] Reviewed against the Product Philosophy Document to confirm no contradiction with the Behavioral Loop or Never Overwhelm rules.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Voice/Conversational Design Lead (required).
