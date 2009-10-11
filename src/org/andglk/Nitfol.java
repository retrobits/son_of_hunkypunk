package org.andglk;

import java.io.File;

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
    	super.onPause();
    	
    	final File f = new File(Glk.getInstance().getFilesDir(FileRef.FILEUSAGE_SAVEDGAME), "autosave");
    	Glk.getInstance().onSelect(new Runnable() {
    		public void run() {
		    	FileStream fs = new FileStream(f.getAbsolutePath(), FileRef.FILEMODE_WRITE, 0);
		    	
		    	saveGame(fs.getPointer());
		    	fs.close();
    		}
    	});
    }
    
    @Override
    protected void onResume() {
    	super.onResume();

    	final File f = new File(Glk.getInstance().getFilesDir(FileRef.FILEUSAGE_SAVEDGAME), "autosave");
    	if (!f.exists())
    		return;

    	Glk.getInstance().onSelect(new Runnable() {
    		@Override
    		public void run() {
    	    	FileStream fs = new FileStream(f.getAbsolutePath(), FileRef.FILEMODE_READ, 0);
    	    	
    	    	restoreGame(fs.getPointer());
    	    	fs.close();
    	    	f.delete();
    		}
    	});
    }
    
    private native void saveGame(int fs);
    private native void restoreGame(int fs);

	native void useFile(int str_p);
}
