/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.util.BitSet;

/** Strict parser for the bounded Linux CPU-list profile; never partially accepts a list. */
final class CpuIdList {
    static final int MAX_CPU_ID = 1_048_575;

    private CpuIdList() { }

    static BitSet parse(String text) {
        BitSet result = new BitSet();
        String value = text.trim();
        if (value.isEmpty()) return result;
        for (String token : value.split(",", -1)) {
            if (!token.matches("[0-9]+(-[0-9]+)?"))
                throw new IllegalArgumentException("invalid CPU list");
            String[] range = token.split("-");
            int first = Integer.parseInt(range[0]);
            int last = range.length == 1 ? first : Integer.parseInt(range[1]);
            if (first > last || last > MAX_CPU_ID)
                throw new IllegalArgumentException("CPU range outside supported bounds");
            result.set(first, last + 1);
        }
        return result;
    }
}
