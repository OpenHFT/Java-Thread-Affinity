Thread Affinity
=============

Lets you bind a thread to a given core, this can improve performance (this library works best on linux).


OpenHFT Java Thread Affinity library

See [affinity/src/test/java](https://github.com/OpenHFT/Java-Thread-Affinity/tree/master/affinity/src/test/java) 
for working examples of how to use this library.

[Thread Affinity usage Heatmap](http://jrvis.com/red-dwarf/?user=openhft&repo=Java-Thread-Affinity)

## Changes

   - V3.1.1 - Upgraded JNA dependency to 4.4.0
   - V2.0.1 - Added getThreadId for the process if of the thread.

## Dependencies

Java-Thread-Affinity will try to use [JNA](https://github.com/java-native-access/jna)
to provide access to native thread-handling functions. JNA should be installed on
your system to get the most from this library.

### JNA version

Java-Thread-Affinity currently depends on JNA version 4.4.0, which in turn
depends on a version of GLIBC >= 2.14. If your operating system is an old one,
with a version of GLIBC released before 2011, this library will not be able to 
invoke native functions.

To work around this problem, fork the repository, and override the `<version>` tag
for the artifacts `jna` and `jna-platform` in the project's `pom` file.

### Installing JNA on Ubuntu


    sudo apt-get install libjna-java


### Installing JNA on CentOS

    sudo yum install jna


## How does CPU allocation work?
The library will read your `/proc/cpuinfo` if you have one or provide one and it will determine your CPU layout.  If you don't have one it will assume every CPU is on one CPU socket.

The library looks for isolated CPUs determined by looking at the CPUs you are not running on by default. 
i.e. if you have 16 CPUs but 8 of them are not available for general use (as determined by the affinity of the process on startup) it will start assigning to those CPUs.

Note: if you have more than one process using this library you need to specify which CPUs the process can use otherwise it will assign the same CPUs to both processes.
To control which CPUs a process can use, add -Daffinity.reserved={cpu-mask-in-hex} to the command line of the process.

Note: the CPU 0 is reserved for the Operating System, it has to run somewhere.


## References

https://github.com/peter-lawrey/Java-Thread-Affinity/wiki/Getting-started

https://github.com/peter-lawrey/Java-Thread-Affinity/wiki/How-it-works

http://vanillajava.blogspot.co.uk/2013/07/micro-jitter-busy-waiting-and-binding.html


## isolcpus

Java-Thread-Affinity requires that you first isolate some CPU's for its to use.

Once a CPU core is isolated, the Linux scheduler will not use the CPU core to run any user-space processes. The isolated CPUs will not participate in load balancing, and will not have tasks running on them unless explicitly assigned. You can manually assign processes to be run on the isolated CPU cores using taskset.

To isolate the 1st and 3rd CPU cores (CPU numbers start from 0) on your system, add the following to the kernel command line during boot:

isolcpus=1,3


## Acquiring a CPU lock for a thread 
You can acquire a lock for a CPU in the following matter

In Java 6

    AffinityLock al = AffinityLock.acquireLock();
    try {
         // do some work locked to a CPU.
    } finally {
         al.release();
    }

In Java 7 or 8

    try (AffinityLock al = AffinityLock.acquireLock()) {
        // do some work while locked to a CPU.
    }

You have further options such as

## Acquiring a CORE lock for a thread
You can reserve a whole core.  If you have hyper-threading enabled, this will use one CPU and leave it's twin CPU unused.

    try (AffinityLock al = AffinityLock.acquireCore()) {
        // do some work while locked to a CPU.
    }

## Controlling layout
You can chose a layout relative to an existing lock.

    try (final AffinityLock al = AffinityLock.acquireLock()) {
        System.out.println("Main locked");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try (AffinityLock al2 = al.acquireLock(AffinityStrategies.SAME_SOCKET,
                                                       AffinityStrategies.ANY)) {
                     System.out.println("Thread-0 locked");
                }
            }
        });
        t.start();
    }
    
In this example, the library will prefer a free CPU on the same Socket as the first thread, otherwise it will pick any free CPU. 

## Getting the thread id.
You can get the current thread id using

    int threadId = AffinitySupport.getThreadId();

## Determining which CPU you are running on.
You can get the current CPU being used by

    int cpuId = AffinitySupport.getCpu();

## Controlling the affinity more directly.
The affinity of the process on start up is

   long baseAffinity = AffinityLock.BASE_AFFINITY;
   
The available CPU for reservation is

   long reservedAffinity = AffinityLock.RESERVED_AFFINITY;
    
If you want to get/set the affinity directly you can do
 
   long currentAffinity = AffinitySupport.getAffinity();
   AffinitySupport.setAffinity(1L << 5); // lock to CPU 5.

# Support Material

[Java Thread Affinity support group](https://groups.google.com/forum/?hl=en-GB#!forum/java-thread-affinity)

For an article on how much difference affinity can make and how to use it http://vanillajava.blogspot.com/2013/07/micro-jitter-busy-waiting-and-binding.html

# Questions and Answers

## Question
I am currently working on a project related to deadlock detection in multithreaded programs in java. We are trying to run threads on different processors and thus came across your github posts regarding the same. https://github.com/peter-lawrey/Java-Thread-Affinity/wiki/Getting-started
Being a beginner, I have little knowledge and thus need your assistance. We need to know how to run threads on specified cpu number and then switch threads when one is waiting. 

## Answer

Use :

``` java
AffinityLock.setAffinity (1L << n);
```

where n is the cpu you want to run the thread on.



