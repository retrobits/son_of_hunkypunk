package org.andglk;

import android.view.View;

public class BlankWindow extends Window {
	private View mView;
	
	public BlankWindow(Glk glk, int rock) {
		super(rock);
		mView = new View(glk.getContext());
		mStream = new BlankStream();
	}

	@Override
	public long[] getSize() {
		return new long[] { 0, 0 };
	}

	@Override
	public View getView() {
		return mView;
	}

	@Override
	public int measureHeight(int size) {
		return 0;
	}

	@Override
	public int measureWidth(int size) {
		return 0;
	}

	@Override
	public int getType() {
		return WINTYPE_BLANK;
	}

	@Override
	public void clear() {
	}
}
