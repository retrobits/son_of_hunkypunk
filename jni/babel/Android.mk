LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := babel
LOCAL_SRC_FILES := babel.cpp zcode.c
LOCAL_CPPFLAGS	:= -W -Wall

include $(BUILD_SHARED_LIBRARY)
