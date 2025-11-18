# Java-Thread-Affinity - Repository TODO

**📋 Part of:** [Chronicle Architecture Documentation](../ARCH_TODO.md)
**Module Layer:** Layer 0 (Foundation)
**Priority:** 🟢 P3
**Last Updated:** 2025-11-18

## Purpose

This TODO file tracks work specific to Java-Thread-Affinity that feeds into the master [ARCH_TODO.md](../ARCH_TODO.md). It helps break down the architecture documentation work into manageable, repository-specific chunks.

## Related Main TODO Files

- [../ARCH_TODO.md](../ARCH_TODO.md) - Master architecture documentation roadmap
- [../TODO_INDEX.md](../TODO_INDEX.md) - Index of all TODO files
- [../ADOC_TODO.md](../ADOC_TODO.md) - AsciiDoc standardization (affects this module)

## Module Information for Architecture Overview

### Basic Information
- [x] **Module Name:** Java-Thread-Affinity
- [x] **Maven Artifact ID:** java-thread-affinity
- [x] **Primary Purpose:** Provide APIs to bind Java threads to specific CPU cores and query affinity, enabling low-latency, predictable scheduling on multi-core systems.
- [x] **Layer in Chronicle Stack:** Layer 0 (Foundation; CPU affinity and scheduling primitives)
- [x] **Dependencies (Chronicle modules):** `chronicle-test-framework` (test-only), shared `java-parent-pom` and `chronicle-quality-rules` for build-time configuration
- [x] **Key Classes/Interfaces:** `Affinity`, `AffinityLock`, `AffinityStrategies`, `AffinityThreadFactory`, `CpuLayout`

### ISO Alignment and Trust Zone

- [x] **Trust zone identified (Edge/Core/Foundation):** Java-Thread-Affinity is a *Foundation (Zone C)* module providing CPU affinity and low-level scheduling primitives consumed by other Chronicle components.
- [x] **Shared standards reviewed:** Review the shared architectural and security standards in `Chronicle-Quality-Rules/src/main/docs` and ensure affinity docs highlight its foundational role, performance characteristics and any relevant security/operational considerations.

### Architecture Information for ARCH_TODO.md Stage 3

**Feeds into:** ARCH_TODO.md Stage 3 - Module Deep Dives (ARCH-MOD-AFFINITY)

- [ ] **Core Abstractions:** [List primary abstractions this module provides]
- [ ] **Interactions with other modules:** [Which Chronicle modules does this use/integrate with?]
- [ ] **Typical use cases:** [List 2-3 common scenarios where this module is used]
- [ ] **Performance characteristics:** [Key performance metrics if applicable]
- [ ] **Design patterns used:** [e.g., flyweight, single writer, etc.]

### Existing Documentation Audit

- [ ] Check if `src/main/docs/architecture-overview.adoc` exists
  - [ ] If yes: Review quality (compare to Chronicle-Bytes standard)
  - [ ] If no: Note as gap for ARCH_TODO Stage 5.5
- [ ] Check if `src/main/docs/project-requirements.adoc` exists
  - [ ] If yes: Review for ARCH_TODO Stage 1.75 (Requirements Overview)
  - [ ] If no: Note as gap for FUNC_TODO.md
- [ ] Check if `src/main/docs/decision-log.adoc` exists
  - [ ] If yes: Review for ARCH_TODO Stage 1.85 (Decision Log Overview)
  - [ ] If no: Note as gap for DECISION_TODO.md
- [ ] Check if `README.adoc` provides good module overview
- [ ] Check if `AGENTS.md` exists and follows canonical template

### Documentation Gaps (for ARCH_TODO Stage 5.5)

**Missing Documentation:**
- [ ] Architecture overview? [Y/N]
- [ ] Requirements documentation? [Y/N]
- [ ] Decision log? [Y/N]
- [ ] Security review? [Y/N]
- [ ] Testing strategy? [Y/N]
- [ ] Performance targets? [Y/N]

**Documentation Quality Issues:**
- [ ] Missing `:toc:`, `:lang: en-GB`, or `:source-highlighter: rouge`?
- [ ] Manual section numbering instead of `:sectnums:`?
- [ ] Broken cross-references?
- [ ] Outdated information?

## Requirements for Architecture Overview (ARCH_TODO Stage 1.75)

**Feeds into:** Requirements Overview consolidation

- [ ] **Identify key functional requirements:** [List 3-5 most important]
- [ ] **Identify key non-functional requirements:**
  - [ ] Performance targets: [e.g., latency, throughput]
  - [ ] Security obligations: [e.g., bounds checking, input validation]
  - [ ] Operability requirements: [e.g., monitoring, logging]
- [ ] **Map requirements to architecture patterns:** [How do requirements drive design?]

## Decisions for Architecture Overview (ARCH_TODO Stage 1.85)

**Feeds into:** Decision Log Overview consolidation

- [ ] **Identify key architectural decisions:** [List 2-4 major decisions]
  - [ ] Decision ID (if in decision-log.adoc):
  - [ ] Brief description:
  - [ ] Rationale:
  - [ ] Alternatives considered:
- [ ] **Identify decision patterns used:**
  - [ ] Off-heap memory? [Y/N - explain]
  - [ ] Single writer principle? [Y/N - explain]
  - [ ] Reference counting? [Y/N - explain]
  - [ ] Flyweight pattern? [Y/N - explain]

## Glossary Terms (ARCH_TODO Stage 1.5)

**Feeds into:** Cross-module glossary

- [ ] **Module-specific terms to include in glossary:**
  - [ ] Term 1: [Definition]
  - [ ] Term 2: [Definition]
  - [ ] [Add more as needed]


## ISO 9001 Quality Management Considerations

**Reference:** [../COMPLIANCE_QUICK_REFERENCE.md](../COMPLIANCE_QUICK_REFERENCE.md)

### Design Inputs (ISO 9001 Clause 8.3.3)
- [ ] **Functional requirements documented?**
  - [ ] Location: `src/main/docs/project-requirements.adoc`
  - [ ] Requirements use Nine-Box taxonomy? (AFFINITY-FN-NNN)
  - [ ] Requirements are testable and verifiable?
- [ ] **Non-functional requirements documented?**
  - [ ] Performance requirements (AFFINITY-NF-P-NNN)
  - [ ] Security requirements (AFFINITY-NF-S-NNN)
  - [ ] Operability requirements (AFFINITY-NF-O-NNN)

### Design Outputs (ISO 9001 Clause 8.3.5)
- [ ] **Architecture documented?**
  - [ ] Location: `src/main/docs/architecture-overview.adoc`
  - [ ] Describes key components and their interactions?
  - [ ] Includes interface specifications?
- [ ] **APIs and interfaces specified?**
  - [ ] Public API documented (JavaDoc)?
  - [ ] Integration points with other modules described?

### Design Verification (ISO 9001 Clause 8.3.4)
- [ ] **Requirements traceable to tests?**
  - [ ] Test classes reference requirement IDs in comments/docs?
  - [ ] Coverage: What % of requirements have corresponding tests?
- [ ] **Test strategy documented?**
  - [ ] Unit test approach
  - [ ] Integration test approach
  - [ ] Performance test approach (if applicable)
- [ ] **Code review evidence?**
  - [ ] PR review process followed?
  - [ ] Review comments addressed?

### Design Changes (ISO 9001 Clause 8.3.4)
- [ ] **Architectural decisions documented?**
  - [ ] Location: `src/main/docs/decision-log.adoc`
  - [ ] Decisions include context, alternatives, rationale?
  - [ ] Impact of changes assessed?
- [ ] **Change history maintained?**
  - [ ] Git commit messages describe rationale?
  - [ ] Breaking changes documented in release notes?

## ISO 27001 Information Security Considerations

**Reference:** [../ARCHITECTURE_RESEARCH_GUIDE.md](../ARCHITECTURE_RESEARCH_GUIDE.md) - Security Research Topics

### Secure Coding (ISO 27001 Control A.8.28)
- [ ] **Input validation implemented?**
  - [ ] Where are untrusted inputs received? [List entry points]
  - [ ] How are malformed inputs handled?
  - [ ] Size limits enforced?
- [ ] **Bounds checking implemented?**
  - [ ] Buffer overflow prevention mechanisms?
  - [ ] Array access validation?
  - [ ] Off-heap memory bounds checked?
- [ ] **Static analysis performed?**
  - [ ] Checkstyle violations reviewed?
  - [ ] SpotBugs security patterns checked?
  - [ ] Suppressions justified and documented?

### Access Control (ISO 27001 Control A.8.3)
- [ ] **Access restrictions implemented?**
  - [ ] Are there authentication/authorization mechanisms? [Y/N]
  - [ ] If yes, where and how are they implemented?
  - [ ] Principle of least privilege followed?
- [ ] **Privileged operations identified?**
  - [ ] Which operations require elevated privileges?
  - [ ] How are they protected?

### Cryptographic Controls (ISO 27001 Control A.8.24)
- [ ] **Cryptography usage identified?**
  - [ ] Is encryption used? [Y/N - where?]
  - [ ] Is hashing used? [Y/N - which algorithms?]
  - [ ] Is TLS/SSL used? [Y/N - configuration?]
- [ ] **Key management?**
  - [ ] How are cryptographic keys managed?
  - [ ] Are keys hardcoded? [Y/N - if yes, flag as risk]

### Network Security (ISO 27001 Control A.8.22)
- [ ] **Network communication security?**
  - [ ] Does this module communicate over network? [Y/N]
  - [ ] If yes, is communication encrypted?
  - [ ] How are network endpoints authenticated?
- [ ] **Network configuration?**
  - [ ] Secure defaults configured?
  - [ ] Insecure protocols disabled?

### Vulnerability Management (ISO 27001 Control A.8.8)
- [ ] **Known vulnerabilities?**
  - [ ] Any open security issues in GitHub?
  - [ ] Any CVEs against dependencies?
- [ ] **Security testing?**
  - [ ] Fuzz testing performed?
  - [ ] Security-specific test cases?
  - [ ] Penetration testing performed?

### Security Documentation
- [ ] **Security review documented?**
  - [ ] Location: `src/main/docs/security-review.adoc`
  - [ ] Threat model documented?
  - [ ] Security controls described?
  - [ ] Known limitations documented?

## Improvement Tasks (ARCH_TODO Stage 5.5)

**Feeds into:** Improve Existing Module Documentation

### High Priority
- [ ] Create missing architecture-overview.adoc (if needed)
- [ ] Add missing front-matter to existing docs
- [ ] Fix broken cross-references
- [ ] Add `:sectnums:` where appropriate

### Medium Priority
- [ ] Expand brief architecture docs (if < 75 lines)
- [ ] Add "Trade-offs and Alternatives" section (following Chronicle-Bytes pattern)
- [ ] Add performance characteristics section
- [ ] Create decision log entries for undocumented decisions

### Low Priority
- [ ] Add diagrams (PlantUML or draw.io)
- [ ] Create example code snippets
- [ ] Expand requirements documentation
- [ ] Add cross-references to other module docs

## Code Quality Tasks

**Reference:** [../QUALITY_PLAYBOOK.md](../QUALITY_PLAYBOOK.md)

- [x] Run Checkstyle scan and document violations
  - Java 21 quality runs for the affinity modules (for example `verify-java-thread-affinity-java21-quality.log` and `Java-Thread-Affinity/affinity/verify-affinity-java21-quality-final2.log`) show `You have 0 Checkstyle violations.` for `affinity` and `affinity-test` under the shared configuration.
- [x] Run SpotBugs scan and document issues
  - After fixing the last two native/initialisation findings in `affinity` (see `verify-affinity-java21-quality-final2.log`), SpotBugs reports `BugInstance size is 0` and `No errors/warnings found` for the main affinity module; the `affinity-test` module is similarly clean under the shared rules.
- [x] Identify any code review follow-ups from CODE_REVIEW_STATUS.md
  - Java-Thread-Affinity now has an updated section in `CODE_REVIEW_STATUS.md` capturing the Java 21 quality status and native/JNA considerations; future review actions (for example around JNI safety or timing APIs) should be recorded there and referenced from this TODO.

## Notes

- 2025-11-18: The affinity modules are Checkstyle- and SpotBugs-clean on Java 21 (`verify-java-thread-affinity-java21-quality.log`, `Java-Thread-Affinity/affinity/verify-affinity-java21-quality-final2.log`). Remaining TODO items in this file (architecture/requirements/ISO checklists for the overall Java-Thread-Affinity project) are longer-running and tracked as deferred work in `TODO_STATUS.md`.

## Completion Checklist

Before marking this repository's contribution to ARCH_TODO as complete:

- [ ] All "Module Information" sections filled out
- [ ] Existing documentation audited
- [ ] Requirements identified for ARCH_TODO Stage 1.75
- [ ] Decisions identified for ARCH_TODO Stage 1.85
- [ ] Glossary terms identified for ARCH_TODO Stage 1.5
- [ ] Documentation gaps documented
- [ ] Improvement tasks prioritized
- [ ] Information contributed to relevant ARCH_TODO stages

---

**When complete, update:** [../ARCH_TODO.md](../ARCH_TODO.md) Stage 3 tracking matrix
