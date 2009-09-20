package org.andglk;

import android.view.View;


public abstract class Window extends CPointed {
	public final static int WINTYPE_ALLTYPES = 0;
	public final static int WINTYPE_PAIR = 1;
	public final static int WINTYPE_BLANK = 2;
	public final static int WINTYPE_TEXTBUFFER = 3;
	public final static int WINTYPE_TEXTGRID = 4;
	public final static int WINTYPE_GRAPHICS = 5;

	public final static int WINMETHOD_LEFT = 0;
	public final static int WINMETHOD_RIGHT = 0x01;
	public final static int WINMETHOD_ABOVE = 0x02;
	public final static int WINMETHOD_BELOW = 0x03;
	public final static int WINMETHOD_DIRMASK = 0x0f;

	public final static int WINMETHOD_FIXED = 0x10;
	public final static int WINMETHOD_PROPORTIONAL = 0x20;
	public final static int WINMETHOD_DIVISIONMASK = 0xf0;

	public void putString(String str) { throw new RuntimeException(new NoSuchMethodException()); }
	public void requestLineEvent(String initial, long maxlen) { throw new RuntimeException(new NoSuchMethodException()); }
	public void put_char(char c) { throw new RuntimeException(new NoSuchMethodException()); }
	public abstract View getView();
	public float measureCharacterWidth() { throw new RuntimeException(new NoSuchMethodException()); }
	public float measureCharacterHeight() { throw new RuntimeException(new NoSuchMethodException()); }
}
