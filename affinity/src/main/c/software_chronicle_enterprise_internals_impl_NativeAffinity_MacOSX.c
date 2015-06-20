/*
 * Copyright 2011-2012 Peter Lawrey & Jerry Shea
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

#include <jni.h>
#include <mach/thread_policy.h>
#include <pthread.h>
#include "software_chronicle_enterprise_internals_impl_NativeAffinity.h"

/*
 * Class:     software_chronicle_enterprise_internals_impl_NativeAffinity
 * Method:    getAffinity0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_software_chronicle_enterprise_internals_impl_NativeAffinity_getAffinity0
  (JNIEnv *env, jclass c) {

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
    
    thread_port_t threadport = pthread_mach_thread_np(pthread_self());

    struct thread_enterprise_internals_policy policy;
    policy.affinity_tag = affinity;
    
    int rc = thread_policy_set(threadport,
         THREAD_AFFINITY_POLICY, (thread_policy_t)&policy,
         THREAD_AFFINITY_POLICY_COUNT);    
    if (rc != KERN_SUCCESS) {
        jclass ex = (*env)->FindClass(env, "java/lang/RuntimeException");
        char msg[100];
        sprintf(msg, "Bad return value from thread_policy_set: %d", rc);
        (*env)->ThrowNew(env, ex, msg);
    }
}
