package org.andglk;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

public class TextGridWindow extends Window {
	private class View extends android.view.View {
		private int _fontSize;
		private Paint mPaint;
		private int _charsW;
		private int _charsH;
		private char[] _framebuf;
		private int _pos;

		public View(Context context) {
			super(context);
			TypedArray ta = context.obtainStyledAttributes(null, new int[] { android.R.attr.textAppearance }, 
					android.R.attr.textViewStyle, 0);
			int res = ta.getResourceId(0, -1);
			ta = context.obtainStyledAttributes(res, new int[] { android.R.attr.textSize, android.R.attr.textColor });
			_fontSize = ta.getDimensionPixelSize(0, -1);
			
			mPaint = new Paint();
			mPaint.setTypeface(Typeface.MONOSPACE);
			mPaint.setAntiAlias(true);
			mPaint.setTextSize(_fontSize);
			mPaint.setColor(ta.getColor(1, 0xffffffff));
			
			_charsW = _charsH = 0;
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
			_charsW = (int) (w / measureCharacterWidth());
			_charsH = (int) (h / measureCharacterHeight());
			
			char[] oldfb = _framebuf;
			_framebuf = new char[_charsW * _charsH];
			
			for (int y = 0; y < Math.min(oldh, _charsH); ++y)
				for (int x = 0; x < Math.min(oldw, _charsW); ++y)
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

		public synchronized void move_cursor(long x, long y) {
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
	}
	private View _view;
	private Glk _glk;

	public TextGridWindow(final Glk glk, long rock) {
		_glk = glk;
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
	public float measureCharacterHeight() {
		return _view.measureCharacterHeight();
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
	public long[] get_size() {
		return _view.getSize();
	}
	
	@Override
	public void move_cursor(long x, long y) {
		_view.move_cursor(x, y);
	}
	
	@Override
	public void putString(String str) {
		super.putString(str);
		_view.putString(str);
	}
}
