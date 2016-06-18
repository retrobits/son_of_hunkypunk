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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.andglk.hunkypunk.R;

import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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
		
		@Override
		public void setReverseVideo(long reverse) {
		}
	}

	private static Window _root;
	
	public Window(int rock) {
		super(rock);
	}
	
	static public Window getRoot() {
		return _root;
	}
	
	static protected void setRoot(Window w) {
		_root = w;
	}
	
	static public Window iterate(Window w) {
		if (w == null)
			return _root;

		PairWindow pw;
		if (w instanceof PairWindow) {
			pw = (PairWindow) w;
			if (pw != null)
				return pw.getLeftChild();
		}

		while ((pw = w.getParent()) != null) {
			if (pw.getLeftChild() == w)
				return pw.getRightChild();
			else
				w = pw;
		}
		
		return null;
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
	
	public abstract void requestLineEvent(String initial, long maxlen, int buffer, int unicode);
	protected native int retainVmArray(int buffer, long length);
	/* release is in the C function to convert event since we can't release 
	 * until we've copied it back
	 */
	public abstract LineInputEvent cancelLineEvent();
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
	
	/** Returns window size.
	 * 
	 * @return [width, height]
	 */
	public abstract int[] getSize();
	
	public long close() {
		Glk.getInstance().waitForUi(new Runnable() {
			@Override
			public void run() {
				doClose();
			}
		});
		if (mStream != null)
			return mStream.windowClosed();
		else
			return 0;
	}

	protected void doClose() {
		PairWindow pair = getParent();
		if (pair != null) {
			pair.notifyGone(this);
			pair.dissolve(this);
		} else {
			_root = null;
			if (getView()!= null && getView().getParent()!=null)
				((ViewGroup) getView().getParent()).removeAllViews();
		}
		release();
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
	
	public void setEchoStream(org.andglk.glk.Stream echoStream) {
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
		//Log.d("Glk/Window", "Window.open " + Long.toString(wintype));
		final Glk glk = Glk.getInstance();
		Window wnd;
		switch (wintype) {
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
		} else {
			PairWindow w = new PairWindow(glk, split, wnd, method, size);
			if (_root == split)
				_root = w;
		}
		
		return wnd.getPointer();
	}

	public void echoOff() {
		mStream.echoOff();
	}
	
	public abstract int getType();
	
	public org.andglk.glk.Stream getStream() {
		return mStream;
	}
	
	public abstract class Stream extends org.andglk.glk.Stream {
		protected org.andglk.glk.Stream mEchoStream;

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

		public void setEchoStream(org.andglk.glk.Stream echoStream) {
			if (mEchoStream != null)
				mEchoStream.echoOff(this);
			mEchoStream = echoStream;
			if (echoStream != null)
				echoStream.echoOn(this);
		}

		public long windowClosed() {
			release();
			return mWritten;
		}
		
		@Override
		public int[] close() {
			// can only be closed by closing its window
			_streams.remove(this);
			if (mGlk.getCurrentStream() == this)
				mGlk.setCurrentStream(null);
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
		
		@Override
		protected byte[] doGetBuffer(int maxLen) throws IOException {
			return null;
		}
		
		@Override
		protected String doGetLine(int maxLen) throws IOException {
			return null;
		}
		
		@Override
		public int getPosition() {
			return mWritten;
		}
		
		@Override
		public void setPosition(int pos, int seekMode) {
		}
	}
	
	public static class NoStyleException extends Exception
	{
		public NoStyleException(int style) {
		}

		private static final long serialVersionUID = -2973852656407342179L;
	}
	
	static int getTextAppearanceId(int style) {
		switch(style) {
		case Glk.STYLE_NORMAL:
			return R.style.normal;
		case Glk.STYLE_EMPHASIZED:
			return R.style.emphasized;
		case Glk.STYLE_PREFORMATTED:
			return R.style.preformatted;
		case Glk.STYLE_HEADER:
			return R.style.header;
		case Glk.STYLE_SUBHEADER:
			return R.style.subheader;
		case Glk.STYLE_ALERT:
			return R.style.alert;
		case Glk.STYLE_NOTE:
			return R.style.note;
		case Glk.STYLE_BLOCKQUOTE:
			return R.style.blockquote;
		case Glk.STYLE_INPUT:
			return R.style.input;
		case Glk.STYLE_USER1:
			return R.style.user1;
		case Glk.STYLE_USER2:
			return R.style.user2;
		case Glk.STYLE_NIGHT:
			return R.style.night;
		case Glk.STYLE_NIGHT_HEADER:
				return R.style.night_header;
		case Glk.STYLE_NIGHT_SUBHEADER:
				return R.style.night_subheader;
		default:
			//Log.w("Glk/Window", "unknown style: " + Integer.toString(style));
			return R.style.normal;
		}
	}
	
	public static void stylehintSet(int wintype, int styl, int hint, int val) {
		//Log.d("Glk/Window/stylehintSet", "setting stylehint: " + wintype + " " + styl + " " + hint + " " + val);
		switch (wintype) {
		case Window.WINTYPE_ALLTYPES:
			TextBufferWindow._stylehints.set(styl, hint, val);
			TextGridWindow._stylehints.set(styl, hint, val);
			break;
		case Window.WINTYPE_TEXTBUFFER:
			TextBufferWindow._stylehints.set(styl, hint, val);
			break;
		case Window.WINTYPE_TEXTGRID:
			TextGridWindow._stylehints.set(styl, hint, val);
			break;
		default:
			Log.w("Glk/Window", "unknown window type " + wintype);
		}
	}
	
	public static void stylehintClear(int wintype, int styl, int hint) {
		//Log.d("Glk/Window/stylehintClear", "clearing stylehint: " + wintype + " " + styl + " " + hint);
		switch (wintype) {
		case Window.WINTYPE_ALLTYPES:
			TextBufferWindow._stylehints.clear(styl, hint);
			TextGridWindow._stylehints.clear(styl, hint);
			break;
		case Window.WINTYPE_TEXTBUFFER:
			TextBufferWindow._stylehints.clear(styl, hint);
			break;
		case Window.WINTYPE_TEXTGRID:
			TextGridWindow._stylehints.clear(styl, hint);
			break;
		default:
			Log.w("Glk/Window", "unknown window type " + wintype);
		}
		
	}
	
	abstract boolean styleDistinguish(int style1, int style2);
	
	@Override
	public int getDispatchClass() {
		return GIDISP_CLASS_WINDOW;
	}

	public static void disableAll() {
		Window w = null;
		while ((w = iterate(w)) != null)
			w.getView().setEnabled(false);
	}

	abstract public void flush();

	public Parcelable saveInstanceState() {
		return null;
	}

	public void restoreInstanceState(Parcelable p) {
	}

	public void writeState(ObjectOutputStream stream) throws IOException {
	}

	public void readState(ObjectInputStream stream) throws IOException {
	}
}
