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


public abstract class CPointed {
	public final static int GIDISP_CLASS_WINDOW = 0;
	public final static int GIDISP_CLASS_STREAM = 1;
	public final static int GIDISP_CLASS_FILEREF = 2;
	public final static int GIDISP_CLASS_SCHANNEL = 3;

	private int mPointer;
	private int mRock;
	private int mDispatchRock;
	
	public CPointed(int rock) {
		mPointer = makePoint();
		setRock(rock);
	}
	
	private native int makePoint();

	public int getPointer() {
		return mPointer;
	}
	
	private native void releasePoint(int ptr);
	
	public void release() {
		if (mPointer != 0) releasePoint(mPointer);
		mPointer = 0;
	}

	public void setRock(int rock) {
		mRock = rock;
	}

	public int getRock() {
		return mRock;
	}
	
	public void setDispatchRock(int rock) {
		mDispatchRock = rock;
	}

	public int getDispatchRock() {
		return mDispatchRock;
	}
	
	public abstract int getDispatchClass();
}
