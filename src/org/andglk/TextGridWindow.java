package org.andglk;

import java.io.IOException;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;

public class TextGridWindow extends Window {
	 public static class TextGridParcelable implements Parcelable {
	     private char[] _frameBuf;
	     
	     public void writeToParcel(Parcel out, int flags) {
	    	 out.writeInt(_frameBuf.length);
	         out.writeCharArray(_frameBuf);
	     }

	     public static final Parcelable.Creator<TextGridParcelable> CREATOR
	             = new Parcelable.Creator<TextGridParcelable>() {
	         public TextGridParcelable createFromParcel(Parcel in) {
	             return new TextGridParcelable(in);
	         }

	         public TextGridParcelable[] newArray(int size) {
	             return new TextGridParcelable[size];
	         }
	     };
	     
	     private TextGridParcelable(Parcel in) {
	    	 _frameBuf = new char[in.readInt()];
	    	 in.readCharArray(_frameBuf);
	     }

		public TextGridParcelable(char[] framebuf) {
			_frameBuf = framebuf;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		public char[] getFrameBuffer() {
			return _frameBuf;
		}
	 }

	 @Override
	public Parcelable saveInstanceState() {
		return mView.onSaveInstanceState();
	}
	
	@Override
	public void restoreInstanceState(Parcelable p) {
		mView.onRestoreInstanceState(p);
	}
	
	private class Stream extends Window.Stream {
		@Override
		public int getPosition() {
			return mView.getPosition();
		}
		
		@Override
		public void setPosition(int pos, int seekMode) {
			mView.setPosition(pos, seekMode);
		}
		
		@Override
		protected void doPutChar(char c) throws IOException {
			if (mView._pos >= mView._charsW * mView._charsH)
				return;
			
			mView._framebuf[mView._pos++] = c;
			
			mView.mIsClear = false;
		}

		@Override
		protected void doPutString(String str) throws IOException {
			mView.putString(str);
		}

		@Override
		public void setStyle(long styl) {
			mView.setStyle((int) styl);
		}
	}

	public boolean mChanged;
	
	protected class View extends android.view.View {
		private int _fontSize;
		private Paint mPaint;
		protected int _charsW;
		protected int _charsH;
		protected char[] _framebuf;
		protected int _pos;
		private boolean mCharEventPending;
		private boolean mLineEventPending;
		private int mLineInputEnd;
		private int mLineInputStart;
		private int mDefaultColor;
		protected boolean mIsClear;
		private Rect mRect = new Rect();
		private Paint mBackPaint;

		public View(Context context) {
			super(context, null, R.attr.textGridWindowStyle);
			mIsClear = true;
			setClickable(true);
			setEnabled(false);
			TypedArray ta = context.obtainStyledAttributes(null, new int[] { android.R.attr.textAppearance }, 
					R.attr.textGridWindowStyle, 0);
			int res = ta.getResourceId(0, -1);
			ta = context.obtainStyledAttributes(res, new int[] { android.R.attr.textSize, android.R.attr.textColor });
			_fontSize = ta.getDimensionPixelSize(0, -1);
			mDefaultColor = ta.getColor(1, 0xffffffff);
			
			mPaint = new Paint();
			mPaint.setTypeface(Typeface.MONOSPACE);
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(_fontSize);
			mPaint.setColor(mDefaultColor);

			mBackPaint = new Paint();
			mBackPaint.setColor(0xffffffff);
			mBackPaint.setStyle(Style.FILL);

			_charsW = _charsH = 0;
		}

		public void setStyle(int styl) {
			Log.w("Glk/TextGridWindow", "style requested but not supported in text grid window");
			// TODO
		}

		public void setPosition(int pos, int seekMode) {
			switch (seekMode) {
			case Stream.SEEKMODE_CURRENT:
				pos += _pos;
				break;
			case Stream.SEEKMODE_END:
				pos += _charsH * _charsW;
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
			oldw = _charsW;
			oldh = _charsH;
			w -= getPaddingLeft() + getPaddingRight();
			h -= getPaddingBottom() + getPaddingTop() - getDescent();
			if (w < 0)
				w = 0;
			if (h < 0)
				h = 0;
			_charsW = (int) (w / measureCharacterWidth());
			_charsH = (int) (h / measureCharacterHeight());
			
			char[] oldfb = _framebuf;
			_framebuf = new char[_charsW * _charsH];
			
			for (int y = 0; y < Math.min(oldh, _charsH); ++y)
				for (int x = 0; x < Math.min(oldw, _charsW); ++x)
					_framebuf[y * _charsW + x] = oldfb[y * oldw + x];
			
			mRect.right = getWidth();
			mRect.bottom = getHeight();
		}

		/** Get recommended descent. Useful for determining bottom padding needed. */
		public float getDescent() {
			return mPaint.getFontMetrics().descent;
		}

		public synchronized void clear() {
			for (int i = 0; i < _charsW * _charsH; ++i)
				_framebuf[i] = ' ';
			
			_pos = 0;
			mIsClear = true;
		}

		public synchronized int[] getSize() {
			return new int[] { _charsW, _charsH };
		}

		public synchronized void moveCursor(long x, long y) {
			_pos = (int) (y * _charsW + x);
			if (_pos < 0)
				_pos = 0;
		}

		public synchronized void putString(String str) {
			if (_pos >= _framebuf.length)
				return;
			
			int end = str.length();
			if (end > _charsW * _charsH - _pos)
				end = _charsW * _charsH - _pos;
			
			if (end == 0)
				return;
			str.getChars(0, end, _framebuf, _pos);
			_pos += end;
			
			mIsClear = false;
		}
		
		@Override
		protected synchronized void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			if (mIsClear)
				return;

			canvas.drawRect(mRect, mBackPaint);
			int px = getPaddingLeft();
			int py = getPaddingTop();
			float ch = measureCharacterHeight();
			for (int y = 0; y < _charsH; y++)
				canvas.drawText(_framebuf, y * _charsW, _charsW, px, py + ch * (y + 1), mPaint);
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
			
			_framebuf[--_pos] = ' ';
			postInvalidate();
		}

		private void addToLine(int c) {
			if (_pos == mLineInputEnd)
				return;
			
			_framebuf[_pos++] = (char) c;
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
			mLineInputEnd = _pos + _charsW;
			mLineInputEnd -= mLineInputEnd % _charsW;
			
			if (mLineInputEnd - mLineInputStart > mMaxLen)
				mLineInputEnd = mLineInputStart + mMaxLen;
			
			setEnabled(true);
			setFocusableInTouchMode(true);
			requestFocus();
		}

		public LineInputEvent cancelLineEvent() {
			if (!mLineEventPending)
				return null;
			
			final String result = String.copyValueOf(_framebuf, mLineInputStart, _pos - mLineInputStart);
			_pos = _pos + _charsW;
			_pos -= _pos % _charsW;
			mLineEventPending = false;
			setEnabled(false);
			setFocusable(false);
			
			return new LineInputEvent(TextGridWindow.this, result, mLineBuffer, mMaxLen, mDispatchRock);
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
			return new TextGridParcelable(_framebuf);
		}
		
		@Override
		protected void onRestoreInstanceState(Parcelable state) {
			_framebuf = ((TextGridParcelable) state).getFrameBuffer();
		}
	}
	
	protected View mView;
	private Glk mGlk;
	private int mLineBuffer;
	private int mMaxLen;
	private int mDispatchRock;

	public TextGridWindow(final Glk glk, int rock) {
		super(rock);
		mGlk = glk;
		mStream = new Stream();
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
		mView.clear();
	}
	
	@Override
	public int[] getSize() {
		return mView.getSize();
	}
	
	public void moveCursor(int x, int y) {
		mView.moveCursor(x, y);
	}

	@Override
	public int measureHeight(int size) {
		return ((int) Math.ceil(mView.measureCharacterHeight())) * size 
			+ mView.getPaddingBottom() + mView.getPaddingTop() + ((int) Math.floor(mView.getDescent()));
	}

	@Override
	public int measureWidth(int size) {
		return ((int) Math.ceil(mView.measureCharacterWidth())) * size + mView.getPaddingLeft() + mView.getPaddingRight();
	}

	@Override
	public int getType() {
		return WINTYPE_TEXTGRID;
	}

	@Override
	public void requestCharEvent() {
		mView.requestCharEvent();
	}

	@Override
	public void requestLineEvent(String initial, long maxlen, int buffer) {
		mLineBuffer = buffer;
		mMaxLen = (int) maxlen;
		mDispatchRock = retainVmArray(buffer, maxlen);
		mView.requestLineEvent(initial);
	}

	@Override
	public void cancelCharEvent() {
		mView.cancelCharEvent();
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		return mView.cancelLineEvent();
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		// TODO fix when styles implemented
		return false;
	}

	@Override
	public void flush() {
		mView.postInvalidate();
	}
}
