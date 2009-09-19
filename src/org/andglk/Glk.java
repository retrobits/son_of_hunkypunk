package org.andglk;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

public class Glk extends Thread {
	private static final long STYLE_NORMAL = 0;
	private Window _root, _currentWindow;
	private FrameLayout _veryRoot;

	@Override
	public void run() {
		// somehow making the method synchronized doesn't work. dalvik thing, perhaps?
		synchronized(this) {
			runProgram();
		}
	}
	
	native private void runProgram();
	
	public Glk(Context context) {
		_veryRoot = new FrameLayout(context);
	}
	
	@SuppressWarnings("unused")
	private Window window_open(Window split, long method, long size, long wintype, long rock) {
		if (split != null) {
			Log.w("Glk", "Window splitting requested but not implemented");
			return null;
		}
		
		switch ((int)wintype) {
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
		Log.d("Glk", "put_string(\"" + str + "\")");
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
		return _veryRoot;
	}
}
