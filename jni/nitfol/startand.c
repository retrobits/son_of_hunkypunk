#include <jni.h>
#include "nitfol.h"
#include "main.h"

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

void Java_org_andglk_Nitfol_useFile(JNIEnv *env, jobject *this, strid_t gameFile)
{
	game_use_file(gameFile);
	ignore_errors = TRUE;
}
