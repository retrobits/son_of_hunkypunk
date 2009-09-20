package org.andglk;

import android.os.Handler;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextBufferWindow implements Window {
	private long _rock;
	protected TextView _view = null;
	private Handler _uiHandler;

	public TextBufferWindow(final ViewGroup parentView, Handler handler, long rock) {
		_rock = rock;
		_uiHandler = handler;
		
		// let's do the thread dance!
		synchronized(this) {
			_uiHandler.post(new Runnable() {
				@Override
				public void run() {
					TextView view = new TextView(parentView.getContext());
					parentView.addView(view);
					synchronized(TextBufferWindow.this) {
						_view = view;
						// and zag
						TextBufferWindow.this.notify();
					}
				}
			});

			while (_view != null) try {
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
	public void requestLineEvent(String initial, long maxlen) {
		// TODO Auto-generated method stub
		
	}
}
