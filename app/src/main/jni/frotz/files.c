/* files.c - Transscription, recording and playback
 *	Copyright (c) 1995-1997 Stefan Jokisch
 *
 * This file is part of Frotz.
 *
 * Frotz is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frotz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */

#include "glkfrotz.h"
#include "glk.h"
#include "glkio.h"

extern void set_more_prompts (bool);

extern bool is_terminator (zchar);

extern bool read_yes_or_no (const char *);

static int script_width = 0;

static FILE *sfp = NULL;
static FILE *rfp = NULL;
static FILE *pfp = NULL;

/*
 * script_open
 *
 * Open the transscript file. 'AMFV' makes this more complicated as it
 * turns transscription on/off several times to exclude some text from
 * the transscription file. This wasn't a problem for the original V4
 * interpreters which always sent transscription to the printer, but it
 * means a problem to modern interpreters that offer to open a new file
 * every time transscription is turned on. Our solution is to append to
 * the old transscription file in V1 to V4, and to ask for a new file
 * name in V5+.
 *
 */

static bool script_valid = FALSE;

void script_open (void)
{

	h_flags &= ~SCRIPTING_FLAG;

	if (h_version < V5 && script_valid)
		sfp = frotzreopen(FILE_SCRIPT);
	else
		sfp = frotzopenprompt(FILE_SCRIPT);

	if (sfp != NULL)
	{
		fseek (sfp, 0, SEEK_END);

		h_flags |= SCRIPTING_FLAG;

		script_valid = TRUE;
		ostream_script = TRUE;

		script_width = 0;

	} else print_string ("Cannot open file\n");

	SET_WORD (H_FLAGS, h_flags)

}/* script_open */

/*
 * script_close
 *
 * Stop transscription.
 *
 */

void script_close (void)
{

	h_flags &= ~SCRIPTING_FLAG;
	SET_WORD (H_FLAGS, h_flags)

		fclose (sfp); ostream_script = FALSE;

}/* script_close */

/*
 * script_new_line
 *
 * Write a newline to the transscript file.
 *
 */

void script_new_line (void)
{

	if (fputc ('\n', sfp) == EOF)
		script_close ();

	script_width = 0;

}/* script_new_line */

/*
 * script_char
 *
 * Write a single character to the transscript file.
 *
 */

void script_char (zchar c)
{

	if (c == ZC_INDENT && script_width != 0)
		c = ' ';

	if (c == ZC_INDENT)
	{ script_char (' '); script_char (' '); script_char (' '); return; }
	if (c == ZC_GAP)
	{ script_char (' '); script_char (' '); return; }

	fputwc (c, sfp); script_width++;

}/* script_char */

/*
 * script_word
 *
 * Write a string to the transscript file.
 *
 */

void script_word (const zchar *s)
{
	int width;
	int i;

	if (*s == ZC_INDENT && script_width != 0)
		script_char (*s++);

	for (i = 0, width = 0; s[i] != 0; i++)

		if (s[i] == ZC_NEW_STYLE || s[i] == ZC_NEW_FONT)
			i++;
		else if (s[i] == ZC_GAP)
			width += 3;
		else if (s[i] == ZC_INDENT)
			width += 2;
		else
			width += 1;

	if (option_script_cols != 0 && script_width + width > option_script_cols) {

		if (*s == ' ' || *s == ZC_INDENT || *s == ZC_GAP)
			s++;

		script_new_line ();

	}

	for (i = 0; s[i] != 0; i++)

		if (s[i] == ZC_NEW_FONT || s[i] == ZC_NEW_STYLE)
			i++;
		else
			script_char (s[i]);

}/* script_word */

/*
 * script_write_input
 *
 * Send an input line to the transscript file.
 *
 */

void script_write_input (const zchar *buf, zchar key)
{
	int width;
	int i;

	for (i = 0, width = 0; buf[i] != 0; i++)
		width++;

	if (option_script_cols != 0 && script_width + width > option_script_cols)
		script_new_line ();

	for (i = 0; buf[i] != 0; i++)
		script_char (buf[i]);

	if (key == ZC_RETURN)
		script_new_line ();

}/* script_write_input */

/*
 * script_erase_input
 *
 * Remove an input line from the transscript file.
 *
 */

void script_erase_input (const zchar *buf)
{
	int width;
	int i;

	for (i = 0, width = 0; buf[i] != 0; i++)
		width++;

	fseek (sfp, -width, SEEK_CUR); script_width -= width;

}/* script_erase_input */

/*
 * script_mssg_on
 *
 * Start sending a "debugging" message to the transscript file.
 *
 */

void script_mssg_on (void)
{

	if (script_width != 0)
		script_new_line ();

	script_char (ZC_INDENT);

}/* script_mssg_on */

/*
 * script_mssg_off
 *
 * Stop writing a "debugging" message.
 *
 */

void script_mssg_off (void)
{

	script_new_line ();

}/* script_mssg_off */

/*
 * record_open
 *
 * Open a file to record the player's input.
 *
 */

void record_open (void)
{
	if ((rfp = frotzopenprompt(FILE_RECORD)) != NULL)
		ostream_record = TRUE;
	else
		print_string ("Cannot open file\n");
}

/*
 * record_close
 *
 * Stop recording the player's input.
 *
 */

void record_close (void)
{

	fclose (rfp); ostream_record = FALSE;

}/* record_close */

/*
 * record_code
 *
 * Helper function for record_char.
 *
 */

static void record_code (int c, bool force_encoding)
{

	if (force_encoding || c == '[' || c < 0x20 || c > 0x7e) {

		int i;

		fputc ('[', rfp);

		for (i = 10000; i != 0; i /= 10)
			if (c >= i || i == 1)
				fputc ('0' + (c / i) % 10, rfp);

		fputc (']', rfp);

	} else fputc (c, rfp);

}/* record_code */

/*
 * record_char
 *
 * Write a character to the command file.
 *
 */

static void record_char (zchar c)
{

	if (c != ZC_RETURN) {
		if (c < ZC_HKEY_MIN || c > ZC_HKEY_MAX) {
			record_code (translate_to_zscii (c), FALSE);
			if (c == ZC_SINGLE_CLICK || c == ZC_DOUBLE_CLICK) {
				record_code (mouse_x, TRUE);
				record_code (mouse_y, TRUE);
			}
		} else record_code (1000 + c - ZC_HKEY_MIN, TRUE);
	}

}/* record_char */

/*
 * record_write_key
 *
 * Copy a keystroke to the command file.
 *
 */

void record_write_key (zchar key)
{

	record_char (key);

	if (fputc ('\n', rfp) == EOF)
		record_close ();

}/* record_write_key */

/*
 * record_write_input
 *
 * Copy a line of input to a command file.
 *
 */

void record_write_input (const zchar *buf, zchar key)
{
	zchar c;

	while ((c = *buf++) != 0)
		record_char (c);

	record_char (key);

	if (fputc ('\n', rfp) == EOF)
		record_close ();

}/* record_write_input */

/*
 * replay_open
 *
 * Open a file of commands for playback.
 *
 */

void replay_open (void)
{
	if ((pfp = frotzopenprompt(FILE_PLAYBACK)) != NULL)
		istream_replay = TRUE;
	else
		print_string ("Cannot open file\n");
}

/*
 * replay_close
 *
 * Stop playback of commands.
 *
 */

void replay_close (void)
{
	fclose (pfp); istream_replay = FALSE;
}

/*
 * replay_code
 *
 * Helper function for replay_key and replay_line.
 *
 */

static int replay_code (void)
{
	int c;

	if ((c = fgetc (pfp)) == '[') {

		int c2;

		c = 0;

		while ((c2 = fgetc (pfp)) != EOF && c2 >= '0' && c2 <= '9')
			c = 10 * c + c2 - '0';

		return (c2 == ']') ? c : EOF;

	} else return c;

}/* replay_code */

/*
 * replay_char
 *
 * Read a character from the command file.
 *
 */

static zchar replay_char (void)
{
	int c;

	if ((c = replay_code ()) != EOF) {

		if (c != '\n') {

			if (c < 1000) {

				c = translate_from_zscii (c);

				if (c == ZC_SINGLE_CLICK || c == ZC_DOUBLE_CLICK) {
					mouse_x = replay_code ();
					mouse_y = replay_code ();
				}

				return c;

			} else return ZC_HKEY_MIN + c - 1000;
		}

		ungetc ('\n', pfp);

		return ZC_RETURN;

	} else return ZC_BAD;

}/* replay_char */

/*
 * replay_read_key
 *
 * Read a keystroke from a command file.
 *
 */

zchar replay_read_key (void)
{
	zchar key;

	key = replay_char ();

	if (fgetc (pfp) != '\n') {

		replay_close ();
		return ZC_BAD;

	} else return key;

}/* replay_read_key */

/*
 * replay_read_input
 *
 * Read a line of input from a command file.
 *
 */

zchar replay_read_input (zchar *buf)
{
	zchar c;

	for (;;) {

		c = replay_char ();

		if (c == ZC_BAD || is_terminator (c))
			break;

		*buf++ = c;

	}

	*buf = 0;

	if (fgetc (pfp) != '\n') {

		replay_close ();
		return ZC_BAD;

	} else return c;

}/* replay_read_input */
