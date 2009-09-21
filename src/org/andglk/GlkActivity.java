package org.andglk;

import android.app.Activity;
import android.os.Bundle;

public class GlkActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(android.R.style.Theme_Light_NoTitleBar);
    	System.loadLibrary("model");
    	Glk glk = new Glk(this);
        setContentView(glk.getView());
    	glk.start();
        super.onCreate(savedInstanceState);
    }
}
