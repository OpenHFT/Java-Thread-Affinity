#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    registerNatives
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_registerNatives
  (JNIEnv *env, jobject obj) {

  }

void WB_DeoptimizeAll(JNIEnv* env, jobject o);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    deoptimizeAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_deoptimizeAll
  (JNIEnv *env, jobject obj) {
  WB_DeoptimizeAll(env, obj);
}

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    isMethodCompiled
 * Signature: (Ljava/lang/reflect/Executable;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_isMethodCompiled
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    isMethodCompilable
 * Signature: (Ljava/lang/reflect/Executable;IZ)Z
 */
JNIEXPORT jboolean JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_isMethodCompilable
  (JNIEnv *, jobject, jobject, jint, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    isMethodQueuedForCompilation
 * Signature: (Ljava/lang/reflect/Executable;)Z
 */
JNIEXPORT jboolean JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_isMethodQueuedForCompilation
  (JNIEnv *, jobject, jobject);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    deoptimizeMethod
 * Signature: (Ljava/lang/reflect/Executable;Z)I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_deoptimizeMethod
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    makeMethodNotCompilable
 * Signature: (Ljava/lang/reflect/Executable;IZ)V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_makeMethodNotCompilable
  (JNIEnv *, jobject, jobject, jint, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    getMethodCompilationLevel
 * Signature: (Ljava/lang/reflect/Executable;Z)I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_getMethodCompilationLevel
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    testSetDontInlineMethod
 * Signature: (Ljava/lang/reflect/Executable;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_testSetDontInlineMethod
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    getCompileQueueSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_getCompileQueueSize
  (JNIEnv *, jobject, jint);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    testSetForceInlineMethod
 * Signature: (Ljava/lang/reflect/Executable;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_testSetForceInlineMethod
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    enqueueMethodForCompilation
 * Signature: (Ljava/lang/reflect/Executable;II)Z
 */
JNIEXPORT jboolean JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_enqueueMethodForCompilation
  (JNIEnv *, jobject, jobject, jint, jint);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    clearMethodState
 * Signature: (Ljava/lang/reflect/Executable;)V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_clearMethodState
  (JNIEnv *, jobject, jobject);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    getMethodEntryBci
 * Signature: (Ljava/lang/reflect/Executable;)I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_getMethodEntryBci
  (JNIEnv *, jobject, jobject);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    getNMethod
 * Signature: (Ljava/lang/reflect/Executable;Z)[Ljava/lang/Object;
 */
JNIEXPORT jobjectArray JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_getNMethod
  (JNIEnv *, jobject, jobject, jboolean);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    youngGC
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_youngGC
  (JNIEnv *, jobject);

/*
 * Class:     software_chronicle_enterprise_internals_hotspot_WBJvm
 * Method:    fullGC
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_hotspot_WBJvm_fullGC
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
