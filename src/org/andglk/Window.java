package org.andglk;


public abstract class Window extends CPointed {
	public final static int WINTYPE_TEXTBUFFER = 3;
	
	public abstract void putString(String str);
	public abstract void requestLineEvent(String initial, long maxlen);
	public abstract void put_char(char c);
}
