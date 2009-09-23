package org.andglk;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class PairWindow extends Window {
	private LinearLayout _view;
	private Window mKey;
	private Window mSub;
	private Glk _glk;
	private Window mKeyWindow;
	private int mMethod;
	private int mSize;

	public PairWindow(final Glk glk, final Window oldw, final Window neww, final int method, final int size) {
		super(0);
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

		View oldv = oldw.getView(), newv = neww.getView();
		View first, second;
		switch (dir) {
		case Window.WINMETHOD_RIGHT:
		case Window.WINMETHOD_BELOW:
			first = oldv;
			second = newv;
			break;
		case Window.WINMETHOD_LEFT:
		case Window.WINMETHOD_ABOVE:
		default:
			first = newv;
			second = oldv;
		}
		
		// transfer layout params of the split window to the pair
		l.setLayoutParams(oldv.getLayoutParams());
		oldv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		ViewGroup oldParent = (ViewGroup) oldv.getParent();
		assert(oldParent != null);
		
		// swap the pair in
		int parentIndex = (oldParent.getChildAt(0) == oldv) ? 0 : 2; // 2 because of the divider
		oldParent.removeView(oldv);
		oldParent.addView(l, parentIndex);
		
		oldw.setParent(this);
		neww.setParent(this);
		
		l.addView(first);
		
		View divider = new View(_glk.getContext());
		if (l.getOrientation() == LinearLayout.VERTICAL) {
			divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
			divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1));
		} else {
			divider.setBackgroundResource(R.drawable.divider_vertical_bright);
			divider.setLayoutParams(new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.FILL_PARENT));
		}
		l.addView(divider);
		l.addView(second);
		
		setArrangement(method, size, neww);
	}

	@Override
	public View getView() {
		return _view;
	}
	
	public float measureCharacterWidth() { return 0.0f; }
	public float measureCharacterHeight() { return 0.0f; }

	public void dissolve(final Window die) {
		_glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				doDissolve(die);
			}
		});
	}

	protected void doDissolve(Window die) {
		Window keep;
		View v;
		LayoutParams lp;
		if (die != mKey) {
			keep = mKey;
	
			// transfer layout parameters back to the key window
			v = mKey.getView();
			lp = getView().getLayoutParams();
		} else {
			// we are closing the key, so we reset the sub's parameters
			keep = mSub;
			v = mSub.getView();
			lp = new LinearLayout.LayoutParams(0, 0, 0);
		}
		v.setLayoutParams(lp);
		
		// first we replace the views
		View keepv = keep.getView();
		ViewGroup thisv = (ViewGroup) getView(), parentv = (ViewGroup) thisv.getParent();
		assert(parentv != null);
		
		int index = (parentv.getChildAt(0) == thisv) ? 0 : 1;
		parentv.removeView(thisv);
		thisv.removeView(keepv);
		parentv.addView(keepv, index);
		
		// then modify window hierarchy
		PairWindow parent = getParent();
		if (parent != null) {
			if (parent.mKey == this)
				parent.mKey = keep;
			else
				parent.mSub = keep;
		}
		
		// ok, we're done
		release();
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
}
