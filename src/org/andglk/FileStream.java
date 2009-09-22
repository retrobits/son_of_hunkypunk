package org.andglk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class FileStream extends Stream {
	private RandomAccessFile mFile;
	private int mWritten = 0;

	public FileStream(FileRef fileref, int fmode, int rock) {
		super(rock);

		String mode;
		File file = fileref.getFile();
		switch (fmode) {
		case FileRef.FILEMODE_WRITE:
			if (file.exists())
				file.delete();
			mode = "rw";
			break;
		default:
			// TODO
			throw new RuntimeException(new NoSuchMethodError("not implemented file mode " + Integer.toString(fmode)));
		}
		
		try {
			mFile = new RandomAccessFile(fileref.getFile(), mode);
		} catch (FileNotFoundException e) {
			assert(false); // should not happen, we make checks earlier
		}
	}

	@Override
	void putChar(char c) {
		try {
			mFile.write(c);
			mWritten++;
		} catch (IOException e) {
			// we don't do error handling, so just log it
			Log.e("Glk/FileStream", "I/O error in putChar", e);
		}
	}

	@Override
	int[] close() {
		try {
			mFile.close();
		} catch (IOException e) {
			// we don't do error handling
			Log.e("Glk/FileStream", "I/O error in close", e);
		}
		release();
		return new int[] { 0, mWritten };
	}
}
