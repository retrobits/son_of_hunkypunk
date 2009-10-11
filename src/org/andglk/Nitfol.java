package org.andglk;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

public class Nitfol extends Activity {
    private Glk glk;
	private ArrayList<Parcelable> mWindowStates;

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
        if (savedInstanceState != null)
        	mWindowStates = savedInstanceState.getParcelableArrayList("windowStates");
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

    	    	if (mWindowStates != null)
	    	    	Glk.getInstance().getUiHandler().post(new Runnable() {
	    	    		public void run() {
			    	    	Window w = null;
			    	    	for (Parcelable p : mWindowStates)
			    	    		if ((w = Window.iterate(w)) != null)
			    	    			w.restoreInstanceState(p);
			    	    		else
			    	    			break;
	    	    		}
	    	    	});
    		}
    	});
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

    	ArrayList<Parcelable> states = new ArrayList<Parcelable>();
    	
    	Window w = null;
    	while ((w = Window.iterate(w)) != null)
    		states.add(w.saveInstanceState());
    	
    	outState.putParcelableArrayList("windowStates", states);
    }
    
    private native void saveGame(int fs);
    private native void restoreGame(int fs);

	native void useFile(int str_p);
}
