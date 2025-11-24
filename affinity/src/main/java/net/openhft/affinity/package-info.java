/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Thread affinity utilities for pinning and coordinating CPU usage.
 *
 * <p>The public API here exposes strategies for binding threads to cores,
 * acquiring {@link net.openhft.affinity.AffinityLock}s, and inspecting CPU
 * layouts to deliver predictable latency on supported platforms.
 */
package net.openhft.affinity;
