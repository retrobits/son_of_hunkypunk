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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

public class FileStream {}

/* moved to native code
public class FileStream extends Stream {
	private RandomAccessFile mFile;

	public FileStream(FileRef fileref, int fmode, int rock) {
		super(rock);
		doOpen(fileref.getFile(), fmode, rock);
	}

	public FileStream(String path, int fmode, int rock) {
		super(rock);
		doOpen(new File(path), fmode, rock);
	}
	
	private void doOpen(File file, int fmode, int rock) {
		try {
			switch (fmode) {
			case FileRef.FILEMODE_WRITE:
				if (file.exists())
					file.delete();
				// fall through
			case FileRef.FILEMODE_READWRITE:
				mFile = new RandomAccessFile(file, "rw");
				break;
			case FileRef.FILEMODE_READ:
				mFile = new RandomAccessFile(file, "r");
				break;
			case FileRef.FILEMODE_WRITEAPPEND:
				mFile = new RandomAccessFile(file, "rw");
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
	protected void doPutChar(char c) throws IOException {
		mFile.write(c);
	}

	@Override
	protected void doClose() throws IOException {
		mFile.close();
	}

	@Override
	protected int doGetChar() throws IOException {
		return mFile.read();
	}

	@Override
	public void setStyle(long styl) {
		// TODO: consider writing transcripts as rich text
	}
	
	@Override
	public void setReverseVideo(long reverse) {
		// TODO: consider writing transcripts as rich text
	}

	@Override
	protected void doPutString(String str) throws IOException {
		mFile.writeBytes(str);
	}

	@Override
	protected byte[] doGetBuffer(int maxLen) throws IOException {
		if (mFile.getFilePointer() == mFile.length())
			return null;
		
		final byte[] buffer = new byte[maxLen];
		final int count = mFile.read(buffer);
		if (count != maxLen) {
			final byte[] res = new byte[count];
			System.arraycopy(buffer, 0, res, 0, count);
			return res;
		}
		return buffer;
	}
 
	@Override
	protected String doGetLine(int maxLen) {
		StringBuilder sb = new StringBuilder(maxLen);
		byte b;
		for (; maxLen > 0; maxLen--)
			try {
				sb.append(b = mFile.readByte());
				if (b == '\n')
					break;
			} catch (IOException e) {
				break;
			}
		return sb.toString();
	}

	@Override
	public int getPosition() {
		try {
			return (int) mFile.getFilePointer();
		} catch (IOException e) {
			Log.e("Glk/FileStream", "I/O when getting position", e);
			return 0;
		}
	}

	@Override
	public void setPosition(int pos, int seekMode) {
		try {
			switch (seekMode) {
			case SEEKMODE_CURRENT:
				pos += mFile.getFilePointer();
				break;
			case SEEKMODE_END:
				pos += mFile.length();
				break;
			case SEEKMODE_START:
			default:
				// we're ok
			}
			
			mFile.seek(pos);
		} catch (IOException e) {
			Log.e("Glk/FileStream", "I/O when seeking", e);
		}
	}
}
*/