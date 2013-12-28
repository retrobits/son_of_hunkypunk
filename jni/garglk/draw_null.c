/******************************************************************************
 *                                                                            *
 * Copyright (C) 2006-2009 by Tor Andersson, Jesse McGrew.                    *
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "glk.h"
#include "garglk.h"

static unsigned char gammamap[256];

//static font_t gfont_table[8];

int gli_cellw = 8;
int gli_cellh = 8;

int gli_image_s = 0;
int gli_image_w = 0;
int gli_image_h = 0;
unsigned char *gli_image_rgb = NULL;

static const int gli_bpp = 3;


//static FT_Library ftlib;
//static FT_Matrix ftmat;

void gli_get_builtin_font(int idx, unsigned char **ptr, unsigned int *len);

static int touni(int enc)
{
    return 0;
}

static void gammacopy(unsigned char *dst, unsigned char *src, int n)
{
}

static void gammacopy_lcd(unsigned char *dst, unsigned char *src, int w, int h, int pitch)
{
}

void gli_initialize_fonts(void)
{
}

/*
 * Drawing
 */

void gli_draw_pixel(int x, int y, unsigned char alpha, unsigned char *rgb)
{
}

void gli_draw_pixel_lcd(int x, int y, unsigned char *alpha, unsigned char *rgb)
{
}

/*
static inline void draw_bitmap(bitmap_t *b, int x, int y, unsigned char *rgb)
{
}

static inline void draw_bitmap_lcd(bitmap_t *b, int x, int y, unsigned char *rgb)
{
}
*/

void gli_draw_clear(unsigned char *rgb)
{
}

void gli_draw_rect(int x0, int y0, int w, int h, unsigned char *rgb)
{
}

/*
static int charkern(font_t *f, int c0, int c1)
{
    return 0;
}

static void getglyph(font_t *f, glui32 cid, int *adv, bitmap_t **glyphs)
{
}
*/

int gli_string_width(int fidx, unsigned char *s, int n, int spw)
{
    return 0;
}

int gli_draw_string(int x, int y, int fidx, unsigned char *rgb,
        unsigned char *s, int n, int spw)
{
    return 0;
}

int gli_draw_string_uni(int x, int y, int fidx, unsigned char *rgb,
        glui32 *s, int n, int spw)
{
    return 0;
}

int gli_string_width_uni(int fidx, glui32 *s, int n, int spw)
{
    return 0;
}

void gli_draw_caret(int x, int y)
{
}

void gli_draw_picture(picture_t *src, int x0, int y0, int dx0, int dy0, int dx1, int dy1)
{
}

