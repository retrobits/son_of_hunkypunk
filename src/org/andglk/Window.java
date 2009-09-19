package org.andglk;

import android.widget.TextView;

public interface Window {
	public final static long WINTYPE_TEXTBUFFER = 3;

	public abstract void putString(String str);

	public abstract void requestLineEvent(String initial, long maxlen);
}
