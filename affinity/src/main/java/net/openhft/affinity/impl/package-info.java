/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: LicenseRef-Proprietary
 */
/**
 * Platform-specific implementations backing Chronicle thread affinity.
 *
 * <p>Contains OS- and JNA-based helpers for querying CPU layouts and binding
 * threads, used internally by the public affinity API.
 */
package net.openhft.affinity.impl;
