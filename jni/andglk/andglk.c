#define _GNU_SOURCE // for strnlen

#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <setjmp.h>
#include "glk.h"
#include "gi_dispa.h"

#define TAG "andglk.c"

/* these are OK to keep */
static JavaVM *_jvm;
static jclass _class, _Event, _LineInputEvent, _Window, _FileRef, _Stream, _Character, _PairWindow, _TextGridWindow,
_CharInputEvent, _ArrangeEvent, _MemoryStream, _CPointed;
static jmethodID _getRock, _getPointer, _getDispatchRock, _getDispatchClass;

/* this should be nulled on exit */
static jobject _this = 0;
static gidispatch_rock_t (*_vm_reg_object)(void *obj, glui32 objclass) = 0;
static void (*_vm_unreg_object)(void *obj, glui32 objclass, gidispatch_rock_t objrock) = 0;
static gidispatch_rock_t (*_vm_reg_array)(void *array, glui32 len, char *typecode) = 0;
static void (*_vm_unreg_array)(void *array, glui32 len, char *typecode, gidispatch_rock_t objrock) = 0;

static jmp_buf _quit_env;
static char* gidispatch_char_array = "&+#!Cn";

#define GLK_JNI_VERSION JNI_VERSION_1_2

jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	_jvm = jvm;

	JNIEnv *env;
	if ((*jvm)->GetEnv(jvm, (void **)&env, GLK_JNI_VERSION))
		return JNI_ERR;

	jclass cls = (*env)->FindClass(env, "org/andglk/Glk");
	_class = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/Event");
	_Event = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/LineInputEvent");
	_LineInputEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/CharInputEvent");
	_CharInputEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/ArrangeEvent");
	_ArrangeEvent = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/Window");
	_Window = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/PairWindow");
	_PairWindow = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/TextGridWindow");
	_TextGridWindow = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/FileRef");
	_FileRef = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/Stream");
	_Stream = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/MemoryStream");
	_MemoryStream = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "java/lang/Character");
	_Character = (*env)->NewGlobalRef(env, cls);

	cls = (*env)->FindClass(env, "org/andglk/CPointed");
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

void Java_org_andglk_Glk_runProgram(JNIEnv *env, jobject this)
{
	if (_this) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, "org/andglk/Glk/AlreadyRunning"),
				"you can't run more than one glk instance");
		return;
	}

	if (!(_this = (*env)->NewGlobalRef(env, this)))
		return;

	if (!setjmp(_quit_env))
		glk_main();
}

#define GDROCK2INT(a) *((glui32 *) &(a))

int Java_org_andglk_Window_retainVmArray(JNIEnv *env, jobject this, int buffer, long length)
{
	if (_vm_reg_array) {
		gidispatch_rock_t rock = _vm_reg_array((void *)buffer, length, gidispatch_char_array);
		return GDROCK2INT(rock);
	}
}

jint Java_org_andglk_CPointed_makePoint(JNIEnv *env, jobject this)
{

	jobject *ptr = malloc(sizeof(jobject));
	*ptr = (*env)->NewGlobalRef(env, this);
	if (_vm_reg_object) {
		static jmethodID setDispatchRock;
		if (setDispatchRock == 0)
			setDispatchRock = (*env)->GetMethodID(env, _CPointed, "setDispatchRock", "(I)V");

		glui32 objclass = (*env)->CallIntMethod(env, this, _getDispatchClass);
		gidispatch_rock_t rock = _vm_reg_object(*ptr, objclass);
		(*env)->CallVoidMethod(env, this, setDispatchRock, (jint) GDROCK2INT(rock));
	}
	return (jint) ptr;
}

#define INT2GDROCK(a) (*(gidispatch_rock_t *) &(a))

void Java_org_andglk_CPointed_releasePoint(JNIEnv *env, jobject this, jint point)
{
	if (!point)
		return;

	jobject *ptr = (jobject *) point;
	(*env)->DeleteGlobalRef(env, *ptr);
	if (_vm_unreg_object) {
		glui32 rock = (*env)->CallIntMethod(env, this, _getDispatchRock);
		glui32 objclass = (*env)->CallIntMethod(env, this, _getDispatchClass);

		_vm_unreg_object(*ptr, objclass, INT2GDROCK(rock));
	}
	free(ptr);
}

void Java_org_andglk_MemoryStream_writeOut(JNIEnv *env, jobject this, jint nativeBuf, jarray jbuf)
{
	char *nbuf = (char *)nativeBuf;
	int len = (*env)->GetArrayLength(env, jbuf);

	jbyte *jbufcontents;
	if (!(jbufcontents = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, jbuf, NULL)))
		return;

	memcpy(nbuf, jbufcontents, len);

	(*env)->ReleasePrimitiveArrayCritical(env, jbuf, jbufcontents, JNI_ABORT);
}

int Java_org_andglk_MemoryStream_retainVmArray(JNIEnv *env, jobject this, int buffer, long len)
{
	Java_org_andglk_Window_retainVmArray(env, this, buffer, len);
}

void Java_org_andglk_MemoryStream_releaseVmArray(JNIEnv *env, jobject this, int buffer, int length, int dispatchRock)
{
	if (_vm_unreg_array)
		_vm_unreg_array((void *)buffer, length, gidispatch_char_array, INT2GDROCK(dispatchRock));
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

	(*env)->DeleteGlobalRef(env, _this);
	_this = 0;
	_vm_reg_array = 0;
	_vm_reg_object = 0;
	_vm_unreg_array = 0;
	_vm_unreg_object = 0;

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
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Character, "toLowerCase", "(C)C");

	return (*env)->CallStaticCharMethod(env, _Character, mid, (jchar) ch);
}

unsigned char glk_char_to_upper(unsigned char ch)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Character, "toUpperCase", "(C)C");

	return (*env)->CallStaticCharMethod(env, _Character, mid, (jchar) ch);
}

winid_t glk_window_get_root(void)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "getRoot", "()Lorg/andglk/Window;");

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
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "open", "(Lorg/andglk/Window;IIII)I");

	return (winid_t) (*env)->CallStaticIntMethod(env, _this, mid, split ? *split : 0, (jint) method, (jint) size, (jint) wintype, (jint) rock);
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
	jint *arr = (*env)->GetIntArrayElements(env, res, NULL);
	if (widthptr) *widthptr = arr[0];
	if (heightptr) *heightptr = arr[1];
	(*env)->ReleaseIntArrayElements(env, res, arr, JNI_ABORT);
	(*env)->DeleteLocalRef(env, res);
}

void glk_window_set_arrangement(winid_t win, glui32 method,
    glui32 size, winid_t keywin)
{
	if (!win)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _PairWindow, "setArrangement", "(IILorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, *win, mid, (jint) method, (jint) size, keywin ? *keywin : 0);
}

void glk_window_get_arrangement(winid_t win, glui32 *methodptr,
    glui32 *sizeptr, winid_t *keywinptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "window_get_arrangement", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win, methodptr, sizeptr, keywinptr);

}

winid_t glk_window_iterate(winid_t win, glui32 *rockptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Window, "iterate", "(Lorg/andglk/Window;)Lorg/andglk/Window;");

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
	return (*env)->CallIntMethod(env, _this, _getRock);
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
		mid = (*env)->GetMethodID(env, _Window, "getParent", "()Lorg/andglk/PairWindow;");

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
		mid = (*env)->GetMethodID(env, _Window, "getSibling", "()Lorg/andglk/Window;");

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
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "getStream", "()Lorg/andglk/Stream;");

	jobject obj = (*env)->CallObjectMethod(env, *win, mid);

	strid_t ret;
	if (obj)
		ret = (strid_t) (*env)->CallIntMethod(env, obj, _getPointer);
	else
		return 0;

	(*env)->DeleteLocalRef(env, obj);
	return ret;
}

void glk_window_set_echo_stream(winid_t win, strid_t str)
{
	if (!win)
		return;
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "setEchoStream", "(Lorg/andglk/Stream;)V");

	(*env)->CallVoidMethod(env, *win, mid, (str) ? *str : 0);
}

strid_t glk_window_get_echo_stream(winid_t win)
{
	if (!win)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "getEchoStream", "()I");

	return (strid_t) (*env)->CallIntMethod(env, *win, mid);
}

void glk_set_window(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "setWindow", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win ? *win : NULL);
}

strid_t glk_stream_open_file(frefid_t fileref, glui32 fmode, glui32 rock)
{
	if (!fileref)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Stream, "openFile", "(Lorg/andglk/FileRef;II)I");

	return (strid_t) (*env)->CallStaticIntMethod(env, _Stream, mid, *fileref, (jint) fmode, (jint) rock);
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

	jobject str = (*env)->NewObject(env, _MemoryStream, mid, (jint) buf, jbuf, (jint) fmode, (jint) rock);
	(*env)->DeleteLocalRef(env, jbuf);
	strid_t ret;
	if (!str)
		return 0;
	else
		ret = (strid_t) (*env)->CallIntMethod(env, str, _getPointer);

	(*env)->DeleteLocalRef(env, str);
	return ret;
}

void glk_stream_close(strid_t str, stream_result_t *result)
{
	if (!str)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "close", "()[I");

	jarray res = (*env)->CallObjectMethod(env, *str, mid);

	if (!result)
		return;

	jint *arr = (*env)->GetIntArrayElements(env, res, NULL);
	result->readcount = arr[0];
	result->writecount = arr[1];
	(*env)->ReleaseIntArrayElements(env, res, arr, JNI_ABORT);
	(*env)->DeleteLocalRef(env, res);
}

strid_t glk_stream_iterate(strid_t str, glui32 *rockptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Stream, "iterate", "(Lorg/andglk/Stream;)Lorg/andglk/Stream;");

	jobject nextstr = (*env)->CallStaticObjectMethod(env, _Stream, mid, str ? *str : 0);

	if (!nextstr)
		return 0;

	if (rockptr)
		*rockptr = (*env)->CallIntMethod(env, nextstr, _getRock);

	strid_t ret = (strid_t) (*env)->CallIntMethod(env, nextstr, _getPointer);

	(*env)->DeleteLocalRef(env, nextstr);
	return ret;
}

glui32 glk_stream_get_rock(strid_t str)
{
	JNIEnv *env = JNU_GetEnv();

	return (*env)->CallIntMethod(env, *str, _getRock);
}

void glk_stream_set_position(strid_t str, glsi32 pos, glui32 seekmode)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "setPosition", "(II)V");

	(*env)->CallVoidMethod(env, *str, mid, (jint) pos, (jint) seekmode);
}

glui32 glk_stream_get_position(strid_t str)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "getPosition", "()I");

	return (*env)->CallIntMethod(env, *str, mid);

}

void glk_stream_set_current(strid_t str)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Stream, "setCurrent", "(Lorg/andglk/Stream;)V");

	(*env)->CallStaticVoidMethod(env, _Stream, mid, str ? *str : 0);
}

strid_t glk_stream_get_current(void)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _Stream, "getCurrent", "()Lorg/andglk/Stream;");

	jobject str = (*env)->CallStaticObjectMethod(env, _Stream, mid);

	strid_t ret;
	if (str)
		ret = (strid_t) (*env)->CallIntMethod(env, str, _getPointer);
	else
		return 0;

	(*env)->DeleteLocalRef(env, str);
	return ret;
}

void glk_put_char(unsigned char ch)
{
	glk_put_char_stream(glk_stream_get_current(), ch);
}

void glk_put_char_stream(strid_t str, unsigned char ch)
{
	if (!str)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "putChar", "(C)V");

	(*env)->CallVoidMethod(env, *str, mid, (jchar) ch);
}

void glk_put_string(char *s)
{
	glk_put_string_stream(glk_stream_get_current(), s);
}

void glk_put_string_stream(strid_t stream, char *s)
{
	if (!stream || !s)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "putString", "(Ljava/lang/String;)V");

	jchar buf[1024];
	char *it = s;
	jchar *jt = buf;
	while (jt - buf < 1024 && *it)
		*(jt++) = *(it++);

	jstring str = (*env)->NewString(env, buf, it - s);

	(*env)->CallVoidMethod(env, *stream, mid, str);

	(*env)->DeleteLocalRef(env, str);
}

void glk_put_buffer(char *s, glui32 len)
{
	glk_put_buffer_stream(glk_stream_get_current(), s, len);
}

void glk_put_buffer_stream(strid_t stream, char *s, glui32 len)
{
	if (!stream || !s)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "putString", "(Ljava/lang/String;)V");

	jchar buf[len];
	char *it = s;
	jchar *jt = buf;
	while (jt - buf < len)
		*(jt++) = *(it++);

	jstring str = (*env)->NewString(env, buf, len);

	(*env)->CallVoidMethod(env, *stream, mid, str);

	(*env)->DeleteLocalRef(env, str);
}

void glk_set_style(glui32 styl)
{
	glk_set_style_stream(glk_stream_get_current(), styl);
}

void glk_set_style_stream(strid_t str, glui32 styl)
{
	if (!str)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "setStyle", "(J)V");

	(*env)->CallVoidMethod(env, *str, mid, (jlong) styl);
}

glsi32 glk_get_char_stream(strid_t str)
{
	if (!str)
		return;
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "getChar", "()I");

	return (*env)->CallIntMethod(env, *str, mid);
}

glui32 glk_get_line_stream(strid_t str, char *buf, glui32 len)
{
	if (!str)
		return;
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "getLine", "(I)Ljava/lang/String;");

	jstring result = (*env)->CallObjectMethod(env, *str, mid, len - 1);
	if (!result)
		return 0;

	int count = jstring2latin1(env, result, buf, len - 1);
	buf[count] = 0;


	(*env)->DeleteLocalRef(env, result);

	return count;
}

glui32 glk_get_buffer_stream(strid_t str, char *buf, glui32 len)
{
	if (!str)
		return;
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Stream, "getBuffer", "(I)[B");

	jarray result = (*env)->CallObjectMethod(env, *str, mid, len);
	if (!result)
		return 0;

	int count = (*env)->GetArrayLength(env, result);
	if (count > len)
		count = len;

	jbyte *jbufcontents = (jbyte *) (*env)->GetPrimitiveArrayCritical(env, result, NULL);
	memcpy(buf, jbufcontents, count);
	(*env)->ReleasePrimitiveArrayCritical(env, result, jbufcontents, 0);
	(*env)->DeleteLocalRef(env, result);

	return count;
}

void glk_stylehint_set(glui32 wintype, glui32 styl, glui32 hint, glsi32 val)
{
	__android_log_print(ANDROID_LOG_WARN, TAG, "style hint requested but not supported\n");
}

void glk_stylehint_clear(glui32 wintype, glui32 styl, glui32 hint)
{
	/* we don't currently support stylehints (TODO) */
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

frefid_t glk_fileref_create_temp(glui32 usage, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _FileRef, "createTemp", "(II)Lorg/andglk/FileRef;");

	jobject fref = (*env)->CallStaticObjectMethod(env, _FileRef, mid, (jint) usage, (jint) rock);

	if (!fref)
		return 0;

	frefid_t ret = (frefid_t) (*env)->CallIntMethod(env, fref, _getPointer);

	(*env)->DeleteLocalRef(env, fref);
	return ret;
}

frefid_t glk_fileref_create_by_name(glui32 usage, char *name, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _FileRef, "createByName", "(ILjava/lang/string;I)Lorg/andglk/FileRef;");

	int len = strnlen(name, 255);
	jchar jnamechars[len];
	int i;
	for (i = 0; i < len; i++)
		jnamechars[i] = (jchar) name[i];

	jstring jname = (*env)->NewString(env, jnamechars, len);

	jobject fref = (*env)->CallStaticObjectMethod(env, _FileRef, mid, (jint) usage, name, (jint) rock);

	(*env)->DeleteLocalRef(env, jname);

	if (!fref)
		return 0;

	frefid_t ret = (frefid_t) (*env)->CallIntMethod(env, fref, _getPointer);

	(*env)->DeleteLocalRef(env, fref);
	return ret;
}

frefid_t glk_fileref_create_by_prompt(glui32 usage, glui32 fmode, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _FileRef, "createByPrompt", "(III)I");

	return (frefid_t) (*env)->CallStaticIntMethod(env, _FileRef, mid, (jint) usage, (jint) fmode, (jint) rock);
}

frefid_t glk_fileref_create_from_fileref(glui32 usage, frefid_t fileref, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _FileRef, "createFromFileRef", "(ILorg/andglk/FileRef;I)Lorg/andglk/FileRef;");

	jobject fref = (*env)->CallStaticObjectMethod(env, _FileRef, mid, (jint) usage, *fileref, (jint) rock);
	if (!fref)
		return 0;

	frefid_t ret = (frefid_t) (*env)->CallIntMethod(env, fref, _getPointer);

	(*env)->DeleteLocalRef(env, fref);
	return ret;
}

void glk_fileref_destroy(frefid_t fref)
{
	if (!fref)
		return;

	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _FileRef, "destroy", "()V");

	(*env)->CallVoidMethod(env, *fref, mid);
}

frefid_t glk_fileref_iterate(frefid_t fref, glui32 *rockptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetStaticMethodID(env, _FileRef, "iterate", "(Lorg/andglk/FileRef;)Lorg/andglk/FileRef;");

	jobject nextfref = (*env)->CallStaticObjectMethod(env, _FileRef, mid, fref ? *fref : 0);

	if (!nextfref)
		return 0;

	if (rockptr)
		*rockptr = (*env)->CallIntMethod(env, nextfref, _getRock);

	frefid_t ret = (frefid_t) (*env)->CallIntMethod(env, nextfref, _getPointer);

	(*env)->DeleteLocalRef(env, nextfref);

	return ret;
}

glui32 glk_fileref_get_rock(frefid_t fref)
{
	JNIEnv *env = JNU_GetEnv();
	return (*env)->CallIntMethod(env, *fref, _getRock);
}

void glk_fileref_delete_file(frefid_t fref)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _FileRef, "deleteFile", "()V");

	(*env)->CallVoidMethod(env, *fref, mid);
}

glui32 glk_fileref_does_file_exist(frefid_t fref)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _FileRef, "doesFileExist", "()Z");

	return (*env)->CallBooleanMethod(env, *fref, mid);
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
			static jfieldID line_id = 0, buf_id, len_id, rock_id;
			if (0 == line_id) {
				line_id = (*env)->GetFieldID(env, _LineInputEvent, "line", "Ljava/lang/String;");
				buf_id = (*env)->GetFieldID(env, _LineInputEvent, "buffer", "I");
				len_id = (*env)->GetFieldID(env, _LineInputEvent, "len", "J");
				rock_id = (*env)->GetFieldID(env, _LineInputEvent, "rock", "I");
			}

			jstring line = (*env)->GetObjectField(env, ev, line_id);
			char * buf = (char *) (*env)->GetIntField(env, ev, buf_id);
			jlong len = (*env)->GetLongField(env, ev, len_id);
			glui32 rock = (*env)->GetIntField(env, ev, rock_id);
			event->val1 = jstring2latin1(env, line, buf, len);
			if (event->val1 != len)
				buf[event->val1] = 0;

			if (_vm_unreg_array)
				_vm_unreg_array(buf, len, gidispatch_char_array, INT2GDROCK(rock));

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
	}
}

void glk_select(event_t *event)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "select", "()Lorg/andglk/Event;");

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
	event->type = evtype_None;
}

void glk_request_timer_events(glui32 millisecs)
{
	/* TODO */
}

void glk_request_line_event(winid_t win, char *buf, glui32 maxlen, glui32 initlen)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;

	if (mid == 0)
		mid = (*env)->GetMethodID(env, _Window, "requestLineEvent", "(Ljava/lang/String;JI)V");

	jstring str = 0;
	jchar jbuf[initlen];

	if (initlen > 0) {
		char *it = buf;
		jchar *jt = jbuf;
		while (jt - jbuf < initlen)
			*(jt++) = *(it++);

		str = (*env)->NewString(env, jbuf, maxlen);
	}

	(*env)->CallVoidMethod(env, *win, mid, str, (jlong) maxlen, (jint) buf);

	if (str)
		(*env)->DeleteLocalRef(env, str);
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
		mid = (*env)->GetMethodID(env, _Window, "cancelLineEvent", "()Lorg/andglk/LineInputEvent;");

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

void gidispatch_set_object_registry(gidispatch_rock_t (*regi)(void *obj, glui32 objclass),
		void (*unregi)(void *obj, glui32 objclass, gidispatch_rock_t objrock))
{
	_vm_reg_object = regi;
	_vm_unreg_object = unregi;
}

gidispatch_rock_t gidispatch_get_objrock(void *obj, glui32 objclass)
{
	JNIEnv *env = JNU_GetEnv();
	glui32 rock = (*env)->CallIntMethod(env, obj, _getDispatchRock);
	return INT2GDROCK(rock);
}

void gidispatch_set_retained_registry(gidispatch_rock_t (*regi)(void *array, glui32 len, char *typecode),
		void (*unregi)(void *array, glui32 len, char *typecode, gidispatch_rock_t objrock))
{
	_vm_reg_array = regi;
	_vm_unreg_array = unregi;
}
