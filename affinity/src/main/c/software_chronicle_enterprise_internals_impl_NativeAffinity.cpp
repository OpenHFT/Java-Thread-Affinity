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
  #include <errno.h>
  #include <stdio.h>
#endif
#include "software_chronicle_enterprise_internals_impl_NativeAffinity.h"

#ifndef __linux__
static void throwUnsupportedOperation(JNIEnv *env, const char *message) {
    jclass exClass = env->FindClass("java/lang/UnsupportedOperationException");
    if (exClass != NULL) {
        env->ThrowNew(exClass, message);
    }
}
#endif

#ifdef __linux__
static void throwRuntimeException(JNIEnv *env, const char *message) {
    jclass exClass = env->FindClass("java/lang/RuntimeException");
    if (exClass != NULL) {
        env->ThrowNew(exClass, message);
    }
}
#endif

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getAffinity0
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getAffinity0
  (JNIEnv *env, jclass c)
{
#ifdef __linux__
    cpu_set_t mask;
    const size_t size = sizeof(mask);

    int res = sched_getaffinity(0, size, &mask);
    if (res < 0)
    {
        return NULL;
    }

    jbyteArray ret = env->NewByteArray((jsize) size);
    if (ret == NULL) {
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
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    setAffinity0
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_setAffinity0
  (JNIEnv *env, jclass c, jbyteArray affinity)
{
#ifdef __linux__
    cpu_set_t mask;
    const size_t size = sizeof(mask);
    CPU_ZERO(&mask);

    jsize length = env->GetArrayLength(affinity);
    if (length > 0) {
        jsize copyLength = length < (jsize) size ? length : (jsize) size;
        env->GetByteArrayRegion(affinity, 0, copyLength, (jbyte *) &mask);
        if (env->ExceptionCheck()) {
            return;
        }
    }

    int res = sched_setaffinity(0, size, &mask);
    if (res != 0) {
        const int error = errno;
        char message[256];
        snprintf(message, sizeof(message),
                 "sched_setaffinity(thread=0, maskBytes=%d) failed: errno=%d (%s)",
                 (int) length, error, strerror(error));
        throwRuntimeException(env, message);
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
#ifdef __linux__
    return (jint) getpid();
#else
    throwUnsupportedOperation(env, "NativeAffinity.getProcessId0 is only supported on Linux");
    return (jint) -1;
#endif
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getThreadId0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getThreadId0
  (JNIEnv *env, jclass c) {
#ifdef __linux__
    return (jint) (pid_t) syscall (SYS_gettid);
#else
    throwUnsupportedOperation(env, "NativeAffinity.getThreadId0 is only supported on Linux");
    return (jint) -1;
#endif
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getCpu0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getCpu0
  (JNIEnv *env, jclass c) {
#ifdef __linux__
    return (jint) sched_getcpu();
#else
    throwUnsupportedOperation(env, "NativeAffinity.getCpu0 is only supported on Linux");
    return (jint) -1;
#endif
}
