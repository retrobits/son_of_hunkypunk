/*
	Copyright © 2009 Rafał Rzepecki <divided.mind@gmail.com>

	This file is part of Hunky Punk.

    Hunky Punk is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Hunky Punk is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Hunky Punk.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.andglk.glk;

import android.view.KeyEvent;

public class CharInputEvent extends Event {
	private int mChar;
	
	public final static int KEYCODE_UNKNOWN = 0xffffffff;
	public final static int KEYCODE_LEFT = 0xfffffffe;
	public final static int KEYCODE_RIGHT = 0xfffffffd;
	public final static int KEYCODE_UP = 0xfffffffc;
	public final static int KEYCODE_DOWN = 0xfffffffb;
	public final static int KEYCODE_RETURN = 0xfffffffa;
	public final static int KEYCODE_DELETE = 0xfffffff9;
	public final static int KEYCODE_ESCAPE = 0xfffffff8;
	public final static int KEYCODE_TAB = 0xfffffff7;
	public final static int KEYCODE_PAGEUP = 0xfffffff6;
	public final static int KEYCODE_PAGEDOWN = 0xfffffff5;
	public final static int KEYCODE_HOME = 0xfffffff4;
	public final static int KEYCODE_END = 0xfffffff3;
	public final static int KEYCODE_FUNC1 = 0xffffffef;
	public final static int KEYCODE_FUNC2 = 0xffffffee;
	public final static int KEYCODE_FUNC3 = 0xffffffed;
	public final static int KEYCODE_FUNC4 = 0xffffffec;
	public final static int KEYCODE_FUNC5 = 0xffffffeb;
	public final static int KEYCODE_FUNC6 = 0xffffffea;
	public final static int KEYCODE_FUNC7 = 0xffffffe9;
	public final static int KEYCODE_FUNC8 = 0xffffffe8;
	public final static int KEYCODE_FUNC9 = 0xffffffe7;
	public final static int KEYCODE_FUNC10 = 0xffffffe6;
	public final static int KEYCODE_FUNC11 = 0xffffffe5;
	public final static int KEYCODE_FUNC12 = 0xffffffe4;


	public CharInputEvent(Window w, int c) {
		super(w);
		mChar = c;
	}

	public int getChar() {
		return mChar;
	}
	public void setChar(int v) {
		mChar = v;
	}
	
	private static CharInputEvent sInstance = new CharInputEvent(null, 0); 
	/** Tries to create a CharInputEvent given an Android KeyEvent.
	 * 
	 * @param window Window which received the KeyEvent.
	 * @param event KeyEvent to analyze.
	 * @return A new CharInputEvent representing <code>event</code> or <code>null</code> if it doesn't correspond to any.
	 */
	public static CharInputEvent fromKeyEvent(Window window, KeyEvent event) {
		int c = KEYCODE_UNKNOWN;
		
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_DEL:
			c = KEYCODE_DELETE;
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			c = KEYCODE_RETURN;
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			c = KEYCODE_DOWN;
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			c = KEYCODE_LEFT;
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			c = KEYCODE_RIGHT;
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			c = KEYCODE_UP;
			break;
		case KeyEvent.KEYCODE_SPACE:
			c = ' ';
			break;
		case KeyEvent.KEYCODE_BACK:
			c = KEYCODE_ESCAPE;
			break;
		case KeyEvent.KEYCODE_TAB:
			c = KEYCODE_TAB;
			break;
		default:
			if (event.isPrintingKey())
				if ((c = event.getUnicodeChar()) < 0 || c > 255)
					c = KEYCODE_UNKNOWN;
		}
		
		sInstance.window = window;
		sInstance.mChar = c;
		
		return sInstance;
	}

	public static boolean accepts(int val) {
		switch (val) {
		case KEYCODE_LEFT:
		case KEYCODE_RIGHT:
		case KEYCODE_UP:
		case KEYCODE_DOWN:
		case KEYCODE_RETURN:
		case KEYCODE_DELETE:
		case KEYCODE_ESCAPE:
		case KEYCODE_TAB:
			return true;
		case KEYCODE_UNKNOWN:
		case KEYCODE_PAGEUP:
		case KEYCODE_PAGEDOWN:
		case KEYCODE_HOME:
		case KEYCODE_END:
		case KEYCODE_FUNC1:
		case KEYCODE_FUNC2:
		case KEYCODE_FUNC3:
		case KEYCODE_FUNC4:
		case KEYCODE_FUNC5:
		case KEYCODE_FUNC6:
		case KEYCODE_FUNC7:
		case KEYCODE_FUNC8:
		case KEYCODE_FUNC9:
		case KEYCODE_FUNC10:
		case KEYCODE_FUNC11:
		case KEYCODE_FUNC12:
			return false;
		default:
			return ((val >= 0 && val < 32) || (val > 126 && val < 160) || val > 255);
		}
	}
}
