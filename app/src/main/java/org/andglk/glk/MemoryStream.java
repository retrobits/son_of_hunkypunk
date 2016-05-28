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

import java.io.IOException;

	/* todo :
	  change const public MemoryStream(int cBuffer, int[] buffer, int mode, int rock)
	  (move copy of bytes to native side non-unicode stream open)
	  private native void writeOutUni(int buffer, int[] buffer2)
	  protected int[] doGetBufferUni(int maxLen) throws IOException
	  protected void doPutChar(int c) throws IOException
	  protected void doClose() throws IOException -> check for unicode call writeOut or writeOutUni
	*/

public class MemoryStream extends Stream {
	private final int mCBuffer;
	private final int[] mBuffer;
	private final int mMode;
	private int mPos;
	private int mDispatchRock;

	public MemoryStream(int cBuffer, byte[] buffer, int mode, int rock) {
		super(rock);
		mCBuffer = cBuffer;
		mBuffer = new int[buffer.length];
		for (int i = 0; i< buffer.length; i++)
			mBuffer[i] = buffer[i];
		mMode = mode;
		mPos = 0;
		if (mode != FileRef.FILEMODE_READ && cBuffer != 0) // we already copied it
			mDispatchRock = retainVmArray(cBuffer, buffer.length);
	}
	
	
	@Override
	protected void doClose() throws IOException {
		if (mMode != FileRef.FILEMODE_READ && mCBuffer != 0) {

			byte[] buf = new byte[mBuffer.length];
			for (int i = 0; i< mBuffer.length; i++)
				buf[i] = (byte)mBuffer[i];

			writeOut(mCBuffer, buf);
			releaseVmArray(mCBuffer, mBuffer.length, mDispatchRock);
		}
	}

	protected native int retainVmArray(int buffer, long len);
	protected native void releaseVmArray(int buffer, int length, int dispatchRock);
	private native void writeOut(int buffer, byte[] buffer2);

	@Override
	protected byte[] doGetBuffer(int maxLen) throws IOException {
		if (mMode == FileRef.FILEMODE_WRITE)
			throw new IOException("tried to read from write-only MemoryBuffer");
		
		int end = mPos + maxLen;
		if (end > mBuffer.length)
			end = mBuffer.length;
		
		byte[] result = new byte[end - mPos];
		for (int i = 0; mPos != end; mPos++)
			result[i++] = (byte)mBuffer[mPos];
		
		return result;
	}

	@Override
	protected int doGetChar() throws IOException {
		if (mMode == FileRef.FILEMODE_WRITE)
			throw new IOException("tried to read from write-only MemoryBuffer");
		
		if (mPos >= mBuffer.length)
			return -1;
		else
			return mBuffer[mPos++];
	}

	@Override
	protected String doGetLine(int maxLen) throws IOException {
		if (mMode == FileRef.FILEMODE_WRITE)
			throw new IOException("tried to read from write-only MemoryBuffer");
		
		int end = mPos + maxLen;
		if (end > mBuffer.length)
			end = mBuffer.length;
		
		byte[] result = new byte[end - mPos];
		for (int i = 0; mPos != end; mPos++)
			if ((result[i++] = (byte)mBuffer[mPos]) == '\n')
				break;
		
		return new String(result);
	}

	@Override
	protected void doPutChar(char c) throws IOException {
		if (mMode == FileRef.FILEMODE_READ)
			throw new IOException("tried to write to a read-only MemoryBuffer");
		
		if (mPos == mBuffer.length)
			return;
		
		mBuffer[mPos++] = (byte) c;
	}

	@Override
	protected void doPutString(String str) throws IOException {
		if (mMode == FileRef.FILEMODE_READ)
			throw new IOException("tried to write to a read-only MemoryBuffer");

		final int len = Math.min(str.length(), mBuffer.length - mPos);
		if (len == 0)
			return;

		for (int i = 0; i < len; i++)
			mBuffer[mPos++] = (byte) str.charAt(i);
	}

	@Override
	public int getPosition() {
		return mPos;
	}

	@Override
	public void setPosition(int pos, int seekMode) {
		switch (seekMode) {
		case SEEKMODE_CURRENT:
			pos += mPos;
			break;
		case SEEKMODE_END:
			pos += mBuffer.length;
			break;
		default:
		}
		mPos = pos;
		if (mPos > mBuffer.length)
			mPos = mBuffer.length;
	}

	@Override
	public void setStyle(long styl) {
	}
	
	@Override
	public void setReverseVideo(long reverse) {
	}
}
