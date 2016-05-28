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

import android.view.View;

public class BlankWindow extends Window {
	private View mView;
	
	public BlankWindow(Glk glk, int rock) {
		super(rock);
		mView = new View(glk.getContext());
		mStream = new BlankStream();
	}

	@Override
	public int[] getSize() {
		return new int[] { 0, 0 };
	}

	@Override
	public View getView() {
		return mView;
	}

	@Override
	public int measureHeight(int size) {
		return 0;
	}

	@Override
	public int measureWidth(int size) {
		return 0;
	}

	@Override
	public int getType() {
		return WINTYPE_BLANK;
	}

	@Override
	public void clear() {
	}

	@Override
	public void requestCharEvent() {
	}

	@Override
	public void cancelCharEvent() {
	}

	@Override
	public void requestLineEvent(String initial, long maxlen, int buffer, int unicode) {
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		return null;
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		return false;
	}

	@Override
	public void flush() {
	}
}
