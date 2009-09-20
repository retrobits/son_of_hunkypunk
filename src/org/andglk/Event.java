package org.andglk;

public class Event {
	public Event(CPointed w) {
		window = w;
		windowPointer = w.getPointer();
	}
	public CPointed window;
	public int windowPointer;
}
