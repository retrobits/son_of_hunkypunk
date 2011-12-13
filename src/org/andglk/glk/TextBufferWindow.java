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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.andglk.hunkypunk.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import android.util.Log;
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
	}

	public static final String TAG = "AndGlk";
	
	@Override
	public Parcelable saveInstanceState() {
		return mView.onSaveInstanceState();
	}
	
	@Override
	public void restoreInstanceState(Parcelable p) {
		mView.onRestoreInstanceState(p);
	}
	
	@Override
	public void writeState(ObjectOutputStream stream) throws IOException {
		super.writeState(stream);
		mView.writeState(stream);
	}
	
	@Override
	public void readState(ObjectInputStream stream) throws IOException {
		super.readState(stream);
		mView.readState(stream);
	}
	
	private class StyleSpan extends TextAppearanceSpan {
		private int style;

		public StyleSpan(int style) throws NoStyleException {
			super(mContext, getTextAppearanceId(style));
			this.style = style;
		}

		public int getStyle() {
			return this.style;
		}
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
			mBuffer.append(str);
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
			
			try {
				final Object span = new StyleSpan((int) mCurrentStyle);
				final SpannableString ss = new SpannableString(mBuffer);
				ss.setSpan(span, 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				mSsb.append(ss);
			} catch (NoStyleException e) {
				mSsb.append(mBuffer);
			}
			
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
			
			// not clear() since it's been handed down to view
			mSsb = new SpannableStringBuilder();
		}

		protected void discardBuffers() {
			mBuffer.setLength(0);
			mSsb.clear();
		}
}

	protected Glk mGlk;
	protected _View mView;
	protected Handler mHandler;
	protected Context mContext;
	private int mLineEventBuffer;
	private long mLineEventBufferLength;
	private int mLineEventBufferRock;
	private boolean mUnicodeEvent = false;

	private class _View extends TextView implements OnEditorActionListener {
		public class _MovementMethod implements MovementMethod {
			@Override
			public boolean canSelectArbitrarily() {
				return false;
			}

			@Override
			public void initialize(TextView widget, Spannable text) {
			}

			@Override
			public boolean onKeyDown(TextView widget, Spannable text, int keyCode, KeyEvent event) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_DOWN:
					return scrollDown();
				case KeyEvent.KEYCODE_DPAD_UP:
					return scrollUp();
				// TODO: cursor movement
				default:
					return false;
				}
			}

			@Override
			public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
				return false;
			}

			@Override
			public boolean onKeyUp(TextView widget, Spannable text, int keyCode, KeyEvent event) {
				return false;
			}

			@Override
			public void onTakeFocus(TextView widget, Spannable text, int direction) {
			}

			@Override
			public boolean onTouchEvent(TextView widget, Spannable text, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_UP) {
					int x = (int) event.getX();
					int y = (int) event.getY();

					if (y > getHeight()/2)
						scrollDown();
					else
						scrollUp();			
					return true;
				}
				else 
					return false;
			}

			@Override
			public boolean onTrackballEvent(TextView widget, Spannable text, MotionEvent event) {
				// maybe TODO
				return false;
			}
		}

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
		
		public void writeState(ObjectOutputStream stream) throws IOException {
			final Editable e = getEditableText();
			if (mLineInputEnabled)
				e.removeSpan(mLineInputSpan);
			
			stream.writeUTF(e.toString());
			stream.writeInt(Selection.getSelectionStart(e));
			stream.writeInt(Selection.getSelectionEnd(e));
			StyleSpan[] spans = e.getSpans(0, e.length(), StyleSpan.class);
			stream.writeLong(spans.length);
			for (StyleSpan ss : spans) {
				stream.writeInt(e.getSpanStart(ss));
				stream.writeInt(e.getSpanEnd(ss));
				stream.writeInt(ss.getStyle());
			}
			
			if (mLineInputEnabled)
				e.setSpan(mLineInputSpan, mLineInputStart, e.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			stream.writeBoolean(mLineInputEnabled);
			stream.writeInt(mLineInputStart);
			stream.writeBoolean(mCharInputEnabled);
		}

		@Override
		public void onRestoreInstanceState(Parcelable state) {
			setFilters(mNormalFilters);
			TextBufferWindow._SavedState ss = (_SavedState) state;
			mLineInputEnabled = ss.mLineInputEnabled;
			if (mCharInputEnabled && !ss.mCharInputEnabled)
				disableCharInput();
			mCharInputEnabled = ss.mCharInputEnabled;
			mLineInputStart = ss.mLineInputStart;
			super.onRestoreInstanceState(ss.mSuperState);
			if (mLineInputEnabled) {
				mLineInputSpan = makeInputSpan();
				getEditableText().setSpan(mLineInputSpan, mLineInputStart - 1, length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				setFilters(mFilters);
			}
			scrollTo(getScrollX(), Math.max(0, (getUltimateBottom()) - getInnerHeight()));
		}

		public void readState(ObjectInputStream stream) throws IOException {
			setFilters(mNormalFilters);

			setText(stream.readUTF(), BufferType.EDITABLE);
			final Editable ed = getEditableText();
			final int selectionStart = stream.readInt();
			final int selectionEnd = stream.readInt();
			Selection.setSelection(ed, selectionStart, selectionEnd);
			final long spanCount = stream.readLong();
			for (long i = 0; i < spanCount; i++) {
				final int spanStart = stream.readInt();
				final int spanEnd = stream.readInt();
				final int spanStyle = stream.readInt();
				try {
					ed.setSpan(new StyleSpan(spanStyle), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (NoStyleException e) {
					// never mind then
				}
			}
			
			mLineInputEnabled = stream.readBoolean();
			final int lineInputStart = stream.readInt();
			final boolean charInputEnabled = stream.readBoolean();
			if (mCharInputEnabled && !charInputEnabled)
				disableCharInput();
			mCharInputEnabled = charInputEnabled;
			mLineInputStart = lineInputStart;
			if (mLineInputEnabled) {
				mLineInputSpan = makeInputSpan();
				ed.setSpan(mLineInputSpan, mLineInputStart - 1, length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
				setFilters(mFilters);
			}
			scrollTo(getScrollX(), Math.max(0, (getUltimateBottom()) - getInnerHeight()));
		}

		/**
		 * Scrolls the view down a page or as far as possible, whichever is less.
		 * 
		 * @return whether any scroll was commenced
		 */
		private boolean scrollDown() {
			final Layout layout = getLayout();
			if (layout == null)
				return false;
			
			final int currentScroll = getScrollY();
			final int innerHeight = getInnerHeight();
			final int ultimateTop = layout.getLineTop(getLineCount()) - getInnerHeight(); 
			if (currentScroll >= ultimateTop)
				return false;
			
			final int fadingEdgeLength = getVerticalFadingEdgeLength();
			final int target = layout.getLineTop(layout.getLineForVertical(currentScroll + innerHeight))
				- fadingEdgeLength;
			
			if (target + fadingEdgeLength < ultimateTop)
				startScrollTo(target);
			else {
				mPaging = false;
				startScrollTo(ultimateTop);
			}
			
			return true;
		}

		/** Scroll the view up a page or as far as possible, whichever is less.
		 * 
		 * @return whether any scroll was commenced
		 */
		public boolean scrollUp() {
			final Layout layout = getLayout();
			if (layout == null)
				return false;
			
			final int currentScroll = getScrollY();
			if (currentScroll <= 0)
				return false;
			
			final int fadingEdgeLength = getVerticalFadingEdgeLength();
			final int target = layout.getLineBottom(layout.getLineForVertical(currentScroll)) 
				- getInnerHeight() + fadingEdgeLength;
			if (target < 0)
				startScrollTo(0);
			else
				startScrollTo(target);
			return true;
		}

		protected void startScrollTo(int dest) {
			final int scrollY = getScrollY();
			final int dy = dest - scrollY;
			if (dy == 0)
				return;
			mScroller.startScroll(getScrollX(), scrollY, 0, dy);
			postInvalidate();
		}

		protected int getUltimateBottom() {
			return getLayout().getLineTop(getLineCount());
		}

		protected int getInnerHeight() {
			return getHeight() - getTotalPaddingBottom() - getTotalPaddingTop();
		}

		@Override
		public boolean onPreDraw() {
			if (FontPath == null || FontPath.compareTo(DefaultFontPath)!=0 || FontSize != DefaultFontSize) {
				FontPath = DefaultFontPath;
				FontSize = DefaultFontSize;
				
				Log.d("HunkyPunk","font: "+FontPath+" "+FontSize);

				try {
				Typeface tf = Typeface.createFromFile(FontPath);
				setTypeface(tf); 
				} catch (Exception ex) {}

				setTextSize(FontSize);		
			}

			/* super method does strange things with cursor and scrolling here */
			return true;
		}

		private boolean mCharInputEnabled;
		private boolean mLineInputEnabled;
		private int mLineInputStart;
		private Object mLineInputSpan;
		private final InputFilter mNewLineFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (dstart == 0)
					while(start < end && source.charAt(start) == '\n')
						start++;

				if (start == end)
					return "";
				
				return source.subSequence(start, end);
			}
		};
		
		private void beautify(Editable e, int position) {
			int len = e.length();
			
			final int NOTHING = 0;
			final int SPACE = 1;
			final int DASH = 2;
			final int SPACEQUOTE = 3;
			final int NDASH = 4;
			final int DOT = 5;
			final int DOUBLEDOT = 6;
			
			int state = NOTHING;
			
			if (position < 0)
				position = 0;
			if (len == position)
				return;
			
			do {
				final char c = e.charAt(position); 
				switch (state) {
				
				case NOTHING:
					switch (c) {
					case ' ':
					case '\n':
						state = SPACE;
						continue;
					case '"':
						e.replace(position, position + 1, "”");
						continue;
					case '-':
						state = DASH;
						continue;
					case '.':
						state = DOT;
						continue;
					default:
						continue;
					}
					
				case SPACE:
					switch (c) {
					case ' ':
					case '\n':
						continue;
					case '"':
						state = SPACEQUOTE;
						continue;
					case '-':
						state = DASH;
						continue;
					case '.':
						state = DOT;
						continue;
					default:
						state = NOTHING;
						continue;
					}

				case DASH:
					switch (c) {
					case ' ':
					case '\n':
						state = SPACE;
						continue;
					case '-':
						state = NDASH;
						continue;
					case '.':
						state = DOT;
						continue;
					case '"':
						e.replace(position, position + 1, "”");
					default:
						state = NOTHING;
						continue;
					}
					
				case SPACEQUOTE:
					switch (c) {
					case ' ':
					case '\n':
						state = SPACE;
						continue;
					case '-':
						e.replace(position - 1, position, "“");
						state = DASH;
						continue;
					case '.':
						e.replace(position - 1, position, "“");
						state = DOT;
						continue;
					case '"':
						e.replace(position, position + 1, "”");
					default:
						e.replace(position - 1, position, "“");
						state = NOTHING;
						continue;
					}
					
				case NDASH:
					switch (c) {
					case '-':
						e.replace(position - 2, position + 1, "—");
						position -= 2;
						len -= 2;
						state = NOTHING;
						continue;
					case '"':
						e.replace(position, position + 1, "”");
					default:
						state = NOTHING;
						break;
					case '.':
						state = DOT;
						break;
					case ' ':
					case '\n':
						state = SPACE;
						break;
					}
					e.replace(position - 2, position, "–");
					position--;
					len--;
					continue;
				
				case DOT:
					switch (c) {
					case '.':
						state = DOUBLEDOT;
						continue;
					case ' ':
					case '\n':
						state = SPACE;
						continue;
					case '-':
						state = DASH;
						continue;
					case '"':
						e.replace(position, position + 1, "”");
					default:
						state = NOTHING;
						continue;
					}

				case DOUBLEDOT:
					switch (c) {
					case ' ':
					case '\n':
						state = SPACE;
						continue;
					case '-':
						state = DASH;
						continue;
					case '"':
						e.replace(position, position + 1, "”");
						break;
					case '.':
						e.replace(position - 2, position + 1, "…");
						position -= 2;
						len -= 2;
						break;
					default:
					}
					state = NOTHING;
					continue;
				}
			} while (++position < len);
		}
		
		private final InputFilter[] mNormalFilters = { mNewLineFilter };
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
		}, mNewLineFilter };
		
		private Scroller mScroller;
		private _MovementMethod mMovementMethod;
		private boolean mPaging;
		
		public _View(Context context) {
			super(context, null, R.attr.textBufferWindowStyle);
			setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);

			setText("", BufferType.EDITABLE);
			setMovementMethod(mMovementMethod = new _MovementMethod());
			setInputType(0
					| InputType.TYPE_CLASS_TEXT 
					| InputType.TYPE_TEXT_FLAG_AUTO_CORRECT 
					| InputType.TYPE_TEXT_FLAG_MULTI_LINE
				);
			setOnEditorActionListener(this);
			setFocusable(false);
			mScroller = new Scroller(context);
			setScroller(mScroller);
			setFilters(mNormalFilters);
		}
		

		public void print(CharSequence text) {
			final int start = length() - 1;
			append(text);
			Editable e = getEditableText();
			beautify(e, start);
		}

		/* see TextBufferWindow.clear() */
		public void clear() {
			mLineInputStart = 0;
			setText("", BufferType.EDITABLE);
			scrollTo(getScrollX(), 0);
		}

		public void enableCharInput() {
			mCharInputEnabled = true;
			enableInput();
		}
		
		private void enableInput() {
			setFocusableInTouchMode(true);
			requestFocus();
			mPaging = true;
			if (getScrollY() != 0)
				scrollDown();
			
			Selection.setSelection(getEditableText(), length());
		}

		protected void disableCharInput() {
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
			if (mMovementMethod.onKeyDown(this, (Spannable) getText(), keyCode, event))
				return true;

			final int ultimateTop = getUltimateBottom() - getInnerHeight(); 
			if (mPaging && scrollDown()) {
				if (mScroller.getFinalY() != ultimateTop)
					return true;
			} else if (keyCode != KeyEvent.KEYCODE_DPAD_UP && getScrollY() < ultimateTop) { 
				startScrollTo(ultimateTop);
				if (mCharInputEnabled)
					// passing the key would be confusing
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

		public void enableLineInput(String initial) {

			final Editable e = getEditableText();
			
			/* don't allow overlapping input spans */
			if (mLineInputEnabled && mLineInputSpan != null) 
				e.removeSpan(mLineInputSpan);

			mLineInputEnabled = true;
			mLineInputSpan = makeInputSpan(); 
			append("\u200b"); // to attach the span to
			mLineInputStart = length();
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
			if (event != null && event.getAction() != KeyEvent.ACTION_DOWN)
				return false;
			
			lineInputAccepted(finishLineInput().toString());
			return true;
		}

		private CharSequence finishLineInput() {
			disableInput();
			setFilters(mNormalFilters);
			
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

	public static String DefaultFontPath = null;
	public static int DefaultFontSize = 0;

	public String FontPath = null;
	public int FontSize = 0;

	public TextBufferWindow(Glk glk, int rock) {
		super(rock);

		mGlk = glk;
		mContext = glk.getContext();
		mView = new _View(mContext);
		mStream = new _Stream();
		mHandler = mGlk.getUiHandler();
	}
	
	public Object makeInputSpan() {
		try {
			return new StyleSpan(Glk.STYLE_INPUT);
		} catch (NoStyleException e) {
			Log.e(TAG, "WTF? shouldn't happen.", e);
			return null;
		}
	}

	public void lineInputAccepted(String result) {
		final org.andglk.glk.Stream echo = mStream.mEchoStream;
		if (echo != null) {
			echo.putString(result);
			echo.putChar('\n');
		}
		
		LineInputEvent lie = new LineInputEvent(this, result, mLineEventBuffer, 
												mLineEventBufferLength, mLineEventBufferRock, mUnicodeEvent);
		mLineEventBufferLength = mLineEventBuffer = mLineEventBufferRock = 0;
		mGlk.postEvent(lie);
	}

	@Override
	public void cancelCharEvent() {
		mGlk.getUiHandler().post(new Runnable() {
			@Override
			public void run() {
				mView.disableCharInput();	
			}
		});
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		((_Stream) mStream).discardBuffers();
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
		return new int[] {0,0};
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
		public void requestLineEvent(final String initial, long maxlen, int buffer, int unicode) {
		flush();
		
		mLineEventBuffer = buffer;
		mLineEventBufferLength = maxlen;
		mLineEventBufferRock = retainVmArray(buffer, maxlen);
		mUnicodeEvent = (unicode != 0);

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
		
		int res1, res2;
		try {
			res1 = getTextAppearanceId(style1);
		} catch (NoStyleException e) {
			res1 = R.style.TextBufferWindow;
		}
		try {
			res2 = getTextAppearanceId(style2);
		} catch (NoStyleException e) {
			res2 = R.style.TextBufferWindow;
		}
		final int[] fields = { android.R.attr.textSize, android.R.attr.textColor, android.R.attr.typeface, android.R.attr.textStyle };
		TypedArray ta1 = mContext.obtainStyledAttributes(res1, fields);
		TypedArray ta2 = mContext.obtainStyledAttributes(res2, fields);
		
		return (ta1.getDimension(0, 0) != ta2.getDimension(0, 0)) ||
			(ta1.getColor(1, 0) != ta2.getColor(1, 0)) ||
			(ta1.getString(2) != ta2.getString(2)) ||
			(ta1.getString(3) != ta2.getString(3));
	}
}
