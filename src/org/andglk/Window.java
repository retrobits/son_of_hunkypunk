package org.andglk;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import android.view.View;

public abstract class Window extends CPointed {
	public class BlankStream extends Stream {
		@Override
		protected int doGetChar() throws IOException {
			return 0;
		}

		@Override
		protected void doPutChar(char c) throws IOException {
		}

		@Override
		protected void doPutString(String str) throws IOException {
		}

		@Override
		public void setStyle(long styl) {
		}
	}

	private static List<Window> _windows = new LinkedList<Window>();
	private static Iterator<Window> _iterator;
	private static Window _last;
	private static Window _root;
	
	public Window(int rock) {
		super(rock);
		_windows.add(this);
	}
	
	static public Window getRoot() {
		return _root;
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
	protected Stream mStream;

	/** Writes @param str to the window's output stream.
	 * 
	 * @param str text to print
	 */
	public void putString(String str) {
		mStream.putString(str);
	}
	
	public abstract void requestLineEvent(String initial, long maxlen);
	public abstract void requestCharEvent();
	public abstract void cancelCharEvent();
	
	/** Writes @p c to the window's output stream.
	 * 
	 * @param c
	 */
	public void putChar(char c) {
		mStream.putChar(c);
	}
	
	public abstract View getView();
	public abstract void clear();
	public abstract long[] getSize();
	
	public long close() {
		PairWindow pair = getParent();
		if (pair != null) {
			pair.notifyGone(this);
			pair.dissolve(this);
		} else
			_root = null;
		release();
		_windows.remove(this);
		return mStream.windowClosed();
	}

	protected void setParent(PairWindow parent) {
		mParent = parent;
	}
	
	public PairWindow getParent() {
		return mParent;
	}
	
	public Window getSibling() {
		if (mParent == null)
			return null;
		return mParent.getSibling(this);
	}
	
	public void setStyle(long styl) {
		mStream.setStyle(styl);
	}
	
	public void setEchoStream(org.andglk.Stream echoStream) {
		mStream.setEchoStream(echoStream);
	}
	
	public int getEchoStream() {
		return mStream.getEchoStream();
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

	public static int open(Window split, int method, int size, int wintype, int rock) {
		final Glk glk = Glk.getInstance();
		Window wnd;
		switch ((int)wintype) {
		case Window.WINTYPE_TEXTBUFFER:
			wnd = new TextBufferWindow(glk, rock);
			break;
		case Window.WINTYPE_TEXTGRID:
			wnd = new TextGridWindow(glk, rock);
			break;
		case Window.WINTYPE_BLANK:
			wnd = new BlankWindow(glk, rock);
			break;
		default:
			Log.w("Glk", "Unimplemented window type requested: " + Long.toString(wintype));
			return 0;
		}
		
		final Window finalWindow = wnd;

		if (split == null) {
			_root = finalWindow;
			glk.waitForUi(new Runnable() {
				@Override
				public void run() {
					glk.getView().addView(finalWindow.getView());
				}
			});
		} else
			new PairWindow(glk, split, wnd, (int) method, (int) size);
		
		return wnd.getPointer();
	}

	public void echoOff() {
		mStream.echoOff();
	}
	
	public abstract int getType();
	
	public org.andglk.Stream getStream() {
		return mStream;
	}
	
	public abstract class Stream extends org.andglk.Stream {
		protected org.andglk.Stream mEchoStream;

		protected Stream() {
			super(0);
		}

		public void echoOff() {
			mEchoStream = null;
		}

		public int getEchoStream() {
			if (mEchoStream != null)
				return mEchoStream.getPointer();
			else
				return 0;
		}

		public void setEchoStream(org.andglk.Stream echoStream) {
			if (mEchoStream != null)
				mEchoStream.echoOff(this);
			mEchoStream = echoStream;
			echoStream.echoOn(this);
		}

		public long windowClosed() {
			release();
			return mWritten;
		}
		
		@Override
		int[] close() {
			// can only be closed by closing its window
			_streams.remove(this);
			return new int[] { 0, 0 };
		}
		
		@Override
		protected void doClose() throws IOException {
			// noop
		}
		
		@Override
		public void putChar(char c) {
			if (mEchoStream != null)
				mEchoStream.putChar(c);
			super.putChar(c);
		}
		
		@Override
		public void putString(String str) {
			if (mEchoStream != null)
				mEchoStream.putString(str);
			super.putString(str);
		}
		
		@Override
		protected int doGetChar() throws IOException {
			return 0;
		}
	}
}
