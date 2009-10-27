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
#include "nitfol.h"
#include "main.h"
#include "quetzal.h"

/* we don't let nitfol play with filesystem, at least now */

strid_t intd_filehandle_open(strid_t savefile, glui32 operating_id, glui32 contents_id, glui32 interp_id, glui32 length)
{
	return 0;
}

strid_t startup_findfile(void)
{
	return 0;
}

glui32 intd_get_size(void)
{
	return 0;
}

void intd_filehandle_make(strid_t savefile)
{
}

void Java_org_andglk_nitfol_Nitfol_useFile(JNIEnv *env, jobject *this, strid_t gameFile)
{
	game_use_file(gameFile);
	ignore_errors = TRUE;
}

void Java_org_andglk_nitfol_Nitfol_saveGame(JNIEnv *env, jobject *this, strid_t saveFile)
{
	savequetzal(saveFile);
}

void Java_org_andglk_nitfol_Nitfol_restoreGame(JNIEnv *env, jobject *this, strid_t saveFile)
{
	set_savefile(saveFile);
}
