/*
 *
 *  *     Copyright (C) ${YEAR}  higherfrequencytrading.com
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
import org.junit.*;
import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author peter.lawrey
 */
public class NativeAffinityTest {
    protected static final int CORES = Runtime.getRuntime().availableProcessors();
    protected static final BitSet CORES_MASK = new BitSet(CORES);

    static {
        CORES_MASK.set(0, CORES, true);
    }

    @BeforeClass
    public static void checkJniLibraryPresent() {
        Assume.assumeTrue(NativeAffinity.LOADED);
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
    @Ignore("TODO AFFINITY-25")
    public void getAffinityReturnsValuePreviouslySet() {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Linux")) {
            System.out.println("Skipping Linux tests");
            return;
        }
        final IAffinity impl = NativeAffinity.INSTANCE;
        final int cores = CORES;
        for (int core = 0; core < cores; core++) {
            final BitSet mask = new BitSet();
            mask.set(core, true);
            getAffinityReturnsValuePreviouslySet(impl, mask);
        }
    }

    @Test
    @Ignore("TODO AFFINITY-25")
    public void JNAwithJNI() {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Linux")) {
            System.out.println("Skipping Linux tests");
            return;
        }
        int nbits = Runtime.getRuntime().availableProcessors();
        BitSet affinity = new BitSet(nbits);
        affinity.set(1);
        NativeAffinity.INSTANCE.setAffinity(affinity);
        BitSet affinity2 = LinuxJNAAffinity.INSTANCE.getAffinity();
        assertEquals(1, NativeAffinity.INSTANCE.getCpu());
        assertEquals(affinity, affinity2);

        affinity.clear();
        affinity.set(2);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
        BitSet affinity3 = NativeAffinity.INSTANCE.getAffinity();
        assertEquals(2, LinuxJNAAffinity.INSTANCE.getCpu());
        assertEquals(affinity, affinity3);

        affinity.set(0, nbits);
        LinuxJNAAffinity.INSTANCE.setAffinity(affinity);
    }

    @Test
    public void showOtherIds() {
        System.out.println("processId: " + NativeAffinity.INSTANCE.getProcessId());
        System.out.println("threadId: " + NativeAffinity.INSTANCE.getThreadId());
        System.out.println("cpu: " + NativeAffinity.INSTANCE.getCpu());
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
        return NativeAffinity.INSTANCE;
    }
}
