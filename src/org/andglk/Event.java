package org.andglk;

public class Event {
	public Event(Window w) {
		window = w;
		windowPointer = w == null ? 0 : w.getPointer();
	}
	public Window window;
	public int windowPointer;
}
