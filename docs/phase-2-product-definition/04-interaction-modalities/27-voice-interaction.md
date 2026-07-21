# Document 27: Voice Interaction

## Document Name
Voice Interaction

## Purpose

Define when and how voice is offered as an input modality across all three pillars, and specify how the AI confirms what it heard and what it is about to do when the user is not looking at a screen. This document scopes voice as one interaction option among several, not a standalone product.

## Why It Exists

Voice logging touches all three pillars independently — food logging in Health, task capture in Productivity, expense notes in Finance — and without a shared contract, each pillar team will invent its own listening pattern, its own confirmation style, and its own tolerance for ambiguity, which fractures the "one assistant" feel the mission depends on. Voice is also uniquely risky for this product: the AI is supposed to act with increasing autonomy per the Proactivity Ladder, but a misheard voice command that gets logged silently, or one that requires the user to stare at a screen to confirm, directly breaks the "never overwhelm" and trust principles. This document exists so every voice-capable moment resolves ambiguity, confirms action, and allows correction in a way that works with no screen in front of the user.

## Approximate Page Count

7-9 pages.

## Sections

1. **Where Voice Appears — The Modality Map** — enumerates every current voice-capable moment across the three pillars (voice food logging, voice task capture, voice expense notes, quick voice reminders) as an available input alongside touch and text, not a replacement for them.
2. **Voice as Optional, Never Mandatory** — the principle that every voice-enabled action must have an equally capable non-voice fallback, and the criteria used to decide when voice is offered versus hidden for a given action.
3. **The No-Screen Confirmation Model** — how the AI confirms what it understood and what it is about to do when the user cannot or is not looking at a screen: spoken-summary patterns, explicit ask-back on ambiguity, and when a confirmation must be affirmative rather than assumed.
4. **Ambiguity & Misrecognition Handling** — the UX rules for what happens when voice input is unclear or only partially understood — ask, guess-and-confirm, or discard — and how that choice maps to the user's current Proactivity Ladder trust level.
5. **Voice + Pillar-Specific Logging Patterns** — a worked "hear → parse → confirm → store" pattern for each pillar: voice food logging (Health), voice task capture (Productivity), and voice expense notes (Finance).
6. **Undo & Correction After Voice Entry** — how a user corrects something that was voice-logged incorrectly, accounting for the fact they may not look at a screen until well after the entry was made.
7. **Tone & Persona of Voice Responses** — how the AI's spoken responses should sound (concise, natural, non-robotic) and how that voice persona relates to, and stays consistent with, the AI's written notification tone.
8. **Multi-Turn Voice Interactions** — when voice supports a back-and-forth clarification dialog versus a single-shot command, and the turn-count limit before the interaction falls back to a screen-based flow.
9. **Situational Appropriateness of Voice (UX-Only)** — moments where voice input or output should not be offered for experience reasons (quiet hours, sensitive amounts, shared/public spaces), described from a user-experience standpoint only.
10. **Out of Scope** — explicitly excludes speech-to-text engine selection, wake-word engineering, and NLU/model behavior, noting these belong to later engineering and AI-implementation phases.

## Deliverables

* Approved Voice Interaction document.
* A cross-pillar Voice Moments Map listing every voice-capable action alongside its required non-voice fallback.
* A No-Screen Confirmation pattern library referenced by all three pillar design teams.

## Dependencies

Requires the Product (Behavioral) Philosophy Document (Phase 1) for the Proactivity Ladder and "Never Overwhelm" rules that govern how confidently voice actions may be auto-confirmed; the Product Pillars Overview (Document 02) for the pillar capability boundaries voice logging must respect; and the forthcoming Notification & Interruption Experience Document (Core Daily Experience group) and pillar experience documents (Core Daily Experience / Pillar Experiences groups) for consistency with existing confirmation and interruption patterns.

## Which Teams Use This

Product, Design (Voice/Conversational UX), Engineering (Voice team), Data Science/ML (NLU), QA, Content/Copy (voice tone and phrasing).

## Completion Criteria

- [ ] Every voice-capable moment listed in the Modality Map has a documented non-voice fallback with equivalent functionality.
- [ ] The No-Screen Confirmation Model has been validated against at least one worked scenario per pillar (Productivity, Finance, Health).
- [ ] Ambiguity-handling rules are explicitly mapped to at least two Proactivity Ladder trust levels.
- [ ] Undo/correction flow for voice-logged entries has been walked through for a misheard-entry scenario in each pillar.
- [ ] Reviewed against the Product Philosophy Document to confirm no contradiction with the Behavioral Loop or "Never Overwhelm" rules.
- [ ] Signed off by: Head of Product (required), Head of Design (required), Voice/Conversational Design Lead (required).
