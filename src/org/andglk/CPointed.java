package org.andglk;

public class CPointed {
	protected int _pointer;

	public CPointed() {
		_pointer = makePoint();
	}
	
	private native int makePoint();

	public int getPointer() {
		return _pointer;
	}
	
	private native void releasePoint(int ptr);
	
	public void release() {
		releasePoint(_pointer);
		_pointer = 0;
	}
}
