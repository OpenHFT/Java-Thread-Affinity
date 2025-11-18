# Makefile for C code
#

# C sources to compile

TARGET_DIR := ../../../target/classes
TARGET := $(TARGET_DIR)/libCEInternals.so

WORKING_DIR := $(TARGET_DIR)/../jni

# Default version if not provided
VERSION ?= unknown

JNI_OS := win32
UNAME_S:= $(shell uname -s)
ifeq ($(UNAME_S), Linux)
    JNI_OS := linux
    LRT := -lrt
endif
ifeq ($(UNAME_S), Darwin)
    JNI_OS := darwin
endif

CC=gcc
CXX=g++

CXXFLAGS ?= -O3 -Wall -Werror -Wextra -Wconversion -fstack-protector-strong -D_FORTIFY_SOURCE=2 -fPIE -pie -DPROJECT_VERSION=\"$(VERSION)\"
CFLAGS ?= -O3 -Wall -Werror -Wextra -Wconversion -fstack-protector-strong -D_FORTIFY_SOURCE=2 -fPIE -pie -DPROJECT_VERSION=\"$(VERSION)\"

INCLUDES := -I"$(JAVA_HOME)/../include" -I"$(JAVA_HOME)/../include/$(JNI_OS)" -I"$(WORKING_DIR)"

# All native source files
NATIVE_CPP_SOURCES := software_chronicle_enterprise_internals_impl_NativeAffinity.cpp net_openhft_ticker_impl_JNIClock.cpp
NATIVE_C_SOURCES :=
ifeq ($(UNAME_S), Darwin)
    NATIVE_C_SOURCES := software_chronicle_enterprise_internals_impl_NativeAffinity_MacOSX.c
endif
ALL_NATIVE_SOURCES := $(NATIVE_CPP_SOURCES) $(NATIVE_C_SOURCES)

# Object files
NATIVE_CPP_OBJECTS := $(patsubst %.cpp,%.o,$(NATIVE_CPP_SOURCES))
NATIVE_C_OBJECTS := $(patsubst %.c,%.o,$(NATIVE_C_SOURCES))
ALL_NATIVE_OBJECTS := $(NATIVE_CPP_OBJECTS) $(NATIVE_C_OBJECTS)

# classpath for javah
ifdef CLASSPATH
JAVAH_CLASSPATH = $(JAVA_BUILD_DIR):$(CLASSPATH)
else
JAVAH_CLASSPATH = $(JAVA_BUILD_DIR)
endif

.PHONY : clean

all: $(TARGET)

$(TARGET): $(ALL_NATIVE_OBJECTS)
	$(CXX) $(CXXFLAGS) -shared -fPIC $(JVM_SHARED_LIBS) $(LRT) $(INCLUDES) $(ALL_NATIVE_OBJECTS) -o $(TARGET)

%.o: %.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) -c $< -o $@

%.o: %.c
	$(CC) $(CFLAGS) $(INCLUDES) -c $< -o $@

clean:
	rm -f $(TARGET) $(ALL_NATIVE_OBJECTS)
