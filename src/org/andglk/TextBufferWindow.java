package org.andglk;

import java.io.IOException;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TextBufferWindow extends Window {
	private class Stream extends Window.Stream {
		@Override
		protected void doPutChar(final char c) throws IOException {
			_uiHandler.post(new Runnable() {
				@Override
				public void run() {
					_view.append(Character.toString(c));
				}
			});
		}

		@Override
		protected void doPutString(final String str) throws IOException {
			_uiHandler.post(new Runnable() {
				@Override
				public void run() {
					_view.append(str);
				}
			});
		}

		@Override
		public void setStyle(final long styl) {
			_glk.waitForUi(new Runnable() {
				@Override
				public void run() {
					_view.setStyle(styl);
				}
			});
		}

		public void echo(String s) {
			if (mEchoStream != null)
				mEchoStream.putString(s);
		}
	}
	
	private class View extends TextView implements OnEditorActionListener, OnKeyListener {
		private int _start;
		private TextAppearanceSpan mStyleSpan;
		private int mStyleStart;
		private boolean mCharEventPending;
		private boolean mLineInputPending;
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
		protected void onTextChanged(CharSequence text, int start, int before,
				int after) {
			if (mStyleSpan == null)
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
				e.setSpan(mStyleSpan, mStyleStart, elen, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			// span is there but shrank to 0
			else if (sstart != -1 && sstart == e.getSpanEnd(mStyleSpan)) {
				mStyleStart = sstart;
				e.removeSpan(mStyleSpan);
			}
		}

		public View(Context context) {
			super(context);
			setTextAppearance(context, R.style.TextBufferWindow);
			setSingleLine(false);
			setText("", BufferType.EDITABLE);
		}
		
		public void requestLineEvent(String initial, long maxlen) {
			setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			setMovementMethod(ArrowKeyMovementMethod.getInstance());
			setFocusableInTouchMode(true);
			setOnEditorActionListener(this);
			mLineInputPending = true;

			final Editable e = getEditableText();
			_start = e.length();
			setStyle(Glk.STYLE_INPUT);
			final InputFilter filter = new Filter(maxlen);

			e.setFilters(new InputFilter[] { filter });
			if (initial != null)
				e.append(initial);
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
			Editable e = getEditableText();
			if (mStyleSpan != null) {
				int start = mStyleStart;
				int end = e.length();
				if (start == end)
					e.removeSpan(mStyleSpan);
				else
					e.setSpan(mStyleSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			
			mStyleStart = e.length(); 

			final int id = getTextAppearanceId((int) styl);
			if (id == 0)
				mStyleSpan = null;
			else
				mStyleSpan = new TextAppearanceSpan(getContext(), id);
		}

		public void requestCharEvent() {
			setOnKeyListener(this);
			setFocusableInTouchMode(true);
			mCharEventPending = true;
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
			
			LineInputEvent e = new LineInputEvent(TextBufferWindow.this, s, mLineBuffer, mMaxLen);
			Editable ed = getEditableText();
			setStyle(Glk.STYLE_NORMAL);
			ed.setFilters(new InputFilter[]{});
			
			append("\n");
			setOnEditorActionListener(null);
			setRawInputType(InputType.TYPE_NULL);
			setMovementMethod(getDefaultMovementMethod());
			setFocusable(false);

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

	public TextBufferWindow(final Glk glk, int rock) {
		super(rock);
		_glk = glk;
		_uiHandler = glk.getUiHandler();
		mStream = new Stream();

		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				_view = new View(glk.getContext());
			}
		});
	}
	
	@Override
	public synchronized void requestLineEvent(final String initial, final long maxlen, int buffer) {
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
	public long[] getSize() {
		int w = _view.getWidth() - _view.getCompoundPaddingLeft() - _view.getCompoundPaddingRight();
		int h = _view.getHeight() - _view.getCompoundPaddingBottom() - _view.getCompoundPaddingTop();
		return new long[] { (long) (w / _view.getPaint().measureText("0")), (long) (h / _view.getLineHeight()) };
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
				_view.append(str);
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
}
