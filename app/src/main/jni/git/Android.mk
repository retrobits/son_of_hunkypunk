#	Copyright © 2009 Rafał Rzepecki <divided.mind@gmail.com>
#
#	This file is part of Hunky Punk.
#
#    Hunky Punk is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    Hunky Punk is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with Hunky Punk.  If not, see <http://www.gnu.org/licenses/>.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := git
LOCAL_SRC_FILES := compiler.c gestalt.c git.c git_unix.c \
	glkop.c heap.c memory.c opcodes.c \
	operands.c peephole.c savefile.c saveundo.c \
	search.c terp.c accel.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../andglk 
LOCAL_CFLAGS	:= -DGARGLK -DANDGLK
LOCAL_STATIC_LIBRARIES := andglk
LOCAL_LDLIBS	:= -llog

include $(BUILD_SHARED_LIBRARY)
