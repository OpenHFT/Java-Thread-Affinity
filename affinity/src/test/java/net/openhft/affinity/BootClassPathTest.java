/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BootClassPathTest {
    @Test
    public void shouldDetectClassesOnClassPath() {
        if (!System.getProperty("java.version").startsWith("1.8"))
            return;
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Thread"));
        assertTrue(BootClassPath.INSTANCE.has("java.lang.Runtime"));
    }
}