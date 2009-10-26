package org.andglk.glk;

public class LineInputEvent extends Event {
	public String line;
	public int buffer;
	public long len;
	public int rock;

	public LineInputEvent(Window w, String s, int lineBuffer, long maxLen, int dispatchRock) {
		super(w);
		line = s;
		buffer = lineBuffer;
		len = maxLen;
		rock = dispatchRock; 
	}
}
