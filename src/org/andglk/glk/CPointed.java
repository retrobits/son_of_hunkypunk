package org.andglk.glk;


public abstract class CPointed {
	public final static int GIDISP_CLASS_WINDOW = 0;
	public final static int GIDISP_CLASS_STREAM = 1;
	public final static int GIDISP_CLASS_FILEREF = 2;
	public final static int GIDISP_CLASS_SCHANNEL = 3;

	protected int mPointer;
	private int mRock;
	private int mDispatchRock;
	
	public CPointed(int rock) {
		mPointer = makePoint();
		setRock(rock);
	}
	
	private native int makePoint();

	public int getPointer() {
		return mPointer;
	}
	
	private native void releasePoint(int ptr);
	
	public void release() {
		releasePoint(mPointer);
		mPointer = 0;
	}

	public void setRock(int rock) {
		mRock = rock;
	}

	public int getRock() {
		return mRock;
	}
	
	public void setDispatchRock(int rock) {
		mDispatchRock = rock;
	}

	public int getDispatchRock() {
		return mDispatchRock;
	}
	
	public abstract int getDispatchClass();
}
