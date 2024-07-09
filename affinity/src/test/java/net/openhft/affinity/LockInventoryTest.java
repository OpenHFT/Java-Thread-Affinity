package net.openhft.affinity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assume;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LockInventoryTest {

    private LockInventory lockInventory;

    @Before
    public void setUp() throws Exception {
        CpuLayout cpuLayout = createMockCpuLayout();
        lockInventory = new LockInventory(cpuLayout);

        // Initialize logicalCoreLocks and physicalCoreLocks with mock data for testing purposes
        Field logicalCoreLocksField = LockInventory.class.getDeclaredField("logicalCoreLocks");
        logicalCoreLocksField.setAccessible(true);
        AffinityLock[] logicalCoreLocks = new AffinityLock[4];
        for (int i = 0; i < 4; i++) {
            logicalCoreLocks[i] = createAffinityLock(i, false, false, lockInventory);
        }
        logicalCoreLocksField.set(lockInventory, logicalCoreLocks);

        Field physicalCoreLocksField = LockInventory.class.getDeclaredField("physicalCoreLocks");
        physicalCoreLocksField.setAccessible(true);
        NavigableMap<Integer, AffinityLock[]> physicalCoreLocks = new TreeMap<>();
        physicalCoreLocks.put(0, new AffinityLock[]{createAffinityLock(0, false, false, lockInventory), createAffinityLock(1, false, false, lockInventory)});
        physicalCoreLocks.put(1, new AffinityLock[]{createAffinityLock(2, false, false, lockInventory), createAffinityLock(3, false, false, lockInventory)});
        physicalCoreLocksField.set(lockInventory, physicalCoreLocks);
    }

    private CpuLayout createMockCpuLayout() {
        return new CpuLayout() {
            @Override
            public int cpus() {
                return 4;
            }

            @Override
            public int sockets() {
                return 1;
            }

            @Override
            public int coresPerSocket() {
                return 4;
            }

            @Override
            public int threadsPerCore() {
                return 1;
            }

            @Override
            public int socketId(int cpuId) {
                return 0;
            }

            @Override
            public int coreId(int cpuId) {
                return cpuId;
            }

            @Override
            public int threadId(int cpuId) {
                return 0;
            }
        };
    }

    private AffinityLock createAffinityLock(int cpuId, boolean base, boolean reserved, LockInventory lockInventory) throws Exception {
        Class<?> clazz = AffinityLock.class;
        Class<?>[] paramTypes = {int.class, boolean.class, boolean.class, LockInventory.class};
        Object[] params = {cpuId, base, reserved, lockInventory};
        return (AffinityLock) clazz.getDeclaredConstructor(paramTypes).newInstance(params);
    }

    @Test
    public void testTryAcquireLockSuccess() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int cpuId = 0;

        AffinityLock[] logicalCoreLocks = getLogicalCoreLocks();
        // Simulate the lock being available
        setAvailable(logicalCoreLocks[cpuId], true);

        AffinityLock result = lockInventory.tryAcquireLock(true, cpuId);
        Assert.assertNotNull(result);
        Assert.assertEquals(logicalCoreLocks[cpuId], result);
    }

    @Test
    public void testTryAcquireLockFailure() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int cpuId = 0;

        AffinityLock[] logicalCoreLocks = getLogicalCoreLocks();
        // Simulate the lock being unavailable
        setAvailable(logicalCoreLocks[cpuId], false);

        AffinityLock result = lockInventory.tryAcquireLock(true, cpuId);
        Assert.assertNull(result);
    }

    @Test
    public void testAcquireCoreSuccess() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int cpuId = 0;
        AffinityStrategy strategy = (cpuId1, cpuId2) -> cpuId1 == cpuId2;

        AffinityLock[] physicalCoreLocks = getPhysicalCoreLocks().get(0);
        // Simulate the lock being available
        setAvailable(physicalCoreLocks[0], true);

        AffinityLock result = lockInventory.acquireCore(true, cpuId, strategy);
        Assert.assertNotNull(result);
        Assert.assertEquals(physicalCoreLocks[0], result);
    }

    @Test
    public void testAcquireCoreFailure() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int cpuId = 0;
        AffinityStrategy strategy = (cpuId1, cpuId2) -> false;

        AffinityLock result = lockInventory.acquireCore(true, cpuId, strategy);
        Assert.assertNull(result);
    }

    @Test
    public void testBindWholeCore() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int logicalCoreID = 0;

        lockInventory.bindWholeCore(logicalCoreID);

        AffinityLock[] physicalCoreLocks = getPhysicalCoreLocks().get(0);
        for (AffinityLock al : physicalCoreLocks) {
            Assert.assertTrue(getBound(al));
            Assert.assertEquals(Thread.currentThread(), getAssignedThread(al));
        }
    }

    @Test
    public void testBindWholeCoreAlreadyBound() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int logicalCoreID = 0;

        AffinityLock[] physicalCoreLocks = getPhysicalCoreLocks().get(0);
        for (AffinityLock al : physicalCoreLocks) {
            al.assignCurrentThread(true, true);
        }

        lockInventory.bindWholeCore(logicalCoreID);

        for (AffinityLock al : physicalCoreLocks) {
            Assert.assertTrue(getBound(al));
            Assert.assertEquals(Thread.currentThread(), getAssignedThread(al));
        }
    }

    @Test
    public void testUpdateLockForCurrentThreadSuccess() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int cpuId = 0;

        AffinityLock al = createAffinityLock(cpuId, false, false, lockInventory);

        boolean result = invokeUpdateLockForCurrentThread(true, al, false);
        Assert.assertTrue(result);
    }

    @Test
    public void testUpdateLockForCurrentThreadFailure() throws Exception {
        Assume.assumeTrue("Skipping test on non-Linux OS", LockCheck.IS_LINUX);

        int cpuId = 0;

        AffinityLock al = createAffinityLock(cpuId, false, false, lockInventory);

        // Simulate IOException by setting an invalid CPU ID
        Field cpuIdField = AffinityLock.class.getDeclaredField("cpuId");
        cpuIdField.setAccessible(true);
        cpuIdField.set(al, Integer.MAX_VALUE);

        boolean result = invokeUpdateLockForCurrentThread(true, al, false);
        Assert.assertFalse(result);
    }

    private boolean invokeUpdateLockForCurrentThread(boolean bind, AffinityLock al, boolean wholeCore) throws Exception {
        Method method = LockInventory.class.getDeclaredMethod("updateLockForCurrentThread", boolean.class, AffinityLock.class, boolean.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, bind, al, wholeCore);
    }

    private AffinityLock[] getLogicalCoreLocks() throws Exception {
        Field field = LockInventory.class.getDeclaredField("logicalCoreLocks");
        field.setAccessible(true);
        return (AffinityLock[]) field.get(lockInventory);
    }

    private NavigableMap<Integer, AffinityLock[]> getPhysicalCoreLocks() throws Exception {
        Field field = LockInventory.class.getDeclaredField("physicalCoreLocks");
        field.setAccessible(true);
        return (NavigableMap<Integer, AffinityLock[]>) field.get(lockInventory);
    }

    private boolean getBound(AffinityLock al) throws Exception {
        Field field = AffinityLock.class.getDeclaredField("bound");
        field.setAccessible(true);
        return field.getBoolean(al);
    }

    private Thread getAssignedThread(AffinityLock al) throws Exception {
        Field field = AffinityLock.class.getDeclaredField("assignedThread");
        field.setAccessible(true);
        return (Thread) field.get(al);
    }

    private void setAvailable(AffinityLock al, boolean available) throws Exception {
        Field field = AffinityLock.class.getDeclaredField("available");
        field.setAccessible(true);
        field.setBoolean(al, available);
    }
}
