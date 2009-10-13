package org.andglk;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

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
	private FrameLayout _frame;
	private Handler _uiHandler = new Handler();
	private BlockingQueue<Event> _eventQueue = new LinkedBlockingQueue<Event>();
	protected boolean _done;
	private Context _context;
	private File mSaveDir;
	private File mTranscriptDir;

	@Override
	public void run() {
		runProgram();
	}
	
	native private void runProgram();
	
	public Glk(Context context) {
		assert (_instance == null);
		_instance = this;
		_frame = new FrameLayout(context) {
			@Override
			protected void onLayout(boolean changed, int left, int top,
					int right, int bottom) {
				super.onLayout(changed, left, top, right, bottom);
				if (changed)
					postEvent(new ArrangeEvent());
			}
		};
		_context = context;
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
		return _frame;
	}

	public Handler getUiHandler() {
		return _uiHandler;
	}

	public void postEvent(Event e) {
		_eventQueue.add(e);
	}

	public synchronized void waitForUi(final Runnable runnable) {
		if (Thread.currentThread().equals(_uiHandler.getLooper().getThread())) {
			runnable.run();
			return;
		}
		
		_done = false;
		_uiHandler.post(new Runnable() {
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
		return _context;
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
			return null;
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
		default:
			Log.w("Glk", "unhandled gestalt selector: " + Integer.toString(sel) + " (value " + val + ")");
		// all below are TODO
		case GESTALT_MOUSEINPUT:
		case GESTALT_TIMER:
		case GESTALT_UNICODE:
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
		_frame.requestLayout();
	}

	public void setCurrentStream(Stream stream) {
		mCurrentStream = stream;
	}

	public Stream getCurrentStream() {
		return mCurrentStream;
	}
	
	public void exit() {
		_uiHandler.post(new Runnable() {
			@Override
			public void run() {
				Window.disableAll();
				Toast.makeText(_context, R.string.game_quit, Toast.LENGTH_SHORT).show();
			}
		});
		_instance = null;
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

	public void setSaveDir(File saveDir) {
		mSaveDir = saveDir;
	}

	public File getSaveDir() {
		return mSaveDir;
	}
}
