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

/**
 * The VersionHelper class provides methods for parsing, comparing, and representing version numbers.
 */
public class VersionHelper {
    private static final String DELIM = ".";
    private final int major;
    private final int minor;
    private final int release;

    /**
     * Constructs a VersionHelper instance with the specified major, minor, and release numbers.
     *
     * @param major_   the major version number
     * @param minor_   the minor version number
     * @param release_ the release version number
     */
    public VersionHelper(int major_, int minor_, int release_) {
        major = major_;
        minor = minor_;
        release = release_;
    }

    /**
     * Constructs a VersionHelper instance by parsing a version string.
     *
     * @param ver the version string to parse
     */
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

    /**
     * Returns a string representation of the version in the format "major.minor.release".
     *
     * @return a string representation of the version
     */
    @Override
    public String toString() {
        return major + DELIM + minor + DELIM + release;
    }

    /**
     * Checks if this VersionHelper is equal to another object.
     *
     * @param o the object to compare to
     * @return true if the objects are equal, false otherwise
     */
    @Override
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

    /**
     * Returns a hash code for this VersionHelper.
     *
     * @return a hash code for this VersionHelper
     */
    @Override
    public int hashCode() {
        return (major << 16) | (minor << 8) | release;
    }

    /**
     * Checks if the major and minor version numbers of this VersionHelper are equal to another VersionHelper.
     *
     * @param ver the VersionHelper to compare to
     * @return true if the major and minor version numbers are equal, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean majorMinorEquals(final VersionHelper ver) {
        return ver != null
                && this.major == ver.major
                && this.minor == ver.minor;
    }

    /**
     * Checks if this VersionHelper is the same or newer than another VersionHelper.
     *
     * @param ver the VersionHelper to compare to
     * @return true if this VersionHelper is the same or newer, false otherwise
     */
    public boolean isSameOrNewer(final VersionHelper ver) {
        return ver != null
                && (this.major > ver.major
                || this.major == ver.major
                && (this.minor > ver.minor
                || this.minor == ver.minor
                && this.release >= ver.release));
    }
}
