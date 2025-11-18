/**
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <jni.h>
#ifdef __linux__
  #include <sched.h>
  #include <sys/syscall.h>
  #include <sys/types.h>
  #include <unistd.h>
  #include <string.h>
#endif
#include "software_chronicle_enterprise_internals_impl_NativeAffinity.h"

#ifndef __linux__
static void throwUnsupportedOperation(JNIEnv *env, const char *message) {
    jclass exClass = env->FindClass("java/lang/UnsupportedOperationException");
    if (exClass == NULL) {
        return; // Class not found, exception already pending
    }
    env->ThrowNew(exClass, message);
    if (env->ExceptionCheck()) {
        return; // Exception already pending
    }
}
#endif

static void throwRuntimeException(JNIEnv *env, const char *message) {
    jclass exClass = env->FindClass("java/lang/RuntimeException");
    if (exClass == NULL) {
        return; // Class not found, exception already pending
    }
    env->ThrowNew(exClass, message);
    if (env->ExceptionCheck()) {
        return; // Exception already pending
    }
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getVersion0
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getVersion0
  (JNIEnv *env, jclass c)
{
    (void)c;
    return env->NewStringUTF(PROJECT_VERSION);
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getAffinity0
 * Signature: ()J
 */
JNIEXPORT jbyteArray JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getAffinity0
  (JNIEnv *env, jclass c) 
{
    (void)c;
#ifdef __linux__
    // The default size of the structure supports 1024 CPUs, should be enough
    // for now In the future we can use dynamic sets, which can support more
    // CPUs, given OS can handle them as well
    cpu_set_t mask;
    const size_t size = sizeof(mask);

    int res = sched_getaffinity(0, size, &mask);
    if (res < 0)
    {
        return NULL;
    }

    jbyteArray ret = env->NewByteArray((jsize) size);
    if (ret == NULL) {
        // OutOfMemoryError already pending
        return NULL;
    }
    env->SetByteArrayRegion(ret, 0, (jsize) size, (const jbyte *) &mask);

    return ret;
#else
    throwUnsupportedOperation(env, "NativeAffinity.getAffinity0 is only supported on Linux");
    return NULL;
#endif
}

/*
 * Class:     software_chronicle_enterprise_internals_NativeAffinity
 * Method:    setAffinity0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_setAffinity0
  (JNIEnv *env, jclass c, jbyteArray affinity)
{
    (void)c;
#ifdef __linux__
    cpu_set_t mask;
    const size_t size = sizeof(mask);
    CPU_ZERO(&mask);

    jsize length = env->GetArrayLength(affinity);
    if (length > 0) {
        jsize copyLength = length < (jsize) size ? length : (jsize) size;
        env->GetByteArrayRegion(affinity, 0, copyLength, (jbyte *) &mask);
    }

    int res = sched_setaffinity(0, size, &mask);
    if (res != 0) {
        throwRuntimeException(env, "sched_setaffinity failed");
    }
#else
    throwUnsupportedOperation(env, "NativeAffinity.setAffinity0 is only supported on Linux");
#endif
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getProcessId0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getProcessId0
  (JNIEnv *env, jclass c) {
    (void)env;
    (void)c;
#ifndef __linux__
    throwUnsupportedOperation(env, "NativeAffinity.getProcessId0 is only supported on Linux");
    return (jint) -1;
#else
      
  return (jint) getpid();
#endif
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getThreadId0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getThreadId0
  (JNIEnv *env, jclass c) {
    (void)env;
    (void)c;
#ifndef __linux__
    throwUnsupportedOperation(env, "NativeAffinity.getThreadId0 is only supported on Linux");
    return (jint) -1;
#else
      
    return (jint) (pid_t) syscall (SYS_gettid);
#endif
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getCpu0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getCpu0
  (JNIEnv *env, jclass c) {
    (void)env;
    (void)c;
#ifndef __linux__
    throwUnsupportedOperation(env, "NativeAffinity.getCpu0 is only supported on Linux");
    return (jint) -1;
#else
      
  return (jint) sched_getcpu();
#endif
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    (void)vm;
    (void)reserved;
    return JNI_VERSION_1_8;
}
