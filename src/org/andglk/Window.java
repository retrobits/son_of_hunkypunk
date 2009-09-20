package org.andglk;


public abstract class Window {
	public final static int WINTYPE_TEXTBUFFER = 3;
	private int _pointer;
	
	public Window(int pointer) {
		_pointer = pointer;
	}
	
	public int getPointer() {
		return _pointer;
	}
	
	public abstract void putString(String str);
	public abstract void requestLineEvent(String initial, long maxlen);
}
