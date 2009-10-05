LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := andglk
LOCAL_SRC_FILES := andglk.c gi_dispa.c
LOCAL_CPPFLAGS	:= -W -Wall
LOCAL_LDLIBS	:= -llog -Lbuild/platforms/android-1.5/arch-arm/usr/lib/

include $(BUILD_STATIC_LIBRARY)
