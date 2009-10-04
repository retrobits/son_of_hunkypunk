LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := nitfol
LOCAL_SRC_FILES := automap.c solve.c infix.c debug.c inform.c quetzal.c undo.c op_call.c decode.c errmesg.c globals.c iff.c init.c main.c\
	io.c z_io.c op_jmp.c op_math.c op_save.c op_table.c op_v6.c oplist.c stack.c zscii.c tokenise.c struct.c objects.c portfunc.c hash.c\
	no_graph.c no_blorb.c no_snd.c startand.c
LOCAL_CFLAGS	:= -I../andglk -DSMART_TOKENIZER -DTWOS16SHORT -DFAST -DUSE_INLINE -DNO_TICK -D_GNU_SOURCE -D_BSD_SOURCE 
LOCAL_STATIC_LIBRARIES := andglk
# LOCAL_LDLIBS = -L./build/platforms/android-1.5/arch-arm/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)
