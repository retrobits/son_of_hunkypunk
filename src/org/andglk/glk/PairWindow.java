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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class PairWindow extends Window {
	private LinearLayout _view;
	private Glk _glk;
	private Window mKeyWindow;
	private int mMethod;
	private int mSize;
	private Window[] mChildren;
	private boolean mWaitingForLayout;
	
	private class _View extends LinearLayout {
		public _View(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			super.onLayout(changed, l, t, r, b);
			synchronized(PairWindow.this) {
				if (mWaitingForLayout) {
					mWaitingForLayout = false;
					PairWindow.this.notify();
				}
			}
		}
	}

	public PairWindow(final Glk glk, final Window oldw, final Window neww, final int method, final int size) {
		super(0);
		mStream = new BlankStream();
		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				init(glk, oldw, neww, method, size);
			}
		});		
	}

	/* this must run in the main thread */
	protected void init(Glk glk, Window oldw, Window neww, int method, int size) {
		mWaitingForLayout = false;
		_glk = glk;
		LinearLayout l = _view = new _View(glk.getContext());
		final PairWindow parent = oldw.getParent();
		setParent(parent);
		if (parent != null)
			parent.mChildren[parent.mChildren[0] == oldw ? 0 : 1] = this;

		int dir = (int) method & Window.WINMETHOD_DIRMASK; 
		
		switch (dir) {
		case Window.WINMETHOD_ABOVE:
		case Window.WINMETHOD_BELOW:
			l.setOrientation(LinearLayout.VERTICAL);
			break;
		case Window.WINMETHOD_LEFT:
		case Window.WINMETHOD_RIGHT:
		default:
			l.setOrientation(LinearLayout.HORIZONTAL);
		}

		View oldv = oldw.getView();
		Window first, second;
		switch (dir) {
		case Window.WINMETHOD_RIGHT:
		case Window.WINMETHOD_BELOW:
			first = oldw;
			second = neww;
			break;
		case Window.WINMETHOD_LEFT:
		case Window.WINMETHOD_ABOVE:
		default:
			first = neww;
			second = oldw;
		}
		
		// transfer layout params of the split window to the pair
		l.setLayoutParams(oldv.getLayoutParams());
		oldv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 0));

		ViewGroup oldParent = (ViewGroup) oldv.getParent();
		assert(oldParent != null);
		
		// swap the pair in
		int parentIndex = (oldParent.getChildAt(0) == oldv) ? 0 : 1;
		oldParent.removeView(oldv);
		oldParent.addView(l, parentIndex);
		
		oldw.setParent(this);
		neww.setParent(this);
		mChildren = new Window[] { first, second };
		
		l.addView(first.getView());
		l.addView(second.getView());
		
		doSetArrangement(method, size, neww);
	}

	@Override
	public View getView() {
		return _view;
	}
	
	public void dissolve(final Window die) {
		_glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				doDissolve(die);
			}
		});
	}

	protected void doDissolve(Window die) {
		Window keep = getSibling(die);
		View keepv = keep.getView();
		ViewGroup parentv = (ViewGroup) _view.getParent();
		
		_view.removeView(keepv);
		
		int idx = parentv.getChildAt(0) == _view ? 0 : 1;
		parentv.removeView(_view);
		parentv.addView(keepv, idx);
		
		keepv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 0));
		
		release();
		PairWindow parent = getParent();
		keep.setParent(parent);
		if (parent != null) {
			parent.mChildren[idx == 0 ? 0 : 1] = keep;
			parent.notifyGone(this);
			parent.setArrangement(parent.mMethod, parent.mSize, parent.mKeyWindow);
		} else {
			// we have the root, so the parent is a framelayout, not a linearlayout
			keepv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
			setRoot(keep);
		}
	}
	
	@Override
	public long close() {
		mChildren[0].close();
		// we should be dissolved by now, and the other child is in our place
		mChildren[1].close();
		return 0;
	}

	Window getSibling(Window die) {
		if (mChildren[0] == die)
			return mChildren[1];
		else return mChildren[0];
	}

	@Override
	public void setStyle(long styl) {
		// noop
	}

	@Override
	public int[] getSize() {
		return new int[] { 0, 0 }; // no meaningful measurement
	}
	
	public void setArrangement(final int method, final int size, final Window keyWindow) {
		_glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				mWaitingForLayout = true;
				doSetArrangement(method, size, keyWindow);
			}
		});
		waitForLayout();
	}

	synchronized private void waitForLayout() {
		if (_glk.getExiting()) return;
		while (mWaitingForLayout)
			try {
				wait(1000);
			} catch (InterruptedException e) {
			}
	}

	protected void doSetArrangement(int method, int size, Window keyWindow) {
		int division = (int) method & Window.WINMETHOD_DIVISIONMASK;
		int dir = method & Window.WINMETHOD_DIRMASK;

		if (keyWindow != null) mKeyWindow = keyWindow;
		mMethod = method;
		mSize = size;
		
		View free = null, constrained = null;
		switch (dir) {
		case WINMETHOD_ABOVE:
		case WINMETHOD_LEFT:
			constrained = _view.getChildAt(0);
			free = _view.getChildAt(1);
			break;
		case WINMETHOD_BELOW:
		case WINMETHOD_RIGHT:
			free = _view.getChildAt(0);
			constrained = _view.getChildAt(1);
			break;
		default:
			assert(false);
		}
		free.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, android.widget.LinearLayout.LayoutParams.FILL_PARENT, size));

		switch (division) {
		case WINMETHOD_PROPORTIONAL:
			constrained.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, android.widget.LinearLayout.LayoutParams.FILL_PARENT, 100 - size + .01f));
			break;
		case WINMETHOD_FIXED:
			boolean horiz = (dir == WINMETHOD_LEFT) || (dir == WINMETHOD_RIGHT);
			int measure = 0;
			if (mKeyWindow != null)
				measure = horiz ? mKeyWindow.measureWidth(size) : mKeyWindow.measureHeight(size);
			constrained.setLayoutParams(new LinearLayout.LayoutParams(horiz ? measure : LinearLayout.LayoutParams.FILL_PARENT, horiz ? LinearLayout.LayoutParams.FILL_PARENT : measure));
		}
		
		_view.requestLayout();
	}

	public void notifyGone(Window window) {
		if (mKeyWindow == window)
			mKeyWindow = null;
		final PairWindow parent = getParent();
		if (parent != null)
			parent.notifyGone(window);
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
		return WINTYPE_PAIR;
	}

	@Override
	public void clear() {
	}

	@Override
	public void requestCharEvent() {
	}

	@Override
	public void cancelCharEvent() {
	}

	@Override
	public void requestLineEvent(String initial, long maxlen, int buffer, int unicode) {
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		return null;
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		return false;
	}

	public Window getLeftChild() {
		return mChildren[0];
	}

	public Window getRightChild() {
		return mChildren[1];
	}

	@Override
	public void flush() {
		mChildren[0].flush();
		mChildren[1].flush();
	}
}
