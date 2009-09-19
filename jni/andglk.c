#include <jni.h>
#include "glk.h"

JavaVM *_jvm;
jclass _class;
JNIEnv *_env;
jobject _this;

#define GLK_JNI_VERSION JNI_VERSION_1_2

jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	_jvm = jvm;

	JNIEnv *env;
	if ((*jvm)->GetEnv(jvm, (void **)&env, GLK_JNI_VERSION))
		return JNI_ERR;

	jclass cls = (*env)->FindClass(env, "org/andglk/Glk");
	_class = (*env)->NewGlobalRef(env, cls);

	return GLK_JNI_VERSION;
}

void Java_org_andglk_Glk_start(JNIEnv *env, jobject this)
{
	_this = (*env)->NewGlobalRef(env, this);
	glk_main();
}


JNIEnv *JNU_GetEnv()
{
    JNIEnv *env;
    (*_jvm)->GetEnv(_jvm,
                          (void **)&env,
                          JNI_VERSION_1_2);
    return env;
}

void glk_exit(void)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_exit", "()V");

	(*env)->CallVoidMethod(env, _this, mid);

}

void glk_set_interrupt_handler(void (*func)(void))
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_set_interrupt_handler", "(L)V");

	(*env)->CallVoidMethod(env, _this, mid, func);

}

void glk_tick(void)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_tick", "()V");

	(*env)->CallVoidMethod(env, _this, mid);

}

glui32 glk_gestalt(glui32 sel, glui32 val)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_gestalt", "(JJ)J");

	return (*env)->CallLongMethod(env, _this, mid, sel, val);

}

glui32 glk_gestalt_ext(glui32 sel, glui32 val, glui32 *arr,
    glui32 arrlen)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_gestalt_ext", "(JJ[JJ)J");

	// FIXME: array translation
	return (*env)->CallLongMethod(env, _this, mid, sel, val, arr, arrlen);

}

unsigned char glk_char_to_lower(unsigned char ch)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_char_to_lower", "(C)C");

	return (*env)->CallCharMethod(env, _this, mid, ch);

}

unsigned char glk_char_to_upper(unsigned char ch)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_char_to_upper", "(C)C");

	return (*env)->CallCharMethod(env, _this, mid, ch);

}

winid_t glk_window_get_root(void)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_root", "()Lorg/andglk/Window;");

	return (*env)->CallObjectMethod(env, _this, mid);

}

winid_t glk_window_open(winid_t split, glui32 method, glui32 size,
    glui32 wintype, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_open", "(Lorg/andglk/Window;JJJJ)Lorg/andglk/Window;");

	return (*env)->CallObjectMethod(env, _this, mid, split, method, size, wintype, rock);

}

void glk_window_close(winid_t win, stream_result_t *result)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_close", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win, result);

}

void glk_window_get_size(winid_t win, glui32 *widthptr,
    glui32 *heightptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_size", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win, widthptr, heightptr);

}

void glk_window_set_arrangement(winid_t win, glui32 method,
    glui32 size, winid_t keywin)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_set_arrangement", "()V");

	(*env)->CallVoidMethod(env, _this, mid, win, method, size, keywin);

}

void glk_window_get_arrangement(winid_t win, glui32 *methodptr,
    glui32 *sizeptr, winid_t *keywinptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_arrangement", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win, methodptr, sizeptr, keywinptr);

}

winid_t glk_window_iterate(winid_t win, glui32 *rockptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_iterate", "(Lorg/andglk/Window;)Lorg/andglk/Window;");

	return (*env)->CallObjectMethod(env, _this, mid, win, rockptr);

}

glui32 glk_window_get_rock(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_rock", "(Lorg/andglk/Window;)J");

	return (*env)->CallLongMethod(env, _this, mid, win);

}

glui32 glk_window_get_type(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_type", "(Lorg/andglk/Window;)J");

	return (*env)->CallLongMethod(env, _this, mid, win);

}

winid_t glk_window_get_parent(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_parent", "(Lorg/andglk/Window;)Lorg/andglk/Window;");

	return (*env)->CallObjectMethod(env, _this, mid, win);

}

winid_t glk_window_get_sibling(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_sibling", "(Lorg/andglk/Window;)Lorg/andglk/Window;");

	return (*env)->CallObjectMethod(env, _this, mid, win);

}

void glk_window_clear(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_clear", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win);

}

void glk_window_move_cursor(winid_t win, glui32 xpos, glui32 ypos)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_move_cursor", "(Lorg/andglk/Window;JJ)V");

	(*env)->CallVoidMethod(env, _this, mid, win, xpos, ypos);

}

strid_t glk_window_get_stream(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_stream", "(Lorg/andglk/Window;)Lorg/andglk/Stream;");

	return (*env)->CallObjectMethod(env, _this, mid, win);

}

void glk_window_set_echo_stream(winid_t win, strid_t str)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_set_echo_stream", "(Lorg/andglk/Window;Lorg/andglk/Stream;)V");

	(*env)->CallVoidMethod(env, _this, mid, win, str);

}

strid_t glk_window_get_echo_stream(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_window_get_echo_stream", "(Lorg/andglk/Window;)Lorg/andglk/Stream;");

	return (*env)->CallObjectMethod(env, _this, mid, win);

}

void glk_set_window(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_set_window", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win);

}

strid_t glk_stream_open_file(frefid_t fileref, glui32 fmode,
    glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_open_file", "(Lorg/andglk/FileRef;JJ)Lorg/andglk/Stream;");

	return (*env)->CallObjectMethod(env, _this, mid, fileref, fmode, rock);

}

strid_t glk_stream_open_memory(char *buf, glui32 buflen, glui32 fmode,
    glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_open_memory", "(JJJ)Lorg/andglk/Stream;");

	return (*env)->CallObjectMethod(env, _this, mid, buf, buflen, fmode, rock);

}

void glk_stream_close(strid_t str, stream_result_t *result)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_close", "(Lorg/andglk/Stream;[FIXME: stream_result_t *])V");

	(*env)->CallVoidMethod(env, _this, mid, str, result);

}

strid_t glk_stream_iterate(strid_t str, glui32 *rockptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_iterate", "(Lorg/andglk/Stream;[FIXME: glui32 *])Lorg/andglk/Stream;");

	return (*env)->CallObjectMethod(env, _this, mid, str, rockptr);

}

glui32 glk_stream_get_rock(strid_t str)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_get_rock", "(Lorg/andglk/Stream;)J");

	return (*env)->CallLongMethod(env, _this, mid, str);

}

void glk_stream_set_position(strid_t str, glsi32 pos, glui32 seekmode)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_set_position", "(Lorg/andglk/Stream;IJ)V");

	(*env)->CallVoidMethod(env, _this, mid, str, pos, seekmode);

}

glui32 glk_stream_get_position(strid_t str)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_get_position", "(Lorg/andglk/Stream;)J");

	return (*env)->CallLongMethod(env, _this, mid, str);

}

void glk_stream_set_current(strid_t str)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_set_current", "(Lorg/andglk/Stream;)V");

	(*env)->CallVoidMethod(env, _this, mid, str);

}

strid_t glk_stream_get_current(void)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stream_get_current", "()Lorg/andglk/Stream;");

	return (*env)->CallObjectMethod(env, _this, mid);

}

void glk_put_char(unsigned char ch)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_put_char", "(C)V");

	(*env)->CallVoidMethod(env, _this, mid, ch);

}

void glk_put_char_stream(strid_t str, unsigned char ch)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_put_char_stream", "(Lorg/andglk/Stream;C)V");

	(*env)->CallVoidMethod(env, _this, mid, str, ch);

}

void glk_put_string(char *s)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_put_string", "([FIXME: char *])V");

	(*env)->CallVoidMethod(env, _this, mid, s);

}

void glk_put_string_stream(strid_t str, char *s)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_put_string_stream", "(Lorg/andglk/Stream;[FIXME: char *])V");

	(*env)->CallVoidMethod(env, _this, mid, str, s);

}

void glk_put_buffer(char *buf, glui32 len)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_put_buffer", "([FIXME: char *]J)V");

	(*env)->CallVoidMethod(env, _this, mid, buf, len);

}

void glk_put_buffer_stream(strid_t str, char *buf, glui32 len)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_put_buffer_stream", "(Lorg/andglk/Stream;[FIXME: char *]J)V");

	(*env)->CallVoidMethod(env, _this, mid, str, buf, len);

}

void glk_set_style(glui32 styl)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_set_style", "(J)V");

	(*env)->CallVoidMethod(env, _this, mid, styl);

}

void glk_set_style_stream(strid_t str, glui32 styl)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_set_style_stream", "(Lorg/andglk/Stream;J)V");

	(*env)->CallVoidMethod(env, _this, mid, str, styl);

}

glsi32 glk_get_char_stream(strid_t str)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_get_char_stream", "(Lorg/andglk/Stream;)I");

	return (*env)->CallIntMethod(env, _this, mid, str);

}

glui32 glk_get_line_stream(strid_t str, char *buf, glui32 len)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_get_line_stream", "(Lorg/andglk/Stream;[FIXME: char *]J)J");

	return (*env)->CallLongMethod(env, _this, mid, str, buf, len);

}

glui32 glk_get_buffer_stream(strid_t str, char *buf, glui32 len)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_get_buffer_stream", "(Lorg/andglk/Stream;[FIXME: char *]J)J");

	return (*env)->CallLongMethod(env, _this, mid, str, buf, len);

}

void glk_stylehint_set(glui32 wintype, glui32 styl, glui32 hint,
    glsi32 val)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stylehint_set", "(JJJI)V");

	(*env)->CallVoidMethod(env, _this, mid, wintype, styl, hint, val);

}

void glk_stylehint_clear(glui32 wintype, glui32 styl, glui32 hint)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_stylehint_clear", "(JJJ)V");

	(*env)->CallVoidMethod(env, _this, mid, wintype, styl, hint);

}

glui32 glk_style_distinguish(winid_t win, glui32 styl1, glui32 styl2)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_style_distinguish", "(Lorg/andglk/Window;JJ)J");

	return (*env)->CallLongMethod(env, _this, mid, win, styl1, styl2);

}

glui32 glk_style_measure(winid_t win, glui32 styl, glui32 hint,
    glui32 *result)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_style_measure", "(Lorg/andglk/Window;JJ[FIXME: glui32 *])J");

	return (*env)->CallLongMethod(env, _this, mid, win, styl, hint, result);

}

frefid_t glk_fileref_create_temp(glui32 usage, glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_create_temp", "(JJ)Lorg/andglk/FileRef;");

	return (*env)->CallObjectMethod(env, _this, mid, usage, rock);

}

frefid_t glk_fileref_create_by_name(glui32 usage, char *name,
    glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_create_by_name", "(J[FIXME: char *]J)Lorg/andglk/FileRef;");

	return (*env)->CallObjectMethod(env, _this, mid, usage, name, rock);

}

frefid_t glk_fileref_create_by_prompt(glui32 usage, glui32 fmode,
    glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_create_by_prompt", "(JJJ)Lorg/andglk/FileRef;");

	return (*env)->CallObjectMethod(env, _this, mid, usage, fmode, rock);

}

frefid_t glk_fileref_create_from_fileref(glui32 usage, frefid_t fref,
    glui32 rock)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_create_from_fileref", "(JLorg/andglk/FileRef;J)Lorg/andglk/FileRef;");

	return (*env)->CallObjectMethod(env, _this, mid, usage, fref, rock);

}

void glk_fileref_destroy(frefid_t fref)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_destroy", "(Lorg/andglk/FileRef;)V");

	(*env)->CallVoidMethod(env, _this, mid, fref);

}

frefid_t glk_fileref_iterate(frefid_t fref, glui32 *rockptr)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_iterate", "(Lorg/andglk/FileRef;[FIXME: glui32 *])Lorg/andglk/FileRef;");

	return (*env)->CallObjectMethod(env, _this, mid, fref, rockptr);

}

glui32 glk_fileref_get_rock(frefid_t fref)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_get_rock", "(Lorg/andglk/FileRef;)J");

	return (*env)->CallLongMethod(env, _this, mid, fref);

}

void glk_fileref_delete_file(frefid_t fref)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_delete_file", "(Lorg/andglk/FileRef;)V");

	(*env)->CallVoidMethod(env, _this, mid, fref);

}

glui32 glk_fileref_does_file_exist(frefid_t fref)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_fileref_does_file_exist", "(Lorg/andglk/FileRef;)J");

	return (*env)->CallLongMethod(env, _this, mid, fref);

}

void glk_select(event_t *event)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_select", "([FIXME: event_t *])V");

	(*env)->CallVoidMethod(env, _this, mid, event);

}

void glk_select_poll(event_t *event)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_select_poll", "([FIXME: event_t *])V");

	(*env)->CallVoidMethod(env, _this, mid, event);

}

void glk_request_timer_events(glui32 millisecs)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_request_timer_events", "(J)V");

	(*env)->CallVoidMethod(env, _this, mid, millisecs);

}

void glk_request_line_event(winid_t win, char *buf, glui32 maxlen,
    glui32 initlen)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_request_line_event", "(Lorg/andglk/Window;[FIXME: char *]JJ)V");

	(*env)->CallVoidMethod(env, _this, mid, win, buf, maxlen, initlen);

}

void glk_request_char_event(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_request_char_event", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win);

}

void glk_request_mouse_event(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_request_mouse_event", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win);

}

void glk_cancel_line_event(winid_t win, event_t *event)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_cancel_line_event", "(Lorg/andglk/Window;[FIXME: event_t *])V");

	(*env)->CallVoidMethod(env, _this, mid, win, event);

}

void glk_cancel_char_event(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_cancel_char_event", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win);

}

void glk_cancel_mouse_event(winid_t win)
{
	JNIEnv *env = JNU_GetEnv();
	static jmethodID mid = 0;
	if (mid == 0)
		mid = (*env)->GetMethodID(env, _class, "glk_cancel_mouse_event", "(Lorg/andglk/Window;)V");

	(*env)->CallVoidMethod(env, _this, mid, win);

}
