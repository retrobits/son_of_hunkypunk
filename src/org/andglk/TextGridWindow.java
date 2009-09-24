package org.andglk;

import java.io.IOException;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;

public class TextGridWindow extends Window {
	private class Stream extends Window.Stream {
		@Override
		public int getPosition() {
			return _view.getPosition();
		}
		
		@Override
		public void setPosition(int pos, int seekMode) {
			_view.setPosition(pos, seekMode);
		}
		
		@Override
		protected void doPutChar(char c) throws IOException {
			_view.putString(Character.toString(c));
		}

		@Override
		protected void doPutString(String str) throws IOException {
			_view.putString(str);
		}

		@Override
		public void setStyle(long styl) {
			_view.setStyle((int) styl);
		}
	}
	
	private class View extends android.view.View {
		private int _fontSize;
		private Paint mPaint;
		private int _charsW;
		private int _charsH;
		private char[] _framebuf;
		private int _pos;
		private boolean mCharEventPending;
		private boolean mLineEventPending;
		private int mLineInputEnd;
		private int mLineInputStart;
		private int mDefaultColor;

		public View(Context context) {
			super(context);
			TypedArray ta = context.obtainStyledAttributes(null, new int[] { android.R.attr.textAppearance }, 
					android.R.attr.textViewStyle, 0);
			int res = ta.getResourceId(0, -1);
			ta = context.obtainStyledAttributes(res, new int[] { android.R.attr.textSize, android.R.attr.textColor });
			_fontSize = ta.getDimensionPixelSize(0, -1);
			mDefaultColor = ta.getColor(1, 0xffffffff);
			
			mPaint = new Paint();
			mPaint.setTypeface(Typeface.MONOSPACE);
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(_fontSize);
			mPaint.setColor(mDefaultColor);
			
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
			h -= getPaddingBottom() + getPaddingTop();
			_charsW = (int) (w / measureCharacterWidth());
			_charsH = (int) (h / measureCharacterHeight());
			
			char[] oldfb = _framebuf;
			_framebuf = new char[_charsW * _charsH];
			
			for (int y = 0; y < Math.min(oldh, _charsH); ++y)
				for (int x = 0; x < Math.min(oldw, _charsW); ++x)
					_framebuf[y * _charsW + x] = oldfb[y * oldw + x];
		}

		/* must be run on the main thread */
		public synchronized void clear() {
			for (int i = 0; i < _charsW * _charsH; ++i)
				_framebuf[i] = ' ';
			
			_pos = 0;
			invalidate();
		}

		public synchronized long[] getSize() {
			return new long[] { _charsW, _charsH };
		}

		public synchronized void moveCursor(long x, long y) {
			_pos = (int) (y * _charsW + x);
		}

		public synchronized void putString(String str) {
			int end = str.length();
			if (end > _charsW * _charsH - _pos)
				end = _charsW * _charsH - _pos;
			
			str.getChars(0, end, _framebuf, _pos);
			_pos = end;
			
			postInvalidate();
		}
		
		@Override
		protected synchronized void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			float ch = measureCharacterHeight(), cw = measureCharacterWidth();
			for (int y = 0; y < _charsH; y++)
				for (int x = 0; x < _charsW; x++)
					canvas.drawText(_framebuf, y * _charsW + x, 1, cw * x, ch * (y + 1), mPaint);
		}

		public void requestCharEvent() {
			setFocusableInTouchMode(true);
			mCharEventPending = true;
		}
		
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			do {
				if (mCharEventPending && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					doneLineInput();
					return true;
				}
				if (mLineEventPending && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
					backspace();
					return true;
				}
				if ((!mCharEventPending && !mLineEventPending) || !event.isPrintingKey())
					break;
	
				int c = event.getUnicodeChar();
				if (c < 0 || c > 255)
					break;
				
				if (mCharEventPending) {
					cancelCharEvent();
					
					Event e = new CharInputEvent(TextGridWindow.this, c);
					_glk.postEvent(e);
				} else {
					addToLine(c);
				}
				return true;
			} while (false);
			return super.onKeyUp(keyCode, event);
		}

		private void backspace() {
			if (_pos == mLineInputStart)
				return;
			
			_framebuf[--_pos] = ' ';
			invalidate();
		}

		private void addToLine(int c) {
			if (_pos == mLineInputEnd)
				return;
			
			_framebuf[_pos++] = (char) c;
			invalidate();
		}

		private void doneLineInput() {
			_glk.postEvent(cancelLineEvent());
		}

		public void requestLineEvent(String initial) {
			mLineEventPending = true;
			mLineInputStart = _pos;
			mLineInputEnd = _pos + _charsW;
			mLineInputEnd -= mLineInputEnd % _charsW;
			
			if (mLineInputEnd - mLineInputStart > mMaxLen)
				mLineInputEnd = mLineInputStart + mMaxLen;
			
			setFocusableInTouchMode(true);
		}

		public LineInputEvent cancelLineEvent() {
			if (!mLineEventPending)
				return null;
			
			final String result = String.copyValueOf(_framebuf, mLineInputStart, _pos - mLineInputStart);
			_pos = _pos + _charsW;
			_pos -= _pos % _charsW;
			mLineEventPending = false;
			setFocusable(false);
			
			return new LineInputEvent(TextGridWindow.this, result, mLineBuffer, mMaxLen);
		}

		public void cancelCharEvent() {
			if (mCharEventPending) {
				setFocusable(false);
				mCharEventPending = false;
			}				
		}
	}
	
	private View _view;
	private Glk _glk;
	private int mLineBuffer;
	private int mMaxLen;

	public TextGridWindow(final Glk glk, int rock) {
		super(rock);
		_glk = glk;
		mStream = new Stream();
		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				init(glk);
			}
		});		
	}

	protected void init(Glk glk) {
		_view = new View(glk.getContext());
	}

	@Override
	public android.view.View getView() {
		return _view;
	}
	
	@Override
	public void clear() {
		_glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				_view.clear();
			}
		});
	}
	
	@Override
	public long[] getSize() {
		return _view.getSize();
	}
	
	public void moveCursor(int x, int y) {
		_view.moveCursor(x, y);
	}

	@Override
	public int measureHeight(int size) {
		return Math.round(_view.measureCharacterHeight() * size) + _view.getPaddingBottom() + _view.getPaddingTop();
	}

	@Override
	public int measureWidth(int size) {
		return Math.round(_view.measureCharacterWidth() * size) + _view.getPaddingLeft() + _view.getPaddingRight();
	}

	@Override
	public int getType() {
		return WINTYPE_TEXTGRID;
	}

	@Override
	public void requestCharEvent() {
		_view.requestCharEvent();
	}

	@Override
	public void requestLineEvent(String initial, long maxlen, int buffer) {
		mLineBuffer = buffer;
		mMaxLen = (int) maxlen;
		_view.requestLineEvent(initial);
	}

	@Override
	public void cancelCharEvent() {
		_view.cancelCharEvent();
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		return _view.cancelLineEvent();
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		// TODO fix when styles implemented
		return false;
	}
}
