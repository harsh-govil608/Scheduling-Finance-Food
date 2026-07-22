# Document 21: Voice/NLU Architecture

## Document Name
Voice/NLU Architecture

## Purpose
Define the speech-to-text and natural-language-understanding model architecture behind the Phase 3 Voice Assistant PRD's parse-confirm-commit pipeline, covering intent classification and slot extraction for every supported voice action across the Productivity, Finance, and Health pillars. This document specifies the ASR/NLU model stack, its confidence and ambiguity-handling architecture, and its interfaces to downstream pillar services; it does not select a final speech-to-text or NLU vendor, and it does not redefine wake-word engineering, on-device audio capture, or the no-screen confirmation UX already owned by Phase 2 and Phase 3.

## Why It Exists
Voice is the product's highest-risk autonomy surface precisely because there is no screen to catch a mistake at the moment it happens, and the Voice Assistant PRD's confidence-threshold-to-confirmation-behavior table is only as real as the model architecture computing that confidence — without a shared ASR/NLU architecture, each pillar's voice logging (food, tasks, expenses) would build its own intent parser with its own notion of confidence, producing inconsistent trust behavior for what the user experiences as one assistant. This document exists to define, before implementation, how speech becomes a structured, confidence-scored intent-and-slot payload that the parse-confirm-commit pipeline can safely act on, how a single utterance spanning two pillars is segmented and routed, and how ambiguity is resolved with a single clarifying question rather than a silent guess — so the PRD's ask-back requirement and Proactivity Ladder gating are backed by a real pipeline rather than an assumption.

## Approximate Page Count
9-11 pages

## Sections
1. **ASR (Speech-to-Text) Pipeline** — audio-capture handoff from the client, streaming versus batch transcription, noise-robustness requirements, and language/accent coverage tied to Phase 2 Localization.
2. **Intent Classification & Slot Extraction** — how transcribed text is classified into one of the Voice Assistant PRD's registered voice actions per pillar, and how each intent's required slots (amount, merchant, task title, meal description, due date) are extracted against a defined parse schema.
3. **Multi-Intent & Cross-Pillar Utterance Handling** — the architecture for segmenting and independently routing a single utterance that contains intents for two different pillars, per the PRD's cross-pillar edge case.
4. **Confidence Scoring & Threshold Architecture** — how per-intent and per-slot confidence is computed and mapped onto the PRD's confidence-threshold-to-confirmation-behavior table, including how the threshold shifts with a user's current Proactivity Ladder rung.
5. **Ambiguity Resolution & Clarification Dialogue** — the architecture for generating a single clarifying question when confidence is low or intent is ambiguous, and the turn-limit mechanics that trigger fallback to a screen-based flow.
6. **Meal-Description Handoff to the Vision Pipeline** — the shared entity schema by which a voice-described meal produces output compatible with the Meal Recognition (Computer Vision) Architecture (Phase 5, Document 20), so the two capture paths converge on one proposal format.
7. **On-Device vs. Server-Side Inference Split** — the criteria for deciding which ASR/NLU stages run on-device versus server-side, driven by the PRD's end-of-utterance-to-spoken-confirmation latency budget, offline/degraded-connectivity behavior consistent with the Offline Experience document, and the privacy sensitivity of voice audio containing financial amounts in shared or public environments.
8. **Personalization & Per-User Adaptation** — how the model adapts to an individual's vocabulary, accent, recurring merchant or task phrasing over time, drawing on the Memory & Context Systems and Learning Systems subsystem groups.
9. **Correction & Post-Hoc Feedback Loop** — how corrections to already-committed voice-logged entries feed back into per-user and per-intent model tuning, without requiring the correction to have happened in the original voice session.
10. **Model Selection Criteria** — vendor-neutral criteria for evaluating candidate ASR and NLU models or providers (multilingual/accent coverage, latency, on-device feasibility, fine-tuning support, cost per inference), explicitly deferring the final selection to implementation.

## Deliverables
- ASR/NLU pipeline architecture diagram from audio-capture handoff through committed structured intent output.
- Intent-and-slot schema for every voice action in the Voice Assistant PRD's supported-action registry, organized per pillar.
- Confidence-threshold architecture mapped explicitly to the PRD's confidence-threshold-to-confirmation-behavior table.
- Multi-intent, cross-pillar utterance handling and routing specification.
- Ambiguity-resolution and clarification-dialogue architecture, including turn-limit and fallback-to-screen mechanics.
- Shared meal-entity schema aligning voice-described and photo-recognized meal output.
- Vendor-neutral model selection criteria rubric.

## Dependencies
Requires AI Platform Overview (Phase 5, Document 01) for the platform's shared subsystem map; requires the Voice Assistant PRD (Phase 3, Document 06) as the product-behavior contract this architecture must satisfy, including its supported-action registry, confidence-threshold-to-confirmation-behavior table, edge cases, and failure scenarios; requires the Voice Interaction document (Phase 2, Document 27) for the no-screen confirmation philosophy and modality map; requires the Meal Recognition (Computer Vision) Architecture (Phase 5, Document 20) for the shared meal-entity handoff schema; informed by the Offline Experience document (Phase 2, Document 30) for degraded-connectivity behavior, the Notification System (Phase 2, Document 14) for pending/autonomous-action notification integration, and Phase 2 Localization for language and accent coverage; draws on the Memory & Context Systems and Learning Systems subsystem groups (Phase 5) for personalization and the correction feedback loop, and the Privacy-Preserving AI Platform Contract (Phase 5) for on-device processing constraints on voice audio.

## Teams
AI/ML Engineering (Speech/NLU), Data Science, Mobile Engineering, Backend Engineering, Design (Voice/Conversational UX), Privacy/Legal, Product

## Completion Criteria
- [ ] Intent-and-slot schema validated against every voice action in the Voice Assistant PRD's supported-action registry with no gaps.
- [ ] Confidence-threshold architecture mapped to the PRD's confidence-threshold-to-confirmation-behavior table for at least two distinct Proactivity Ladder trust levels.
- [ ] Multi-intent utterance handling reviewed against the PRD's cross-pillar edge case.
- [ ] Meal-description handoff schema reviewed jointly with the Meal Recognition (Computer Vision) Architecture owner for compatibility.
- [ ] Signed off by: Head of AI/ML (required), Principal Architect (required), Voice/Conversational Design Lead (required).
