package org.andglk;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.view.View;

public abstract class Window extends CPointed {
	private static List<Window> _windows = new LinkedList<Window>();
	private static Iterator<Window> _iterator;
	private static Window _last;
	
	public Window(int rock) {
		super(rock);
		_windows.add(this);
	}
	
	static public Window iterate(Window w) {
		if (w == null)
			_iterator = _windows.iterator();
		else if (_last != w) {
			_iterator = _windows.iterator();
			while (_iterator.next() != w);
		}
		_last = _iterator.next();
		return _last;
	}

	public final static int WINTYPE_ALLTYPES = 0;
	public final static int WINTYPE_PAIR = 1;
	public final static int WINTYPE_BLANK = 2;
	public final static int WINTYPE_TEXTBUFFER = 3;
	public final static int WINTYPE_TEXTGRID = 4;
	public final static int WINTYPE_GRAPHICS = 5;

	public final static int WINMETHOD_LEFT = 0;
	public final static int WINMETHOD_RIGHT = 0x01;
	public final static int WINMETHOD_ABOVE = 0x02;
	public final static int WINMETHOD_BELOW = 0x03;
	public final static int WINMETHOD_DIRMASK = 0x0f;

	public final static int WINMETHOD_FIXED = 0x10;
	public final static int WINMETHOD_PROPORTIONAL = 0x20;
	public final static int WINMETHOD_DIVISIONMASK = 0xf0;
	
	private PairWindow mParent = null;
	private long _written = 0;
	protected Stream mEchoStream, mStream;

	/** Writes @param str to the window's output stream.
	 * 
	 * @param str text to print
	 */
	public void putString(String str) {
		if (mEchoStream != null)
			mEchoStream.putString(str);
		_written += str.length(); 
	}
	
	public void requestLineEvent(String initial, long maxlen) { throw new RuntimeException(new NoSuchMethodException()); }
	
	/** Writes @p c to the window's output stream.
	 * 
	 * @param c
	 */
	public void putChar(char c) {
		if (mEchoStream != null)
			mEchoStream.putChar(c);
		_written++;
	}
	
	public abstract View getView();
	public void clear() { throw new RuntimeException(new NoSuchMethodException()); }
	public abstract long[] getSize();
	public void move_cursor(long x, long y) { throw new RuntimeException(new NoSuchMethodException()); }
	
	public long close() {
		PairWindow pair = getParent();
		if (pair != null) {
			pair.notifyGone(this);
			pair.dissolve(this);
		}
		release();
		_windows.remove(this);
		return _written;
	}

	protected void setParent(PairWindow parent) {
		mParent = parent;
	}
	
	public PairWindow getParent() {
		return mParent;
	}
	
	public void setStyle(long styl) {
		if (mEchoStream != null)
			mEchoStream.setStyle(styl);
	}
	
	public void setEchoStream(Stream echoStream) {
		mEchoStream = echoStream;
	}

	/** Get pixel size from window-specific measurement.
	 * 
	 * @note This value can change.
	 * 
	 * @param size Width in window-specific units.
	 * @return Number of pixels represented by size.
	 */
	abstract public int measureWidth(int size);

	/** Get pixel size from window-specific measurement.
	 * 
	 * @note This value can change.
	 * 
	 * @param size Height in window-specific units.
	 * @return Number of pixels represented by size.
	 */
	abstract public int measureHeight(int size);
}
