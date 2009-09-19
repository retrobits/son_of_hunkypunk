package org.andglk;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Glk {
	private TextView _view;
	private Window _window;

	native public void start();
	
	public Glk(Context context) {
		_view = new TextView(context);
		_window = new Window(_view);
	}
	
	Window glk_window_open(Window split, long method, long size, long wintype, long rock) {
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
	
	void glk_set_window(Window window) {
		// NOOP -- we only have a single window yet
	}
	
	void glk_put_string(String str) {
		_view.append(str);
	}

	public View getView() {
		return _view;
	}
}
