package org.andglk;

public class LineInputEvent extends Event {
	public String line;

	public LineInputEvent(Window w, String s) {
		super(w);
		line = s;
	}
}
