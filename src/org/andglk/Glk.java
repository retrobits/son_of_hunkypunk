package org.andglk;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class Glk extends Thread {
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

	@SuppressWarnings("unused")
	private Window _root, _currentWindow;
	private FrameLayout _frame;
	private Handler _uiHandler = new Handler();
	private BlockingQueue<Event> _eventQueue = new LinkedBlockingQueue<Event>();
	protected boolean _done;
	private Context _context;

	@Override
	public void run() {
		runProgram();
	}
	
	native private void runProgram();
	
	public Glk(Context context) {
		assert (_instance == null);
		_instance = this;
		_frame = new FrameLayout(context);
		_context = context;
	}
	
	@SuppressWarnings("unused") // referenced in C
	private int window_open(Window split, long method, long size, long wintype, int rock) {
		Window wnd;
		switch ((int)wintype) {
		case Window.WINTYPE_TEXTBUFFER:
			wnd = new TextBufferWindow(this, rock);
			break;
		case Window.WINTYPE_TEXTGRID:
			wnd = new TextGridWindow(this, rock);
			break;
		default:
			Log.w("Glk", "Unimplemented window type requested: " + Long.toString(wintype));
			return 0;
		}
		
		final Window finalWindow = wnd;

		if (split == null)
			waitForUi(new Runnable() {
				@Override
				public void run() {
					_frame.addView(finalWindow.getView());
				}
			});
		else
			new PairWindow(this, split, wnd, method, size);
		
		return wnd.getPointer();
	}
	
	@SuppressWarnings("unused")
	private void set_window(Window window) {
		_currentWindow = window;
	}
	
	@SuppressWarnings("unused")
	private void put_string(final String str) {
		_currentWindow.putString(str);
	}
	
	@SuppressWarnings("unused")
	private void set_style(long styl) {
		_currentWindow.setStyle(styl);
	}
	
	@SuppressWarnings("unused")
	private void request_line_event(Window win, final String initial, long maxlen)
	{
		win.requestLineEvent(initial, maxlen);
	}
	
	@SuppressWarnings("unused")
	private Event select()
	{
		Log.d("Glk", "select()");
		Event ev;
		while (true) {
			try {
				ev = _eventQueue.take();
				Log.d("Glk", "select()ed event " + ev.toString());
				return ev;
			} catch (InterruptedException e) {
			}
		}
	}
	
	@SuppressWarnings("unused")
	private char char_to_lower(char c)
	{
		return Character.toLowerCase(c);
	}
	
	@SuppressWarnings("unused")
	private int fileref_create_by_prompt(long usage, long fmode, long rock)
	{
		switch((int) usage) {
		default:
			Log.w("Glk", "unimplemented usage in fileref_create_by_prompt: " + Long.toString(usage));
			return 0;
		}
	}
	
	@SuppressWarnings("unused")
	private void put_char(char c) {
		_currentWindow.putChar(c);
	}
	
	public View getView() {
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
					Glk.this.notify();
					_done = true;
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
		String name;
		switch (usage) {
		case FileRef.FILEUSAGE_SAVEDGAME:
			name = "savedgames";
			break;
		case FileRef.FILEUSAGE_TRANSCRIPT:
			name = "transcripts";
			break;
		default:
			Log.e("Glk", "I don't know where to place files with usage = " + Integer.toString(usage));
			return null;
		}
		
		return getContext().getDir(name, Context.MODE_PRIVATE);
	}
	
	/** Query Glk capabilities.
	 * 
	 * @param sel Selector -- which capability you are requesting information about.
	 * @param val Parameter for that selector. Optional, pass 0 if not needed.
	 * @return An array which first element is the main return value and the rest is any additional information pertinent.
	 */
	public int[] gestalt(int sel, int val) {
		final int[] zero = { 0 };
		
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
				return new int[] { 1 };
			else
				return new int[] { 0 };
		case GESTALT_CHARINPUT:
			// TODO: handle special characters; this needs getChar support too.
			if (val > 0 && isPrintable((char) val) && val != 10)
				return new int[] { 1 };
			else
				return new int[] { 0 };
		default:
			Log.w("Glk", "unhandled gestalt selector: " + Integer.toString(sel) + " (value " + val + ")");
		case GESTALT_UNICODE:
			return zero; // TODO (implement unicode, right)
		}
	}

	private static boolean isPrintable(char val) {
		if ((val >= 0 && val < 10) || (val > 10 && val < 32) || (val > 126 && val < 160) || val > 255)
			return false;
		else
			return true;
	}
}
