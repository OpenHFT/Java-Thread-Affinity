/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.affinity.impl;


import net.openhft.affinity.IAffinity;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author peter.lawrey
 */
public enum NullAffinity implements IAffinity {
    INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(NullAffinity.class.getName());

    @Override
    public long getAffinity() {
        return -1;
    }

    @Override
    public void setAffinity(final long affinity) {
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("unable to set mask to " + Long.toHexString(affinity) + " as the JNIa nd JNA libraries and not loaded");
    }

    @Override
    public int getCpu() {
        return -1;
    }

    @Override
    public int getProcessId() {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(name.split("@")[0]);
    }

    @Override
    public int getThreadId() {
        throw new UnsupportedOperationException();
    }


}
