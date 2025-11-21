# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java-Thread-Affinity is a library that binds threads to specific CPU cores to improve performance, particularly on Linux systems. The library uses JNA (Java Native Access) to provide cross-platform thread affinity control with platform-specific implementations for Linux, Windows, macOS, and Solaris.

## Build Commands

```bash
# Build and run all tests
mvn clean verify

# Build without tests
mvn clean install -DskipTests

# Build only the affinity module
cd affinity && mvn clean verify

# Run a specific test class
mvn test -Dtest=AffinityLockTest

# Run a single test method
mvn test -Dtest=AffinityLockTest#testAcquireLock
```

Note: On Linux x86_64, the build will automatically compile native C code in `affinity/src/main/c/` during the `process-classes` phase. To skip native compilation, use `-DdontMake`.

## Module Structure

The project is a multi-module Maven build:

- **affinity**: Core library containing thread affinity APIs and platform-specific implementations
- **affinity-test**: Integration tests for the affinity module

## Architecture

### Platform Detection and Implementation Selection

The library uses a static initializer in `Affinity.java` that detects the OS at runtime and selects the appropriate `IAffinity` implementation:

- **Linux**: `LinuxJNAAffinity` (via JNA) - full affinity control, can get/set thread affinity, query CPU, get process/thread IDs
- **Windows**: `WindowsJNAAffinity` (via JNA) - thread affinity via kernel API, `getCpu()` returns -1
- **macOS**: `OSXJNAAffinity` (via JNA) - provides process/thread IDs only, no affinity modification
- **Solaris**: `SolarisJNAAffinity` (via JNA) - similar to macOS
- **Fallback**: `NullAffinity` - dummy implementation when JNA is unavailable

All implementations are in `affinity/src/main/java/net/openhft/affinity/impl/`.

### CPU Layout and Lock Management

The library builds a CPU topology model from `/proc/cpuinfo` (Linux) or assumes all CPUs are on one socket:

- `CpuLayout` interface represents CPU topology (cores, sockets, threads per core)
- `VanillaCpuLayout` parses `/proc/cpuinfo` to build the layout
- `NoCpuLayout` is used when CPU info is unavailable
- `LockInventory` tracks which threads hold locks on which CPUs

### AffinityLock Mechanism

`AffinityLock` is the main user-facing API that manages CPU reservations:

- Uses file-based locks in `java.io.tmpdir` (typically `/tmp/cpu-N.lock`) to coordinate between processes
- Supports try-with-resources pattern for automatic cleanup
- Provides strategies for CPU selection: `ANY`, `SAME_CORE`, `SAME_SOCKET`, `DIFFERENT_CORE`, `DIFFERENT_SOCKET`
- Can acquire locks by explicit CPU ID or string configuration ("last", "last-1", "any", "none", etc.)

The library distinguishes between:
- **BASE_AFFINITY**: CPUs available to the process on startup
- **RESERVED_AFFINITY**: CPUs reserved for thread affinity (isolated CPUs not in base affinity)

Use `-Daffinity.reserved={hex-mask}` to control which CPUs a process can reserve.

### Native Code

The `affinity/src/main/c/` directory contains:
- JNI implementations for higher-performance affinity operations (currently commented out in favour of JNA)
- Native clock implementation (`JNIClock.cpp`)
- Platform-specific code for macOS

## Key Classes

- `Affinity`: Static utility for low-level affinity operations (`getAffinity()`, `setAffinity()`, `getCpu()`, `getThreadId()`)
- `AffinityLock`: High-level API for acquiring CPU locks with automatic cleanup
- `AffinityThreadFactory`: Thread factory that automatically binds threads to CPUs based on affinity strategies
- `AffinityStrategies`: Enum of CPU selection strategies
- `CpuLayout`: Interface for CPU topology information

## Project Conventions (from AGENTS.md)

This project follows Chronicle Software standards:

- **Language**: British English spelling (organisation, licence, synchronised)
- **Character set**: ISO-8859-1 only, avoid Unicode/smart quotes
- **Javadoc**: Only document what isn't obvious from the signature - behavioural contracts, edge cases, thread safety, performance characteristics
- **Commit messages**: Subject <= 72 chars, imperative mood, reference issues, explain root cause -> fix -> impact
- **Testing**: Always run `mvn -q verify` before opening PRs
- **Documentation**: Keep .adoc files synchronised with code changes

## Common Development Patterns

### Acquiring CPU affinity in application code

```java
// Simple lock
try (AffinityLock lock = AffinityLock.acquireLock()) {
    // work pinned to a CPU
}

// Lock a whole core (avoids hyperthreading sibling)
try (AffinityLock lock = AffinityLock.acquireCore()) {
    // work
}

// Lock specific CPU
try (AffinityLock lock = AffinityLock.acquireLock(5)) {
    // runs on CPU 5
}

// Lock with strategy relative to another lock
try (AffinityLock lock1 = AffinityLock.acquireLock()) {
    try (AffinityLock lock2 = lock1.acquireLock(
            AffinityStrategies.SAME_SOCKET,
            AffinityStrategies.ANY)) {
        // lock2 prefers same socket as lock1
    }
}
```

### Using AffinityThreadFactory

```java
ExecutorService es = Executors.newFixedThreadPool(4,
    new AffinityThreadFactory("worker",
        AffinityStrategies.DIFFERENT_CORE,
        AffinityStrategies.ANY));
```

## Testing Notes

Tests assume the system has isolated CPUs configured via kernel command line (`isolcpus=...`). Without isolated CPUs, some tests may not behave as expected since the library prioritises assigning threads to CPUs not in BASE_AFFINITY.

To check current lock state: `AffinityLock.dumpLocks()` prints CPU assignments.
