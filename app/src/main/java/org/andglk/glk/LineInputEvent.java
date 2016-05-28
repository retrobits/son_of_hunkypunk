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

public class LineInputEvent extends Event {
	public String line;
	public int buffer;
	public long len;
	public int rock;
	public int unicode;

	public LineInputEvent(Window w, String s, int lineBuffer, long maxLen, int dispatchRock, boolean unicode) {
		super(w);
		line = s;
		buffer = lineBuffer;
		len = maxLen;
		rock = dispatchRock; 
		this.unicode = unicode ? 1:0;
	}
}
