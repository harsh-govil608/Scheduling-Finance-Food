# Document 29: Supply Chain Security

## Document Name
Supply Chain Security

## Purpose
Define the security requirements for every software dependency, build tool, container base image, and third-party library the platform relies on — protecting against a compromised or malicious upstream package, build tool, or CI/CD component reaching production. This document specifies what the supply chain security program must require and enforce, not the specific tooling chosen to implement it.

## Why It Exists
Modern breaches increasingly originate in the software supply chain — a compromised open-source package, a poisoned build tool, a hijacked maintainer account, or a tampered container base image — rather than direct attacks on production systems, and these attacks are especially dangerous because they arrive already trusted, inside code the platform's own engineers wrote and reviewed. A platform that ingests SMS content, bank transactions, health records, and forms AI memories of a person's life cannot treat dependency security as an afterthought delegated implicitly to whichever engineer happens to run `install`; a single compromised transitive dependency in the finance or AI inference path could exfiltrate user data or corrupt an autonomous action before any other Phase 6 control has a chance to detect it. This document exists to force dependency and build-pipeline risk to be managed as a first-class, continuously monitored security discipline rather than a one-time review at initial adoption.

## Approximate Page Count
7-9 pages

## Sections
1. **Dependency Vetting Process** — the criteria and required approval workflow for introducing any new third-party library, SDK, or package into the codebase (license compatibility, maintenance activity, known CVE history, transitive dependency depth).
2. **SBOM (Software Bill of Materials) Requirements** — what must be generated and tracked for every release artifact (direct and transitive dependencies, versions, licenses, provenance), the generation format standard, and where SBOMs are archived for audit.
3. **Vulnerability Scanning & Patching SLAs** — automated scanning cadence (per-commit, nightly, per-release) and the required remediation timeline for known vulnerabilities by severity (critical, high, medium, low), including the escalation path when a fix is not yet available upstream.
4. **Build Pipeline & CI/CD Integrity** — requirements for securing the build environment itself (isolated runners, signed build steps, reproducible builds, restricted access to pipeline configuration) so the pipeline cannot be used as an injection point, cross-referenced to CI/CD (Phase 4 Doc 30).
5. **Artifact Signing & Provenance Verification** — requirements for cryptographically signing build artifacts and container images, and verifying signatures/provenance before any artifact is promoted to a downstream environment.
6. **Container & Base Image Security** — requirements for base image sourcing (approved registries only), minimalism (no unnecessary packages), scanning cadence, and rebuild triggers when an upstream base image publishes a fix.
7. **Open-Source License Compliance** — the process for classifying dependency licenses against the company's approved license list and flagging/escalating copyleft or otherwise incompatible licenses before merge.
8. **Compromised Dependency Response Playbook** — the incident-response procedure specific to a supply chain compromise (e.g. a malicious package version published to a registry), including how affected services are identified and rolled back.
9. **Internal Package & Registry Governance** — controls for the company's own internal/private package registries, including who can publish, signing requirements for internal packages, and namespace-squatting protections.
10. **Third-Party Build Tool & IDE Plugin Policy** — vetting requirements for developer-facing tools (IDE extensions, linters, formatters, package manager plugins) that execute code on engineer machines or in CI, a less obvious but real supply chain surface.

## Deliverables
- Dependency vetting checklist and approval workflow, integrated into the pull-request process.
- SBOM generation standard and archival policy, applied to every release artifact.
- Vulnerability scanning tool integration and documented patching SLA table by severity.
- Build pipeline hardening specification (isolated runners, signed steps, access controls).
- Artifact signing and provenance verification requirement, enforced at deployment gate.
- Approved base image registry list and container scanning policy.
- Open-source license compliance policy with an approved/disallowed license list.
- Compromised dependency incident-response playbook.

## Dependencies
Requires Security Program & Governance (Phase 6 Doc 01), Threat Model (Phase 6 Doc 02); requires CI/CD (Phase 4 Doc 30), Code Standards (Phase 4 Doc 53), Security Architecture Overview (Phase 4 Doc 55); informs Third-Party Risk Management (Phase 6 Doc 30) for vendor-supplied software components.

## Teams
Security, Platform Engineering, DevOps/Infrastructure, Engineering Leadership, Legal (license compliance)

## Completion Criteria
- [ ] SBOM requirements defined and generation verified for every release artifact.
- [ ] Vulnerability scanning integrated into CI with enforced patching SLAs by severity.
- [ ] Artifact signing and provenance verification enforced as a deployment gate.
- [ ] Compromised dependency response playbook tested via at least one tabletop exercise.
- [ ] Open-source license compliance policy published and integrated into the dependency approval workflow.
- [ ] Signed off by: CISO (required), VP Engineering (required), Head of Platform Engineering (required).
