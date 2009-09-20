package org.andglk;

import android.view.View;


public abstract class Window extends CPointed {
	public final static int WINTYPE_TEXTBUFFER = 3;

	public final static int WINMETHOD_LEFT = 0;
	public final static int WINMETHOD_RIGHT = 0x01;
	public final static int WINMETHOD_ABOVE = 0x02;
	public final static int WINMETHOD_BELOW = 0x03;
	public final static int WINMETHOD_DIRMASK = 0x0f;

	public final static int WINMETHOD_FIXED = 0x10;
	public final static int WINMETHOD_PROPORTIONAL = 0x20;
	public final static int WINMETHOD_DIVISIONMASK = 0xf0;

	public void putString(String str) {}
	public void requestLineEvent(String initial, long maxlen) {}
	public void put_char(char c) {}
	public abstract View getView();
	public abstract float measureZeroWidth();
	public abstract float measureZeroHeight();
}
