/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Abstractions for low-jitter time sources.
 *
 * <p>Defines a simple ticker interface used by affinity and related utilities to
 * obtain high-resolution timestamps without tying callers to a specific clock
 * implementation.
 */
package net.openhft.ticker;
