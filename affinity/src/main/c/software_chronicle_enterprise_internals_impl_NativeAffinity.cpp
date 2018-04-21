/* vim: syntax=cpp
 * Copyright 2011 Higher Frequency Trading
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <jni.h>
#include <sched.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <unistd.h>
#include <string.h>

#include "software_chronicle_enterprise_internals_impl_NativeAffinity.h"

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getAffinity0
 * Signature: ()J
 */
JNIEXPORT jbyteArray JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getAffinity0
  (JNIEnv *env, jclass c) 
{
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

    jbyteArray ret = env->NewByteArray(size);
    jbyte* bytes = env->GetByteArrayElements(ret, 0);
    memcpy(bytes, &mask, size);
    env->SetByteArrayRegion(ret, 0, size, bytes);

    return ret;
}

/*
 * Class:     software_chronicle_enterprise_internals_NativeAffinity
 * Method:    setAffinity0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_setAffinity0
  (JNIEnv *env, jclass c, jbyteArray affinity)
{
    cpu_set_t mask;
    const size_t size = sizeof(mask);
    CPU_ZERO(&mask);

    jbyte* bytes = env->GetByteArrayElements(affinity, 0);
    memcpy(&mask, bytes, size);

    sched_setaffinity(0, size, &mask);
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getProcessId0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getProcessId0
  (JNIEnv *env, jclass c) {
  return (jint) getpid();
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getThreadId0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getThreadId0
  (JNIEnv *env, jclass c) {
    return (jint) (pid_t) syscall (SYS_gettid);
}

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getCpu0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getCpu0
  (JNIEnv *env, jclass c) {
  return (jint) sched_getcpu();
}

