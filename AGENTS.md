# AGENTS.md

## Scope
- Java-Thread-Affinity provides affinity management with Java and native components.

## Build and test
- Preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Module-scoped example:
  - `mvn -pl affinity -am test -l logs/mvn-affinity-test.log`
  - `mvn -pl affinity-test -am test -l logs/mvn-affinity-osgi-test.log`
- Native build note:
  - `make` runs via the `make-c` profile from `affinity/src/main/c` on Linux non-arm.
  - Use `-DdontMake` to skip native compilation when needed.
- Review logs:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- Do not commit logs/.

## Repo map
- Java sources: `affinity/src/main/java`, `affinity-test/src/main/java`.
- Native sources: `affinity/src/main/c`.
- Tests: `affinity/src/test/java`, `affinity-test/src/test/java`.

## Constraints
- Java baseline: 8 (avoid newer language features).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Preserve public APIs and configuration strings used for affinity selection.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation on hot paths.
- Some tests require Linux `/proc/cpuinfo` and will skip when unavailable; do not remove the skip guards.

## Docs and review checklist
- Update examples or README sections when public behaviour changes.
- For large mechanical changes, declare the transformation rule and keep it consistent.

## References
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging and decision record templates.
