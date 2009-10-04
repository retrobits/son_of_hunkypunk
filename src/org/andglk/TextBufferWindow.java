package org.andglk;

import java.io.IOException;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TextBufferWindow extends Window {
	private class Stream extends Window.Stream {
		@Override
		protected void doPutChar(final char c) throws IOException {
			_view.print(Character.toString(c));
		}

		@Override
		protected void doPutString(final String str) throws IOException {
			_view.print(str);
		}

		@Override
		public void setStyle(final long styl) {
			_view.setStyle(styl);
		}

		public void echo(String s) {
			if (mEchoStream != null)
				mEchoStream.putString(s);
		}
	}
	
	private class View extends TextView implements OnEditorActionListener, OnKeyListener {
		public class _MovementMethod extends ScrollingMovementMethod {
			@Override
			protected boolean left(TextView widget, Spannable buffer) {
				int selStart;
				
				if (mLineInputPending && ((selStart = Selection.getSelectionStart(buffer)) > _start)) {
					Selection.setSelection(buffer, selStart - 1);
					scrollToEnd();
					return true;
				}

				if (super.left(widget, buffer))
					return true;
				
				android.view.View v = focusSearch(FOCUS_LEFT);
				if (v == null)
					v = focusSearch(FOCUS_UP);
				if (v != null) {
					v.requestFocus(FOCUS_LEFT);
					return true;
				}
				
				return false;
			}

			protected boolean right(TextView widget, Spannable buffer) {
				int selEnd;
				
				if (mLineInputPending && ((selEnd = Selection.getSelectionEnd(buffer)) < buffer.length())) {
					Selection.setSelection(buffer, selEnd + 1);
					scrollToEnd();
					return true;
				}

				if (super.right(widget, buffer))
					return true;
				
				android.view.View v = focusSearch(FOCUS_RIGHT);
				if (v == null)
					v = focusSearch(FOCUS_DOWN);
				if (v != null) {
					v.requestFocus(FOCUS_RIGHT);
					return true;
				}
				
				return false;
			}
			
			@Override
			public void onTakeFocus(TextView widget, Spannable text, int dir) {
				// do nothing, else cursor will jump
			}
		}

		private int _start;
		private TextAppearanceSpan mStyleSpan;
		private int mStyleStart;
		private boolean mCharEventPending;
		private boolean mLineInputPending;
		private SpannableStringBuilder mText;
		private class Filter implements InputFilter {
			private long _maxlen;

			public Filter(long maxlen) {
				_maxlen = maxlen;
			}

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				CharSequence result;
				int len = dest.length();
				
				if (dend < _start) {
					result = dest.subSequence(dstart, dend);
				} else if (dstart >= _start) {
					result = null;
					len = len - (dend - dstart) + (end - start); 
				} else {
					result = TextUtils.concat(dest.subSequence(dstart, _start), source.subSequence(start, end));
					if (result instanceof Spannable)
						Selection.setSelection((Spannable) result, result.length());
					len = len - (dend - dstart) + result.length();
				} 
				
				if (len - _start > _maxlen) {
					if (result == null)
						result = source.subSequence(start, end);
					result = result.subSequence(0, (int) _maxlen);
				}

				return result;
			}
		}
		
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (mLineInputPending && event.isPrintingKey()) {
				Spannable text = getEditableText();
				if (Selection.getSelectionStart(text) < _start) {
					int stop = Selection.getSelectionEnd(text);
					if (stop < _start)
						stop = _start;
					Selection.setSelection(text, _start, stop);
				}
			}
			
			return super.onKeyDown(keyCode, event);
		}

		public void scrollToEnd() {
            int padding = getTotalPaddingTop() + getTotalPaddingBottom();
            int line = getLineCount() - 1;
            scrollTo(getScrollX(), getLayout().getLineTop(line+1) - getHeight() - padding);
		}

		public void print(String str) {
			mText.append(str);
//			final Layout layout = getLayout();
//            scrollTo(getScrollX(), layout.getLineTop(getLineCount()));
//            Touch.scrollTo(this, layout, getScrollX(), getScrollY());
		}

		@Override
		protected void onTextChanged(CharSequence text, int start, int before,
				int after) {
			if ((!mLineInputPending) || mStyleSpan == null)
				return;

			/* the point is that if the span was to vanish, 
			 * we remove it and if it gets removed we revive it when needed
			 * (that's because 0-len spans behave funny)
			 */
			Editable e = getEditableText();

			final int sstart = e.getSpanStart(mStyleSpan);
			final int elen = e.length();
			
			// span is missing and we can put it in
			if (sstart == -1 && elen > mStyleStart)
				e.setSpan(mStyleSpan, mStyleStart, elen, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			// span is there but shrank to 0
			else if (sstart != -1 && sstart == e.getSpanEnd(mStyleSpan)) {
				mStyleStart = sstart;
				e.removeSpan(mStyleSpan);
			}
		}

		public View(Context context) {
			super(context, null, R.attr.textBufferWindowStyle);
			setMovementMethod(new _MovementMethod());
			mText = new SpannableStringBuilder();
			setEnabled(false);
		}
		
		public void requestLineEvent(String initial, long maxlen) {
			setStyle(Glk.STYLE_INPUT);
			setText(mText);
			setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			setEnabled(true);
			setFocusableInTouchMode(true);
			setOnEditorActionListener(this);
			mLineInputPending = true;

			final Editable e = getEditableText();
			_start = e.length();
			final InputFilter filter = new Filter(maxlen);

			e.setFilters(new InputFilter[] { filter });
			if (initial != null)
				print(initial);
			Selection.setSelection(e, e.length());
			requestFocus();
		}

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId != EditorInfo.IME_NULL || event.getAction() != KeyEvent.ACTION_DOWN)
				return false;
			
			_glk.postEvent(cancelLineEvent());
			return true;
		}

		private String getLineInput() {
			final CharSequence text = getText();
			return text.subSequence(_start, text.length()).toString();
		}

		/** Sets current style to @p styl.
		 * 
		 * @note Must be ran in the main thread.
		 * @param styl
		 */
		public void setStyle(long styl) {
			if (mStyleSpan != null) {
				int start = mStyleStart;
				int end = mText.length();
				if (start == end)
					mText.removeSpan(mStyleSpan);
				else
					mText.setSpan(mStyleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			
			if (mText != null)
				mStyleStart = mText.length();
			else
				mStyleStart = 0;

			final int id = getTextAppearanceId((int) styl);
			if (id == 0)
				mStyleSpan = null;
			else
				mStyleSpan = new TextAppearanceSpan(mContext, id);
		}

		public void requestCharEvent() {
			setOnKeyListener(this);
			setEnabled(true);
			setFocusableInTouchMode(true);
			mCharEventPending = true;
			requestFocus();
		}

		@Override
		public boolean onKey(android.view.View v, int keyCode, KeyEvent event) {
			if (event.getAction() != KeyEvent.ACTION_UP || !event.isPrintingKey())
				return false;
			
			int c = event.getUnicodeChar();
			if (c < 0 || c > 255)
				return false;
			
			cancelCharEvent();
			
			Event e = new CharInputEvent(TextBufferWindow.this, c);
			_glk.postEvent(e);
			return true;
		}

		public void cancelCharEvent() {
			if (mCharEventPending) {
				setOnKeyListener(null);
				setFocusable(false);
				setEnabled(false);
				mCharEventPending = false;
			}
		}

		public LineInputEvent cancelLineEvent() {
			if (!mLineInputPending)
				return null;
			
			mLineInputPending = false;
			String s = getLineInput();
			
			Stream stream = (Stream) mStream;
			stream.echo(s);
			stream.echo("\n");
			
			LineInputEvent e = new LineInputEvent(TextBufferWindow.this, s, mLineBuffer, mMaxLen, mDispatchRock);
			
			_glk.waitForUi(new Runnable() {
				@Override
				public void run() {
					Editable ed = getEditableText();
					ed.setFilters(new InputFilter[]{});
					
					ed.append("\n");
					mText = new SpannableStringBuilder(ed);
					setStyle(Glk.STYLE_NORMAL);
					setOnEditorActionListener(null);
					setRawInputType(InputType.TYPE_NULL);
					setEnabled(false);
					setFocusable(false);
				}
			});
			
			return e;
		}
	}

	@SuppressWarnings("unused")
	private long _rock;
	protected View _view = null;
	private Handler _uiHandler;
	private Glk _glk;
	private int mLineBuffer;
	private long mMaxLen;
	private Context mContext;
	private int mDispatchRock;

	public TextBufferWindow(final Glk glk, int rock) {
		super(rock);
		_glk = glk;
		_uiHandler = glk.getUiHandler();
		mStream = new Stream();
		mContext = glk.getContext();

		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				_view = new View(mContext);
			}
		});
	}
	
	@Override
	public synchronized void requestLineEvent(final String initial, final long maxlen, int buffer) {
		mDispatchRock = retainVmArray(buffer, maxlen);
		
		mLineBuffer = buffer;
		mMaxLen = maxlen;
		_uiHandler.post(new Runnable() {
			@Override
			public void run() {
				_view.requestLineEvent(initial, maxlen);
			}
		});
	}

	@Override
	public android.view.View getView() {
		return _view;
	}
	
	@Override
	public int[] getSize() {
		int w = _view.getWidth() - _view.getCompoundPaddingLeft() - _view.getCompoundPaddingRight();
		int h = _view.getHeight() - _view.getCompoundPaddingBottom() - _view.getCompoundPaddingTop();
		return new int[] { Math.round(w / _view.getPaint().measureText("0")), (h / _view.getLineHeight()) };
	}

	@Override
	public int measureHeight(int size) {
		return _view.getLineHeight() * size + _view.getCompoundPaddingTop() + _view.getCompoundPaddingBottom();
	}

	@Override
	public int measureWidth(int size) {
		return Math.round(_view.getPaint().measureText("0") * size) + _view.getCompoundPaddingLeft() + _view.getCompoundPaddingRight();
	}

	@Override
	public int getType() {
		return WINTYPE_TEXTBUFFER;
	}

	@Override
	public void clear() {
		// TODO perhaps do something nicer
		
		// Java is crazy. Is there really no library function for that?
		int count = (int) (getSize()[1] + 1);
		StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++)
			sb.append('\n');
		
		// I mean, in normal language it would just 
		// be "\n" * count for fscks sake
		final String str = sb.toString();
		
		_uiHandler.post(new Runnable() {
			@Override
			public void run() {
				_view.print(str);
			}
		});
	}

	@Override
	public void requestCharEvent() {
		_uiHandler.post(new Runnable() {
			@Override
			public void run() {
				_view.requestCharEvent();
			}
		});
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
