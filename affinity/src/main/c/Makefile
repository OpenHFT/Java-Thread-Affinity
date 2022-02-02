#!/usr/bin/make -f
#
# Makefile for C code
#

# C sources to compile

TARGET_DIR := ../../../target/classes
TARGET := $(TARGET_DIR)/libCEInternals.so

WORKING_DIR := $(TARGET_DIR)/../jni

JAVA_CLASSES = software.chronicle.enterprise.internals.impl.NativeAffinity net.openhft.ticker.impl.JNIClock

JNI_STUBS := $(subst .,_,$(JAVA_CLASSES))
JNI_SOURCES := $(patsubst %,%.cpp,$(JNI_STUBS))

JAVA_BUILD_DIR := $(TARGET_DIR)

JAVA_HOME ?= /usr/java/default
JAVA_LIB := $(JAVA_HOME)/jre/lib
JVM_SHARED_LIBS := -L$(JAVA_LIB)/amd64/server -L$(JAVA_LIB)/i386/server  -L$(JAVA_LIB)/amd64/jrockit/ -L$(JAVA_LIB)/i386/jrockit/ -L$(JAVA_LIB)/ppc64le/server -L$(JAVA_LIB)/ppc64le/jrockit/ -L$(JAVA_HOME)/lib/server

CXX=g++
INCLUDES := -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -I $(WORKING_DIR)

# classpath for javah
ifdef CLASSPATH
JAVAH_CLASSPATH = $(JAVA_BUILD_DIR):$(CLASSPATH)
else
JAVAH_CLASSPATH = $(JAVA_BUILD_DIR)
endif

.PHONY : clean

all: $(TARGET)

$(TARGET): $(JNI_SOURCES)
	$(CXX) -O3 -Wall -shared -fPIC $(JVM_SHARED_LIBS) -ljvm -lrt $(INCLUDES) $(JNI_SOURCES) -o $(TARGET)

clean:
	rm $(TARGET)
