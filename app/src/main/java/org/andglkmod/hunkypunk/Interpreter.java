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

package org.andglkmod.hunkypunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.andglkmod.glk.Glk;
import org.andglkmod.glk.Window;
import org.andglkmod.glk.TextBufferWindow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class Interpreter extends Activity {
    private static final String TAG = "hunkypunk.Interpreter";
    private Glk glk;
    private File mDataDir;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.loadLibrary("andglk-loader");

        if (getSharedPreferences("Night", Context.MODE_PRIVATE).getBoolean("NightOn", false))
            setTheme(R.style.theme2);
        else
            setTheme(R.style.theme);
        setFont();

        Intent i = getIntent();
        Uri uri = i.getData();
        String terp = i.getStringExtra("terp");
        String ifid = i.getStringExtra("ifid");
        mDataDir = Paths.gameStateDir(this, uri, ifid);
        File saveDir = new File(mDataDir, "savegames");
        saveDir.mkdir();

        glk = new Glk(this);

        if (i.getBooleanExtra("landscape", false))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(glk.getView());
        glk.setAutoSave(getBookmark(), 0);
        glk.setSaveDir(saveDir);
        glk.setTranscriptDir(Paths.transcriptDirectory(this)); // there goes separation, and so cheaply...

        ArrayList<String> args = new ArrayList<String>();
        args.add("lib" + terp + ".so");
        if (terp.compareTo("tads") == 0 && getBookmark().exists()) {
            args.add("-r");
            args.add(getBookmark().getAbsolutePath());
        }
        args.add(uri.getPath());

        String arga[] = new String[args.size()];
        glk.setArguments(args.toArray(arga));

        super.onCreate(savedInstanceState);

        // dead code, doesn't work
        // TODO: remove all the Parcelable/SavedState objects and onRestoreInstanceState code in Windows
        //if (savedInstanceState != null)
        //	restore(savedInstanceState.getParcelableArrayList("windowStates"));
        //else

        if (i.getBooleanExtra("loadBookmark", false) || savedInstanceState != null) {
            /* either the user's intent is to restore from bookmark,
               or the OS has killed our app and is now restoring state */
            loadBookmark();
        }
        glk.start();
		/*
			Sets the night mode privately if it was previously on, otherwise it is left so
			Basically, acts like a restore and overwrites the colors accoring to the switch
			value.
		 */
        SharedPreferences sharedPrefs = getSharedPreferences("Night", Context.MODE_PRIVATE);
        if (sharedPrefs.getBoolean("NightOn", false)) {
            org.andglkmod.glk.TextBufferWindow.DefaultBackground = Color.DKGRAY;
            org.andglkmod.glk.TextBufferWindow.DefaultTextColor = Color.WHITE;
            org.andglkmod.glk.TextBufferWindow.DefaultInputStyle = Glk.STYLE_NIGHT;
        } else {
            org.andglkmod.glk.TextBufferWindow.DefaultBackground = Color.WHITE;
            org.andglkmod.glk.TextBufferWindow.DefaultTextColor = Color.BLACK;
            org.andglkmod.glk.TextBufferWindow.DefaultInputStyle = Glk.STYLE_INPUT;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(getApplication());
        //noinspection ResourceType
        inflater.inflate(R.layout.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent intent;
        switch (item.getNumericShortcut()) {
            case '1':
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                finish();
                break;
            case '2':
                AlertDialog builder;
                try {
                    builder = DialogBuilder.showAboutDialog(this);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void loadBookmark() {
        final File f = getBookmark();
        if (!f.exists())
            return;

        final File ws = getWindowStates();
        if (ws.exists()) try {
            final FileInputStream fis = new FileInputStream(ws);
            if (fis != null) {
                final ObjectInputStream ois = new ObjectInputStream(fis);
                Glk.getInstance().onSelect(new Runnable() {
                    @Override
                    public void run() {
                        Glk.getInstance().waitForUi(new Runnable() {
                            public void run() {
                                Window w = null;
                                try {
                                    while ((w = Window.iterate(w)) != null) {
                                        w.readState(ois);
                                        w.flush();
                                    }
                                    ois.close();
                                    fis.close();
                                } catch (IOException e) {
                                    Log.w(TAG, "error while reading window states", e);
                                }
                            }
                        });
                    }
                });
            }
        } catch (IOException e) {
            Log.e(TAG, "error while opening window state stream", e);
        }
    }

	/* dead code, doesn't work
	// TODO: remove all the Parcelable/SavedState objects and onRestoreInstanceState code in Windows
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
			    	    		if ((w = Window.iterate(w)) != null) {
			    	    			w.restoreInstanceState(p);
									w.flush();
								}
			    	    		else
			    	    			break;
	    	    		}
	    	    	});
    		}
    	});
    }
	*/

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        glk.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (setFont()) glk.getView().invalidate();
    }

	/* Changing font fixed with a workaround solution. Apparently,
 	 * all that was needed was setting the returned value to the
	 * default value in TextBufferWindow and performing the other changes there.
	 * Path option is eliminated. Later, would be thought of letting the user
	 * upload downloaded fonts, but for now the available fonts are fixed. More
	 * fonts are to be added.
	 */

    private boolean setFont() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //TODO: changing font is broken (text overflows the view)

        //String fontFolder = prefs.getString("fontFolderPath", "/sdcard/Fonts");
        //String fontFile = prefs.getString("fontFileName", "Droid Serif");
        //String fontPath;
        //File fonts = new File(fontFolder, fontFile);


        int fontSize = 16;
        try {
            fontSize = Integer.parseInt(prefs.getString("fontSize", Integer.toString(fontSize)));
        } catch (Exception e) {
        }

        String fontName = prefs.getString("fontFileName", "Droid Serif (default)"); //returns the Svalue in "fontfileName"-preference and otherwise "DSerif"
        //debugging msg
        //if (TextBufferWindow.DefaultFontName == null)
        //			Toast.makeText(getApplicationContext(), "Font " + fontName + " set.",Toast.LENGTH_LONG).show();

        TextBufferWindow.DefaultFontName = fontName;

        if (TextBufferWindow.DefaultFontSize != fontSize) {
            //(TextBufferWindow.DefaultFontPath == null
            //|| TextBufferWindow.DefaultFontPath.compareTo(fontPath)!=0

            //TextBufferWindow.DefaultFontPath = fontPath;
            TextBufferWindow.DefaultFontSize = fontSize;
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glk.isAlive()) {
            glk.postAutoSaveEvent(getBookmark().getAbsolutePath());

            final File ws = getWindowStates();
            try {
                final FileOutputStream fos = new FileOutputStream(ws);
                final ObjectOutputStream oos = new ObjectOutputStream(fos);
                Window w = null;
                while ((w = Window.iterate(w)) != null)
                    w.writeState(oos);
                oos.close();
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "error while writing windowstates", e);
            }
        }
    }

    private File getWindowStates() {
        return new File(mDataDir, "windowStates");
    }

    private File getBookmark() {
        return new File(mDataDir, "bookmark");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        glk.postExitEvent();
    }

    /**
     * A method for recursively finding a view given a tag. If there
     * is no view with such tag, then the parameter is not set and
     * keeps its previous value unchanged.
     *
     * @param vg  parent ViewGroup from which the iteration starts
     * @param tag Tag Object to identify the needed View
     */

    public View findViewByTag(ViewGroup vg, Object tag) {

        View result = null;

        if (vg == null)
            return null;

        for (int i = 0; i < vg.getChildCount(); i++) {
            //because some are not set and we don't like NullPtrs
            if (vg.getChildAt(i).getTag() != null) {
                if (vg.getChildAt(i).getTag().toString().equals(tag))
                    result = vg.getChildAt(i);
            }
        }
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i) instanceof ViewGroup) {
                result = findViewByTag((ViewGroup) vg.getChildAt(i), tag);
                if (result != null) break;
            }
        }
        return result;
    }
}
