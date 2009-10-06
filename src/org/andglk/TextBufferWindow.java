package org.andglk;

import java.io.IOException;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TextBufferWindow extends Window {
	private Object makeStyleSpan(long style) {
		final int id = getTextAppearanceId((int) style);
		if (id == 0)
			return null;
		else
			return new TextAppearanceSpan(mContext, id);
	}

	private class _Stream extends Stream {
		private long mCurrentStyle = Glk.STYLE_NORMAL;
		private StringBuilder mBuffer = new StringBuilder();
		private SpannableStringBuilder mSsb = new SpannableStringBuilder();

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
			applyStyle();
			mCurrentStyle = styl;
		}

		private void applyStyle() {
			if (mBuffer.length() == 0)
				return;
			
			final Object span = makeStyleSpan(mCurrentStyle);
			if (span != null) {
				final SpannableString ss = new SpannableString(mBuffer);
				ss.setSpan(span, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mSsb.append(ss);
			} else
				mSsb.append(mBuffer);
			
			mBuffer.setLength(0);
		}

		public void flush() {
			applyStyle();
			
			if (mSsb.length() == 0)
				return;
			
			final Spannable ssb = mSsb;
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mView.print(ssb);
				}
			});
			
			mSsb = new SpannableStringBuilder();
		}
}

	protected Glk mGlk;
	protected _View mView;
	protected Handler mHandler;
	protected Context mContext;
	private int mLineEventBuffer;
	private long mLineEventBufferLength;
	private int mLineEventBufferRock;
	
	private class _View extends TextView implements OnEditorActionListener {
		public class _MovementMethod implements MovementMethod {

			@Override
			public boolean canSelectArbitrarily() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void initialize(TextView widget, Spannable text) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onKeyDown(TextView widget, Spannable text,
					int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onKeyOther(TextView view, Spannable text,
					KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onKeyUp(TextView widget, Spannable text,
					int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onTakeFocus(TextView widget, Spannable text,
					int direction) {
				// TODO Auto-generated method stub

			}

			@Override
			public boolean onTouchEvent(TextView widget, Spannable text,
					MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onTrackballEvent(TextView widget, Spannable text,
					MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}

		}

		private boolean mCharInputEnabled;
		private boolean mLineInputEnabled;
		private int mLineInputStart;
		private Object mLineInputSpan;

		public _View(Context context) {
			super(context, null, R.attr.textBufferWindowStyle);
			setText("", BufferType.EDITABLE);
			setMovementMethod(new _MovementMethod());
			setInputType(0
					| InputType.TYPE_CLASS_TEXT 
					| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT 
					| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				);
			setOnEditorActionListener(this);
			setFocusable(false);
		}

		public void print(CharSequence text) {
			append(text);
			scrollToEnd();
		}

		private void scrollToEnd() {
			// this trivial expression below is ripped right out of the ScrollingMovementMethod
			scrollTo(getScrollX(), getLayout().getLineTop(getLineCount()) - (getHeight() - getTotalPaddingTop() - getTotalPaddingBottom()));
		}

		/* see TextBufferWindow.clear() */
		public void clear() {
			setText("", BufferType.EDITABLE);
		}

		public void enableCharInput() {
			mCharInputEnabled = true;
			enableInput();
		}
		
		private void enableInput() {
			setFocusableInTouchMode(true);
			requestFocus();
			
			Selection.setSelection(getEditableText(), length());
		}

		private void disableCharInput() {
			mCharInputEnabled = false;
			
			disableInput();
		}
		
		private void disableInput() {
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

		public void enableLineInput(String initial) {
			mLineInputEnabled = true;
			mLineInputSpan = makeStyleSpan(Glk.STYLE_INPUT); 
			append("\u200b"); // to attach the span to
			mLineInputStart = length();
			final Editable e = getEditableText();
			e.setSpan(mLineInputSpan, mLineInputStart - 1, mLineInputStart, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			if (initial != null)
				e.append(initial);
			
			enableInput();
		}
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (!mLineInputEnabled)
				return false;
			
			lineInputAccepted(finishLineInput().toString());
			return true;
		}

		private CharSequence finishLineInput() {
			disableInput();
			
			final Editable e = getEditableText();
			final int len = e.length();
			e.setSpan(mLineInputSpan, mLineInputStart - 1, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			final CharSequence result = e.subSequence(mLineInputStart, len);
			
			e.append("\n");
			mLineInputSpan = null;
			mLineInputStart = -1;
			mLineInputEnabled = false;
			
			return result;
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
	
	public void lineInputAccepted(String result) {
		final org.andglk.Stream echo = mStream.mEchoStream;
		if (echo != null) {
			echo.putString(result);
			echo.putChar('\n');
		}
		
		LineInputEvent lie = new LineInputEvent(this, result, mLineEventBuffer, 
				mLineEventBufferLength, mLineEventBufferRock);
		mLineEventBufferLength = mLineEventBuffer = mLineEventBufferRock = 0;
		mGlk.postEvent(lie);
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
		((_Stream) mStream).flush();
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
	public void requestLineEvent(final String initial, long maxlen, int buffer) {
		flush();
		
		mLineEventBuffer = buffer;
		mLineEventBufferLength = maxlen;
		mLineEventBufferRock = retainVmArray(buffer, maxlen);
		
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mView.enableLineInput(initial);
			}
		});
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
