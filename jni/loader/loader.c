#include <jni.h>
#include <android/log.h>
#include <pthread.h>
#include <dlfcn.h>
#include <stdio.h>

#include "glk.h"
#include "glkstart.h"
#include "gi_dispa.h"

// linkage fn pointers
void ( * andglk_loader_glk_main ) (JavaVM* , JNIEnv*, jobject, const char*, glkunix_startup_t* data) = NULL; 
void ( * andglk_loader_glk_Glk_notifyLinked ) (JNIEnv *env, jobject this) = NULL;
jint ( * andglk_loader_glk_CPointed_makePoint ) (JNIEnv *env, jobject this) = NULL;
void ( * andglk_loader_glk_CPointed_releasePoint ) (JNIEnv *env, jobject this, jint point) = NULL;
void ( * andglk_loader_glk_MemoryStream_writeOut )(JNIEnv *env, jobject this, jint nativeBuf, jarray jbuf) = NULL;
int  ( * andglk_loader_glk_MemoryStream_retainVmArray )(JNIEnv *env, jobject this, int buffer, long length) = NULL;
void ( * andglk_loader_glk_MemoryStream_releaseVmArray )(JNIEnv *env, jobject this, int buffer, int length, int dispatchRock) = NULL;

static JavaVM* _jvm;
static void* _handle = NULL;
static int _init_error = 0;
pthread_mutex_t _muQuery = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t _muGame = PTHREAD_MUTEX_INITIALIZER;

//static jclass _GlkWrapperClass;
//static jobject _GlkWrapperObj;
//static jmethodID _GlkWrapper_onTerpExit;

void* link(const char* terpPath, const char *fn) 
{
	void* fp = NULL;

	if (!_handle) {
		LOGD("loader.dlopen %s", terpPath);
		_handle = dlopen(terpPath, RTLD_LOCAL | RTLD_LAZY);  
		if (!_handle) {
			LOGE("dlopen failed for %s", terpPath);
			_init_error = 1;
		}
	}	

	if (!_init_error) {
		fp = dlsym(_handle, fn);   
		if (!fp) { 
			LOGE("dlsym failed for %s", fn); 
			_init_error = 1; 
		}
	} 
	return fp;
}

JNIEXPORT void Java_org_andglk_glk_Glk_startTerp
(JNIEnv *env, jobject obj1, jstring terpPath, jstring saveFilePath, jint argc, jobjectArray argv)
{
	// begin synchronize

	int ct = 0;
	while (pthread_mutex_trylock(&_muGame)!=0) {
		sleep(100);
		if(ct++>5) {
			LOGE("failed to acquire game thread lock, bailing");
			return;
		}
	}

	pthread_mutex_lock (&_muQuery);

	(*env)->GetJavaVM(env, &_jvm);

	_init_error = 0;

	/* Init exit game callback */
	//_GlkWrapperObj = obj1;
	//_GlkWrapperClass = (*env)->GetObjectClass(env, GlkWrapperObj);
	//GlkWrapper_onTerpExit = (*env)->GetMethodID(env, GlkWrapperClass, "onTerpExit", "()V");

	// load game plugin lib
	const char *copy_terpPath = (*env)->GetStringUTFChars(env, terpPath, 0);

	// link entry points
	andglk_loader_glk_main = link(copy_terpPath, "andglk_loader_glk_main");   
	andglk_loader_glk_Glk_notifyLinked = link(copy_terpPath, "andglk_loader_glk_Glk_notifyLinked");
	andglk_loader_glk_MemoryStream_retainVmArray = link(copy_terpPath,"andglk_loader_glk_MemoryStream_retainVmArray");   
	andglk_loader_glk_MemoryStream_releaseVmArray = link(copy_terpPath,"andglk_loader_glk_MemoryStream_releaseVmArray");   
	andglk_loader_glk_MemoryStream_writeOut = link(copy_terpPath,"andglk_loader_glk_MemoryStream_writeOut");   
	andglk_loader_glk_CPointed_makePoint = link(copy_terpPath,"andglk_loader_glk_CPointed_makePoint");   
	andglk_loader_glk_CPointed_releasePoint = link(copy_terpPath,"andglk_loader_glk_CPointed_releasePoint");   

	(*env)->ReleaseStringUTFChars(env, terpPath, copy_terpPath);

	// end synchronize
	pthread_mutex_unlock (&_muQuery);

	// copy parms
	glkunix_startup_t startdata;
	startdata.argc = argc;
	startdata.argv = malloc(argc * sizeof(char*));
	
	// process argc/argv 
	jstring argv0 = NULL;
	int i;
	for(i = 0; i < argc; i++) {
		argv0 = (*env)->GetObjectArrayElement(env, argv, i);
		startdata.argv[i] = (char*)((*env)->GetStringUTFChars(env, argv0, 0));
	}

	// notify glk we are linked
	andglk_loader_glk_Glk_notifyLinked(env, obj1);

	const char *copy_saveFilePath = (*env)->GetStringUTFChars(env, saveFilePath, 0);

	// start the game
	andglk_loader_glk_main(_jvm, env, obj1, copy_saveFilePath, &startdata);   

	(*env)->ReleaseStringUTFChars(env, terpPath, copy_saveFilePath);

	// begin synchronize
	pthread_mutex_lock (&_muQuery);

	dlclose(_handle);           	 // unload terp
	
	// free memory
	for(i = 0; i < argc; i++) {
		argv0 = ((*env)->GetObjectArrayElement(env, argv, i));
		(*env)->ReleaseStringUTFChars(env, argv0, startdata.argv[i]);
	}
	free(startdata.argv);

	// clear pointers
	_handle = NULL;
	andglk_loader_glk_main = NULL;
	andglk_loader_glk_MemoryStream_retainVmArray = NULL;
	andglk_loader_glk_MemoryStream_releaseVmArray = NULL;
	andglk_loader_glk_MemoryStream_writeOut = NULL;
	andglk_loader_glk_CPointed_makePoint = NULL;
	andglk_loader_glk_CPointed_releasePoint = NULL;

	// signal game has exited
	//(*env)->CallVoidMethod(env, GlkWrapperObj, GlkWrapper_onTerpExit);

	// end synchronize
	pthread_mutex_unlock (&_muQuery);
	pthread_mutex_unlock (&_muGame);

	LOGD("loader exit");
}
JNIEXPORT void Java_org_andglkmod_glk_Glk_startTerp
(JNIEnv *env, jobject obj1, jstring terpPath, jstring saveFilePath, jint argc, jobjectArray argv)
{
	Java_org_andglk_glk_Glk_startTerp(env,obj1,terpPath,saveFilePath,argc,argv);
}

JNIEXPORT jint Java_org_andglk_glk_CPointed_makePoint(JNIEnv *env, jobject this)
{
	jint result = -1;
	const char *FN = "andglk_loader_glk_CPointed_makePoint";

	// begin synchronize
	pthread_mutex_lock (&_muQuery);

	if (andglk_loader_glk_CPointed_makePoint) 
		result = andglk_loader_glk_CPointed_makePoint(env, this);
	else 
		LOGE("invalid call -- not linked to %s", FN);	

	// end synchronize
	pthread_mutex_unlock (&_muQuery);

	return result;
}
JNIEXPORT jint Java_org_andglkmod_glk_CPointed_makePoint(JNIEnv *env, jobject this)
{
	return Java_org_andglk_glk_CPointed_makePoint(env,this);
}

JNIEXPORT void Java_org_andglk_glk_CPointed_releasePoint(JNIEnv *env, jobject this, jint point)
{
	const char *FN = "andglk_loader_glk_CPointed_releasePoint";

	// begin synchronize
	pthread_mutex_lock (&_muQuery);

	if (andglk_loader_glk_CPointed_releasePoint) 
		andglk_loader_glk_CPointed_releasePoint(env, this, point);
	else 
		LOGE("invalid call -- not linked to %s", FN);	

	// end synchronize
	pthread_mutex_unlock (&_muQuery);
}
JNIEXPORT void Java_org_andglkmod_glk_CPointed_releasePoint(JNIEnv *env, jobject this, jint point)
{
	Java_org_andglk_glk_CPointed_releasePoint(env,this,point);
}

JNIEXPORT void Java_org_andglk_glk_MemoryStream_writeOut(JNIEnv *env, jobject this, jint nativeBuf, jarray jbuf)
{
	const char *FN = "andglk_loader_glk_MemoryStream_writeOut";

	// begin synchronize
	pthread_mutex_lock (&_muQuery);

	if (andglk_loader_glk_MemoryStream_writeOut) 
		andglk_loader_glk_MemoryStream_writeOut(env, this, nativeBuf, jbuf);
	else 
		LOGE("invalid call -- not linked to %s", FN);	

	// end synchronize
	pthread_mutex_unlock (&_muQuery);
}
JNIEXPORT void Java_org_andglkmod_glk_MemoryStream_writeOut(JNIEnv *env, jobject this, jint nativeBuf, jarray jbuf)
{
	Java_org_andglk_glk_MemoryStream_writeOut(env,this,nativeBuf,jbuf);
}

JNIEXPORT int Java_org_andglk_glk_MemoryStream_retainVmArray(JNIEnv *env, jobject this, int buffer, long len)
{
	int result = -1;
	const char *FN = "andglk_loader_glk_MemoryStream_retainVmArray";

	// begin synchronize
	pthread_mutex_lock (&_muQuery);

	if (andglk_loader_glk_MemoryStream_retainVmArray) 
		result = andglk_loader_glk_MemoryStream_retainVmArray(env, this, buffer, len);
	else 
		LOGE("invalid call -- not linked to %s", FN);	

	// end synchronize
	pthread_mutex_unlock (&_muQuery);

	return result;
}
JNIEXPORT int Java_org_andglkmod_glk_MemoryStream_retainVmArray(JNIEnv *env, jobject this, int buffer, long len)
{
	return Java_org_andglk_glk_MemoryStream_retainVmArray(env,this,buffer,len);
}

JNIEXPORT void Java_org_andglk_glk_MemoryStream_releaseVmArray(JNIEnv *env, jobject this, int buffer, int length, int dispatchRock)
{
	const char *FN = "andglk_loader_glk_MemoryStream_releaseVmArray";

	// begin synchronize
	pthread_mutex_lock (&_muQuery);

	if (andglk_loader_glk_MemoryStream_releaseVmArray) 
		andglk_loader_glk_MemoryStream_releaseVmArray(env, this, buffer, length, dispatchRock);
	else 
		LOGE("invalid call -- not linked to %s", FN);	

	// end synchronize
	pthread_mutex_unlock (&_muQuery);
}
JNIEXPORT void Java_org_andglkmod_glk_MemoryStream_releaseVmArray(JNIEnv *env, jobject this, int buffer, int length, int dispatchRock)
{
	Java_org_andglk_glk_MemoryStream_releaseVmArray(env,this,buffer,length,dispatchRock);
}

JNIEXPORT int Java_org_andglk_glk_Window_retainVmArray(JNIEnv *env, jobject this, int buffer, long length)
{
	return Java_org_andglk_glk_MemoryStream_retainVmArray(env, this, buffer, length);
}
JNIEXPORT int Java_org_andglkmod_glk_Window_retainVmArray(JNIEnv *env, jobject this, int buffer, long length)
{
	return Java_org_andglk_glk_Window_retainVmArray(env,this,buffer,length);
}
