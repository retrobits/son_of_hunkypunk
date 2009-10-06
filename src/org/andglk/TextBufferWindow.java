package org.andglk;

import java.io.IOException;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class TextBufferWindow extends Window {
	private class _Stream extends Stream {
		private long mCurrentStyle = Glk.STYLE_NORMAL;
		private StringBuilder mBuffer = new StringBuilder();

		@Override
		protected void doPutChar(char c) throws IOException {
			mBuffer.append(c);
		}

		@Override
		protected void doPutString(String str) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setStyle(long styl) {
			if (styl == mCurrentStyle)
				return;
			flushBuffer();
			mCurrentStyle = styl;
		}

		protected void flushBuffer() {
			if (mBuffer.length() == 0)
				return;
			
			final String text = mBuffer.toString();
			final long style = mCurrentStyle;
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mView.print(text, style);
				}
			});
			
			mBuffer.setLength(0);
		}
	}

	protected Glk mGlk;
	protected _View mView;
	protected Handler mHandler;
	protected Context mContext;
	
	private class _View extends TextView {
		private boolean mCharInputEnabled;

		public _View(Context context) {
			super(context, null, R.attr.textBufferWindowStyle);
			setText("", BufferType.SPANNABLE);
		}

		public void print(String text, long style) {
			final Object span = makeStyleSpan(style);
			if (span != null) {
				final SpannableString ss = new SpannableString(text);
				ss.setSpan(span, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				append(ss);
			} else
				append(text);
			
			scrollToEnd();
		}

		private void scrollToEnd() {
			// this trivial expression below is ripped right out of the ScrollingMovementMethod
			scrollTo(getScrollX(), getLayout().getLineTop(getLineCount()) - (getHeight() - getTotalPaddingTop() - getTotalPaddingBottom()));
		}

		private Object makeStyleSpan(long style) {
			final int id = getTextAppearanceId((int) style);
			if (id == 0)
				return null;
			else
				return new TextAppearanceSpan(mContext, id);
		}

		/* see TextBufferWindow.clear() */
		public void clear() {
			setText("", BufferType.SPANNABLE);
		}

		public void enableCharInput() {
			mCharInputEnabled = true;

			final CharSequence cs = getText();
			assert(cs instanceof Spannable);
			final Spannable s = (Spannable) cs;
			Selection.setSelection(s, s.length());
			
			setFocusableInTouchMode(true);
			requestFocus();
		}

		private void disableCharInput() {
			mCharInputEnabled = false;
			
			final Spannable s = (Spannable) getText();
			Selection.removeSelection(s);
			
			setFocusable(false);
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (mCharInputEnabled) {
				Event ev = CharInputEvent.fromKeyEvent(TextBufferWindow.this, event);
				if (ev != null) {
					mGlk.postEvent(ev);
					disableCharInput();
					return true;
				}
			}
			
			return super.onKeyDown(keyCode, event);
		}
	}

	public TextBufferWindow(Glk glk, int rock) {
		super(rock);
		mGlk = glk;
		mContext = glk.getContext();
		mView = new _View(mContext);
		mStream = new _Stream();
		mHandler = mGlk.getUiHandler();
	}
	
	@Override
	public void cancelCharEvent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mView.clear();
			}
		});
	}

	@Override
	public void flush() {
		((_Stream) mStream).flushBuffer();
	}

	@Override
	public int[] getSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView() {
		return mView;
	}

	@Override
	public int measureHeight(int size) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int measureWidth(int size) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void requestCharEvent() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mView.enableCharInput();
			}
		});
	}

	@Override
	public void requestLineEvent(String initial, long maxlen, int buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		if (style1 == style2)
			return false;
		
		int res1 = getTextAppearanceId(style1), res2 = getTextAppearanceId(style2);
		if (res1 == 0)
			res1 = R.style.TextBufferWindow;
		if (res2 == 0)
			res2 = R.style.TextBufferWindow;
		final int[] fields = { android.R.attr.textSize, android.R.attr.textColor, android.R.attr.typeface, android.R.attr.textStyle };
		TypedArray ta1 = mContext.obtainStyledAttributes(res1, fields);
		TypedArray ta2 = mContext.obtainStyledAttributes(res2, fields);
		
		return (ta1.getDimension(0, 0) != ta2.getDimension(0, 0)) ||
			(ta1.getColor(1, 0) != ta2.getColor(1, 0)) ||
			(ta1.getString(2) != ta2.getString(2)) ||
			(ta1.getString(3) != ta2.getString(3));
	}
}
