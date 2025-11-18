/**
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
#include <jni.h>
#include <mach/thread_policy.h>
#include <pthread.h>
#include <stdio.h>
#include "software_chronicle_enterprise_internals_impl_NativeAffinity.h"

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getAffinity0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getAffinity0
  (JNIEnv *env, jclass c) {
    (void)env;
    (void)c;

    thread_port_t threadport = pthread_mach_thread_np(pthread_self());

    struct thread_enterprise_internals_policy policy;
    policy.affinity_tag = 0;
    mach_msg_type_number_t count = THREAD_AFFINITY_POLICY_COUNT;
    boolean_t get_default = FALSE;
        
    if ((thread_policy_get(threadport,
         THREAD_AFFINITY_POLICY, (thread_policy_t)&policy,
         &count, &get_default)) != KERN_SUCCESS) {
        return ~0LL;
    }
      
    return (jlong) policy.affinity_tag;
}

/*
 * Class:     software_chronicle_enterprise_internals_NativeAffinity
 * Method:    setAffinity0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_setAffinity0
  (JNIEnv *env, jclass c, jlong affinity) {
    (void)c;
    
    thread_port_t threadport = pthread_mach_thread_np(pthread_self());

    struct thread_enterprise_internals_policy policy;
    policy.affinity_tag = affinity;
    
    int rc = thread_policy_set(threadport,
         THREAD_AFFINITY_POLICY, (thread_policy_t)&policy,
         THREAD_AFFINITY_POLICY_COUNT);    
    if (rc != KERN_SUCCESS) {
        jclass ex = (*env)->FindClass(env, "java/lang/RuntimeException");
        if (ex == NULL) {
            return; // Class not found, exception already pending
        }
        if ((*env)->ExceptionCheck(env)) {
            return; // Exception already pending
        }
        char msg[100];
        snprintf(msg, sizeof(msg), "Bad return value from thread_policy_set: %d", rc);
        (*env)->ThrowNew(env, ex, msg);
        if ((*env)->ExceptionCheck(env)) {
            return; // Exception already pending
        }
    }
}
