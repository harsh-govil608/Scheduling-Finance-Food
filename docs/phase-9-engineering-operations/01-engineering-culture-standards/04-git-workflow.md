# Document 04: Git Workflow

## Document Name
Git Workflow

## Purpose
Define the branching strategy, commit message conventions, and pull request review requirements engineers follow inside every repository defined by Repository Strategy (Doc 03), so that how code moves from a laptop to production is predictable and independent of which team or repo an engineer is working in.

## Why It Exists
Without a shared workflow, every team invents its own branching model, commit style, and merge ritual, which makes cross-team contribution slower, makes release tooling harder to automate uniformly, and turns simple questions like "is this safe to merge" into repo-specific trivia. This document exists so that the mechanics of using git are a solved, shared problem, freeing code review (Doc 02) to focus on substance rather than process.

## Approximate Page Count
6-8 pages

## Sections
1. **Branching Strategy** — the trunk-based-development-versus-GitFlow decision, branch naming convention, and the expectation that feature branches are short-lived.
2. **Commit Conventions** — the conventional-commit message format, the expectation of atomic commits, and the requirement to link a commit or PR to its originating ticket/issue.
3. **Pull Request Lifecycle** — guidance on PR size, when to open a draft PR, and the required fields in the PR description template.
4. **Review & Merge Requirements** — required approvals and required automated checks before merge, cross-referencing CI/CD (Phase 4, Doc 30) and Code Standards (Phase 4, Doc 53), and the sanctioned merge strategy (squash, rebase, or merge commit) per repository type.
5. **Release Branching & Tagging** — how branches and tags interact with the Release Process (Phase 4, Doc 54), including the hotfix branch procedure for production incidents.
6. **Feature Branches & Feature Flags Interplay** — how trunk-based development combined with Feature Flags (Phase 4, Doc 26) replaces long-lived feature branches for incomplete work, and when a long-lived branch is still the right call.
7. **Merge Conflicts & Rebasing Etiquette** — norms for keeping a branch current with trunk, when to rebase versus merge, and the rules around force-pushing to a shared branch.
8. **Git Hygiene & Repository Cleanliness** — stale branch cleanup cadence, large-file/binary handling policy, and prevention of secrets being committed to history, tying into Phase 6 security controls.

## Deliverables
- Branching strategy diagram and branch naming convention specification
- Commit message convention with accompanying enforcement tooling (e.g., commit-msg hook or CI check)
- Standard pull request description template
- Merge strategy policy, specified per repository type from Repository Strategy's inventory

## Dependencies
Depends on CI/CD (Phase 4, Doc 30) for the required pre-merge checks referenced in Review & Merge Requirements, on Feature Flags (Phase 4, Doc 26) for trunk-based development to be viable, on Release Process (Phase 4, Doc 54) for release branch/tag alignment, and on Code Standards (Phase 4, Doc 53) for the review-gate minimums this workflow enforces. Depends on Repository Strategy (Phase 9, Doc 03) for the set of repositories this workflow governs, and on Coding Standards — Practice Layer (Phase 9, Doc 02) for the review culture that fills this workflow's mechanics.

## Teams
Engineering (all), Platform Engineering, Developer Experience (DevEx)

## Completion Criteria
- [ ] Branching strategy and naming convention adopted across every repository in the Repository Strategy inventory.
- [ ] Commit message convention enforced via an automated pre-commit or CI check.
- [ ] Standard PR template in active use across all current repositories.
- [ ] Hotfix branch procedure exercised at least once (drill or real incident) and confirmed to align with the Release Process.
- [ ] Signed off by: VP Engineering (required), Platform Engineering Lead (required).
