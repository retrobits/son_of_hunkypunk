LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := andglk-loader
LOCAL_CFLAGS := -DANDGLK
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../andglk

LOCAL_LDLIBS := -llog -ldl

LOCAL_SRC_FILES := \
loader.c 

#include $(BUILD_STATIC_LIBRARY)
include $(BUILD_SHARED_LIBRARY)
