package org.andglk;

public class Event {
	public Event(Window w) {
		window = w;
		windowPointer = w.getPointer();
	}
	public Window window;
	public int windowPointer;
}
