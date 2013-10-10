/*
	Copyright © 2009-2010 Rafał Rzepecki <divided.mind@gmail.com>

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

package org.andglk.hunkypunk;

import android.os.Environment;
import java.io.File;

public abstract class Paths {
	public static File cardDirectory() {
		return new File(Environment.getExternalStorageDirectory().getPath());
	}

	public static File dataDirectory() {
		File f = new File(cardDirectory(),"Android/data/org.andglk.hunkypunk");
		if (!f.exists()) f.mkdir();
		return f;
	}

	public static File coverDirectory() {
		File f = new File(dataDirectory(),"covers");
		if (!f.exists()) f.mkdir();
		return f;
	}

	public static File tempDirectory() {
		File f = new File(dataDirectory(),"temp");
		if (!f.exists()) f.mkdir();
		return f;
	}

	public static File fontDirectory() {
		File f = new File(cardDirectory(),"Fonts");
		if (!f.exists()) f.mkdir();
		return f;
	}

	public static File ifDirectory() {
		File f = new File(cardDirectory(),"Interactive Fiction");
		if (!f.exists()) f.mkdir();
		return f;
	}

	public static File transcriptDirectory() {
		File f = new File(ifDirectory(),"transcripts");
		if (!f.exists()) f.mkdir();
		return f;
	}
}