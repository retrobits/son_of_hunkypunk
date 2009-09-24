package org.andglk;

public class LineInputEvent extends Event {
	public String line;
	public int buffer;
	public long len;

	public LineInputEvent(Window w, String s, int lineBuffer, long maxLen) {
		super(w);
		line = s;
		buffer = lineBuffer;
		len = maxLen;
	}
}
