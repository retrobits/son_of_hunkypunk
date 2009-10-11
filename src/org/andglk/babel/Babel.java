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
		FileInputStream fis = new FileInputStream(f);
		FileChannel fc = fis.getChannel();
		MappedByteBuffer map = fc.map(MapMode.READ_ONLY, 0, f.length());
		
		final String ifid = examine(map);
		Log.d(TAG, "examined " + f + ", found IFID " + ifid);
		return ifid;
	}

	private native static String examine(MappedByteBuffer map);
}
