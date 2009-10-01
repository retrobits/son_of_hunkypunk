#define _GNU_SOURCE
#include <stdlib.h>
#include "nitfol.h"

static char *game_filename = NULL;

/*
static void set_game_filename(const char *name)
{
	n_free(game_filename);
	game_filename = 0;

	game_filename = canonicalize_file_name(name);

	if(!game_filename)
		game_filename = n_strdup(name);
}

*/
strid_t intd_filehandle_open(strid_t savefile, glui32 operating_id, glui32 contents_id, glui32 interp_id, glui32 length)
{
	return 0;
//	char *name;
//	strid_t str;
//	if(operating_id != 0x554e4958 /* 'UNIX' */)
//		return 0;
//	if(contents_id != 0)
//		return 0;
//	if(interp_id != 0x20202020 /* '    ' */)
//		return 0;
//
//	name = (char *) n_malloc(length+1);
//	glk_get_buffer_stream(savefile, name, length);
//	name[length] = 0;
//	str = glkandroid_stream_open_pathname(name, fileusage_Data | fileusage_BinaryMode, 0);
//	if(str)
//		set_game_filename(name);
//	n_free(name);
//	return str;
}

strid_t startup_findfile(void)
{
	return 0;
//	static DIR *dir = NULL;
//	static char *pathstart = NULL;
//	static char *path = NULL;
//	strid_t stream;
//	struct dirent *d;
//	char *name = NULL;
//
//	if(!pathstart) {
//		char *p = search_path;
//		if(!p)
//			return 0;
//		pathstart = n_strdup(p);
//		if(!(path = n_strtok(pathstart, ":"))) {
//			n_free(pathstart);
//			pathstart = 0;
//			return 0;
//		}
//	}
//
//	do {
//		if(!dir) {
//			dir = opendir(path);
//			if(!dir) {
//				n_free(pathstart);
//				pathstart = 0;
//				return 0;
//			}
//		}
//		d = readdir(dir);
//		if(!d) {
//			closedir(dir);
//			dir = NULL;
//			if(!(path = n_strtok(NULL, ":"))) {
//				n_free(pathstart);
//				pathstart = 0;
//				return 0;
//			}
//		}
//	} while(!dir);
//
//	name = (char *) n_malloc(n_strlen(path) + n_strlen(d->d_name) + 2);
//	n_strcpy(name, path);
//	n_strcat(name, "/");
//	n_strcat(name, d->d_name);
//	stream = glkunix_stream_open_pathname(name, fileusage_Data | fileusage_BinaryMode, 0);
//	if(stream)
//		set_game_filename(name);
//	n_free(name);
//	return stream;
}

glui32 intd_get_size(void)
{
	return 0;
}

void intd_filehandle_make(strid_t savefile)
{
}

