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

package org.andglkmod.babel;

import java.io.File;
import java.io.IOException;
import android.util.Log;

public class Babel {
	private static final String TAG = "Babel";

	static {
		try {
			System.loadLibrary("babel");
		} catch (UnsatisfiedLinkError e) {
			Log.e(TAG, "Failed to load babel native library", e);
		}
	}
	
	public static String examine(File f) throws IOException {
		if (f == null || !f.exists() || !f.canRead()) {
			Log.w(TAG, "File does not exist or is not readable: " + f);
			return null;
		}
		
		if (f.length() == 0) {
			Log.w(TAG, "File is empty: " + f);
			return null;
		}
		
		if (f.length() > 10 * 1024 * 1024) { // 10MB limit
			Log.w(TAG, "File too large: " + f + " (" + f.length() + " bytes)");
			return null;
		}
		
		try {
			final String ifid = examine(f.getAbsolutePath());
			//Log.d(TAG, "examined " + f + ", found IFID " + ifid);
			return ifid;
		} catch (Exception e) {
			Log.e(TAG, "Native crash while examining file: " + f, e);
			return null;
		} catch (Error e) {
			Log.e(TAG, "Native error while examining file: " + f, e);
			return null;
		}
	}

	private native static String examine(String filename);
}
