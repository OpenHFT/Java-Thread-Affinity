/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.affinity;

import java.util.*;
import java.util.function.IntPredicate;

/** Pure coarse-class preference. Maximum frequency is deliberately not a ranking input. */
final class PerformanceCpuOrder {
    final List<Integer> ids;
    final String reason;
    private PerformanceCpuOrder(List<Integer> ids, String reason) {
        this.ids = Collections.unmodifiableList(ids);
        this.reason = reason;
    }
    static PerformanceCpuOrder choose(List<Integer> legacy, CpuCapabilitySnapshot snapshot, IntPredicate freeCore) {
        return choose(legacy, snapshot, freeCore, NumaLocality.unknown());
    }
    static PerformanceCpuOrder choose(List<Integer> legacy, CpuCapabilitySnapshot snapshot,
                                      IntPredicate freeCore, NumaLocality locality) {
        List<Integer> eligible = new ArrayList<>();
        for (int id : legacy) if (snapshot.cpus().containsKey(id)) eligible.add(id);
        if (eligible.isEmpty()) return new PerformanceCpuOrder(eligible, "no_online_allowed_candidates");
        Set<CpuCapability.CoreType> classes = new HashSet<>();
        for (int id : eligible) classes.add(snapshot.cpus().get(id).coreType());
        boolean ranked = !classes.contains(CpuCapability.CoreType.UNKNOWN) && classes.size() > 1;
        String reason = classes.contains(CpuCapability.CoreType.UNKNOWN)
                ? "unknown_class" : "homogeneous";
        if (!ranked && !locality.known())
            return new PerformanceCpuOrder(eligible, "legacy_order_" + reason + "; numa_unknown");
        // Read availability once: a comparator must not observe a changing lock state.
        Map<Integer, Boolean> free = new HashMap<>();
        for (int id : eligible) free.put(id, !ranked || freeCore.test(id));
        eligible.sort(Comparator.comparingInt((Integer id) -> ranked
                && snapshot.cpus().get(id).coreType() == CpuCapability.CoreType.EFFICIENCY ? 1 : 0)
                .thenComparingInt(id -> locality.known() && !locality.contains(id) ? 1 : 0)
                .thenComparingInt(id -> free.get(id) ? 0 : 1));
        return new PerformanceCpuOrder(eligible, (ranked ? "performance_class" : reason)
                + "; cpu0_locality=" + locality.source + "; free_core_within_class_and_node");
    }
}
