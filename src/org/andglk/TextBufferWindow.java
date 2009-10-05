package org.andglk;

import java.io.IOException;


import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class TextBufferWindow extends Window {
	private class _Stream extends Stream {
		@Override
		protected void doPutChar(char c) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		protected void doPutString(String str) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public void setStyle(long styl) {
			// TODO Auto-generated method stub

		}
	}

	private Glk mGlk;
	private _View mView;
	
	private class _View extends TextView {
		public _View(Context context) {
			super(context, null, R.attr.textBufferWindowStyle);
		}
	}

	public TextBufferWindow(Glk glk, int rock) {
		super(rock);
		mGlk = glk;
		mView = new _View(glk.getContext());
		mStream = new _Stream();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestLineEvent(String initial, long maxlen, int buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		// TODO Auto-generated method stub
		return false;
	}
}
