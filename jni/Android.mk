LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := andglk
LOCAL_SRC_FILES := andglk.c model.c

include $(BUILD_SHARED_LIBRARY)
