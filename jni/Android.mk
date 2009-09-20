LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := andglk
LOCAL_SRC_FILES := andglk.c

include $(BUILD_STATIC_LIBRARY)

LOCAL_MODULE    := model
LOCAL_SRC_FILES := model.c
LOCAL_STATIC_LIBRARIES := andglk
LOCAL_LDLIBS = -L./build/platforms/android-1.5/arch-arm/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)
