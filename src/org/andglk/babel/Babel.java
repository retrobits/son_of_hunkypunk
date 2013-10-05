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

package org.andglk.babel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import android.util.Log;

public class Babel {
	private static final String TAG = "Babel";

	static {
		System.loadLibrary("babel");
	}
	
	public static String examine(File f) throws IOException {
		final String ifid = examine(f.getAbsolutePath());
		//Log.d(TAG, "examined " + f + ", found IFID " + ifid);
		return ifid;
	}

	private native static String examine(String filename);
}
