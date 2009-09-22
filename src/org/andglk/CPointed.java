package org.andglk;


public class CPointed {
	protected int mPointer;
	private int mRock;
	
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
}
