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

LOCAL_MODULE    := frotz
LOCAL_SRC_FILES := buffer.c   files.c      input.c  \
object.c   random.c    stream.c  variable.c \
err.c      glkmisc.c    main.c   process.c  redirect.c  table.c \
fastmem.c  glkscreen.c  math.c   quetzal.c  sound.c     text.c  
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../andglk 
LOCAL_CFLAGS	:= -DANDGLK 
LOCAL_STATIC_LIBRARIES := andglk
LOCAL_LDLIBS	:= -llog

include $(BUILD_SHARED_LIBRARY)
