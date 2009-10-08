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

#define IFID_BUFLEN 32
	char ifid_buffer[IFID_BUFLEN];
	ifid_buffer[0] = 0;

	if (zcode_treaty(	CLAIM_STORY_FILE_SEL, buffer, len, 0, 0) == VALID_STORY_FILE_RV)
		zcode_treaty(GET_STORY_FILE_IFID_SEL, buffer, len, ifid_buffer, IFID_BUFLEN);
	else
		return 0;

	return env->NewStringUTF(ifid_buffer);
}
