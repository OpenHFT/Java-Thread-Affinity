/* vim: syntax=cpp
 * Copyright 2015 Higher Frequency Trading
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
#include "net_openhft_ticker_impl_JNIClock.h"

#if defined(__i386__)
static __inline__ unsigned long long rdtsc(void) {
  unsigned long long int x;
     __asm__ volatile (".byte 0x0f, 0x31" : "=A" (x));
     return x;
}

#elif defined(__x86_64__)
static __inline__ unsigned long long rdtsc(void) {
  unsigned hi, lo;
  __asm__ __volatile__ ("rdtsc" : "=a"(lo), "=d"(hi));
  return ( (unsigned long long)lo)|( ((unsigned long long)hi)<<32 );
}

#elif defined(__MIPS_32__)
#define rdtsc(dest) \
   _ _asm_ _ _ _volatile_ _("mfc0 %0,$9; nop" : "=r" (dest))

#elif defined(__MIPS_SGI__)
#include <time.h>

static __inline__ unsigned long long rdtsc (void) {
  struct timespec tp;
  clock_gettime (CLOCK_SGI_CYCLE, &tp);
  return (unsigned long long)(tp.tv_sec * (unsigned long long)1000000000) + (unsigned long long)tp.tv_nsec;
}
#elif defined(__PPC64__)
unsigned long long rdtsc(){
  unsigned long long rval;
  __asm__ __volatile__("mfspr %%r3, 268": "=r" (rval));
  return rval;
}
#endif

/*
 * Class:     net_openhft_clock_impl_JNIClock
 * Method:    rdtsc0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_net_openhft_ticker_impl_JNIClock_rdtsc0
   (JNIEnv *env, jclass c) {
   return (jlong) rdtsc();
}
