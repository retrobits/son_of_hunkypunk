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

	public PairWindow(final Glk glk, final Window oldw, final Window neww, final long method, final long size) {
		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				init(glk, oldw, neww, method, size);
			}
		});		
	}

	/* this must run in the main thread */
	protected void init(Glk glk, Window oldw, Window neww, long method,
			long size) {
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
		
		int division = (int) method & Window.WINMETHOD_DIVISIONMASK;
		LinearLayout.LayoutParams lp;

		if (division == WINMETHOD_FIXED) {
			boolean horiz = l.getOrientation() == LinearLayout.HORIZONTAL; 
			float zeroSize =  horiz ?
					neww.measureCharacterWidth() :
						neww.measureCharacterHeight();
			int total = Math.round(zeroSize * size);
			lp = new LinearLayout.LayoutParams(horiz ? total : LayoutParams.FILL_PARENT, horiz ? LayoutParams.FILL_PARENT : total);
		} else
			lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, size);
		
		newv.setLayoutParams(lp);

		// transfer layout params of the split window to the pair
		l.setLayoutParams(oldv.getLayoutParams());
		oldv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		ViewGroup oldParent = (ViewGroup) oldv.getParent();
		assert(oldParent != null);
		
		// swap the pair in
		int parentIndex = (oldParent.getChildAt(0) == oldv) ? 0 : 1;
		oldParent.removeView(oldv);
		oldParent.addView(l, parentIndex);
		
		oldw.setParent(this);
		neww.setParent(this);
		
		mKey = oldw;
		mSub = neww;
		
		l.addView(first);
		l.addView(second);
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
		if (die != mKey) {
			keep = mKey;
	
			// transfer layout parameters back to the key window
			View v = mKey.getView();
			LayoutParams lp = getView().getLayoutParams();
			v.setLayoutParams(lp);
		} else
			// we are closing the key, so we don't care about layout parameters
			keep = mSub;
		
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
}
