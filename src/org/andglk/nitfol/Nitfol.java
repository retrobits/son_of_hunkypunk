/*
	Copyright © 2009 Rafał Rzepecki <divided.mind@gmail.com>

	This file is part of Hunky Punk.

    Hunky Punk is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Hunky Punk is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Hunky Punk.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.andglk.nitfol;

import java.io.File;
import java.util.ArrayList;

import org.andglk.glk.FileRef;
import org.andglk.glk.FileStream;
import org.andglk.glk.Glk;
import org.andglk.glk.Window;
import org.andglk.hunkypunk.HunkyPunk;
import org.andglk.hunkypunk.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

public class Nitfol extends Activity {
    private Glk glk;
	private File mDataDir;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setTheme(R.style.theme);
    	System.loadLibrary("nitfol");
    	glk = new Glk(this);
        setContentView(glk.getView());
        Intent i = getIntent();
        Uri uri = i.getData();
        String dataDirName = i.getStringExtra("ifid");
        mDataDir = getDir(dataDirName, MODE_PRIVATE);
        File saveDir = new File(mDataDir, "savegames");
        saveDir.mkdir();
        glk.setSaveDir(saveDir);
        glk.setTranscriptDir(HunkyPunk.getTranscriptDir()); // there goes separation, and so cheaply...
        useFile(new FileStream(uri.getPath(), FileRef.FILEMODE_READ, 0).getPointer());
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        	restore(savedInstanceState.getParcelableArrayList("windowStates"));
    	glk.start();
    }
    
    private void restore(final ArrayList<Parcelable> windowStates) {
    	final File f = getBookmark(); 
    	if (!f.exists())
    		return;

    	Glk.getInstance().onSelect(new Runnable() {
    		@Override
    		public void run() {
    	    	if (windowStates != null)
	    	    	Glk.getInstance().waitForUi(new Runnable() {
	    	    		public void run() {
			    	    	Window w = null;
			    	    	for (Parcelable p : windowStates)
			    	    		if ((w = Window.iterate(w)) != null)
			    	    			w.restoreInstanceState(p);
			    	    		else
			    	    			break;
	    	    		}
	    	    	});
    		}
    	});

    	FileStream fs = new FileStream(f.getAbsolutePath(), FileRef.FILEMODE_READ, 0);
    	
    	restoreGame(fs.getPointer());
    }

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	glk.onConfigurationChanged(newConfig);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	final File f = getBookmark();

    	if (!glk.isAlive()) {
    		f.delete();
    		return;
    	}
    	
    	Glk.getInstance().onSelect(new Runnable() {
    		public void run() {
		    	FileStream fs = new FileStream(f.getAbsolutePath(), FileRef.FILEMODE_WRITE, 0);
		    	
		    	saveGame(fs.getPointer());
		    	fs.close();
    		}
    	});
    }
    
    private File getBookmark() {
		return new File(mDataDir, "bookmark");
	}

	@Override
    protected void onStop() {
    	// so we don't have to cleanup which would be a major pain in the ass
    	// because the interps weren't designed to be run again in the same process
    	// (I know bc I've wasted whole day trying to figure out how to do that cleanly)
    	// and we'll get thawed anyway
    	System.exit(0);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

    	if (!glk.isAlive())
    		return;

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
