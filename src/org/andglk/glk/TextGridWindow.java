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

import org.andglk.hunkypunk.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.FloatMath;
import android.view.KeyEvent;

public class TextGridWindow extends Window {
	 public static class TextGridParcelable implements Parcelable {
		public char[] mFrameBuf;
		public int mHeight;
		public int mWidth;
		public boolean mLineEventPending;
		public boolean mCharEventPending;
	     
	     @Override
		public void writeToParcel(Parcel out, int flags) {
	    	 out.writeInt(mHeight);
	    	 out.writeInt(mWidth);
	    	 assert (mFrameBuf.length == mHeight * mWidth);
	         out.writeCharArray(mFrameBuf);
	         out.writeBooleanArray(new boolean[] { mLineEventPending, mCharEventPending });
	     }

	     public static final Parcelable.Creator<TextGridParcelable> CREATOR
	             = new Parcelable.Creator<TextGridParcelable>() {
	         @Override
			public TextGridParcelable createFromParcel(Parcel in) {
	             return new TextGridParcelable(in);
	         }

	         @Override
			public TextGridParcelable[] newArray(int size) {
	             return new TextGridParcelable[size];
	         }
	     };
	     
	     private TextGridParcelable(Parcel in) {
	    	 mHeight = in.readInt();
	    	 mWidth = in.readInt();
	    	 mFrameBuf = new char[mHeight * mWidth];
	    	 in.readCharArray(mFrameBuf);
	    	 boolean[] val = new boolean[2];
	    	 in.readBooleanArray(val);
	    	 mLineEventPending = val[0];
	    	 mCharEventPending = val[1];
	     }

		public TextGridParcelable() {
		}

		@Override
		public int describeContents() {
			return 0;
		}
	}

	 @Override
	public Parcelable saveInstanceState() {
		 if (mView == null) return null; 
		 return mView.onSaveInstanceState();
	}
	
	@Override
	public void restoreInstanceState(Parcelable p) {
		if (mView == null) return;
		mView.onRestoreInstanceState(p);
	}
	
	private class Stream extends Window.Stream {
		@Override
		public int getPosition() {
			if (mView == null) return 0; 
			return mView.getPosition();
		}
		
		@Override
		public void setPosition(int pos, int seekMode) {
			if (mView == null) return; 
			mView.setPosition(pos, seekMode);
		}
		
		@Override
		protected void doPutChar(char c) throws IOException {
			if (mView == null) return; 

			if (c == 0x0a) {
				mView._pos += mView.mWidth;
				mView._pos -= mView._pos % mView.mWidth;
			}
			else {
				if (mView._pos >= mView.mWidth * mView.mHeight)
					return;
			
				mView._styleBuf[mView._pos] = mView.mCurrentStyle;
				mView._reverseBuf[mView._pos] = mView.mReverseVideo;
				mView.mFrameBufTemp[mView._pos++] = c;
			}
			
			mView.mIsClear = false;
		}

		@Override
		protected void doPutString(String str) throws IOException {
			if (mView == null || str == null) return; 

			mView.putString(str);
		}

		@Override
		public void setStyle(long styl) {
			if (mView == null) return; 

			mView.setStyle((int) styl);
		}
		
		@Override
		public void setReverseVideo(long reverse) {
			if (mView == null) return; 

			mView.setReverseVideo(reverse != 0);
		}
	}

	public boolean mChanged;
	
	protected class View extends android.view.View {
		private final int _fontSize;
		private final Paint mPaint;
		protected int mWidth;
		protected int mHeight;
		protected char[] mFrameBuf, mFrameBufTemp;
		private int[] _styleBuf;
		private boolean[] _reverseBuf;
		protected int _pos;
		private boolean mCharEventPending;
		private boolean mLineEventPending;
		private int mLineInputEnd;
		private int mLineInputStart;
		private final int mDefaultColor;
		protected boolean mIsClear;
		private final Rect mRect = new Rect();
		private final Paint mBackPaint;
		private int mCurrentStyle;
		private boolean mReverseVideo;

		public View(Context context) {
			super(context, null, R.attr.textGridWindowStyle);
			mIsClear = true;
			setClickable(true);
			setEnabled(false);
			TypedArray ta = context.obtainStyledAttributes(null, new int[] { android.R.attr.textAppearance }, 
					R.attr.textGridWindowStyle, 0);
			int res = ta.getResourceId(0, -1);
			ta = context.obtainStyledAttributes(res, new int[] { android.R.attr.textSize, android.R.attr.textColor });
			_fontSize = (int)(ta.getDimensionPixelSize(0, -1)*1.2);

			mDefaultColor = ta.getColor(1, 0xffffffff);
			
			mPaint = new Paint();
			mPaint.setTypeface(Typeface.MONOSPACE);
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(_fontSize);
			mPaint.setColor(mDefaultColor);
			mPaint.setSubpixelText(true);

			mBackPaint = new Paint();
			mBackPaint.setColor(0xffffffff);
			mBackPaint.setStyle(Style.FILL);

			mWidth = mHeight = 0;
		}

		public void setStyle(int styl) {
			mCurrentStyle = styl;
		}
		
		public void setReverseVideo(boolean reverse) {
			mReverseVideo = reverse;
		}
		
		public void setPosition(int pos, int seekMode) {
			switch (seekMode) {
			case Stream.SEEKMODE_CURRENT:
				pos += _pos;
				break;
			case Stream.SEEKMODE_END:
				pos += mHeight * mWidth;
				break;
			case Stream.SEEKMODE_START:
			default:
				// we're ok
			}
			
			_pos = pos;
		}

		public int getPosition() {
			return _pos;
		}

		public float measureCharacterHeight() {
			return mPaint.getFontSpacing();
		}
		
		public float measureCharacterWidth() {
			return mPaint.measureText("0");
		}
		
		@Override
		protected synchronized void onSizeChanged(int w, int h, int oldw, int oldh) {
			oldw = mWidth;
			oldh = mHeight;
			w -= getPaddingLeft() + getPaddingRight();
			h -= getPaddingBottom() + getPaddingTop() - getDescent();
			if (w < 0)
				w = 0;
			if (h < 0)
				h = 0;
			mWidth = (int) (w / measureCharacterWidth());
			mHeight = (int) (h / measureCharacterHeight());
			
			char[] oldfb = mFrameBufTemp;
			mFrameBuf = new char[mWidth * mHeight];
			mFrameBufTemp = new char[mWidth * mHeight];
			_styleBuf = new int[mWidth * mHeight];
			_reverseBuf = new boolean[mWidth * mHeight];
			
			for (int y = 0; y < Math.min(oldh, mHeight); ++y)
				for (int x = 0; x < Math.min(oldw, mWidth); ++x)
					mFrameBufTemp[y * mWidth + x] = oldfb[y * oldw + x];
			
			//System.arraycopy(mFrameBuf, 0, mFrameBufTemp, 0, mWidth * mHeight);
			
			mRect.right = getWidth();
			mRect.bottom = getHeight();
		}

		/** Get recommended descent. Useful for determining bottom padding needed. */
		public float getDescent() {
			return mPaint.getFontMetrics().descent;
		}

		public synchronized void clear() {
			for (int i = 0; i < mWidth * mHeight; ++i) {
				mFrameBufTemp[i] = ' ';
				_styleBuf[i] = mCurrentStyle;
				_reverseBuf[i] = mReverseVideo;
			}
			
			_pos = 0;
			mIsClear = true;
		}

		public synchronized int[] getSize() {
			/*
				this is a hack until the native side
				is synchronized with the java UI.  For now,
				just report standard screen size if still zero.
				This allows many games to run that would otherwise
				just give up.
			*/

			int w = mWidth == 0 ? 50:mWidth;
			int h = mHeight == 0 ? 20:mHeight;
			return new int[] { w, h };
		}

		public synchronized void moveCursor(long x, long y) {
			_pos = (int) (y * mWidth + x);
			if (_pos < 0)
				_pos = 0;
		}

		public synchronized void putString(String str) {
			if (mFrameBufTemp == null || str == null || _pos >= mFrameBufTemp.length)
				return;
			
			int end = str.length();
			if (end > mWidth * mHeight - _pos)
				end = mWidth * mHeight - _pos;
			
			if (end == 0)
				return;
			str.getChars(0, end, mFrameBufTemp, _pos);
			for (int i = 0; i < end; i++) {
				_styleBuf[_pos+i] = mCurrentStyle;
				_reverseBuf[_pos+i] = mReverseVideo;
			}
			_pos += end;
			
			mIsClear = false;
		}
		
		@Override
		protected synchronized void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			if (mIsClear)
				return;

			canvas.drawRect(mRect, mBackPaint);
			int tmpStyle = _styleBuf[0];
			boolean tmpReverse = _reverseBuf[0];
			for (int y = 0; y < mHeight; y++) {
				int start = 0;
				int x = 0;
				for (x = 0; x < mWidth; x++) {
					if (tmpStyle != _styleBuf[y*mWidth+x] || tmpReverse != _reverseBuf[y*mWidth+x]) {
						drawText(canvas, tmpStyle, tmpReverse, y, start, x);
						start = x;
						tmpStyle = _styleBuf[y*mWidth+x];
						tmpReverse = _reverseBuf[y*mWidth+x];
					}
				}
				if (x != start) {
					drawText(canvas, tmpStyle, tmpReverse, y, start, x);
				}
			}
		}

		/** Draw text using style and reverse information
		 * @param canvas
		 * @param style
		 * @param reverse if the text should be drawn in reverse
		 * @param y which line to draw on
		 * @param start x-coordinate where to start drawing on this line
		 * @param end x-coordinate where to end drawing on this line
		 */
		private void drawText(Canvas canvas, int style, boolean reverse, int y, int start, int end) {
			int px = getPaddingLeft();
			int py = getPaddingTop();
			float chw = measureCharacterWidth();
			float chh = measureCharacterHeight();
			// new style for next char draw previous chars with old style
			TextPaint p = new TextPaint(mPaint);
			p.bgColor = mBackPaint.getColor();
			p = stylehints.getPaint(getContext(), p, style, reverse);
			// Background paint
			Paint bgpaint = new Paint();
			bgpaint.setColor(p.bgColor);
			bgpaint.setStyle(Style.FILL);
			canvas.drawRect(FloatMath.floor(px + chw*start), FloatMath.floor(py+chh*y), FloatMath.ceil(px + chw*end), FloatMath.ceil(py+chh*(y+1)), bgpaint);
			canvas.drawText(mFrameBufTemp, y * mWidth + start, end-start, px + chw*start, py + chh * (y + 1) - p.descent(), p);
		}

		public void requestCharEvent() {
			mGlk.waitForUi(new Runnable() {
				@Override
				public void run() {
					mCharEventPending = true;
					setEnabled(true);
					setFocusableInTouchMode(true);
					requestFocus();
				}
			});
		}
		
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			do {
				if (mLineEventPending && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					doneLineInput();
					return true;
				}
				if (mLineEventPending && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					backspace();
					return true;
				}
				if (mCharEventPending) {
					Event ev = CharInputEvent.fromKeyEvent(TextGridWindow.this, event);
					if (ev != null) {
						mGlk.postEvent(ev);
						cancelCharEvent();
						return true;
					}
				}

				if (!mLineEventPending)
					break;
	
				int c = event.getUnicodeChar();
				if (c < 0 || c > 255)
					break;
	
				addToLine(c);
				return true;
			} while (false);
			
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				android.view.View v = focusSearch(FOCUS_LEFT);
				if (v == null)
					v = focusSearch(FOCUS_UP);
				if (v != null) {
					v.requestFocus(FOCUS_LEFT);
					return true;
				}
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				android.view.View v = focusSearch(FOCUS_RIGHT);
				if (v == null)
					v = focusSearch(FOCUS_DOWN);
				if (v != null) {
					v.requestFocus(FOCUS_RIGHT);
					return true;
				}
				
			}
			
			return super.onKeyDown(keyCode, event);
		}

		private void backspace() {
			if (_pos == mLineInputStart)
				return;
			
			mFrameBufTemp[--_pos] = ' ';
			postInvalidate();
		}

		private void addToLine(int c) {
			if (_pos == mLineInputEnd)
				return;
			
			_styleBuf[_pos] = mCurrentStyle;
			_reverseBuf[_pos] = mReverseVideo;
			mFrameBufTemp[_pos++] = (char) c;
			mIsClear = false;
			postInvalidate();
		}

		private void doneLineInput() {
			final Event e = cancelLineEvent();
			if (null != e)
				mGlk.postEvent(e);
		}

		public void requestLineEvent(String initial) {
			mLineEventPending = true;
			mLineInputStart = _pos;
			mLineInputEnd = _pos + mWidth;
			mLineInputEnd -= mLineInputEnd % mWidth;
			
			if (mLineInputEnd - mLineInputStart > mMaxLen)
				mLineInputEnd = mLineInputStart + mMaxLen;
			
			setEnabled(true);
			setFocusableInTouchMode(true);
			requestFocus();
		}

		public LineInputEvent cancelLineEvent() {
			if (!mLineEventPending)
				return null;
			
			final String result = String.copyValueOf(mFrameBufTemp, mLineInputStart, _pos - mLineInputStart);
			_pos = _pos + mWidth;
			_pos -= _pos % mWidth;
			mLineEventPending = false;
			setEnabled(false);
			setFocusable(false);
			
			return new LineInputEvent(TextGridWindow.this, result, mLineBuffer, mMaxLen, mDispatchRock, mUnicodeEvent);
		}

		public void cancelCharEvent() {
			if (mCharEventPending) {
				setEnabled(false);
				setFocusable(false);
				mCharEventPending = false;
			}				
		}
		
		@Override
		public Parcelable onSaveInstanceState() {
			TextGridParcelable ss = new TextGridParcelable();
			ss.mFrameBuf = mFrameBufTemp;
			ss.mHeight = mHeight;
			ss.mWidth = mWidth;
			ss.mLineEventPending = mLineEventPending;
			ss.mCharEventPending = mCharEventPending;
			return ss;
		}
		
		@Override
		protected void onRestoreInstanceState(Parcelable state) {
			TextGridParcelable ss = (TextGridParcelable) state;
			mFrameBufTemp = ss.mFrameBuf;
			//mFrameBufTemp = new char[mFrameBuf.length];
			//System.arraycopy(mFrameBuf, 0, mFrameBufTemp, 0, mFrameBuf.length);
			mHeight = ss.mHeight;
			mWidth = ss.mWidth;
			mLineEventPending = ss.mLineEventPending;
			if (mCharEventPending && !ss.mCharEventPending)
				cancelCharEvent();
		}

		public void flush() {
			/*
			char tmp[] = mFrameBuf;
			mFrameBuf = mFrameBufTemp;
			mFrameBufTemp = tmp;
			*/
			postInvalidate();
		}
	}
	
	protected View mView;
	private final Glk mGlk;
	private int mLineBuffer;
	private int mMaxLen;
	private int mDispatchRock;
	private boolean mUnicodeEvent = false;

	public TextGridWindow(final Glk glk, int rock) {
		super(rock);
		mGlk = glk;
		mStream = new Stream();
		stylehints = new Styles(_stylehints);
		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				init(glk);
			}
		});		
	}

	protected void init(Glk glk) {
		mView = new View(glk.getContext());
	}

	@Override
	public android.view.View getView() {
		return mView;
	}
	
	@Override
	public void clear() {
		if (mView == null) return; 
		mView.clear();
	}
	
	@Override
	public int[] getSize() {
		if (mView == null) return new int[]{0,0,0,0}; 
		return mView.getSize();
	}
	
	public void moveCursor(int x, int y) {
		if (mView == null) return;
		mView.moveCursor(x, y);
	}

	@Override
	public int measureHeight(int size) {
		if (mView == null) return 0;
		return ((int) FloatMath.ceil(mView.measureCharacterHeight())) * size 
			+ mView.getPaddingBottom() + mView.getPaddingTop() + ((int) FloatMath.floor(mView.getDescent()));
	}

	@Override
	public int measureWidth(int size) {
		if (mView == null) return 0;
		return ((int) FloatMath.ceil(mView.measureCharacterWidth())) * size + mView.getPaddingLeft() + mView.getPaddingRight();
	}

	@Override
	public int getType() {
		return WINTYPE_TEXTGRID;
	}

	@Override
	public void requestCharEvent() {
		if (mView == null) return;
		mView.requestCharEvent();
	}

	@Override
	public void requestLineEvent(String initial, long maxlen, int buffer, int unicode) {
		if (mView == null) return;

		mLineBuffer = buffer;
		mMaxLen = (int) maxlen;
		mDispatchRock = retainVmArray(buffer, maxlen);
		mView.requestLineEvent(initial);
		mUnicodeEvent = (unicode != 0);
	}

	@Override
	public void cancelCharEvent() {
		if (mView == null) return;

		mView.cancelCharEvent();
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		if (mView == null) return null;

		return mView.cancelLineEvent();
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		// TODO fix when styles implemented
		return false;
	}

	@Override
	public void flush() {
		if (mView == null) return;

		mView.flush();
	}
	
	public static Styles _stylehints = new Styles();
	public Styles stylehints;
}
