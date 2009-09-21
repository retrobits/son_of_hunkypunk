package org.andglk;

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
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TextBufferWindow extends Window {
	private class View extends TextView implements OnEditorActionListener {
		private int _start;
		private TextAppearanceSpan mStyleSpan;
		private int mStyleStart;
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
			
			String s = getLineInput();
			Event e = new LineInputEvent(TextBufferWindow.this, s);
			Editable ed = getEditableText();
			setStyle(Glk.STYLE_NORMAL);
			ed.setFilters(new InputFilter[]{});
			
			append("\n");
			setOnEditorActionListener(null);
			setRawInputType(InputType.TYPE_NULL);
			setMovementMethod(getDefaultMovementMethod());
			setFocusable(false);
			
			_glk.postEvent(e);
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
			
			switch((int) styl) {
			case Glk.STYLE_HEADER:
				mStyleSpan = new TextAppearanceSpan(getContext(), R.style.header);
				break;
			case Glk.STYLE_SUBHEADER:
				mStyleSpan = new TextAppearanceSpan(getContext(), R.style.subheader);
				break;
			case Glk.STYLE_INPUT:
				mStyleSpan = new TextAppearanceSpan(getContext(), R.style.input);
				break;
			case Glk.STYLE_BLOCKQUOTE:
				mStyleSpan = new TextAppearanceSpan(getContext(), R.style.blockquote);
				break;
			default:
				Log.w("Glk", "TextBufferWindow doesn't know style " + Long.toString(styl));
				// fall through, normal is default
			case Glk.STYLE_NORMAL:
				mStyleSpan = null;
			}
		}
	}

	@SuppressWarnings("unused")
	private long _rock;
	protected View _view = null;
	private Handler _uiHandler;
	private Glk _glk;

	public TextBufferWindow(final Glk glk, long rock) {
		_glk = glk;
		_rock = rock;
		_uiHandler = glk.getUiHandler();

		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				_view = new View(glk.getContext());
			}
		});
	}
	
	@Override
	public synchronized void putString(final String str) {
		super.putString(str);
		_uiHandler.post(new Runnable() {
			@Override
			public void run() {
				_view.append(str);
			}
		});
	}

	@Override
	public synchronized void requestLineEvent(final String initial, final long maxlen) {
		_uiHandler.post(new Runnable() {
			@Override
			public void run() {
				_view.requestLineEvent(initial, maxlen);
			}
		});
	}

	@Override
	public void put_char(final char c) {
		super.put_char(c);
		_uiHandler.post(new Runnable() {
			@Override
			public void run() {
				_view.append(Character.toString(c));
			}
		});
	}

	@Override
	public android.view.View getView() {
		return _view;
	}
	
	@Override
	public float measureCharacterHeight() {
		return _view.getLineHeight();
	}
	
	@Override
	public float measureCharacterWidth() {
		return _view.getPaint().measureText("0");
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
}
