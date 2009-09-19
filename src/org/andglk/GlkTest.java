package org.andglk;

import android.app.Activity;
import android.os.Bundle;

public class GlkTest extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	System.loadLibrary("andglk");
    	Glk glk = new Glk();
    	glk.start();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}