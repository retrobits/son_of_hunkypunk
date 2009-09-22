package org.andglk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class FileStream extends Stream {
	private RandomAccessFile mFile;
	private int mWritten = 0;
	private int mRead = 0;

	public FileStream(FileRef fileref, int fmode, int rock) {
		super(rock);

		File file = fileref.getFile();
		try {
			switch (fmode) {
			case FileRef.FILEMODE_WRITE:
				if (file.exists())
					file.delete();
				mFile = new RandomAccessFile(fileref.getFile(), "rw");
				break;
			case FileRef.FILEMODE_READ:
				mFile = new RandomAccessFile(fileref.getFile(), "r");
				break;
			case FileRef.FILEMODE_WRITEAPPEND:
				mFile = new RandomAccessFile(fileref.getFile(), "rw");
				mFile.seek(mFile.length());
				break;
			default:
				// TODO
				throw new RuntimeException(new NoSuchMethodError("not implemented file mode " + Integer.toString(fmode)));
			}
		
		} catch (FileNotFoundException e) {
			assert(false); // should not happen, we make checks earlier
		} catch (IOException e) {
			Log.e("Glk/FileStream", "I/O when opening file", e);
			throw new RuntimeException(e);
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
		return new int[] { mRead, mWritten };
	}

	@Override
	int getChar() {
		try {
			int res = mFile.read();
			mRead++;
			return res;
		} catch (IOException e) {
			Log.e("Glk/FileStream", "I/O error in getChar", e);
			return -1;
		}
	}

	@Override
	public void setStyle(long styl) {
		// TODO: consider writing transcripts as rich text
	}

	@Override
	protected void doPutString(String str) throws IOException {
		mFile.writeBytes(str);
	}
}
