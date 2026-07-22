# Document 28: Voice UX

## Document Name
Voice UX

## Purpose
Define the actual designed voice interaction experience for the assistant: the sound design (earcons for listening, processing, confirming, and error states), the spoken confirmation and clarification script templates, the voice persona and tone of voice, and the conversational UI patterns — both audio-only and screen-paired — that let a user complete a voice action with no screen in front of them. This document is the design-system-level realization of the behavioral commitments in Voice Interaction (Phase 2, Doc 27) and the engineering-facing pipeline in the Voice Assistant PRD (Phase 3, Doc 06); it turns "the assistant confirms what it heard" into an actual sound, an actual sentence, and an actual visual state.

## Why It Exists
Phase 2 Doc 27 established that voice must confirm what it heard without requiring a screen, and Phase 3 Doc 06 specified the confidence-threshold-to-confirmation-behavior pipeline that decides when the assistant should ask versus assume — but neither document specifies what that confirmation actually sounds or reads like: what tone plays when an entry is heard, what words the assistant says back, how a clarifying question is worded so it takes one turn instead of three, or how the same assistant stays recognizable across three pillars and, over time, multiple device types. Left undesigned, each pillar team invents its own beeps and phrasing, the wording drifts from the written notification tone, and the "one assistant" feel the mission depends on fractures at exactly the moment — a spoken confirmation, often with no visual anchor — where the user has the least other context available to catch a mistake or trust the result. This document exists to give voice a designed identity: a persona, an earcon library, and a script system, so that hearing the assistant confirm an action feels like hearing the same assistant every time, regardless of pillar or device.

## Approximate Page Count
8-10 pages.

## Sections
1. **Voice Persona & Tone of Voice** — the concrete character of the assistant's spoken voice (word choice, sentence length, formality, warmth) that operationalizes Phase 2 Doc 27's "concise, natural, non-robotic" requirement, and the rules keeping it consistent with the written notification tone.
2. **Sound Design System — Earcons & State Tones** — the short, distinct non-speech sounds for listening-started, heard-and-processing, confirmed/committed, needs-clarification, and error/misheard states, and how this library is distinguished from the general Notification System's audio alerts.
3. **Spoken Confirmation Script Templates** — the actual phrase templates and read-back formats per pillar action (task capture, expense note, food log, reminder) that satisfy the PRD's spoken-confirmation content requirement, including how variable data (amounts, times, item names) is slotted into natural sentences.
4. **No-Screen Conversational UI Patterns** — the exact dialogue patterns for ask-back and clarification questions, phrasing rules that keep a clarification to a single question wherever possible, and how a "guess-and-confirm" response is worded versus an "ask-first" response.
5. **Visual Voice UI (Screen-Present Companion)** — the on-screen elements that pair with voice when a screen is available (listening indicator, live transcript, confirmation card) and how these visual states stay synchronized with the audio state so the two never contradict each other.
6. **Latency & Silence Handling in Voice Design** — the filler sounds or short spoken cues used while the system is processing, designed against the PRD's end-of-utterance-to-spoken-confirmation latency budget so a pause never reads as a failure.
7. **Multi-Turn Dialogue & Fallback-to-Screen Transition Design** — the turn-by-turn design of a clarification exchange and the specific audio/visual transition used when the turn limit is exceeded and the interaction hands off to a screen-based flow.
8. **Cross-Platform Voice Consistency** — how persona, earcons, and scripts stay recognizably the same across phone, wearable, and other ambient contexts, while verbosity and timing adapt to each surface's constraints.
9. **Error, Misrecognition & Correction Voice Patterns** — the specific sound and spoken language used when the assistant misheard something, and how a post-hoc correction (per Doc 27's undo/correction model) is voiced back to confirm the fix landed.
10. **Voice Design QA & Usability Testing Requirements** — how persona, earcons, and scripts are validated with real users for clarity, tone, and trust before ship, kept distinct from the ASR/NLU accuracy testing owned by the Voice/NLU Architecture (Phase 5, Doc 21).

## Deliverables
* Approved Voice UX document.
* An earcon/sound library specification covering every voice interaction state.
* A spoken confirmation and clarification script/phrase-template library, organized per pillar action.
* A voice persona style guide usable by Content/Copy for any new voice-capable feature.
* A screen-present voice UI pattern set (listening indicator, live transcript, confirmation card).

## Dependencies
Requires Voice Interaction (Phase 2, Doc 27) for the no-screen confirmation philosophy, modality map, and ambiguity-handling-to-trust-level rules this document must give sound and language to; requires the Voice Assistant PRD (Phase 3, Doc 06) for the supported voice-action registry, the confidence-threshold-to-confirmation-behavior table, the latency budget, and the multi-turn turn-limit mechanics these scripts and transitions are designed against; requires the Voice/NLU Architecture (Phase 5, Doc 21) for the ambiguity-resolution and clarification-dialogue architecture this document's dialogue patterns must fit within; draws on the forthcoming Phase 7 Notifications document for consistency between voice earcons and general notification audio, and the forthcoming Phase 7 Motion and Design Language documents for visual-state pairing; informed by Accessibility (Design System Implementation) (Phase 7, Doc 29) for the visual/haptic equivalents required wherever this document defines an audio-only pattern.

## Teams
Design (Voice/Conversational UX), Sound Design/Audio, Content/Copy, Product, Engineering (Voice), Engineering (Client), Data Science/ML (NLU), QA, Accessibility Lead (as reviewer).

## Completion Criteria
- [ ] Every voice action in the Voice Assistant PRD's supported-action registry has an approved spoken confirmation script template.
- [ ] The earcon library covers all five core states (listening, processing, confirmed, needs-clarification, error) with no two states sharing an indistinguishable sound.
- [ ] The voice persona style guide has been checked against the written notification tone for consistency.
- [ ] The fallback-to-screen transition has been walked through end-to-end for at least one pillar scenario.
- [ ] Every audio-only pattern in this document has a corresponding visual/haptic equivalent confirmed with the Accessibility (Design System Implementation) owner.
- [ ] Signed off by: Head of Design (required), Voice/Conversational Design Lead (required), Head of Product (required).
