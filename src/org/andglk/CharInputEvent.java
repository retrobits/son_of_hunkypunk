package org.andglk;

public class CharInputEvent extends Event {
	private int mChar;

	public CharInputEvent(Window w, int c) {
		super(w);
		mChar = c;
	}

	public int getChar() {
		return mChar;
	}
}
