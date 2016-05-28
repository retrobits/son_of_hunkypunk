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

#define _GNU_SOURCE // for strnlen

#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <setjmp.h>
#include "glk.h"
#include "garglk.h"
#include "glkstart.h"
#include "gi_dispa.h"

#define TAG "andglk.c"

extern jobject _this;
extern jclass _class;

/* these are OK to keep */
static JavaVM *_jvm;

jclass _class;
static jclass _Event, _LineInputEvent, _Window, _FileRef, _Stream, _PairWindow, _TextGridWindow,
	_CharInputEvent, _ArrangeEvent, _MemoryStream, _CPointed, _ExitEvent, _AutoSaveEvent, _AutoRestoreEvent;
static jmethodID _getRock, _getPointer, _getDispatchRock, _getDispatchClass;

/* this should be nulled on exit */
jobject _this = 0;
static jmp_buf _quit_env;
static char* gidispatch_char_array = "&+#!Cn";

#define GLK_JNI_VERSION JNI_VERSION_1_2

void ( * andglk_exit_hook ) (void) = NULL; 
void ( * andglk_set_autosave_hook ) (const char* filename) = NULL; 
void ( * andglk_set_autorestore_hook ) (const char* filename) = NULL;

jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	LOGD("andglk.JNI_OnLoad");

	_jvm = jvm;

	JNIEnv *env;
	if ((*jvm)->GetEnv(jvm, (void **)&env, GLK_JNI_VERSION))
		return JNI_ERR;

	jclass cls = (*env)->FindClass(env, "org/andglk/glk/Glk");
	_class = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/Event");
	_Event = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/LineInputEvent");
	_LineInputEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/CharInputEvent");
	_CharInputEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/ArrangeEvent");
	_ArrangeEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/ExitEvent");
	_ExitEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/AutoSaveEvent");
	_AutoSaveEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/Window");
	_Window = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/PairWindow");
	_PairWindow = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/TextGridWindow");
	_TextGridWindow = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/FileRef");
	_FileRef = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/Stream");
	_Stream = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/MemoryStream");
	_MemoryStream = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/glk/CPointed");
	_CPointed = (*env)->NewGlobalRef(env, cls);

	_getRock = (*env)->GetMethodID(env, cls, "getRock", "()I");
	_getPointer = (*env)->GetMethodID(env, cls, "getPointer", "()I");
	_getDispatchRock = (*env)->GetMethodID(env, cls, "getDispatchRock", "()I");
	_getDispatchClass = (*env)->GetMethodID(env, cls, "getDispatchClass", "()I");

	return GLK_JNI_VERSION;
}

static glui32 jstring2latin1(JNIEnv *env, jstring str, char *buf, glui32 maxlen)
{
	glui32 len = (*env)->GetStringLength(env, str);
	if (len > maxlen)
		len = maxlen;

	const jchar * jbuf = (*env)->GetStringChars(env, str, NULL);
	if (!jbuf)
		return 0;

	int i;
	for (i = 0; i < len; ++i)
		buf[i] = jbuf[i];

	(*env)->ReleaseStringChars(env, str, jbuf);
	return len;
}

static glui32 jstring2latin1_uni(JNIEnv *env, jstring str, glui32 *buf, glui32 maxlen)
{
	glui32 len = (*env)->GetStringLength(env, str);
	if (len > maxlen)
		len = maxlen;

	const jchar * jbuf = (*env)->GetStringChars(env, str, NULL);
	if (!jbuf)
		return 0;

	int i;
	for (i = 0; i < len; ++i)
		buf[i] = jbuf[i];

	(*env)->ReleaseStringChars(env, str, jbuf);
	return len;
}



void andglk_loader_glk_main(JavaVM* jvm, JNIEnv *env, jobject this, const char* saveFilePath, glkunix_startup_t* startdata)
{
	JNI_OnLoad(jvm, NULL);

   	if (_this) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, "org/andglk/glk/Glk/AlreadyRunning"),
				"you can't run more than one glk instance");
		return;
	}

	if (!(_this = (*env)->NewGlobalRef(env, this)))
		return;

	if (!setjmp(_quit_env)) {

		if (glkunix_startup_code(startdata)) {

 			if (andglk_set_autorestore_hook) {
				andglk_set_autorestore_hook(saveFilePath);
			}
		
			if (!setjmp(_quit_env)) {
				glk_main();
				glk_exit();
			}
		}
	}
}

int andglk_loader_glk_MemoryStream_retainVmArray(JNIEnv *env, jobject this, int buffer, long length)
{
	if (gli_register_arr) {
		gidispatch_rock_t rock = gli_register_arr((void *)buffer, length, gidispatch_char_array);
		return rock.num;
	}
}

jint andglk_loader_glk_CPointed_makePoint(JNIEnv *env, jobject this)
{
	jobject *ptr = malloc(sizeof(jobject));
	*ptr = (*env)->NewGlobalRef(env, this);
	if (gli_register_obj) {
		static jmethodID setDispatchRock;
		if (setDispatchRock == 0)
			setDispatchRock = (*env)->GetMethodID(env, _CPointed, "setDispatchRock", "(I)V");

		glui32 objclass = (*env)->CallIntMethod(env, *ptr, _getDispatchClass);
		if (objclass == gidisp_Class_Window || objclass == gidisp_Class_Schannel) {
			gidispatch_rock_t rock = gli_register_obj(ptr, objclass);
			(*env)->CallVoidMethod(env, *ptr, setDispatchRock, rock);
		}
	}
	return (jint) ptr;
}

void andglk_loader_glk_CPointed_releasePoint(JNIEnv *env, jobject this, jint point)
{
	if (!point)
		return;

	jobject *ptr = (jobject *) point;
	if (gli_unregister_obj) {
		gidispatch_rock_t rock;
		rock.num = (*env)->CallIntMethod(env, *ptr, _getDispatchRock);
		glui32 objclass = (*env)->CallIntMethod(env, *ptr, _getDispatchClass);

		if (objclass == gidisp_Class_Window || objclass == gidisp_Class_Schannel) {
			gli_unregister_obj(ptr, objclass, rock);
		}
	}
	(*env)->DeleteGlobalRef(env, *ptr);
	free(ptr);
}

void andglk_loader_glk_MemoryStream_writeOut(JNIEnv *env, jobject this, jint nativeBuf, jarray jbuf)
{
	char *nbuf = (char *)nativeBuf;
	int len = (*env)->GetArrayLength(env, jbuf);

	jbyte *jbufcontents;
	if (!(jbufcontents = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, jbuf, NULL)))
		return;

	memcpy(nbuf, jbufcontents, len);

	(*env)->ReleasePrimitiveArrayCritical(env, jbuf, jbufcontents, JNI_ABORT);
}

void andglk_loader_glk_MemoryStream_releaseVmArray(JNIEnv *env, jobject this, int buffer, int length, int dispatchRock)
{
	if (gli_unregister_arr) {
		gidispatch_rock_t rock;
		rock.num = dispatchRock;
		gli_unregister_arr((void *)buffer, length, gidispatch_char_array, rock);
	}
}

void andglk_loader_glk_Glk_notifyLinked(JNIEnv *env, jobject this)
{
	jclass cls = (*env)->FindClass(env, "org/andglk/glk/Glk");
	jmethodID mid = (*env)->GetMethodID(env, cls, "notifyLinked", "()V");
	(*env)->CallVoidMethod(env, this, mid);
}

JNIEnv *JNU_GetEnv()
{
	JNIEnv *env;
	(*_jvm)->GetEnv(_jvm, (void **)&env, GLK_JNI_VERSION);
	return env;
}

void glk_exit(void)
{
	JNIEnv *env = JNU_GetEnv();

	glk_select_poll(NULL); /* flush any pending output */

	gli_streams_close_all();
	gli_fileref_delete_all();
	(*env)->DeleteGlobalRef(env, _this);
	_this = 0;

	// any cleaner way to have glk_exit() not returning (as per spec)?
	longjmp(_quit_env, 1);
}

void glk_set_interrupt_handler(void (*func)(void))
{
	/* this cheap library doesn't understand interrupts */
}

void glk_tick(void)
{
	/* we run in an own thread, OS takes care of this */
}

glui32 glk_gestalt(glui32 sel, glui32 val)
{
	return glk_gestalt_ext(sel, val, NULL, 0);
}

glui32 glk_gestalt_ext(glui32 sel, glui32 val, glui32 *arr,
    glui32 arrlen)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "gestalt", "(II)[I");

	jarray ret = (*env)->CallObjectMethod(env, _this, mid, (jint) sel, (jint) val);

	jint *array = (*env)->GetIntArrayElements(env, ret, NULL);
	glui32 res = array[0];

	if (arr) {
		jint len = (*env)->GetArrayLength(env, ret);
		if (len > arrlen)
			len = arrlen;

		int i;
		for (i = 0; i < len; i++)
			arr[i] = array[i+1];
	}

	(*env)->ReleaseIntArrayElements(env, ret, array, JNI_ABORT);
	(*env)->DeleteLocalRef(env, ret);

	return res;
}

unsigned char glk_char_to_lower(unsigned char ch)
{
	return tolower(ch);
}

unsigned char glk_char_to_upper(unsigned char ch)
{
	return toupper(ch);
}

winid_t glk_window_get_root(void)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "getRoot", "()Lorg/andglk/glk/Window;");

	jobject obj = (*env)->CallStaticObjectMethod(env, _Window, mid);

	winid_t ret;
	if (obj)
		ret = (winid_t) (*env)->CallIntMethod(env, obj, _getPointer);
	else
		return 0;

	(*env)->DeleteLocalRef(env, obj);
	return ret;
}

winid_t glk_window_open(winid_t split, glui32 method, glui32 size,
    glui32 wintype, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	(*_jvm)->AttachCurrentThread(_jvm, &env, NULL); 

	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "open", "(Lorg/andglk/glk/Window;IIII)I");

	return (winid_t) (*env)->CallStaticIntMethod(env, _Window, mid, split ? *split : 0, (jint) method, (jint) size, (jint) wintype, (jint) rock);
}

void glk_window_close(winid_t win, stream_result_t *result)
{
	if (!win)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "close", "()J");

	glui32 written = (*env)->CallLongMethod(env, *win, mid);
	if (result) {
		result->readcount = 0;
		result->writecount = written;
	}

	strid_t str = gli_find_window_stream(win);
	if (str) gli_delete_stream(str);
}

void glk_window_get_size(winid_t win, glui32 *widthptr, glui32 *heightptr)
{
	if (!win)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "getSize", "()[I");

	jarray res = (*env)->CallObjectMethod(env, *win, mid);

	if (res) {
		jint *arr = (*env)->GetIntArrayElements(env, res, NULL);
		if (widthptr) *widthptr = arr[0];
		if (heightptr) *heightptr = arr[1];
		
		// this has timing problems (need to wait for ui to be ready)
		// LOGD("glk_window_get_size w:%d h:%d",arr[0],arr[1]);

		(*env)->ReleaseIntArrayElements(env, res, arr, JNI_ABORT);
		(*env)->DeleteLocalRef(env, res);
	}
}

void glk_window_set_arrangement(winid_t win, glui32 method,
    glui32 size, winid_t keywin)
{
	if (!win)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _PairWindow, "setArrangement", "(IILorg/andglk/glk/Window;)V");

	(*env)->CallVoidMethod(env, *win, mid, (jint) method, (jint) size, keywin ? *keywin : 0);
}

void glk_window_get_arrangement(winid_t win, glui32 *methodptr,
    glui32 *sizeptr, winid_t *keywinptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "window_get_arrangement", "(Lorg/andglk/glk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win, methodptr, sizeptr, keywinptr);

}

winid_t glk_window_iterate(winid_t win, glui32 *rockptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "iterate", "(Lorg/andglk/glk/Window;)Lorg/andglk/glk/Window;");

	jobject nextwin = (*env)->CallStaticObjectMethod(env, _Window, mid, win ? *win : 0);

	winid_t ret;
	if (!nextwin)
		return 0;

	if (rockptr)
		*rockptr = (*env)->CallIntMethod(env, nextwin, _getRock);
	ret = (winid_t) (*env)->CallIntMethod(env, nextwin, _getPointer);

	(*env)->DeleteLocalRef(env, nextwin);

	return ret;
}

glui32 glk_window_get_rock(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	return (*env)->CallIntMethod(env, *win, _getRock);
}

glui32 glk_window_get_type(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "getType", "()I");

	return (*env)->CallIntMethod(env, *win, mid);

}

winid_t glk_window_get_parent(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "getParent", "()Lorg/andglk/glk/PairWindow;");

	jobject parent = (*env)->CallObjectMethod(env, *win, mid);
	winid_t ret;
	if (parent)
		ret = (winid_t) (*env)->CallIntMethod(env, parent, _getPointer);
	else
		return 0;

	(*env)->DeleteLocalRef(env, parent);

	return ret;
}

winid_t glk_window_get_sibling(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "getSibling", "()Lorg/andglk/glk/Window;");

	jobject sibling = (*env)->CallObjectMethod(env, *win, mid);
	winid_t ret;

	if (sibling)
		ret = (winid_t) (*env)->CallIntMethod(env, sibling, _getPointer);
	else
		return 0;

	(*env)->DeleteLocalRef(env, sibling);
	return 0;
}

void glk_window_clear(winid_t win)
{
	if (!win)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "clear", "()V");

	(*env)->CallVoidMethod(env, *win, mid);
}

void glk_window_move_cursor(winid_t win, glui32 xpos, glui32 ypos)
{
	if (!win)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _TextGridWindow, "moveCursor", "(II)V");

	(*env)->CallVoidMethod(env, *win, mid, (jint) xpos, (jint) ypos);
}

strid_t glk_window_get_stream(winid_t win)
{
	strid_t str = gli_find_window_stream(win);

	if (!str) {
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Window, "getStream", "()Lorg/andglk/glk/Stream;");

		jobject obj = (*env)->CallObjectMethod(env, *win, mid);
		if (obj) {
			str = gli_new_stream(strtype_Window, FALSE, TRUE, 0, FALSE);
			str->st = (jobject*) (*env)->CallIntMethod(env, obj, _getPointer);
			(*env)->DeleteLocalRef(env, obj);
			str->winid = win;
		}
	}
	return str;
}

void glk_window_set_echo_stream(winid_t win, strid_t str)
{
	return; //todo -- transcript is broken

	if (!win || str->type != strtype_Window)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "setEchoStream", "(Lorg/andglk/glk/Stream;)V");

	(*env)->CallVoidMethod(env, *win, mid, str ? *(str->st) : 0);
}

strid_t glk_window_get_echo_stream(winid_t win)
{
	if (!win) 
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "getEchoStream", "()I");

	strid_t str = gli_new_stream(strtype_Window, FALSE, TRUE, 0, FALSE);
	str->st = (jobject*) (*env)->CallIntMethod(env, *win, mid);
	return str;
}

void glk_set_window(winid_t win)
{
	strid_t str = NULL;
	if (win)
		str = glk_window_get_stream(win);

   	glk_stream_set_current(str);
	
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "setWindow", "(Lorg/andglk/glk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win ? *win : NULL);
}
		
strid_t glk_stream_open_file(frefid_t fileref, glui32 fmode, glui32 rock)
{
    return gli_stream_open_file(fileref, fmode, rock, FALSE);
}

strid_t glkunix_stream_open_pathname(const char *pathname, glui32 textmode, glui32 rock)
{
    return gli_stream_open_pathname(pathname, (textmode != 0), rock);
}

strid_t glk_stream_open_memory(char *buf, glui32 buflen, glui32 fmode, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _MemoryStream, "<init>", "(I[BII)V");

	jarray jbuf = (*env)->NewByteArray(env, buflen);
	if (fmode != filemode_Write && buf) {
		jbyte *jbufcontents = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, jbuf, NULL);
		memcpy(jbufcontents, buf, buflen);
		(*env)->ReleasePrimitiveArrayCritical(env, jbuf, jbufcontents, 0);
	}

	jobject obj = (*env)->NewObject(env, _MemoryStream, mid, (jint) buf, jbuf, (jint) fmode, (jint) rock);
	(*env)->DeleteLocalRef(env, jbuf);

	if (obj) {
		strid_t str = gli_new_stream(strtype_Memory, 
									 (fmode != filemode_Write), 
									 (fmode != filemode_Read), 
									 rock,
									 FALSE);

		str->st = (jobject*) (*env)->CallIntMethod(env, obj, _getPointer);
		(*env)->DeleteLocalRef(env, obj);
		return str;
	}
	else
		return 0;
}

void glk_stream_close(strid_t str, stream_result_t *result)
{
	if (!str)
		return;

	if (str->type == strtype_File) {
		fclose(str->file);

		if (result) {
			result->readcount = str->readcount;
			result->writecount = str->writecount;
		}
	}
	else {
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "close", "()[I");

		jarray res = (*env)->CallObjectMethod(env, *(str->st), mid);

		if (result) {
			jint *arr = (*env)->GetIntArrayElements(env, res, NULL);
			result->readcount = arr[0];
			result->writecount = arr[1];
			(*env)->ReleaseIntArrayElements(env, res, arr, JNI_ABORT);
			(*env)->DeleteLocalRef(env, res);
		}
	}
}

strid_t glk_stream_iterate(strid_t str, glui32 *rockptr)
{
	return gli_stream_iterate(str, rockptr);
}

glui32 glk_stream_get_rock(strid_t str)
{
	return gli_stream_get_rock(str);
}

void glk_stream_set_position(strid_t str, glsi32 pos, glui32 seekmode)
{
	if (!str) return;

	if (str->type == strtype_File) {
		//if (str->unicode)
		//    pos *= 4;
		fseek(str->file, pos, 
              ((seekmode == seekmode_Current) ? 1 :
			   ((seekmode == seekmode_End) ? 2 : 0)));
	} else {
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "setPosition", "(II)V");

		(*env)->CallVoidMethod(env, *(str->st), mid, (jint) pos, (jint) seekmode);
	}
}

glui32 glk_stream_get_position(strid_t str)
{
	if (!str) return;

	if (str->type == strtype_File) {
		//if (str->unicode)
		//	return ftell(str->file) / 4;
		//else
			return ftell(str->file);
	} else {
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "getPosition", "()I");

		return (*env)->CallIntMethod(env, *(str->st), mid);
	}
}

void glk_stream_set_current(strid_t str)
{
	gli_stream_set_current(str);
}

strid_t glk_stream_get_current(void)
{
	return gli_stream_get_current();
}

void glk_put_char_uni(glui32 ch)
{
	unsigned char lilch = (unsigned char)ch;
	glk_put_char(lilch);
}

void glk_put_char(unsigned char ch)
{
	glk_put_char_stream(glk_stream_get_current(), ch);
}

void glk_put_char_stream_uni(strid_t str, glui32 ch)
{
	unsigned char lilch = (unsigned char)ch;
	glk_put_char_stream(str, lilch);
}

void glk_put_char_stream(strid_t str, unsigned char ch)
{
	if (!str) return;

	if (str->type == strtype_File) {

		if (str->textfile) {
			putc(ch < 0x80 ? ch : '?', str->file);
		} else {
			putc((unsigned char)ch, str->file);
		}
		//fflush(str->file);
	} else {
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "putChar", "(C)V");

		(*env)->CallVoidMethod(env, *(str->st), mid, (jchar) ch);
	}

	str->writecount++;
}

void glk_put_string(char *s)
{
	glk_put_string_stream(glk_stream_get_current(), s);
}

void glk_put_string_stream(strid_t str, char *s)
{
	glk_put_buffer_stream(str, s, strlen(s));
}

void glk_put_buffer_uni(glui32 *buf, glui32 len)
{
	char chbuf[len+1];

	glui32 *it = buf;
	char *jt = &chbuf[0];
	while (jt - chbuf < len)
		*(jt++) = (char)(*(it++));
	*jt = '\0';

	glk_put_buffer(&chbuf[0], len);
}

void glk_put_buffer(char *s, glui32 len)
{
	glk_put_buffer_stream(glk_stream_get_current(), s, len);
}

void glk_put_buffer_stream(strid_t str, char *s, glui32 len)
{
	int lx;

	if (!str || !s) return;

	if (str->type == strtype_File) {
		for (lx=0; lx<len; lx++) {
			if (str->textfile)
				putc(s[lx] < 0x80 ? s[lx] : '?', str->file);
			else
				putc((unsigned char)(s[lx]), str->file);
		}
		//fflush(str->file);
	} else {
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "putString", "(Ljava/lang/String;)V");

		jchar buf[len];
		char *it = s;
		jchar *jt = buf;
		while (jt - buf < len)
			*(jt++) = *(it++);

		jstring jstr = (*env)->NewString(env, buf, len);

		(*env)->CallVoidMethod(env, *(str->st), mid, jstr);

		(*env)->DeleteLocalRef(env, jstr);
	}
	str->writecount += len;
}

void glk_set_style(glui32 styl)
{
	glk_set_style_stream(glk_stream_get_current(), styl);
}

void glk_set_style_stream(strid_t str, glui32 styl)
{
	if (!str || str->type != strtype_Window)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "setStyle", "(J)V");

	(*env)->CallVoidMethod(env, *(str->st), mid, (jlong) styl);
}

void garglk_set_reversevideo(glui32 reverse)
{
	garglk_set_reversevideo_stream(glk_stream_get_current(), reverse);
}

void garglk_set_reversevideo_stream(stream_t *str, glui32 reverse)
{
	if (!str || str->type != strtype_Window)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "setReverseVideo", "(J)V");

	(*env)->CallVoidMethod(env, *(str->st), mid, (jlong) reverse);
}

glsi32 glk_get_char_stream(strid_t str)
{
	int res;

    if (!str || !str->readable)
        return -1;

	if (str->type == strtype_File) {		
		res = getc(str->file);
	} else {	
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "getChar", "()I");

		res = (*env)->CallIntMethod(env, *(str->st), mid);
	}

	if (res != -1) {
		str->readcount++;
		return (glsi32)res;
	} else {
		return res;
	}
}

glui32 glk_get_line_stream(strid_t str, char *buf, glui32 len)
{
	int count = 0;

	if (!str)
		return;

	if (str->type == strtype_File) {		
		char *res;
		res = fgets(buf, len, str->file);
		if (!res)
			return 0;

		count = strlen(buf);
	} else {	
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "getLine", "(I)Ljava/lang/String;");

		jstring result = (*env)->CallObjectMethod(env, *(str->st), mid, len - 1);
		if (!result)
			return 0;

		int count = jstring2latin1(env, result, buf, len - 1);
		buf[count] = 0;

		(*env)->DeleteLocalRef(env, result);
	}

	str->readcount+=count;
	return count;
}

glui32 glk_get_buffer_stream(strid_t str, char *buf, glui32 len)
{
	glui32 count;

	if (!str)
		return;

	if (str->type == strtype_File) {		
		count = fread(buf, 1, len, str->file);
		/* Assume the file is Latin-1 encoded, so we don't have to do
		   any conversion. */
	} else {	
		JNIEnv *env = JNU_GetEnv();
		static jmethodID mid = 0;
		if (mid == 0)
			mid = (*env)->GetMethodID(env, _Stream, "getBuffer", "(I)[B");

		jarray result = (*env)->CallObjectMethod(env, *(str->st), mid, len);
		if (!result)
			return 0;

		int count = (*env)->GetArrayLength(env, result);
		if (count > len)
			count = len;

		jbyte *jbufcontents = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, result, NULL);
		memcpy(buf, jbufcontents, count);
		(*env)->ReleasePrimitiveArrayCritical(env, result, jbufcontents, 0);
		(*env)->DeleteLocalRef(env, result);
		
	}
	str->readcount += count;
	return count;
}

void glk_stylehint_set(glui32 wintype, glui32 styl, glui32 hint, glsi32 val)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "stylehintSet", "(IIII)V");

	(*env)->CallStaticVoidMethod(env, _Window, mid, (jint) wintype, (jint) styl, (jint) hint, (jint) val);
}

void glk_stylehint_clear(glui32 wintype, glui32 styl, glui32 hint)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "stylehintClear", "(III)V");

	(*env)->CallStaticVoidMethod(env, _Window, mid, (jint) wintype, (jint) styl, (jint) hint);
}

glui32 glk_style_distinguish(winid_t win, glui32 styl1, glui32 styl2)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "styleDistinguish", "(II)Z");

	return (*env)->CallBooleanMethod(env, *win, mid, styl1, styl2);
}

glui32 glk_style_measure(winid_t win, glui32 styl, glui32 hint, glui32 *result)
{
	__android_log_print(ANDROID_LOG_WARN, TAG, "style measure requested but not supported\n");
}

frefid_t glk_fileref_create_by_prompt(glui32 usage, glui32 fmode, glui32 rock)
{
    fileref_t *fref;
    int val, filter;
    char *prompt;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _FileRef, "getPathByPrompt", "(III)Ljava/lang/String;");

	jstring filePath = (*env)->CallStaticObjectMethod(env, _FileRef, mid, usage, fmode, rock);
	if (!filePath)
        /* The player just hit return. It would be nice to provide a
            default value, but this implementation is too cheap. */
		return NULL;

	const char* copy_filePath = (*env)->GetStringUTFChars(env, filePath, 0);
    fref = gli_new_fileref(copy_filePath, usage, rock);
	(*env)->ReleaseStringUTFChars(env, filePath, copy_filePath);	

    if (!fref)
    {
        gli_strict_warning("fileref_create_by_prompt: unable to create fileref.");
        return NULL;
    }

    return fref;
}

static void event2glk(JNIEnv *env, jobject ev, event_t *event)
{
	if (!ev) {
		event->win = NULL;
		event->val1 = event->val2 = event->type = 0;
		return;
	}

	static jfieldID window = 0;
	if (window == 0)
		window = (*env)->GetFieldID(env, _Event, "windowPointer", "I");

	event->win = (winid_t) (*env)->GetIntField(env, ev, window);

	if ((*env)->IsInstanceOf(env, ev, _LineInputEvent)) {
		event->type = evtype_LineInput;
		{
			static jfieldID line_id = 0, buf_id, len_id, rock_id, unicode_id;
			if (0 == line_id) {
				line_id = (*env)->GetFieldID(env, _LineInputEvent, "line", "Ljava/lang/String;");
				buf_id = (*env)->GetFieldID(env, _LineInputEvent, "buffer", "I");
				len_id = (*env)->GetFieldID(env, _LineInputEvent, "len", "J");
				rock_id = (*env)->GetFieldID(env, _LineInputEvent, "rock", "I");
				unicode_id = (*env)->GetFieldID(env, _LineInputEvent, "unicode", "I");
			}

			jstring line = (*env)->GetObjectField(env, ev, line_id);
			jlong len = (*env)->GetLongField(env, ev, len_id);
			jlong unicode = (*env)->GetIntField(env, ev, unicode_id);
			
			gidispatch_rock_t rock;
			rock.num = (*env)->GetIntField(env, ev, rock_id);

			if (unicode) {
				glui32 * buf = (glui32 *) (*env)->GetIntField(env, ev, buf_id);
				event->val1 = jstring2latin1_uni(env, line, buf, len);
				if (event->val1 != len)
					buf[event->val1] = 0;
				if (gli_unregister_arr)
					gli_unregister_arr(buf, len, gidispatch_char_array, rock); //INT2GDROCK(rock));
			}
			else {
				char * buf = (char *) (*env)->GetIntField(env, ev, buf_id);
				event->val1 = jstring2latin1(env, line, buf, len);
				if (event->val1 != len)
					buf[event->val1] = 0;
				if (gli_unregister_arr)
					gli_unregister_arr(buf, len, gidispatch_char_array, rock); //INT2GDROCK(rock));
			}

			(*env)->DeleteLocalRef(env, line);

			event->val2 = 0;
		}
	} else if ((*env)->IsInstanceOf(env, ev, _CharInputEvent)) {
		event->type = evtype_CharInput;
		{
			static jfieldID char_id = 0;
			if (0 == char_id)
				char_id = (*env)->GetFieldID(env, _CharInputEvent, "mChar", "I");

			event->val1 = (*env)->GetIntField(env, ev, char_id);
		}
		event->val2 = 0;
	} else if ((*env)->IsInstanceOf(env, ev, _ArrangeEvent)) {
		event->type = evtype_Arrange;
		event->val1 = event->val2 = 0;
	} else if ((*env)->IsInstanceOf(env, ev, _ExitEvent)) {
		if (andglk_exit_hook) 
			andglk_exit_hook();
		else
			glk_exit();
	} else if ((*env)->IsInstanceOf(env, ev, _AutoSaveEvent) && andglk_set_autosave_hook) {
		jfieldID fileName_id = (*env)->GetFieldID(env, _AutoSaveEvent, "FileName", "Ljava/lang/String;");
		char* fileName = (*env)->GetObjectField(env, ev, fileName_id);
		const char* copy_fileName = (*env)->GetStringUTFChars(env, fileName, 0);
		if (copy_fileName && copy_fileName[0]) andglk_set_autosave_hook(copy_fileName);
		(*env)->ReleaseStringUTFChars(env, fileName, copy_fileName);	

		jfieldID lineEvent_id = (*env)->GetFieldID(env, _AutoSaveEvent, "LineEvent", "I");
		int lineEvent = (*env)->GetIntField(env, ev, lineEvent_id);

		if (lineEvent) {
			event->type = evtype_LineInput;
			event->val1 = 0;
		} else {
			event->type = evtype_CharInput;
			event->val1 = keycode_Unknown;
		}
		event->val2 = 0;
	} 
}

void glk_select(event_t *event)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "select", "()Lorg/andglk/glk/Event;");

	jobject ev = (*env)->CallObjectMethod(env, _this, mid);
	event2glk(env, ev, event);

	(*env)->DeleteLocalRef(env, ev);
}

void glk_select_poll(event_t *event)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "flush", "()V");

	(*env)->CallVoidMethod(env, _this, mid);
	if (event) event->type = evtype_None;
}

void glk_request_timer_events(glui32 millisecs)
{
	/* TODO */
}

void gli_request_line_event(winid_t win, void *buf, glui32 maxlen, glui32 initlen, glui32 unicode)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;

	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "requestLineEvent", "(Ljava/lang/String;JII)V");

	jstring str = 0;
	jchar jbuf[initlen];

	if (initlen > 0) {

		if (unicode) {
			glui32 *it = buf;
			jchar *jt = jbuf;
			while (jt - jbuf < initlen)
				*(jt++) = *(it++);
		} else {
			char *it = buf;
			jchar *jt = jbuf;
			while (jt - jbuf < initlen)
				*(jt++) = *(it++);
		}

		str = (*env)->NewString(env, jbuf, maxlen);
	}

	(*env)->CallVoidMethod(env, *win, mid, str, (jlong) maxlen, (jint) buf, (jint) unicode);

	if (str)
		(*env)->DeleteLocalRef(env, str);
}

void glk_request_line_event_uni(winid_t win, glui32 *buf, glui32 maxlen, glui32 initlen)
{
	gli_request_line_event(win, buf, maxlen, initlen, 1);
}

void glk_request_line_event(winid_t win, char *buf, glui32 maxlen, glui32 initlen)
{
	gli_request_line_event(win, buf, maxlen, initlen, 0);
}

void glk_request_char_event_uni(winid_t win)
{
	glk_request_char_event(win);
}

void glk_request_char_event(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "requestCharEvent", "()V");

	(*env)->CallVoidMethod(env, *win, mid);
}

void glk_request_mouse_event(winid_t win)
{
	/* TODO */
}

void glk_cancel_line_event(winid_t win, event_t *event)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "cancelLineEvent", "()Lorg/andglk/glk/LineInputEvent;");

	jobject ev = (*env)->CallObjectMethod(env, *win, mid);
	if (event)
		event2glk(env, ev, event);

	(*env)->DeleteLocalRef(env, ev);
}

void glk_cancel_char_event(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "cancelCharEvent", "()V");

	(*env)->CallVoidMethod(env, *win, mid);
}

void glk_cancel_mouse_event(winid_t win)
{
	/* TODO */
}

gidispatch_rock_t gidispatch_get_objrock(void *obj, glui32 objclass)
{
    switch (objclass)
    {
		//case gidisp_Class_Schannel:
        case gidisp_Class_Window:
			if (obj) {
				JNIEnv *env = JNU_GetEnv();
				gidispatch_rock_t rock;
				rock.num = (*env)->CallIntMethod(env, *(jobject*)obj, _getDispatchRock);
				return rock;
			}
        case gidisp_Class_Stream:
            return ((stream_t *)obj)->disprock;
        case gidisp_Class_Fileref:
            return ((fileref_t *)obj)->disprock;
    }
	gidispatch_rock_t dummy;
	dummy.num = 0;
	return dummy;
}
