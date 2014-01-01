/******************************************************************************
 *                                                                            *
 * Copyright (C) 2006-2009 by Tor Andersson.                                  *
 * Copyright (C) 2010 by Ben Cressey, Chris Spiegel.                          *
 *                                                                            *
 * This file is part of Gargoyle.                                             *
 *                                                                            *
 * Gargoyle is free software; you can redistribute it and/or modify           *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation; either version 2 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * Gargoyle is distributed in the hope that it will be useful,                *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with Gargoyle; if not, write to the Free Software                    *
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA *
 *                                                                            *
 *****************************************************************************/

/* TODO: add mouse down event */

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <setjmp.h>

#include <jni.h>
#include <android/log.h>

#include "glk.h"
#include "glkstart.h"
#include "garglk.h"

#define TAG "sysand.c"

extern jobject _this;
extern jclass _class;

//static char* gidispatch_char_array = "&+#!Cn";

#define GLK_JNI_VERSION JNI_VERSION_1_2

/* these are OK to keep */
static JavaVM *_jvm;
jclass _class;
static jclass _Event, _LineInputEvent, _Window, _FileRef, _Stream, _PairWindow, _TextGridWindow,
	_CharInputEvent, _ArrangeEvent, _MemoryStream, _CPointed, _ExitEvent, _AutoSaveEvent, _AutoRestoreEvent;
static jmethodID _getRock, _getPointer, _getDispatchRock, _getDispatchClass;
static jmp_buf _quit_env;

/* this should be nulled on exit */
jobject _this = 0;

#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "HunkyPunk", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "HunkyPunk", __VA_ARGS__) 

//#define DEBUG_LOGGING
#ifdef DEBUG_LOGGING
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "HunkyPunk", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "HunkyPunk", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "HunkyPunk", __VA_ARGS__)
#define LOG(...)  __android_log_print(ANDROID_LOG_DEBUG  , "HunkyPunk", __VA_ARGS__)
#else
#define LOGV(...) 
#define LOGD(...) 
#define LOGI(...) 
#define LOG(...)  
#endif

JNIEnv *JNU_GetEnv()
{
	JNIEnv *env;
	(*_jvm)->GetEnv(_jvm, (void **)&env, GLK_JNI_VERSION);
	return env;
}

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


static int timeout(void *data)
{
    return TRUE;
}

void glk_request_timer_events(glui32 millisecs)
{
}

void winabort(const char *fmt, ...)
{
    va_list ap;
    char buf[256];
    va_start(ap, fmt);
    vsprintf(buf, fmt, ap);
    va_end(ap);
    // XXX MessageBoxA(NULL, buf, "Fatal error", MB_ICONERROR);
	//abort();
}

void winexit(void)
{
	JNIEnv *env = JNU_GetEnv();

	glk_select_poll(NULL); /* flush any pending output */

	gli_streams_close_all();
	//todo gli_fileref_delete_all();
	//todo (*env)->DeleteGlobalRef(env, _this);
	_this = 0;

	// any cleaner way to have glk_exit() not returning (as per spec)?
	longjmp(_quit_env, 1);
}

void winchoosefile(char *prompt, char *buf, int len, int filter) 
//, GtkFileChooserAction action, const char *button)
{
	//show save dialog

	/*
    if (result == GTK_RESPONSE_ACCEPT)
        strcpy(buf, gtk_file_chooser_get_filename(GTK_FILE_CHOOSER(filedlog)));
    else
        strcpy(buf, "");
	*/
}

void winopenfile(char *prompt, char *buf, int len, int filter)
{
    char realprompt[256];
    sprintf(realprompt, "Open: %s", prompt);
    winchoosefile(realprompt, buf, len, filter); //,GTK_FILE_CHOOSER_ACTION_OPEN, GTK_STOCK_OPEN);
}

void winsavefile(char *prompt, char *buf, int len, int filter)
{
    char realprompt[256];
    sprintf(realprompt, "Save: %s", prompt);
    winchoosefile(realprompt, buf, len, filter); //,GTK_FILE_CHOOSER_ACTION_SAVE, GTK_STOCK_SAVE);
}

void winclipstore(glui32 *text, int len)
{
}

void winclipsend(int source)
{
}

void winclipreceive(int source)
{
}

void wininit(int *argc, char **argv)
{
	gli_conf_graphics = FALSE;
	gli_conf_sound = FALSE;
}

void winopen(void)
{
}

void wintitle(void)
{
    char buf[256];

    if (strlen(gli_story_title))
        sprintf(buf, "%s", gli_story_title);
    else if (strlen(gli_story_name))
        sprintf(buf, "%s - %s", gli_story_name, gli_program_name);
    else
        sprintf(buf, "%s", gli_program_name);
    //set_title(buf);
}

void winrepaint(int x0, int y0, int x1, int y1)
{
}

void gli_select(event_t *event, int polled)
{
	/*
    gli_curevent = event;
    gli_event_clearevent(event);

    while (gtk_events_pending())
        gtk_main_iteration();
    gli_dispatch_event(gli_curevent, polled);

    if (!polled)
    {
        while (gli_curevent->type == evtype_None && !timeouts)
        {
            gtk_main_iteration();
            gli_dispatch_event(gli_curevent, polled);
        }
    }

    if (gli_curevent->type == evtype_None && timeouts)
    {
        gli_event_store(evtype_Timer, NULL, 0, 0);
        gli_dispatch_event(gli_curevent, polled);
        timeouts = 0;
    }

    gli_curevent = NULL;
	*/
}

/* monotonic clock for profiling */
/*
void wincounter(glktimeval_t *time)
{

    struct timespec tick;
    clock_gettime(CLOCK_MONOTONIC, &tick);

    time->high_sec = 0;
    time->low_sec  = (unsigned int) tick.tv_sec;
    time->microsec = (unsigned int) tick.tv_nsec / 1000;
}
*/

/* todo: move hooks to header */
void (*glk_signal_autorestore_hook)(char* path) = NULL;
void (*glk_signal_autosave_hook)(char* path) = NULL;

void andglk_loader_glk_main2(JavaVM* jvm, JNIEnv *env, jobject this,int argc, char *argv[])
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
		glkunix_startup_t startdata;
		startdata.argc = argc;
		startdata.argv = malloc(argc * sizeof(char*));
		memcpy(startdata.argv, argv, argc * sizeof(char*));

		gli_startup(argc, argv);

		if (glkunix_startup_code(&startdata))
			glk_main();

		free(startdata.argv);
		glk_exit();
	}
}

/* functions to implement */

/* window.c */
void glk_window_get_size(window_t *win, glui32 *width, glui32 *height){}
void gli_windows_rearrange(void){}
void gli_windows_redraw(){}
void gli_window_redraw(window_t *win){}

/* winpair.c */
window_pair_t *win_pair_create(window_t *win, glui32 method, window_t *key, glui32 size){return NULL;}
void win_pair_destroy(window_pair_t *dwin){}
void win_pair_rearrange(window_t *win, rect_t *box){}
void win_pair_redraw(window_t *win){}
void win_pair_click(window_pair_t *dwin, int x, int y){}

/* wintext.c */
window_textbuffer_t *win_textbuffer_create(window_t *win) {return NULL;}
void win_textbuffer_destroy(window_textbuffer_t *dwin) {}
void win_textbuffer_rearrange(window_t *win, rect_t *box) {}
void win_textbuffer_redraw(window_t *win) {}
void win_textbuffer_putchar_uni(window_t *win, glui32 ch) {}
int win_textbuffer_unputchar_uni(window_t *win, glui32 ch) {return 0;}
void win_textbuffer_clear(window_t *win) {}
void win_textbuffer_init_line(window_t *win, char *buf, int maxlen, int initlen) {}
void win_textbuffer_init_line_uni(window_t *win, glui32 *buf, int maxlen, int initlen){}
void win_textbuffer_cancel_line(window_t *win, event_t *ev){}
void win_textbuffer_click(window_textbuffer_t *dwin, int x, int y){}
void gcmd_buffer_accept_readchar(window_t *win, glui32 arg){}
void gcmd_buffer_accept_readline(window_t *win, glui32 arg){}
int gcmd_accept_scroll(window_t *win, glui32 arg){return 0;}
glui32 win_textbuffer_draw_picture(window_textbuffer_t *dwin, glui32 image, glui32 align, glui32 scaled, glui32 width, glui32 height){return 0;}
glui32 win_textbuffer_flow_break(window_textbuffer_t *dwin){return 0;}

/* wingrid.c */
window_textgrid_t *win_textgrid_create(window_t *win){return NULL;}
void win_textgrid_destroy(window_textgrid_t *dwin){}
void win_textgrid_rearrange(window_t *win, rect_t *box){}
void win_textgrid_redraw(window_t *win){}
void win_textgrid_putchar_uni(window_t *win, glui32 ch){}
int win_textgrid_unputchar_uni(window_t *win, glui32 ch){return 0;}
void win_textgrid_clear(window_t *win){}
void win_textgrid_move_cursor(window_t *win, int xpos, int ypos){}
void win_textgrid_init_line(window_t *win, char *buf, int maxlen, int initlen){}
void win_textgrid_init_line_uni(window_t *win, glui32 *buf, int maxlen, int initlen){}
void win_textgrid_cancel_line(window_t *win, event_t *ev){}
void win_textgrid_click(window_textgrid_t *dwin, int x, int y){}
void gcmd_grid_accept_readchar(window_t *win, glui32 arg){}
void gcmd_grid_accept_readline(window_t *win, glui32 arg){}

/* null implementations */

/* winmask.c */
int gli_copyselect = FALSE;
int gli_drawselect = FALSE;
int gli_claimselect = FALSE;

/* wingfx.c */
window_graphics_t *win_graphics_create(window_t *win){return NULL;}
void win_graphics_destroy(window_graphics_t *cutwin){}
void win_graphics_rearrange(window_t *win, rect_t *box){}
void win_graphics_get_size(window_t *win, glui32 *width, glui32 *height){}
void win_graphics_redraw(window_t *win){}
void win_graphics_click(window_graphics_t *dwin, int x, int y){}
glui32 win_graphics_draw_picture(window_graphics_t *cutwin,
								 glui32 image, glsi32 xpos, glsi32 ypos,
								 int scale, glui32 imagewidth, glui32 imageheight){return 0;}
void win_graphics_erase_rect(window_graphics_t *cutwin, int whole, glsi32 xpos, glsi32 ypos, glui32 width, glui32 height){}
void win_graphics_fill_rect(window_graphics_t *cutwin, glui32 color, glsi32 xpos, glsi32 ypos, glui32 width, glui32 height){}
void win_graphics_set_background_color(window_graphics_t *cutwin, glui32 color){}

/* babeldata.c */
void gli_initialize_babel(void){}

/* draw.c */
void gli_initialize_fonts(void){}

/* imgload.c */
picture_t *gli_picture_load(unsigned long id){return NULL;}
void gli_piclist_decrement(void){}
