package org.andglk;

import android.app.Activity;
import android.os.Bundle;

public class GlkActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.loadLibrary("model");
    	Glk glk = new Glk(this);
        setContentView(glk.getView());
    	glk.start();
        super.onCreate(savedInstanceState);
    }
}
