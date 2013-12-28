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

#include "glk.h"
#include "garglk.h"

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
    //exit(0);
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
