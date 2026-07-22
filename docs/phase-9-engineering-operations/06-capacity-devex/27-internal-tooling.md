# Document 27: Internal Tooling

## Document Name
Internal Tooling

## Purpose
Define the practice and ownership model for the internal tools engineering builds for itself — admin dashboards, debugging tools, and data-inspection tools for systems like AI Memory that are otherwise opaque to the humans operating them. This document specifies the build-vs-buy criteria for internal tooling, who owns each category of tool once built, and how internal tools are held to the same quality and support bar as customer-facing product rather than being treated as disposable scripts.

## Why It Exists
An AI system that proactively manages a user's life accumulates state — memory, context, inferred preferences, in-flight proactive actions — that is invisible unless engineers build a way to see it; without dedicated, owned internal tooling, debugging a bad proactive suggestion or a corrupted memory record means an engineer improvising a one-off script under incident pressure, with no guarantee it still works the next time it's needed. At 100M+ user scale, ad hoc internal tooling is also a support and security liability: an unowned admin dashboard with unclear access controls is exactly the kind of surface that leaks sensitive user data. This document exists to make internal tooling a deliberately built, owned, and maintained category of engineering work instead of an afterthought assembled during firefights.

## Approximate Page Count
6-8 pages

## Sections
1. **Internal Tooling Categories** — the taxonomy of internal tools the organization builds: admin/operations dashboards, debugging and incident-response tools, and data-inspection tools for AI systems (notably AI Memory).
2. **Build-vs-Buy Decision Criteria** — the explicit criteria for deciding whether an internal tooling need is met by an off-the-shelf product, a configured commercial platform, or a purpose-built internal tool, including the cost and differentiation thresholds that tip the decision.
3. **AI Memory & Context Inspection Tooling** — the specific requirements for tools that let engineers inspect, query, and (with appropriate controls) correct a user's stored memory and context state, cross-referencing the Memory System Architecture.
4. **Access Control & Audit Requirements** — the authentication, authorization, and audit-logging bar every internal tool must meet before it can touch production user data, given the financial and health-sensitive data the product handles.
5. **Ownership Model** — the default rule that the team whose domain a tool serves owns that tool's roadmap and on-call, versus the cases where a central Internal Tooling/Platform team owns cross-cutting tools used by every service team.
6. **Internal Tool Quality Bar** — the minimum standard (documentation, test coverage, incident-response readiness) an internal tool must meet before it is trusted for use during a live incident, distinguishing this bar from a disposable one-off script.
7. **Tool Discovery & Deprecation** — how engineers find out an internal tool already exists before building a duplicate, and the process for deprecating and retiring internal tools that have fallen out of use or out of date with the systems they inspect.
8. **Internal Tooling Roadmap & Prioritization** — how requests for new internal tools are collected, prioritized against feature-team roadmaps, and resourced, given that internal tooling rarely competes well against user-facing work without deliberate protection.
9. **Support Model** — the support commitment (response time, escalation path) for an internal tool when it breaks during an incident, distinct from customer-facing product support SLAs.

## Deliverables
* Internal tooling taxonomy and an inventory of existing tools mapped to owning team
* Documented build-vs-buy decision criteria with worked examples
* AI Memory/context inspection tool requirements, including access-control and audit specifications
* Ownership matrix assigning each tool category to a default owning team
* Internal tool quality bar checklist, distinguishing incident-ready tools from disposable scripts
* Internal tooling roadmap intake and prioritization process

## Dependencies
Requires the Memory System Architecture (Phase 5, Doc 7) for the data model that AI Memory inspection tooling must expose safely, and Privacy Architecture (Phase 6, Doc 13) for the access-control and audit requirements any tool touching user memory or context data must satisfy. Coordinates with Developer Experience (Phase 9, Doc 26) on the ownership boundary between environment/workflow tooling and product-specific internal tools, and with the On-Call and Productivity Metrics document group (Phase 9) for how debugging tooling is used during live incidents.

## Teams
Platform Engineering, AI Platform Team, Developer Experience (DevEx), Security/Privacy Engineering, Backend Service Teams

## Completion Criteria
- [ ] Internal tooling taxonomy published with an inventory of existing tools and their owning teams.
- [ ] Build-vs-buy criteria validated against at least two real historical tooling decisions.
- [ ] AI Memory inspection tool access-control and audit requirements reviewed and approved by Security/Privacy Engineering.
- [ ] Ownership matrix has no internal tool category left unassigned to an owning team.
- [ ] Tool discovery and deprecation process is documented and has a named point of entry (catalog or directory).
- [ ] Signed off by: VP Engineering (required), Head of Platform Engineering (required), Head of Security/Privacy Engineering (required for tools touching user data).
