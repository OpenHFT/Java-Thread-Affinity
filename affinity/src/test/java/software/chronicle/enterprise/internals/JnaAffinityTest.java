/*
 *
 *  *     Copyright (C) 2016  higherfrequencytrading.com
 *  *
 *  *     This program is free software: you can redistribute it and/or modify
 *  *     it under the terms of the GNU Lesser General Public License as published by
 *  *     the Free Software Foundation, either version 3 of the License.
 *  *
 *  *     This program is distributed in the hope that it will be useful,
 *  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *     GNU Lesser General Public License for more details.
 *  *
 *  *     You should have received a copy of the GNU Lesser General Public License
 *  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package software.chronicle.enterprise.internals;

import net.openhft.affinity.IAffinity;
import net.openhft.affinity.impl.LinuxJNAAffinity;
import net.openhft.affinity.impl.Utilities;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author peter.lawrey
 */
public class JnaAffinityTest {
    protected static final int CORES = Runtime.getRuntime().availableProcessors();
    protected static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(LinuxJNAAffinity.LOADED);
    }

    @Test
    public void getAffinityCompletesGracefully() {
        System.out.println("affinity: " + Utilities.toBinaryString(getImpl().getAffinity()));
    }

    @Test
    public void getAffinityReturnsValidValue() {
        final BitSet affinity = getImpl().getAffinity();
        assertTrue(
                "Affinity mask " + Utilities.toBinaryString(affinity) + " must be non-empty",
                !affinity.isEmpty()
        );
        final int allCoresMask = (1 << CORES) - 1;
        assertTrue(
                "Affinity mask " + Utilities.toBinaryString(affinity) + " must be <=(2^" + CORES + "-1 = " + allCoresMask + ")",
                affinity.length() <= CORES_MASK.length()
        );
    }

    @Test
    public void setAffinityCompletesGracefully() {
        BitSet affinity = new BitSet(1);
        affinity.set(0, true);
        getImpl().setAffinity(affinity);
    }

    @Test
    public void getAffinityReturnsValuePreviouslySet() {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Linux")) {
            System.out.println("Skipping Linux tests");
            return;
        }
        final IAffinity impl = LinuxJNAAffinity.INSTANCE;
        final int cores = CORES;
        for (int core = 0; core < cores; core++) {
            final BitSet mask = new BitSet();
            mask.set(core, true);
            getAffinityReturnsValuePreviouslySet(impl, mask);
        }
    }


    @Test
    public void showOtherIds() {
        System.out.println("processId: " + LinuxJNAAffinity.INSTANCE.getProcessId());
        System.out.println("threadId: " + LinuxJNAAffinity.INSTANCE.getThreadId());
        System.out.println("cpu: " + LinuxJNAAffinity.INSTANCE.getCpu());
    }

    private void getAffinityReturnsValuePreviouslySet(final IAffinity impl,
                                                      final BitSet mask) {

        impl.setAffinity(mask);
        final BitSet _mask = impl.getAffinity();
        assertEquals(mask, _mask);
    }

    @After
    public void tearDown() {
        getImpl().setAffinity(CORES_MASK);
    }

    public IAffinity getImpl() {
        return LinuxJNAAffinity.INSTANCE;
    }
}
