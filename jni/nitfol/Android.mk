#	Copyright © 2009 Rafał Rzepecki <divided.mind@gmail.com>
#	
#	This file is part of Hunky Punk.
#	
#	Hunky Punk is free software: you can redistribute it and/or modify
#	it under the terms of the GNU General Public License as published by
#	the Free Software Foundation, either version 3 of the License, or
#	(at your option) any later version.
#	
#	Hunky Punk is distributed in the hope that it will be useful,
#	but WITHOUT ANY WARRANTY; without even the implied warranty of
#	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#	GNU General Public License for more details.
#	
#	You should have received a copy of the GNU General Public License
#	along with Hunky Punk.  If not, see <http://www.gnu.org/licenses/>.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := nitfol
LOCAL_SRC_FILES := automap.c solve.c infix.c debug.c inform.c quetzal.c undo.c op_call.c decode.c errmesg.c globals.c iff.c init.c main.c\
	io.c z_io.c op_jmp.c op_math.c op_save.c op_table.c op_v6.c oplist.c stack.c zscii.c tokenise.c struct.c objects.c portfunc.c hash.c\
	no_graph.c no_blorb.c no_snd.c startunix.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../andglk 
LOCAL_CFLAGS	:= -DSMART_TOKENIZER -DTWOS16SHORT -DFAST -DUSE_INLINE -DNO_TICK -D_GNU_SOURCE -D_BSD_SOURCE -DANDGLK
LOCAL_STATIC_LIBRARIES := andglk
LOCAL_LDLIBS	:= -llog

include $(BUILD_SHARED_LIBRARY)
