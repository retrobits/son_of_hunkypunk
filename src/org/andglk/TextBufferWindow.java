package org.andglk;

import java.io.IOException;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TextBufferWindow extends Window {
	public static class _SavedState implements Parcelable {
		public static final Parcelable.Creator<_SavedState> CREATOR = new Parcelable.Creator<_SavedState>() {
			@Override
			public _SavedState createFromParcel(Parcel source) {
				return new _SavedState(source);
			}

			@Override
			public _SavedState[] newArray(int size) {
				return new _SavedState[size];
			}
		};
		
		public Parcelable mSuperState;
		public boolean mLineInputEnabled;
		public int mLineInputStart;
		public boolean mCharInputEnabled;

		public _SavedState(Parcel source) {
			mSuperState = TextView.SavedState.CREATOR.createFromParcel(source);
			mLineInputEnabled = source.readByte() == 1;
			mCharInputEnabled = source.readByte() == 1;
			mLineInputStart = source.readInt();
		}

		public _SavedState() {
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			mSuperState.writeToParcel(dest, flags);
			dest.writeByte((byte) (mLineInputEnabled ? 1 : 0));
			dest.writeByte((byte) (mCharInputEnabled ? 1 : 0));
			dest.writeInt(mLineInputStart);
		}

		@Override
		public int describeContents() {
			return 0;
		}
	};
	@Override
	public Parcelable saveInstanceState() {
		return mView.onSaveInstanceState();
	}
	
	@Override
	public void restoreInstanceState(Parcelable p) {
		mView.onRestoreInstanceState(p);
	}
	
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

		private int mScrollLimit = 0;
		private boolean mPaging = false;
		
		@Override
		public Parcelable onSaveInstanceState() {
			TextBufferWindow._SavedState ss = new TextBufferWindow._SavedState();
			final Editable e = getEditableText();
			if (mLineInputEnabled)
				e.removeSpan(mLineInputSpan);
			ss.mSuperState = super.onSaveInstanceState();
			if (mLineInputEnabled)
				e.setSpan(mLineInputSpan, mLineInputStart, e.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			ss.mLineInputEnabled = mLineInputEnabled;
			ss.mLineInputStart = mLineInputStart;
			ss.mCharInputEnabled = mCharInputEnabled;
			return ss;
		}
		
		@Override
		public void onRestoreInstanceState(Parcelable state) {
			setFilters(mNoFilters);
			TextBufferWindow._SavedState ss = (_SavedState) state;
			mLineInputEnabled = ss.mLineInputEnabled;
			mCharInputEnabled = ss.mCharInputEnabled;
			mLineInputStart = ss.mLineInputStart;
			super.onRestoreInstanceState(ss.mSuperState);
			if (mLineInputEnabled) {
				mLineInputSpan = makeStyleSpan(Glk.STYLE_INPUT); 
				getEditableText().setSpan(mLineInputSpan, mLineInputStart - 1, length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				setFilters(mFilters);
			}
			scrollTo(getScrollX(), Math.max(0, (mScrollLimit = getUltimateBottom()) - getInnerHeight()));
			mPaging = false;
		}
		
		private void scrollDown() {
			final Layout layout = getLayout();
			if (layout == null)
				return;
			final int lineCount = getLineCount();
			final int ultimateBottom = getUltimateBottom();
			final int innerHeight = getInnerHeight();
			final int wantedScroll = Math.max(0, ultimateBottom - innerHeight);
			
			// this is annoying to page half a line and it's probably a prompt anyway
			// thus we clear paging flag if we're cutting it
			mPaging = wantedScroll > mScrollLimit &&
				layout.getLineForVertical(mScrollLimit + innerHeight) != lineCount - 1;
			
			if (mPaging)
				startScrollTo(mScrollLimit);
			else {
				mScrollLimit = layout.getLineTop(lineCount - 1);
				startScrollTo(wantedScroll);
			}
		}
		
		private void startScrollTo(int dest) {
			final int scrollY = getScrollY();
			final int dy = dest - scrollY;
			if (dy == 0)
				return;
			mScroller.startScroll(getScrollX(), scrollY, 0, dy);
			postInvalidate();
		}



		private int getUltimateBottom() {
			return getLayout().getLineTop(getLineCount());
		}

		private int getInnerHeight() {
			return getHeight() - getTotalPaddingBottom() - getTotalPaddingTop();
		}

		@Override
		public boolean onPreDraw() {
			/* super method does strange things with cursor and scrolling here */
			return true;
		}

		private boolean mCharInputEnabled;
		private boolean mLineInputEnabled;
		private int mLineInputStart;
		private Object mLineInputSpan;
		private final InputFilter[] mNoFilters = {};
		private final InputFilter[] mFilters = { new InputFilter() {
			SpannableStringBuilder mSsb = new SpannableStringBuilder();

			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				assert mLineInputEnabled;
				
				if (dstart > mLineInputStart)
					return null;
				
				mSsb.clear();
				mSsb.append(dest, dstart, mLineInputStart);
				mSsb.append(source, start, end);
				
				return mSsb;
			}
			
		}};
		private Scroller mScroller;
		
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
			mScroller = new Scroller(context);
			setScroller(mScroller);
		}
		

		public void print(CharSequence text) {
			append(text);
		}

		/* see TextBufferWindow.clear() */
		public void clear() {
			setText("", BufferType.EDITABLE);
			mScrollLimit = 0;
		}

		public void enableCharInput() {
			mCharInputEnabled = true;
			enableInput();
		}
		
		private void enableInput() {
			setFocusableInTouchMode(true);
			requestFocus();
			scrollDown();
			
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
			if (mPaging) {
				pageDown();
				return true;
			}
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

		private void pageDown() {
			final Layout layout = getLayout();
			final int innerHeight = getInnerHeight();
			mScrollLimit = layout.getLineTop(layout.getLineForVertical(mScrollLimit + innerHeight));
			scrollDown();
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
			
			setFilters(mFilters);
			
			enableInput();
		}
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (!mLineInputEnabled)
				return false;
			if (event.getAction() != KeyEvent.ACTION_DOWN)
				return false;
			
			lineInputAccepted(finishLineInput().toString());
			return true;
		}

		private CharSequence finishLineInput() {
			disableInput();
			setFilters(mNoFilters);
			
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
