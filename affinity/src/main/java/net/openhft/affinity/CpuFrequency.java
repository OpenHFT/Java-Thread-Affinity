/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.OptionalLong;

/** Optional CPUFreq hardware maximum; never substitutes a governor cap or current MHz. */
final class CpuFrequency {
    private CpuFrequency() { }

    static OptionalLong readMaximum(Path path, List<String> diagnostics) {
        try {
            String text = new String(Files.readAllBytes(path), StandardCharsets.US_ASCII).trim();
            if (!text.matches("[0-9]+")) throw new IllegalArgumentException("invalid frequency");
            long value = Long.parseLong(text);
            if (value <= 0) throw new IllegalArgumentException("non-positive frequency");
            return OptionalLong.of(value);
        } catch (NoSuchFileException unavailable) {
            // CPUFreq is optional; absence is represented by the empty value and attempted source.
            return OptionalLong.empty();
        } catch (IOException | SecurityException | IllegalArgumentException invalid) {
            diagnostics.add(path + ": unavailable or invalid maximum frequency (" + invalid.getClass().getSimpleName() + ")");
            return OptionalLong.empty();
        }
    }
}
