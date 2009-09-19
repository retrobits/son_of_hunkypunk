LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := andglk
LOCAL_SRC_FILES := andglk.c

include $(BUILD_STATIC_LIBRARY)

LOCAL_MODULE    := model
LOCAL_SRC_FILES := model.c
LOCAL_STATIC_LIBRARIES := andglk

include $(BUILD_SHARED_LIBRARY)
