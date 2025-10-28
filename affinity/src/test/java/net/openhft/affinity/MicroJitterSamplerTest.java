/*
 * Copyright 2016-2025 chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MicroJitterSamplerTest {

    private static final Field COUNT_FIELD;
    private static final Field TOTAL_TIME_FIELD;
    private static final Method AS_STRING_METHOD;

    static {
        try {
            COUNT_FIELD = MicroJitterSampler.class.getDeclaredField("count");
            COUNT_FIELD.setAccessible(true);
            TOTAL_TIME_FIELD = MicroJitterSampler.class.getDeclaredField("totalTime");
            TOTAL_TIME_FIELD.setAccessible(true);
            AS_STRING_METHOD = MicroJitterSampler.class.getDeclaredMethod("asString", long.class);
            AS_STRING_METHOD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to access MicroJitterSampler internals for testing", e);
        }
    }

    @Test
    public void resetClearsCountsAndTotalTime() throws Exception {
        MicroJitterSampler sampler = new MicroJitterSampler();
        int[] counts = (int[]) COUNT_FIELD.get(sampler);
        Arrays.fill(counts, 7);
        TOTAL_TIME_FIELD.setLong(sampler, 42L);

        sampler.reset();

        assertArrayEquals("All jitter buckets should reset to zero after reset",
                new int[counts.length], counts);
        assertEquals("Total time should reset to zero", 0L, TOTAL_TIME_FIELD.getLong(sampler));
    }

    @Test
    public void sampleAccumulatesTotalTime() throws Exception {
        MicroJitterSampler sampler = new MicroJitterSampler();
        sampler.reset();

        sampler.sample(1_000L);
        sampler.sample(500L);

        assertEquals("Total sampled interval should accumulate nanos",
                1_500L, TOTAL_TIME_FIELD.getLong(sampler));
    }

    @Test
    public void printFormatsCountsPerHour() throws Exception {
        MicroJitterSampler sampler = new MicroJitterSampler();
        int[] counts = (int[]) COUNT_FIELD.get(sampler);
        Arrays.fill(counts, 0);
        counts[0] = 2;  // 2us bucket
        counts[10] = 1; // 60us bucket
        TOTAL_TIME_FIELD.setLong(sampler, 3_600_000_000_000L); // 1 hour in ns

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())) {
            sampler.print(ps);
        }

        String output = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        String lineSeparator = System.lineSeparator();
        String expected = "After 3600 seconds, the average per hour was" + lineSeparator +
                "2us\t2" + lineSeparator +
                "60us\t1" + lineSeparator + lineSeparator;
        assertEquals(expected, output);
    }

    @Test
    public void asStringConvertsUnits() throws Exception {
        assertEquals("999ns", AS_STRING_METHOD.invoke(null, 999L));
        assertEquals("2us", AS_STRING_METHOD.invoke(null, 2_000L));
        assertEquals("2ms", AS_STRING_METHOD.invoke(null, 2_000_000L));
        assertEquals("3sec", AS_STRING_METHOD.invoke(null, 3_000_000_000L));
    }
}
