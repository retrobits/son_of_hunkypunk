package org.andglk;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Glk extends Thread {
	private static final long STYLE_NORMAL = 0;
	private TextView _view;
	private Window _window;
	private Handler _handler;

	@Override
	public void run() {
		// somehow making the method synchronized doesn't work. dalvik thing, perhaps?
		synchronized(this) {
			runProgram();
		}
	}
	
	native private void runProgram();
	
	public Glk(Context context) {
		_view = new TextView(context);
		_window = new Window(_view);
		_handler = new Handler();
	}
	
	@SuppressWarnings("unused")
	private Window window_open(Window split, long method, long size, long wintype, long rock) {
		if (wintype != Window.WINTYPE_TEXTBUFFER) {
			Log.w("Glk", "Unimplemented window type requested: " + Long.toString(wintype));
			return null;
		}
		
		if (split != null) {
			Log.w("Glk", "Window splitting requested but not implemented");
			return null;
		}
		
		return _window;
	}
	
	@SuppressWarnings("unused")
	private void set_window(Window window) {
		// NOOP -- we only have a single window yet
	}
	
	@SuppressWarnings("unused")
	private void put_string(final String str) {
		Log.d("Glk", "put_string(\"" + str + "\")");
		if (str != null)
			_handler.post(new Runnable() {
				@Override
				public void run() {
					_view.append(str);
				}
			});
	}
	
	@SuppressWarnings("unused")
	private void set_style(long styl) {
		if (styl != STYLE_NORMAL)
			Log.w("Glk", "Unimplemented style requested: " + Long.toString(styl));
	}
	
	@SuppressWarnings("unused")
	private void request_line_event(Window win, final String initial, long maxlen)
	{
		if (initial != null)
			_handler.post(new Runnable() {
				@Override
				public void run() {
					_view.append(initial);
				}
			});
	}
	
	@SuppressWarnings("unused")
	private Event select()
	{
		Log.d("Glk", "select()");
		while (true) {
			try {
				wait();
				break;
			} catch (InterruptedException e) {
			}
		}
		return null;
	}

	public View getView() {
		return _view;
	}
}
