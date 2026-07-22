# Document 07: Device Trust

## Document Name
Device Trust

## Purpose
Define how the platform establishes, verifies, and continuously re-evaluates trust in the physical device a user is operating from — including jailbreak/root detection, device binding for sensitive actions, and the policy response when a device's trust posture degrades. This document specifies the device-trust policy and verification requirements layered on top of Phase 4's authentication and gateway architecture; it does not redefine those underlying systems.

## Why It Exists
Authentication proves who a user is; device trust addresses a different question — whether the device making the request is itself safe to act on, since a compromised, jailbroken, or malware-infected device can silently exfiltrate session tokens or manipulate what the user believes they are approving even after correct authentication. Given that this platform enables banking-linked financial actions, health record access, and increasingly autonomous AI actions on a user's behalf, a stolen credential used from a trustworthy device is a materially different risk than a valid session running on a compromised device. This document exists to define the policy that closes that gap: what device signals are required, how sensitive actions get bound to a specific verified device, and what happens automatically when device trust can no longer be established.

## Approximate Page Count
7-9 pages

## Sections
1. **Device Trust Model Overview** — the tiers of device trust the platform recognizes (unverified, verified, bound/high-trust) and what each tier is permitted to do.
2. **Jailbreak / Root Detection Policy** — requirements for detecting compromised device states on mobile clients and the mandatory platform response (restrict, warn, block sensitive actions).
3. **Device Binding for Sensitive Actions** — the policy requiring specific high-sensitivity actions (e.g. linking a new bank account, large transfers, health record export) to be bound to a previously verified device, with defined behavior for new/unrecognized devices.
4. **Device Registration & Deregistration Lifecycle** — how a device becomes trusted, how many trusted devices a user may have, and how a device is removed from trust (user-initiated, automatic on inactivity, or forced on suspected compromise).
5. **Device Risk Signals** — the signal set considered in device risk scoring (OS integrity, app tamper detection, emulator/virtualization detection, network reputation) and how these feed session risk continuously, not just at login.
6. **Lost/Stolen Device Response Policy** — the required user-facing and backend response when a device is reported lost or stolen, including forced session revocation and re-verification requirements.
7. **Cross-Device Continuity Policy** — how device trust policy accommodates legitimate multi-device use (phone, web, tablet) without weakening the binding guarantees for sensitive actions.
8. **AI Agent Action Constraints Tied to Device Trust** — policy on whether/how the AI's autonomous actions (Proactivity Ladder) are constrained or require step-up when the originating context involves a lower-trust device.
9. **Relationship to Phase 4 Authentication & Gateway Architecture** — explicit statement that this document defines device-trust policy and required signals, while Phase 4 documents define the technical session and gateway mechanisms that carry and enforce them.

## Deliverables
- Device trust tier definition with permitted actions per tier.
- Jailbreak/root detection requirement and mandated response behavior.
- Device binding requirement list for sensitive action categories.
- Device registration/deregistration lifecycle policy.
- Device risk signal catalog feeding continuous session risk scoring.
- Lost/stolen device response runbook.

## Dependencies
Security Program & Governance (Phase 6 Doc 01), Threat Model (Phase 6 Doc 02), Zero Trust Architecture (Phase 6 Doc 03), Authentication Policy (Phase 6 Doc 05), Authorization Policy & Access Governance (Phase 6 Doc 06), Authentication Architecture (Phase 4 Doc 07), Security Architecture Overview (Phase 4 Doc 55).

## Teams
Security, Engineering, Mobile/Client Engineering, Product

## Completion Criteria
- [ ] Device trust tiers defined and mapped to a concrete permitted-action list.
- [ ] Jailbreak/root detection validated on both supported mobile platforms.
- [ ] Device binding enforced end-to-end for at least the banking-integration sensitive-action flow.
- [ ] Lost/stolen device response runbook tested with a simulated revocation.
- [ ] Signed off by: CISO (required), Head of Mobile/Client Engineering (required), Head of Engineering (required).
