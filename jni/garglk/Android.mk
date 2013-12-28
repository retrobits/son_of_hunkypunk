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

LOCAL_MODULE    := garglk
LOCAL_SRC_FILES := gi_blorb.c gi_dispa.c \
	cggestal.c cgblorb.c cgfref.c cgmisc.c \
	cgstyle.c cgstream.c cgunicod.c cgdate.c \
	window.c winblank.c winpair.c wingrid.c wintext.c \
	winmask.c wingfx.c \
	event.c config.c \
	fontdata.c babeldata.c \
	sndnull.c draw_null.c imgload_null.c imgscale_null.c \
	sysand.c

LOCAL_CFLAGS	:= -DANDROID -DGARGLKINI=\"gargoyle.ini\"
#LOCAL_LDLIBS	:= -llog

include $(BUILD_STATIC_LIBRARY)
