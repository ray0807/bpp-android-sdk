LOCAL_PATH:= $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MODULE := libnhr

LOCAL_SRC_FILES :=  ./src/nhr_common.c \
                   	./src/nhr_gz.c \
                   	./src/nhr_map.c \
                   	./src/nhr_memory.c \
                   	./src/nhr_request_method_common.c \
                   	./src/nhr_request_method_get.c \
                   	./src/nhr_request_method_post.c \
                   	./src/nhr_request_private.c \
                   	./src/nhr_request_public.c \
                   	./src/nhr_response.c \
                   	./src/nhr_string.c \
                   	./src/nhr_thread.c \
					./cJSON/cJSON.c
LOCAL_CFLAGS += -Os \
				-Wall \
				-MMD \
				-fno-asynchronous-unwind-tables \
				-fdata-sections \
				-ffunction-sections \
				-fno-math-errno \
				-fno-signed-zeros \
				-fno-tree-vectorize \
				-fomit-frame-pointer \
				-D_FILE_OFFSET_BITS=64 \
				-D_LARGEFILE_SOURCE \
				-D_REENTRANT \
				-g \
				-D_ISOC99_SOURCE \
				-D_POSIX_C_SOURCE=200112 \
				-D_XOPEN_SOURCE=600 \
				-DHAVE_AV_CONFIG_H \
				-std=c99 \
				-D_GNU_SOURCE=1 \
				-DUSE_VAR_BIT_DEPTH \
				-DUSE_PRED
LOCAL_LDLIBS = -llog -lz
LOCAL_C_INCLUDES := $(LOCAL_PATH)/ \
                    $(LOCAL_PATH)/src

include $(BUILD_STATIC_LIBRARY)

