package org.andglk.glk;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TextAppearanceSpan;

/** This class handles all the styles a window can have. 
 * */
public class Styles {
	/** Create an empty styles object */
	public Styles() {
	}
	
	/** Create a styles from an existing styles (useful when opening a window and fixing the styles) */
	public Styles(Styles styles) {
		for (int style=0; style < Glk.STYLE_NUMSTYLES; style++) {
			for (int hint=0; hint < Glk.STYLEHINT_NUMHINTS; hint++) {
				_styles[style][hint] = styles._styles[style][hint];
			}
		}
	}

	// _styles contains all the hints, indexed by style and hint
	private final Integer[][] _styles = new Integer[Glk.STYLE_NUMSTYLES][Glk.STYLEHINT_NUMHINTS];
	
	/** sets a style hint */
	public void set(int styl, int hint, int val) {
		if (styl >= 0 && styl < Glk.STYLE_NUMSTYLES && hint >= 0 && hint < Glk.STYLEHINT_NUMHINTS) {
			_styles[styl][hint] = val;
		} else {
			//Log.w("Glk/Styles/set", "unknown style or hint: " + styl + " " + hint);
		}
	}
	
	/** clears a style hint */
	public void clear(int styl, int hint) {
		if (styl >= 0 && styl < Glk.STYLE_NUMSTYLES && hint >= 0 && hint < Glk.STYLEHINT_NUMHINTS) {
			_styles[styl][hint] = null;
		} else {
			//Log.w("Glk/Styles/clear", "unknown style or hint: " + styl + " " + hint);
		}
	}
	
	/** Get a text paint for the given style, using the hints 
	 * @param b */
	public TextPaint getPaint(Context context, TextPaint mPaint, int styl, boolean reverse) {
		// Create an copy of the paint
		TextPaint tpx = new TextPaint();
		tpx.set(mPaint);
		
		// Update it using the span we get from the style resource
		new StyleSpan(context, styl, reverse).updateDrawState(tpx);
		
		return tpx;
	}

	private static int determineStyle(int styl) {
		if (styl == Glk.STYLE_HEADER && TextBufferWindow.ChangeTypeInColor)
			styl = Glk.STYLE_NIGHT_HEADER;

		if (styl == Glk.STYLE_SUBHEADER && TextBufferWindow.ChangeTypeInColor)
			styl = Glk.STYLE_NIGHT_SUBHEADER;

		return styl;
	}

	public class StyleSpan extends TextAppearanceSpan {
		private final int _styl;
		private final boolean _reverse;

		public StyleSpan(Context context, int styl, boolean reverse) {
			super(context,Window.getTextAppearanceId(determineStyle(styl)));
			_styl = styl;
			_reverse = reverse;
		}
		
		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			updatePaint(_styl, _reverse, ds);
		}

		@Override
		public void updateMeasureState(TextPaint ds) {
			super.updateMeasureState(ds);
			//updatePaint(_styl, _reverse, ds);
		}

		public int getStyle() {
			return _styl;
		}
		
		public int getReverse() {
			return _reverse ? 1 : 0;
		}
	}
	
	// Update paint of style according to style hints
	private void updatePaint(int styl, boolean reverse, TextPaint ds) {
		// These are the hints we are going to use
		Integer[] hints = _styles[styl];
		// Set typeface first, because it's used by WEIGHT and OBLIQUE cases
		if (hints[Glk.STYLEHINT_PROPORTIONAL] != null) {
			if (Integer.valueOf(0).equals(hints[Glk.STYLEHINT_PROPORTIONAL])) {
				ds.setTypeface(Typeface.MONOSPACE);
			} else {
				ds.setTypeface(Typeface.SERIF);
			}
		}

		// We must modify typeface if we have weight or oblique hints
		Integer weight = hints[Glk.STYLEHINT_WEIGHT];
		Integer oblique = hints[Glk.STYLEHINT_OBLIQUE];
		if (weight != null || oblique != null) {
			// Code taken from TextAppearanceSpan
			Typeface tf = ds.getTypeface();
			int style = 0;

			if (tf != null) {
				style = tf.getStyle();
			}

			if (Integer.valueOf(0).equals(weight)) {
				style &= ~Typeface.BOLD;
			} else if (Integer.valueOf(1).equals(weight)) {
				style |= Typeface.BOLD;
			}
			if (Integer.valueOf(0).equals(oblique)) {
				style &= ~Typeface.ITALIC;
			} else if (Integer.valueOf(1).equals(oblique)) {
				style |= Typeface.ITALIC;
			}

			if (tf != null) {
				tf = Typeface.create(tf, style);
			} else {
				tf = Typeface.defaultFromStyle(style);
			}

			int fake = style & ~tf.getStyle();

			if ((fake & Typeface.BOLD) != 0) {
				ds.setFakeBoldText(true);
			}

			if ((fake & Typeface.ITALIC) != 0) {
				ds.setTextSkewX(-0.25f);
			}

			ds.setTypeface(tf);
			// End of code taken from TextAppearanceSpan
		}

		// We increase or decrease size by 10% for each step of the size hint (+1 means increase by 10%)
		if (hints[Glk.STYLEHINT_SIZE] != null) {
			double size = ds.getTextSize() * (10.0+hints[Glk.STYLEHINT_SIZE])/10.0;
			ds.setTextSize((float) size);
		}

		if (hints[Glk.STYLEHINT_TEXTCOLOR] != null) {
			ds.setColor(hints[Glk.STYLEHINT_TEXTCOLOR]);
		}
		
		if (hints[Glk.STYLEHINT_BACKCOLOR] != null) {
			ds.bgColor = hints[Glk.STYLEHINT_BACKCOLOR];
		}
		
		if (reverse || Integer.valueOf(1).equals(hints[Glk.STYLEHINT_REVERSECOLOR])) {
			// swap background and foreground colors
			int color = ds.getColor();
			ds.setColor(ds.bgColor);
			ds.bgColor = color;
		}
	}

	public StyleSpan getSpan(Context context, int styl, boolean reverse) {
		return new StyleSpan(context, styl, reverse);
	}
}