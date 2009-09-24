package org.andglk;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

public class GlkActivity extends Activity {
    private Glk glk;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(android.R.style.Theme_Light_NoTitleBar);
    	System.loadLibrary("model");
    	glk = new Glk(this);
        setContentView(glk.getView());
    	glk.start();
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	glk.onConfigurationChanged(newConfig);
    }
}
