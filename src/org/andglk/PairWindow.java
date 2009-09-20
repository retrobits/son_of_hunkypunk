package org.andglk;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class PairWindow extends Window {
	private LinearLayout _view;

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
		
		l.addView(first);
		l.addView(second);
	}

	@Override
	public View getView() {
		return _view;
	}
	
	public float measureCharacterWidth() { return 0.0f; }
	public float measureCharacterHeight() { return 0.0f; }
}
