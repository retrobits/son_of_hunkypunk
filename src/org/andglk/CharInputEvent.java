package org.andglk;

import android.view.KeyEvent;

public class CharInputEvent extends Event {
	private int mChar;

	public CharInputEvent(Window w, int c) {
		super(w);
		mChar = c;
	}

	public int getChar() {
		return mChar;
	}

	/** Tries to create a CharInputEvent given an Android KeyEvent.
	 * 
	 * @param window Window which received the KeyEvent.
	 * @param event KeyEvent to analyze.
	 * @return A new CharInputEvent representing <code>event</code> or <code>null</code> if it doesn't correspond to any.
	 */
	public static CharInputEvent fromKeyEvent(Window window, KeyEvent event) {
		if (event.isPrintingKey()) {
			final int c = event.getUnicodeChar();
			if (c >= 0 && c < 256)
				return new CharInputEvent(window, c);
		}
		
		return null;
	}
}
