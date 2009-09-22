package org.andglk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class FileStream extends Stream {
	private RandomAccessFile mFile;

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
}
