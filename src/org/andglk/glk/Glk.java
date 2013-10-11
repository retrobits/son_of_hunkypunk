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

package org.andglk.glk;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.andglk.hunkypunk.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/** <strong>DO NOT EVER INSTANTIATE OR START THIS CLASS MORE THAN ONCE IN A PROCESS' LIFETIME</strong> */
public class Glk extends Thread {
	public static class AlreadyRunning extends Exception {
		private static final long serialVersionUID = -8966218915411360727L;
	}
	
	public final static int STYLE_NORMAL = 0;
	public final static int STYLE_EMPHASIZED = 1;
	public final static int STYLE_PREFORMATTED = 2;
	public final static int STYLE_HEADER = 3;
	public final static int STYLE_SUBHEADER = 4;
	public final static int STYLE_ALERT = 5;
	public final static int STYLE_NOTE = 6;
	public final static int STYLE_BLOCKQUOTE = 7;
	public final static int STYLE_INPUT = 8;
	public final static int STYLE_USER1 = 9;
	public final static int STYLE_USER2 = 10;
	public final static int STYLE_NUMSTYLES = 11;
	
	public final static int STYLEHINT_INDENTATION = 0;
	public final static int STYLEHINT_PARAINDENTATION = 1;
	public final static int STYLEHINT_JUSTIFICATION = 2;
	public final static int STYLEHINT_SIZE = 3;
	public final static int STYLEHINT_WEIGHT = 4;
	public final static int STYLEHINT_OBLIQUE = 5;
	public final static int STYLEHINT_PROPORTIONAL = 6;
	public final static int STYLEHINT_TEXTCOLOR = 7;
	public final static int STYLEHINT_BACKCOLOR = 8;
	public final static int STYLEHINT_REVERSECOLOR = 9;
	public final static int STYLEHINT_NUMHINTS = 10;

	public final static int STYLEHINT_JUST_LEFTFLUSH = 0;
	public final static int STYLEHINT_JUST_LEFTRIGHT = 1;
	public final static int STYLEHINT_JUST_CENTERED = 2;
	public final static int STYLEHINT_JUST_RIGHTFLUSH = 3;

	private static Glk _instance;
	
	public final static int GESTALT_VERSION = 0;
	public final static int GESTALT_CHARINPUT = 1;
	public final static int GESTALT_LINEINPUT = 2;
	public final static int GESTALT_CHAROUTPUT = 3;
	public final static int GESTALT_CHAROUTPUT_CANNOTPRINT = 0;
	public final static int GESTALT_CHAROUTPUT_APPROXPRINT = 1;
	public final static int GESTALT_CHAROUTPUT_EXACTPRINT = 2;
	public final static int GESTALT_MOUSEINPUT = 4;
	public final static int GESTALT_TIMER = 5;
	public final static int GESTALT_GRAPHICS = 6;
	public final static int GESTALT_DRAWIMAGE = 7;
	public final static int GESTALT_SOUND = 8;
	public final static int GESTALT_SOUNDVOLUME = 9;
	public final static int GESTALT_SOUNDNOTIFY = 10;
	public final static int GESTALT_HYPERLINKS = 11;
	public final static int GESTALT_HYPERLINKINPUT = 12;
	public final static int GESTALT_SOUNDMUSIC = 13;
	public final static int GESTALT_GRAPHICSTRANSPARENCY = 14;
	public final static int GESTALT_UNICODE = 15;

	private Stream mCurrentStream;
	private FrameLayout mFrame;
	private Handler mUiHandler = new Handler();
	private BlockingQueue<Event> _eventQueue = new LinkedBlockingQueue<Event>();
	protected boolean _done;
	private Context mContext;
	private File mSaveDir;
	private File mTranscriptDir;

	private int _autoSaveLineEvent = 0;
	private File _autoSave = null;
	private String _autoSavePath = "";
	private String[] _arguments = {};
	private boolean _needToSave = false;
	private boolean _exiting = false;

	@Override
	public void run() {
		startTerp(_arguments[0], _autoSavePath, _arguments.length, _arguments);
		notifyQuit();
		_instance = null;
		Window.setRoot(null);
	}

	// loader has successfully loaded and linked to glk interpreter and is about to start
	public void notifyLinked() {
		// just a concept, not sure if its useful
	}
	
	private void notifyQuit() {
		mUiHandler.post(new Runnable() {
			@Override
			public void run() {
				TypedArray ta = mContext.obtainStyledAttributes(null, 
																new int[] { android.R.attr.textAppearance }, 
																R.attr.textGridWindowStyle, 
																0);
				int res = ta.getResourceId(0, -1);
				ta = mContext.obtainStyledAttributes(res, new int[] { android.R.attr.textSize, android.R.attr.textColor });
				int fontSize = (int)(ta.getDimensionPixelSize(0, -1));

				final View overlay = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.floating_notification, null);
				TextView tw = (TextView)(overlay.findViewById(R.id.message));
				tw.setTextSize(fontSize);
				tw.setText(R.string.game_quit);

				overlay.measure(View.MeasureSpec.makeMeasureSpec(mFrame.getWidth(), MeasureSpec.AT_MOST), 
						View.MeasureSpec.makeMeasureSpec(mFrame.getHeight(), MeasureSpec.AT_MOST));
				Bitmap bitmap = Bitmap.createBitmap(overlay.getMeasuredWidth(), overlay.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
				overlay.layout(0, 0, overlay.getMeasuredWidth(), overlay.getMeasuredHeight());
				overlay.draw(new Canvas(bitmap));
				mFrame.setForeground(new BitmapDrawable(bitmap));
				mFrame.setForegroundGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			}
		});
	}

	native private void startTerp(String terpPath, String saveFilePath, int argc, String[] argv);
	
	public Glk(Context context) {
		assert (_instance == null);
		_instance = this;
		mFrame = new FrameLayout(context) {
			@Override
			protected void onLayout(boolean changed, int left, int top,
					int right, int bottom) {
				super.onLayout(changed, left, top, right, bottom);
				if (changed)
					postEvent(new ArrangeEvent());
			}
		};
		mContext = context;

		Activity activity = (Activity) context;
		if (activity != null)
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}
	
	public void setWindow(Window window) {
		mCurrentStream = window.getStream();
	}
	
	@SuppressWarnings("unused")
	private Event select()
	{
		flush();
		Event ev;
		while (true) {
			try {
				ev = _eventQueue.take();
				if (ev instanceof SystemEvent) {
					((SystemEvent) ev).run();
					continue;
				}
				return ev;
			} catch (InterruptedException e) {
			}
		}
	}
	
	public ViewGroup getView() {
		return mFrame;
	}

	public Handler getUiHandler() {
		return mUiHandler;
	}

	public void postEvent(Event e) {
		_needToSave = _needToSave || (e instanceof CharInputEvent || e instanceof LineInputEvent);
		_eventQueue.add(e);
	}

	public void postExitEvent() {
		setExiting(true);
		_eventQueue.add(new ExitEvent(Window.getRoot()));
	}

	public boolean postAutoSaveEvent(String fileName) {
		if (_needToSave) {
			//Toast.makeText(mContext, "Saved game.", Toast.LENGTH_SHORT).show();
			_eventQueue.add(new AutoSaveEvent(Window.getRoot(),fileName, 1)); //_autoSaveLineEvent));
			_needToSave = false;
			return true;
		}
		return false;
	}

	public synchronized void waitForUi(final Runnable runnable) {
		if (_exiting) return;

		if (Thread.currentThread().equals(mUiHandler.getLooper().getThread())) {
			runnable.run();
			return;
		}
		
		_done = false;
		mUiHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized(Glk.this) {
					runnable.run();
					_done = true;
					Glk.this.notify();
				}
			}
		});

		while (!_done) try {
			wait();
		} catch (InterruptedException e) {
			// try again
		}
	}

	public Context getContext() {
		return mContext;
	}

	public static Glk getInstance() {
		return _instance;
	}

	/** Get directory to place game data in.
	 * 
	 * @param usage Usage of files placed in the directory. 
	 * One of {@link FileRef#FILEUSAGE_DATA}, {@link FileRef#FILEUSAGE_INPUTRECORD}, 
	 * {@link FileRef#FILEUSAGE_SAVEDGAME}, {@link FileRef#FILEUSAGE_TRANSCRIPT}.
	 * @return Directory to place the files in. <code>null</code> if this type of files cannot be stored.
	 */
	public File getFilesDir(int usage) {
		switch (usage) {
		case FileRef.FILEUSAGE_SAVEDGAME:
			return getSaveDir();
		case FileRef.FILEUSAGE_TRANSCRIPT:
			return getTranscriptDir();
		default:
			Log.e("Glk", "I don't know where to place files with usage = " + Integer.toString(usage));
			return getSaveDir();
			//return null;
		}
	}
	
	/** Query Glk capabilities.
	 * 
	 * @param sel Selector -- which capability you are requesting information about.
	 * @param val Parameter for that selector. Optional, pass 0 if not needed.
	 * @return An array which first element is the main return value and the rest is any additional information pertinent.
	 */
	static final int[] sZero = { 0 };
	static final int[] sOne = { 1 };

	public int[] gestalt(int sel, int val) {
		
		//Log.d("Glk","gestalt " + Integer.toString(sel));

		switch (sel) {
		case GESTALT_VERSION:
			return new int[] { 0x700 };
		case GESTALT_CHAROUTPUT:
			if (isPrintable((char) val)) // we only do latin-1 ATM
				return new int[] { GESTALT_CHAROUTPUT_EXACTPRINT, 1 };
			else
				return new int[] { GESTALT_CHAROUTPUT_CANNOTPRINT, 0 };
		case GESTALT_LINEINPUT:
			if (isPrintable((char) val) && val != 10)
				return sOne;
			else
				return sZero;
		case GESTALT_CHARINPUT:
			// TODO: handle special characters; this needs getChar support too.
			return CharInputEvent.accepts(val) ? sOne : sZero;
		case GESTALT_UNICODE:
			return sZero;
		default:
			Log.w("Glk", "unhandled gestalt selector: " + Integer.toString(sel) + " (value " + val + ")");
		// all below are TODO
		case GESTALT_MOUSEINPUT:
		case GESTALT_TIMER:
		case GESTALT_GRAPHICS:
		case GESTALT_DRAWIMAGE:
		case GESTALT_GRAPHICSTRANSPARENCY:
		case GESTALT_SOUND:
		case GESTALT_SOUNDMUSIC:
		case GESTALT_SOUNDVOLUME:
		case GESTALT_SOUNDNOTIFY:
		case GESTALT_HYPERLINKS:
		case GESTALT_HYPERLINKINPUT:
			return sZero;
		}
	}

	private static boolean isPrintable(char val) {
		if ((val >= 0 && val < 10) || (val > 10 && val < 32) || (val > 126 && val < 160) || val > 255)
			return false;
		else
			return true;
	}

	public void onConfigurationChanged(Configuration newConfig) {
		mFrame.requestLayout();
	}

	public void setCurrentStream(Stream stream) {
		mCurrentStream = stream;
	}

	public Stream getCurrentStream() {
		return mCurrentStream;
	}
	
	public void flush() {
		final Window root = Window.getRoot();
		if (root != null)
			root.flush();
	}

	public void onSelect(Runnable runnable) {
		postEvent(new SystemEvent(runnable));
	}

	public void setTranscriptDir(File transcriptDir) {
		mTranscriptDir = transcriptDir;
	}

	public File getTranscriptDir() {
		return mTranscriptDir;
	}

	public void setNeedToSave(boolean need) {		
		_needToSave = need;
	}	
	
	public void setAutoSave(File autoSave, int autoSaveLineEvent) {		
		_autoSave = autoSave;
		if (_autoSave.exists()) _autoSavePath = _autoSave.getAbsolutePath();
		_autoSaveLineEvent = autoSaveLineEvent;
	}	

	public File getAutoSave() {
		return _autoSave;
	}
	
	public void setSaveDir(File saveDir) {
		mSaveDir = saveDir;
	}

	public File getSaveDir() {
		return mSaveDir;
	}

	public void setExiting(boolean flag) {
		_exiting = flag;
	}

	public boolean getExiting() {
		return _exiting;
	}

	public void setArguments(String[] arguments) {
		_arguments = arguments;
	}

	public String[] getArguments() {
		return _arguments;
	}

	/** returns a path appropriate for Android Andglk
	 * 
	 */
	public String sanitizePath (String path) {
		if (_autoSave == null) return path;

		String sanePath = new String(path);
		if (!sanePath.startsWith("/")) {
			int ix = sanePath.lastIndexOf('/');
			if (ix > -1) sanePath = sanePath.substring(ix+1);
			File saneFile = new File(_autoSave.getParentFile(), sanePath);
			sanePath = saneFile.getAbsolutePath();
		}
		return sanePath;
	}
}
