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

jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	return JNI_VERSION_1_4;
}

extern "C" int32 zcode_treaty(int32 selector, void *story_file, int32 extent, void *output, int32 output_extent);

extern "C" jstring Java_org_andglk_babel_Babel_examine(JNIEnv *env, jclass cls, jobject jbuf)
{
	void *buffer = env->GetDirectBufferAddress(jbuf);
	if (!buffer)
		return 0;
	jlong len = env->GetDirectBufferCapacity(jbuf);
	if (!len)
		return 0;

#define IFID_BUFLEN 128
	char ifid_buffer[IFID_BUFLEN];
	ifid_buffer[0] = 0;

	if (zcode_treaty(	CLAIM_STORY_FILE_SEL, buffer, len, 0, 0) == VALID_STORY_FILE_RV)
		zcode_treaty(GET_STORY_FILE_IFID_SEL, buffer, len, ifid_buffer, IFID_BUFLEN);
	else
		return 0;

	return env->NewStringUTF(ifid_buffer);
}
