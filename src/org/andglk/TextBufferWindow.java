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
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextBufferWindow implements Window {
	private class View extends TextView {
		private class Filter implements InputFilter {
			private long _maxlen;
			private int _start;

			public Filter(int start, long maxlen) {
				_start = start;
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

		public View(Context context) {
			super(context);
		}
		
		public void requestLineEvent(String initial, long maxlen) {
			setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			setMovementMethod(ArrowKeyMovementMethod.getInstance());
			setFocusableInTouchMode(true);

			final Editable e = getEditableText();
			final int start = e.length();
			final InputFilter filter = new Filter(start, maxlen);

			e.setFilters(new InputFilter[] { filter });
			if (initial != null)
				e.append(initial);
			Selection.setSelection(e, e.length());
			requestFocus();
		}
	}

	private long _rock;
	protected View _view = null;
	private Handler _uiHandler;

	public TextBufferWindow(final ViewGroup parentView, Handler handler, long rock) {
		_rock = rock;
		_uiHandler = handler;
		
		// let's do the thread dance!
		synchronized(this) {
			_uiHandler.post(new Runnable() {
				@Override
				public void run() {
					View view = new View(parentView.getContext());
					parentView.addView(view);
					synchronized(TextBufferWindow.this) {
						_view = view;
						// and zag
						TextBufferWindow.this.notify();
					}
				}
			});

			while (_view == null) try {
				// zig
				wait();
			} catch (InterruptedException e) {
				// try again
			}
		}
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
}
