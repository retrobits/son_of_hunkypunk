/*
	Copyright © 2009 Rafał Rzepecki <divided.mind@gmail.com>

	This file is part of Hunky Punk.

	Hunky Punk is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Hunky Punk is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Hunky Punk.  If not, see <http://www.gnu.org/licenses/>.
*/

#include <jni.h>

#include "treaty.h"
#include "babel_handler.h"

jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	if(jvm || reserved){} //remove unused warning

	return JNI_VERSION_1_6;
}

extern "C" int32 zcode_treaty(int32 selector, void *story_file, int32 extent, void *output, int32 output_extent);
extern "C" int32 glulx_treaty(int32 selector, void *story_file, int32 extent, void *output, int32 output_extent);
extern "C" int32 tads2_treaty(int32 selector, void *story_file, int32 extent, void *output, int32 output_extent);
extern "C" int32 tads3_treaty(int32 selector, void *story_file, int32 extent, void *output, int32 output_extent);

extern "C" jstring Java_org_andglk_babel_Babel_examine(JNIEnv *env, jclass cls, jstring filepath)
{
	if(cls){} //remove unused warning

	const char* copy_filepath = env->GetStringUTFChars(filepath, 0);
	
	// Add null pointer checks
	if (!copy_filepath) {
		return 0;
	}
	
	// Initialize babel with the file
	char* format = babel_init(copy_filepath);
	if (!format) {
		env->ReleaseStringUTFChars(filepath, copy_filepath);
		return 0;
	}

	void* sf = babel_get_story_file();
	uint32 sl = babel_get_story_length();
	
	// Add safety checks for story file
	if (!sf || sl == 0) {
		babel_release();
		env->ReleaseStringUTFChars(filepath, copy_filepath);
		return 0;
	}

#define IFID_BUFLEN 128
	char ifid_buffer[IFID_BUFLEN];
	ifid_buffer[0] = 0;

	// should be able to do this... why not?  todo: redo libbabel.so from upstream dist
	//babel_treaty(GET_STORY_FILE_IFID_SEL, ifid_buffer, IFID_BUFLEN);

	// Add try-catch style protection and bounds checking
	if (sl >= 0x1D && zcode_treaty(CLAIM_STORY_FILE_SEL, sf, sl, 0, 0) == VALID_STORY_FILE_RV)
		zcode_treaty(GET_STORY_FILE_IFID_SEL, sf, sl, ifid_buffer, IFID_BUFLEN);
	else if (sl >= 4 && tads2_treaty(CLAIM_STORY_FILE_SEL, sf, sl, 0, 0) == VALID_STORY_FILE_RV)
		tads2_treaty(GET_STORY_FILE_IFID_SEL, sf, sl, ifid_buffer, IFID_BUFLEN);
	else if (sl >= 4 && tads3_treaty(CLAIM_STORY_FILE_SEL, sf, sl, 0, 0) == VALID_STORY_FILE_RV)
		tads3_treaty(GET_STORY_FILE_IFID_SEL, sf, sl, ifid_buffer, IFID_BUFLEN);
	else if (sl >= 0x20 && glulx_treaty(CLAIM_STORY_FILE_SEL, sf, sl, 0, 0) == VALID_STORY_FILE_RV)
		glulx_treaty(GET_STORY_FILE_IFID_SEL, sf, sl, ifid_buffer, IFID_BUFLEN);

	babel_release();

	env->ReleaseStringUTFChars(filepath, copy_filepath);	

	if (ifid_buffer[0])
		return env->NewStringUTF(ifid_buffer);
	else
		return 0;
}
extern "C" jstring Java_org_andglkmod_babel_Babel_examine(JNIEnv *env, jclass cls, jstring filepath)
{
	return Java_org_andglk_babel_Babel_examine(env,cls,filepath);
}
