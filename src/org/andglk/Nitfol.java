package org.andglk;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

public class Nitfol extends Activity {
    private Glk glk;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(R.style.theme);
    	System.loadLibrary("nitfol");
    	glk = new Glk(this);
        setContentView(glk.getView());
        useFile(new FileStream(getIntent().getData().getPath(), FileRef.FILEMODE_READ, 0).getPointer());
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
    }
    
    native void useFile(int str_p);
}
