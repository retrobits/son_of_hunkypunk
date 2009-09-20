package org.andglk;


public interface Window {
	public final static int WINTYPE_TEXTBUFFER = 3;
	
	public abstract void putString(String str);
	public abstract void requestLineEvent(String initial, long maxlen);
}
