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
	private FrameLayout _veryRoot;
	private Handler _uiHandler = new Handler();
	private BlockingQueue<Event> _eventQueue = new LinkedBlockingQueue<Event>();

	@Override
	public void run() {
		runProgram();
	}
	
	native private void runProgram();
	
	public Glk(Context context) {
		_veryRoot = new FrameLayout(context);
	}
	
	@SuppressWarnings("unused")
	private Window window_open(int pointer, Window split, long method, long size, long wintype, long rock) {
		if (split != null) {
			Log.w("Glk", "Window splitting requested but not implemented");
			return null;
		}
		
		switch ((int)wintype) {
		case Window.WINTYPE_TEXTBUFFER:
			return _root = new TextBufferWindow(this, pointer, rock);
		default:
			Log.w("Glk", "Unimplemented window type requested: " + Long.toString(wintype));
			return null;
		}
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
	private FileRef fileref_create_by_prompt(int pointer, long usage, long fmode, long rock)
	{
		switch((int) usage) {
		default:
			Log.w("Glk", "unimplemented usage in fileref_create_by_prompt: " + Long.toString(usage));
			return null;
		}
	}
	
	@SuppressWarnings("unused")
	private void put_char(char c) {
		_currentWindow.put_char(c);
	}
	
	public View getView() {
		return _veryRoot;
	}

	public Handler getUiHandler() {
		return _uiHandler;
	}

	public void postEvent(Event e) {
		_eventQueue.add(e);
	}
}
