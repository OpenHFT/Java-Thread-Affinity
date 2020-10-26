/*
 * Copyright 2016-2020 chronicle.software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.affinity.impl;

public class VersionHelper {
    private static final String DELIM = ".";
    private final int major;
    private final int minor;
    private final int release;

    public VersionHelper(int major_, int minor_, int release_) {
        major = major_;
        minor = minor_;
        release = release_;
    }

    public VersionHelper(String ver) {
        if (ver != null && (ver = ver.trim()).length() > 0) {
            final String[] parts = ver.split("\\.");
            major = parts.length > 0 ? Integer.parseInt(parts[0]) : 0;
            minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            release = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        } else {
            major = minor = release = 0;
        }
    }

    public String toString() {
        return major + DELIM + minor + DELIM + release;
    }

    public boolean equals(Object o) {
        if (o != null && (o instanceof VersionHelper)) {
            VersionHelper ver = (VersionHelper) o;
            return this.major == ver.major
                    && this.minor == ver.minor
                    && this.release == ver.release;

        } else {
            return false;
        }
    }

    public int hashCode() {
        return (major << 16) | (minor << 8) | release;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public boolean majorMinorEquals(final VersionHelper ver) {
        return ver != null
                && this.major == ver.major
                && this.minor == ver.minor;
    }

    public boolean isSameOrNewer(final VersionHelper ver) {
        return ver != null
                && (this.major > ver.major
                || this.major == ver.major
                && (this.minor > ver.minor
                || this.minor == ver.minor
                && this.release >= ver.release));
    }
}

