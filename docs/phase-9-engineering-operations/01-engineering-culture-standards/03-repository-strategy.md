# Document 03: Repository Strategy

## Document Name
Repository Strategy

## Purpose
Define the monorepo-versus-polyrepo decision framework and the resulting repository organization scheme covering the 9 backend services, client applications, and AI/ML systems, so that every new repository request and every cross-repo change follows a known, defensible structure rather than ad hoc precedent.

## Why It Exists
Left ungoverned, repository sprawl compounds quietly: every new service or client spins up its own repo with its own conventions, shared code gets copy-pasted instead of packaged, ownership boundaries blur, and CI cost and blast radius grow unpredictably. This document exists to make the repository topology a deliberate, documented decision — tying repository boundaries to the service and team boundaries already established in Phase 4 — so that "where does this code live" has one obvious answer at any scale.

## Approximate Page Count
6-8 pages

## Sections
1. **Monorepo vs. Polyrepo Decision Framework** — the criteria used to decide (build tooling maturity, ownership boundary clarity, blast-radius tolerance, CI cost) and the resulting decision with its explicit rationale and rejected alternatives.
2. **Repository Inventory & Boundaries** — the enumerated repository set: the 9 backend services (User, Calendar, Task, Finance, Health, Notification, Search, Media, Analytics — Phase 4 Docs 09-17), client applications (mobile, web), AI/ML systems (Phase 5), and shared libraries/infrastructure-as-code.
3. **Shared Code & Library Strategy** — how common code (shared types, internal SDKs, design-system components) is packaged, versioned, and consumed across repository boundaries without falling back to copy-paste duplication.
4. **Ownership & CODEOWNERS Mapping** — how repository and directory-level ownership maps to the team structure defined in the Engineering Handbook (Doc 01), and how ownership is kept accurate as teams reorganize.
5. **Access Control & Repository Governance** — who can create a new repository, who can archive or delete one, fork/mirror policy, and how repository-level access ties into the broader access-governance model from Phase 6 (Security, Privacy & Trust).
6. **Build & Dependency Isolation** — how the chosen repository boundaries interact with CI/CD (Phase 4, Doc 30) to keep build times bounded and to contain the blast radius of a change to the repositories that actually depend on it.
7. **Repository Lifecycle** — the request process for creating a new repository, the deprecation/archival process for a retired one, and the naming convention every repository must follow.
8. **Cross-Repo Coordination for Breaking Changes** — the process for coordinating a change that spans multiple repositories (for example, an API contract change), tying into API Contracts (Phase 4, Doc 50) and Versioning (Phase 4, Doc 51).

## Deliverables
- Monorepo/polyrepo decision record, including rejected alternatives and rationale
- Repository inventory table mapping every repository to its owning team and service
- CODEOWNERS policy and repository naming convention standard
- Repository creation and deprecation request process
- Cross-repo breaking-change coordination checklist

## Dependencies
Requires Service Decomposition (Phase 4, Doc 02) and the 9 backend service architecture documents (Phase 4, Docs 09-17). Depends on CI/CD (Phase 4, Doc 30) for build/isolation constraints and on API Contracts (Phase 4, Doc 50) and Versioning (Phase 4, Doc 51) for cross-repo change coordination. Depends on the Engineering Handbook (Phase 9, Doc 01) for team/ownership mapping and feeds Git Workflow (Phase 9, Doc 04), which defines branch mechanics within each repository this document defines.

## Teams
Platform Engineering, Engineering Leadership, Developer Experience (DevEx), Security

## Completion Criteria
- [ ] Monorepo/polyrepo decision ratified and documented with rejected alternatives recorded.
- [ ] Every existing and near-term-planned repository mapped to a single owning team in the inventory table.
- [ ] CODEOWNERS policy validated against at least one real repository.
- [ ] Repository creation/deprecation process tested end-to-end on at least one request.
- [ ] Signed off by: VP Engineering (required), Platform Engineering Lead (required).
