package software.chronicle.enterprise.internals.hotspot;

import software.chronicle.enterprise.internals.impl.NativeAffinity;

import java.lang.reflect.Executable;
import java.security.BasicPermission;

public class WBJvm {

  @SuppressWarnings("serial")
  public static class JvmPermission extends BasicPermission {
    public JvmPermission(String s) {
      super(s);
    }
  }

  private WBJvm() {}
  // trigger this to load.
  private static final NativeAffinity AFFINITY= NativeAffinity.INSTANCE;

  private static final WBJvm instance = new WBJvm();

  /**
   * Returns the singleton WBJvm instance.
   *
   * The returned WBJvm object should be carefully guarded
   * by the caller, since it can be used to read and write data
   * at arbitrary memory addresses. It must never be passed to
   * untrusted code.
   */
  public synchronized static WBJvm getWBJvm() {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkPermission(new JvmPermission("getInstance"));
    }
    return instance;
  }

  // Get the maximum heap size supporting COOPs
//  public native long getCompressedOopsMaxHeapSize();
  // Arguments
//  public native void printHeapSizes();


  // Compiler
  public native void    deoptimizeAll();
  public        boolean isMethodCompiled(Executable method) {
    return isMethodCompiled(method, false);
  }
  public native boolean isMethodCompiled(Executable method, boolean isOsr);
  public        boolean isMethodCompilable(Executable method) {
    return isMethodCompilable(method, -1 );
  }
  public        boolean isMethodCompilable(Executable method, int compLevel) {
    return isMethodCompilable(method, compLevel, false /*not osr*/);
  }
  public native boolean isMethodCompilable(Executable method, int compLevel, boolean isOsr);
  public native boolean isMethodQueuedForCompilation(Executable method);
  public        int     deoptimizeMethod(Executable method) {
    return deoptimizeMethod(method, false );
  }
  public native int     deoptimizeMethod(Executable method, boolean isOsr);
  public        void    makeMethodNotCompilable(Executable method) {
    makeMethodNotCompilable(method, -1);
  }
  public        void    makeMethodNotCompilable(Executable method, int compLevel) {
    makeMethodNotCompilable(method, compLevel, false);
  }
  public native void    makeMethodNotCompilable(Executable method, int compLevel, boolean isOsr);
  public        int     getMethodCompilationLevel(Executable method) {
    return getMethodCompilationLevel(method, false);
  }
  public native int     getMethodCompilationLevel(Executable method, boolean isOsr);
  public native boolean testSetDontInlineMethod(Executable method, boolean value);
  public        int     getCompileQueuesSize() {
    return getCompileQueueSize(-1);
  }
  public native int     getCompileQueueSize(int compLevel);
  public native boolean testSetForceInlineMethod(Executable method, boolean value);
  public        boolean enqueueMethodForCompilation(Executable method, int compLevel) {
    return enqueueMethodForCompilation(method, compLevel, -1 );
  }
  public native boolean enqueueMethodForCompilation(Executable method, int compLevel, int entry_bci);
  public native void    clearMethodState(Executable method);
  public native int     getMethodEntryBci(Executable method);
  public native Object[] getNMethod(Executable method, boolean isOsr);

  // force Young GC
  public native void youngGC();

  // force Full GC
  public native void fullGC();

  // CPU features
//  public native String getCPUFeatures();
}
