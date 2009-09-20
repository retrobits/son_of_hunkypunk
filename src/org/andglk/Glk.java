package org.andglk;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class Glk extends Thread {
	private static final long STYLE_NORMAL = 0;
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
		_frame = new FrameLayout(context);
		_context = context;
	}
	
	@SuppressWarnings("unused")
	private int window_open(Window split, long method, long size, long wintype, long rock) {
		Window wnd;
		switch ((int)wintype) {
		case Window.WINTYPE_TEXTBUFFER:
			wnd = new TextBufferWindow(this, rock);
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
		if (styl != STYLE_NORMAL)
			Log.w("Glk", "Unimplemented style requested: " + Long.toString(styl));
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
}
