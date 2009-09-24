package org.andglk;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class PairWindow extends Window {
	private LinearLayout _view;
	private Glk _glk;
	private Window mKeyWindow;
	private int mMethod;
	private int mSize;
	private Window[] mChildren;

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
		_glk = glk;
		LinearLayout l = _view = new LinearLayout(glk.getContext());
		l.setWeightSum(100);

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
		int parentIndex = (oldParent.getChildAt(0) == oldv) ? 0 : 2; // 2 because of the divider
		oldParent.removeView(oldv);
		oldParent.addView(l, parentIndex);
		
		oldw.setParent(this);
		neww.setParent(this);
		mChildren = new Window[] { first, second };
		
		l.addView(first.getView());
		
		View divider = new View(_glk.getContext());
		if (l.getOrientation() == LinearLayout.VERTICAL) {
			divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
			divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1));
		} else {
			divider.setBackgroundResource(R.drawable.divider_vertical_bright);
			divider.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.FILL_PARENT));
		}
		l.addView(divider);
		l.addView(second.getView());
		
		setArrangement(method, size, neww);
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
		
		int idx = parentv.getChildAt(0) == _view ? 0 : 2;
		parentv.removeView(_view);
		parentv.addView(keepv);
		
		keepv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT, 0));
		
		release();
		PairWindow parent = getParent();
		if (parent != null) {
			keep.setParent(parent);
			parent.mChildren[idx == 0 ? 0 : 1] = keep;
			parent.notifyGone(this);
			parent.setArrangement(parent.mMethod, parent.mSize, parent.mKeyWindow);
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
	public long[] getSize() {
		return new long[] { 0, 0 }; // no meaningful measurement
	}
	
	public void setArrangement(int method, int size, Window keyWindow) {
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
			free = _view.getChildAt(2);
			break;
		case WINMETHOD_BELOW:
		case WINMETHOD_RIGHT:
			free = _view.getChildAt(0);
			constrained = _view.getChildAt(2);
			break;
		default:
			assert(false);
		}
		free.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, android.widget.LinearLayout.LayoutParams.FILL_PARENT, 0));

		switch (division) {
		case WINMETHOD_PROPORTIONAL:
			constrained.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, android.widget.LinearLayout.LayoutParams.FILL_PARENT, size));
			break;
		case WINMETHOD_FIXED:
			boolean horiz = (dir == WINMETHOD_LEFT) || (dir == WINMETHOD_RIGHT);
			int measure = 0;
			if (mKeyWindow != null)
				measure = horiz ? mKeyWindow.measureWidth(size) : mKeyWindow.measureHeight(size);
			constrained.setLayoutParams(new LinearLayout.LayoutParams(horiz ? measure : LinearLayout.LayoutParams.FILL_PARENT, horiz ? LinearLayout.LayoutParams.FILL_PARENT : measure));
		}
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
	public void requestLineEvent(String initial, long maxlen, int buffer) {
	}

	@Override
	public LineInputEvent cancelLineEvent() {
		return null;
	}

	@Override
	boolean styleDistinguish(int style1, int style2) {
		return false;
	}
}
