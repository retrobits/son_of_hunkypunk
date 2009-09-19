package org.andglk;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Glk {
	private static final long STYLE_NORMAL = 0;
	private TextView _view;
	private Window _window;

	native public void start();
	
	public Glk(Context context) {
		_view = new TextView(context);
		_window = new Window(_view);
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
	private void put_string(String str) {
		_view.append(str);
	}
	
	@SuppressWarnings("unused")
	private void set_style(long styl) {
		if (styl != STYLE_NORMAL)
			Log.w("Glk", "Unimplemented style requested: " + Long.toString(styl));
	}

	public View getView() {
		return _view;
	}
}
