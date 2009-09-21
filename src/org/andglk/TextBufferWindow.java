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
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TextBufferWindow extends Window {
	private class View extends TextView implements OnEditorActionListener {
		private int _start;
		private TextAppearanceSpan _inputSpan;
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
				
				// hack around strange span behaviour
				// (TextAppearanceSpan with 0 size affects whole paragraph)
				Spannable s = View.this.getEditableText();
				if (len > _start && s.getSpanStart(_inputSpan) == -1)
					View.this.getEditableText().setSpan(_inputSpan, _start, _start, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				else if (s.getSpanStart(_inputSpan) != -1 && len == _start)
					View.this.getEditableText().removeSpan(_inputSpan);
				
				return result;
			}
		}

		public View(Context context) {
			super(context);
			setTextAppearance(context, R.style.normal);
			setSingleLine(false);
		}
		
		public void requestLineEvent(String initial, long maxlen) {
			setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			setMovementMethod(ArrowKeyMovementMethod.getInstance());
			setFocusableInTouchMode(true);
			setOnEditorActionListener(this);

			final Editable e = getEditableText();
			_start = e.length();
			_inputSpan = new TextAppearanceSpan(getContext(), R.style.input);
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
			if (ed.length() > _start)
				ed.setSpan(_inputSpan, _start, ed.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			else
				ed.removeSpan(_inputSpan);
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
}
