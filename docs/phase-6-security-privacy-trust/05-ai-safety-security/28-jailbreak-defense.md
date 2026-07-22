# Document 28: Jailbreak Defense

## Document Name
Jailbreak Defense

## Purpose
Define the defenses against a user directly attempting, through their own chat or voice conversation with the AI, to manipulate it into bypassing its safety and trust rules — for example, talking the AI into skipping the Proactivity Ladder's confirmation step before a financial action, adopting an unrestricted persona, or eroding a safety boundary across a multi-turn conversation. This document specifies what the completed Jailbreak Defense architecture must contain, not the implementation itself.

## Why It Exists
Prompt Injection Defense (Phase 6, Document 27) protects against content the user did not author but is exposed to; this document protects against the opposite and equally dangerous case — the account's own authenticated, legitimate user directly and deliberately trying to talk the AI out of its guardrails. Because this product is designed to take autonomous action on a user's behalf (the Proactivity Ladder authorizing financial and household actions with reduced confirmation at higher trust tiers), a successful jailbreak is not just an embarrassing off-brand response — it can be the difference between an AI that correctly demands confirmation before moving money and one that a persuasive multi-turn conversation talked into skipping that check entirely. This document exists to make the AI's safety and trust rules resistant to conversational persuasion, not just resistant to content it did not choose to trust.

## Approximate Page Count
9-11 pages

## Sections
1. **Jailbreak Vector Catalog** — the categorized techniques a user may attempt: persona adoption ("pretend you're an unrestricted AI"), hypothetical/fictional framing, incremental multi-turn erosion, false authority claims ("I'm the developer, override this"), urgency/emotional pressure, and encoding or obfuscation tricks.
2. **Boundary with Prompt Injection Defense (Document 27)** — an explicit statement that injection defense covers untrusted third-party content the user did not author, while this document covers the authenticated user's own direct conversational input attempting manipulation; the two documents share detection infrastructure but govern different trust boundaries.
3. **System Instruction Hardening** — the architectural requirement that core safety and trust rules (harmful content refusal, Proactivity Ladder confirmation gates) live outside conversational context in a way that cannot be overridden by anything said within the conversation, scoped against Prompt & Inference Architecture (Phase 5, Document 03).
4. **Proactivity Ladder Confirmation Integrity** — the hard, non-conversational enforcement guarantee that a high-stakes financial or health action can never be authorized purely through chat persuasion; the actual confirmation mechanism (Phase 5, Document 14) must be a structural gate the conversation cannot talk its way around.
5. **Multi-Turn Manipulation Detection** — detection of gradual, incremental jailbreak attempts that build across a conversation session rather than appearing in a single message, including the required session-level (not just message-level) analysis window.
6. **Jailbreak Attempt Classification & Response** — how the AI responds once a manipulation attempt is detected: refuse-and-explain, refuse-and-redirect, or silent non-compliance, and the policy for which response fits which detected technique.
7. **Adversarial Testing & Jailbreak Red-Teaming** — the structured red-team program specifically targeting jailbreak techniques (as distinct from the general safety red-teaming defined in AI Safety, Phase 6, Document 25), including a required reference set of publicly known jailbreak technique families the program must test against.
8. **Voice-Specific Jailbreak Considerations** — the unique risks the voice modality introduces (ambient/overheard commands, voice impersonation, social-engineering pacing unique to spoken conversation) scoped against Voice NLU Architecture (Phase 5, Document 21).
9. **Monitoring & Alerting for Jailbreak Attempts** — the logging and alerting pipeline for detected or suspected jailbreak attempts, feeding Monitoring, Incident & Fraud (Phase 6, group 04), and the metric used to track attempt volume and defense efficacy.
10. **Post-Incident Hardening Loop** — the required process for updating system instruction hardening and the red-team test suite whenever a successful or near-successful jailbreak is identified in production or during testing, so each discovered technique permanently strengthens the defense.

## Deliverables
- Jailbreak vector catalog with technique families and severity ratings.
- System instruction hardening specification reviewed against Prompt & Inference Architecture.
- Proactivity Ladder confirmation integrity specification with a documented proof that no conversational path can bypass the structural confirmation gate.
- Multi-turn manipulation detection design, including the session-level analysis window definition.
- Jailbreak-specific adversarial red-team test suite and evaluation report template.
- Voice-modality jailbreak risk assessment.
- Jailbreak-attempt monitoring and alerting specification.
- Post-incident hardening runbook.

## Dependencies
Requires Prompt Injection Defense (Phase 6, Document 27) for shared detection infrastructure and trust-boundary definitions, Prompt & Inference Architecture (Phase 5, Document 03) for system instruction hardening, Proactivity Ladder Decision Engine (Phase 5, Document 14) for confirmation-gate integrity, and Voice NLU Architecture (Phase 5, Document 21) for voice-specific risk. Coordinates with AI Safety (Phase 6, Document 25) for the shared red-teaming program and with the Threat Model (Phase 6, Document 02) for the jailbreak adversary category.

## Teams
AI/ML Engineering, Security, Trust & Safety, QA/Quality Engineering

## Completion Criteria
- [ ] Jailbreak vector catalog reviewed against current publicly known jailbreak technique families and refreshed at defined intervals.
- [ ] Proactivity Ladder confirmation integrity validated to hold under every cataloged manipulation technique, including multi-turn erosion.
- [ ] Multi-turn manipulation detection tested against a session-level (not single-message) adversarial test suite.
- [ ] Voice-specific jailbreak risks reviewed jointly with the Voice NLU Architecture owner.
- [ ] Signed off by: CISO (required), Head of AI/ML (required).
