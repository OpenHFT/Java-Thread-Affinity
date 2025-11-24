# Java Thread Affinity AGENTS

- Follow repository `AGENTS.md` for base rules; this file adds module specifics. Durable docs live in `src/main/docs/` with the landing page at `README.adoc`.
- Module purpose: expose cross-platform thread affinity APIs and helpers (`Affinity`, `AffinityLock`) to bind threads to CPU cores and manage reservations.
- Build commands: full build `mvn -q clean verify`; module-only without tests `mvn -pl affinity -am -DskipTests install`.
- Quality gates: keep Checkstyle/SpotBugs clean; avoid unsafe native calls; ensure affinity operations fail fast on unsupported platforms; include tests for Linux/Windows/macOS guards.
- Documentation: maintain Nine-Box IDs in `src/main/docs/project-requirements.adoc` and link decisions/tests to them; British English, ASCII/ISO-8859-1, `:source-highlighter: rouge`.
- Guardrails: affinity changes can be platform-specific; keep defaults safe when capabilities are absent; document any new system properties or JNI/JNA dependency changes in the docs.
