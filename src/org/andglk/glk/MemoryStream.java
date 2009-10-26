package org.andglk.glk;

import java.io.IOException;

public class MemoryStream extends Stream {
	private final int mCBuffer;
	private final byte[] mBuffer;
	private final int mMode;
	private int mPos;
	private int mDispatchRock;

	public MemoryStream(int cBuffer, byte[] buffer, int mode, int rock) {
		super(rock);
		mCBuffer = cBuffer;
		mBuffer = buffer;
		mMode = mode;
		mPos = 0;
		if (mode != FileRef.FILEMODE_READ && cBuffer != 0) // we already copied it
			mDispatchRock = retainVmArray(cBuffer, buffer.length);
	}
	
	
	@Override
	protected void doClose() throws IOException {
		if (mMode != FileRef.FILEMODE_READ && mCBuffer != 0) {
			writeOut(mCBuffer, mBuffer);
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
			result[i++] = mBuffer[mPos];
		
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
			if ((result[i++] = mBuffer[mPos]) == '\n')
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
}
