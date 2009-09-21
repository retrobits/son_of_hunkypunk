package org.andglk;

import android.util.Log;

public class FileRef extends CPointed {
	private FileRef(int rock) {
		super(rock);
	}

	public static int createByPrompt(int usage, int mode, int rock) {
		switch (usage) {
		default:
			Log.w("Glk", "unimplemented FileRef.createByPrompt usage " + Integer.toString(usage));
			return 0;
		}
	}
}
