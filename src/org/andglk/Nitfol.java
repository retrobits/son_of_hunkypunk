package org.andglk;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;

public class Nitfol extends Activity {
    private Glk glk;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Debug.startMethodTracing("nitfol");
    	setTheme(R.style.theme);
    	System.loadLibrary("nitfol");
    	glk = new Glk(this);
        setContentView(glk.getView());
        Uri uri = getIntent().getData();
        useFile(new FileStream(uri.getPath(), FileRef.FILEMODE_READ, 0).getPointer());
    	glk.start();
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	glk.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onPause() {
    	// XXX we don't support pausing yet
    	super.onPause();
    	finish();
    	Debug.stopMethodTracing();
    }
    
    native void useFile(int str_p);
}
