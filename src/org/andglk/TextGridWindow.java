package org.andglk;

import android.content.Context;
import android.content.res.TypedArray;

public class TextGridWindow extends Window {
	private class View extends android.view.View {
		private int _fontSize;

		public View(Context context) {
			super(context);
			TypedArray ta = context.obtainStyledAttributes(null, new int[] { android.R.attr.textAppearance }, 
					android.R.attr.textViewStyle, 0);
			int res = ta.getResourceId(0, -1);
			ta = context.obtainStyledAttributes(res, new int[] { android.R.attr.textSize });
			_fontSize = ta.getDimensionPixelSize(0, -1);
		}

		public float measureCharacterHeight() {
			return _fontSize;
		}
	}
	private View _view;

	public TextGridWindow(final Glk glk, long rock) {
		glk.waitForUi(new Runnable() {
			@Override
			public void run() {
				init(glk);
			}
		});		
	}

	protected void init(Glk glk) {
		_view = new View(glk.getContext());
	}

	@Override
	public View getView() {
		return _view;
	}
	
	@Override
	public float measureCharacterHeight() {
		return _view.measureCharacterHeight();
	}
}
