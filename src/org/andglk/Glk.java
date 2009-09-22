package org.andglk;

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
		_currentWindow.put_char(c);
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
}
