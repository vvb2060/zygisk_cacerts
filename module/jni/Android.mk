LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE           := vvb2060
LOCAL_SRC_FILES        := main.cpp
LOCAL_CFLAGS           := -fno-threadsafe-statics
include $(BUILD_SHARED_LIBRARY)
