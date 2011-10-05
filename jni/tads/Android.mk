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

LOCAL_MODULE    := tads
LOCAL_SRC_FILES := 	tads2/osifc.c tads2/osrestad.c tads2/oem.c \
	tads2/argize.c tads2/bif.c tads2/bifgdum.c tads2/cmap.c tads2/cmd.c tads2/dat.c tads2/dbgtr.c tads2/errmsg.c \
	tads2/execmd.c tads2/fio.c tads2/fioxor.c tads2/getstr.c tads2/ler.c tads2/linfdum.c tads2/lst.c tads2/mch.c \
	tads2/mcm.c tads2/mcs.c tads2/obj.c tads2/oserr.c tads2/os0.c tads2/out.c tads2/output.c tads2/ply.c \
	tads2/qas.c tads2/regex.c tads2/run.c tads2/runstat.c tads2/suprun.c tads2/trd.c tads2/voc.c tads2/vocab.c \
	\
	tads3/vmcrc.cpp tads3/vmmain.cpp tads3/std.cpp tads3/std_dbg.cpp tads3/charmap.cpp \
	tads3/resload.cpp tads3/resldexe.cpp tads3/vminit.cpp tads3/vmini_nd.cpp \
	tads3/vmconsol.cpp tads3/vmconnom.cpp tads3/vmconhmp.cpp tads3/vminitim.cpp \
	tads3/vmcfgmem.cpp tads3/vmobj.cpp tads3/vmundo.cpp tads3/vmtobj.cpp tads3/vmpat.cpp \
	tads3/vmstrcmp.cpp tads3/vmdict.cpp tads3/vmgram.cpp tads3/vmstr.cpp tads3/vmcoll.cpp \
	tads3/vmiter.cpp tads3/vmlst.cpp tads3/vmsort.cpp tads3/vmsortv.cpp tads3/vmbignum.cpp \
	tads3/vmvec.cpp tads3/vmintcls.cpp tads3/vmanonfn.cpp tads3/vmlookup.cpp \
	tads3/vmbytarr.cpp tads3/vmcset.cpp tads3/vmfilobj.cpp tads3/vmstack.cpp tads3/vmerr.cpp \
	tads3/vmerrmsg.cpp tads3/vmpool.cpp tads3/vmpoolim.cpp tads3/vmtype.cpp tads3/vmtypedh.cpp \
	tads3/utf8.cpp tads3/vmglob.cpp tads3/vmrun.cpp tads3/vmfunc.cpp tads3/vmmeta.cpp tads3/vmsa.cpp \
	tads3/vmbiftio.cpp tads3/vmbif.cpp tads3/vmbifl.cpp tads3/vmimage.cpp tads3/vmimg_nd.cpp tads3/vmrunsym.cpp \
	tads3/vmsrcf.cpp tads3/vmfile.cpp tads3/vmbiftad.cpp tads3/vmsave.cpp \
	tads3/vmbift3.cpp tads3/vmbt3_nd.cpp tads3/vmregex.cpp tads3/vmhosttx.cpp \
	tads3/vmhostsi.cpp tads3/vmhash.cpp tads3/vmmcreg.cpp tads3/vmbifreg.cpp \
	\
	t23run.cpp \
	osansi1.c osansi2.c osansi3.c \
	osglk.c \
	osnoban.c \
	t2askf.c \
	t2indlg.c \
	t3askf.cpp \
	t3indlg.cpp \
	memicmp.c \
	vmuni_cs.cpp

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../andglk \
	$(LOCAL_PATH)/../../andglk \
	$(LOCAL_PATH)/tads2 $(LOCAL_PATH)/tads2/glk \
	$(LOCAL_PATH)/tads3 $(LOCAL_PATH)/tads3/glk

LOCAL_CFLAGS	:= -DOS_UINT_DEFINED -DVMGLOB_STRUCT -DGARGOYLE -DANDGLK
#-DGLK

LOCAL_STATIC_LIBRARIES := andglk
LOCAL_LDLIBS	:= -llog

include $(BUILD_SHARED_LIBRARY)
