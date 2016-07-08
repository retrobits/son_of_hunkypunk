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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.andglk.glk.Styles.StyleSpan;
import org.andglk.hunkypunk.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.method.MovementMethod;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


public class TextBufferWindow extends Window {
    public static class _SavedState implements Parcelable {
        public static final Parcelable.Creator<_SavedState> CREATOR = new Parcelable.Creator<_SavedState>() {
            @Override
            public _SavedState createFromParcel(Parcel source) {
                return new _SavedState(source);
            }

            @Override
            public _SavedState[] newArray(int size) {
                return new _SavedState[size];
            }
        };

        public Parcelable mSuperState;
        public boolean mLineInputEnabled;
        public int mLineInputStart;
        public boolean mCharInputEnabled;

        public _SavedState(Parcel source) {
            mSuperState = TextView.SavedState.CREATOR.createFromParcel(source);
            mLineInputEnabled = source.readByte() == 1;
            mCharInputEnabled = source.readByte() == 1;
            mLineInputStart = source.readInt();
        }

        public _SavedState() {
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            mSuperState.writeToParcel(dest, flags);
            dest.writeByte((byte) (mLineInputEnabled ? 1 : 0));
            dest.writeByte((byte) (mCharInputEnabled ? 1 : 0));
            dest.writeInt(mLineInputStart);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }

    public static final String TAG = "Glk/TextBufferWindow";

    @Override
    public Parcelable saveInstanceState() {
        return mView.onSaveInstanceState();
    }

    @Override
    public void restoreInstanceState(Parcelable p) {
        mView.onRestoreInstanceState(p);
    }

    @Override
    public void writeState(ObjectOutputStream stream) throws IOException {
        super.writeState(stream);
        mView.writeState(stream);
    }

    @Override
    public void readState(ObjectInputStream stream) throws IOException {
        super.readState(stream);
        mView.readState(stream);
    }

    private class _Stream extends Stream {
        private long mCurrentStyle = Glk.STYLE_NORMAL;
        private boolean mReverseVideo = false;
        private final StringBuilder mBuffer = new StringBuilder();
        private SpannableStringBuilder mSsb = new SpannableStringBuilder();

        @Override
        protected void doPutChar(char c) throws IOException {
            mBuffer.append(c);
        }

        @Override
        protected void doPutString(String str) throws IOException {
            mBuffer.append(str);
        }

        @Override
        public void setStyle(long styl) {
            if (styl == mCurrentStyle)
                return;
            applyStyle();
            mCurrentStyle = styl;
        }

        @Override
        public void setReverseVideo(long reverse) {
            if ((reverse != 0) == mReverseVideo)
                return;
            applyStyle();
            mReverseVideo = (reverse != 0);
        }

        private void applyStyle() {
            if (mBuffer != null && mBuffer.length() == 0)
                return;

            final SpannableString ss = new SpannableString(mBuffer);
            if (ss.length() > 0)
                ss.setSpan(stylehints.getSpan(mContext, (int) mCurrentStyle, mReverseVideo),
                        0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSsb.append(ss);

            mBuffer.setLength(0);
        }

        public void flush() {
            applyStyle();

            if (mSsb.length() == 0)
                return;

            final Spannable ssb = mSsb;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mView.print(ssb);
                }
            });

            // not clear() since it's been handed down to view
            mSsb = new SpannableStringBuilder();
        }

        protected void discardBuffers() {
            mBuffer.setLength(0);
            mSsb.clear();
        }
    }

    private class _ScrollView extends ScrollView {
        public _ScrollView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            fullScroll(View.FOCUS_DOWN);
        }
    }

    protected Glk mGlk;
    protected _View mView;
    protected Handler mHandler;
    protected Context mContext;
    private int mLineEventBuffer;
    private long mLineEventBufferLength;
    private int mLineEventBufferRock;
    private boolean mUnicodeEvent = false;

    private class _CommandView extends EditText {

        public boolean mCharInputEnabled;
        public boolean mLineInputEnabled;
        private TextWatcher mWatcher =
                new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                    }

                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        int char_inp = 0;

                        // Hack to get single key input.
                        // OnKeyUp is not reliable
                        if (mCharInputEnabled) {

                            try {
                                if (count == 1 && s.charAt(start) > 0 && s.charAt(start) < 255)
                                    char_inp = s.charAt(start);
                            } catch (Exception ex) {
                            }

                            if (char_inp > 0) {
                                disableCharInput();

                                CharInputEvent ev = new CharInputEvent(TextBufferWindow.this, s.charAt(start));
                                mGlk.postEvent(ev);

                                ToggleCommandView();
                            }
                        }

                        // Standard line input handler
                        // OnKeyUp doesn't work on all devices (i.e. Xperia J, Android 4.1.2)
                        else {

                            try {
                                if (count >= 1 && s.charAt(start) == '\n')
                                    char_inp = '\n';
                            } catch (Exception ex) {
                            }

                            if (char_inp > 0) {
                                disableInput();

                                SpannableStringBuilder sb = new SpannableStringBuilder();
                                sb.append(getText().toString().replace("\n", "") + "\n");

                                Object sp = stylehints.getSpan(mContext, Glk.STYLE_INPUT, false);
                                if (sb.length() > 0)
                                    sb.setSpan(sp, 0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                lineInputAccepted(sb);

                                ToggleCommandView();
                            }
                        }
                    }
                };

        public _CommandView(Context context) {
            super(context, null, R.attr.textBufferWindowEditStyle);

            setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            setBackgroundResource(0);
            //setTextSize(DefaultFontSize);
            setTypeface(TextBufferWindow.this.getDefaultTypeface());
            addTextChangedListener(mWatcher);
        }

        public void clear() {
            setText("");
            Object sp = stylehints.getSpan(mContext, Glk.STYLE_INPUT, false);
            getText().setSpan(sp, 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }

        public void enableCharInput() {
            mCharInputEnabled = true;
            enableInput();
        }

        public void disableCharInput() {
            mCharInputEnabled = false;
            disableInput();
        }

		/*
        public void enableWatcher(){
			addTextChangedListener(mWatcher);
		}
		public void disableWatcher() {
            //causes exception in TextView
			removeTextChangedListener(mWatcher);
		}
		*/

        public void enableInput() {
            setFocusableInTouchMode(true);
            requestFocus();
            showKeyboard();
        }

        public void disableInput() {
            setFocusable(false);
        }

        private void showKeyboard() {
            (new Handler()).postDelayed(
                    new Runnable() {
                        public void run() {
                            _CommandView.this.dispatchTouchEvent(
                                    MotionEvent.obtain(
                                            SystemClock.uptimeMillis(),
                                            SystemClock.uptimeMillis(),
                                            MotionEvent.ACTION_DOWN,
                                            0, 0, 0
                                    )
                            );
                            _CommandView.this.dispatchTouchEvent(
                                    MotionEvent.obtain(
                                            SystemClock.uptimeMillis(),
                                            SystemClock.uptimeMillis(),
                                            MotionEvent.ACTION_UP,
                                            0, 0, 0
                                    )
                            );
                        }
                    }, 100);
        }

        private int FontSize = 0;

        @Override
        public boolean onPreDraw() {
            if (FontSize != DefaultFontSize) {
                FontSize = DefaultFontSize;
                setTextSize(FontSize);
                mPrompt.setTextSize(FontSize);
            }
            return true;
        }
    }

    private class _PromptView extends TextView {
        public _PromptView(Context context) {
            super(context, null, R.attr.textBufferWindowStyle);

            setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            setBackgroundResource(0);
            //setTextSize(DefaultFontSize);
            setTypeface(TextBufferWindow.this.getDefaultTypeface());
        }
    }

    private class _HorListView extends HorizontalScrollView {
        public _HorListView(Context context) {
            super(context, null, R.attr.textBufferWindowStyle);
            setTag("_HorListViewTAG");
        }
    }

    private class _View extends EditText {

        public class _MovementMethod implements MovementMethod {

            private float mDownX;
            private float mDownY;
            private final float SCROLL_THRESHOLD = 10;
            private boolean isOnClick;
            private long downTime;

            public boolean onGenericMotionEvent(TextView widget, Spannable text, MotionEvent event) {
                return false;
            }

            @Override
            public boolean canSelectArbitrarily() {
                return false;
            }

            @Override
            public void initialize(TextView widget, Spannable text) {
            }

            @Override
            public boolean onKeyDown(TextView widget, Spannable text, int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyUp(TextView widget, Spannable text, int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public void onTakeFocus(TextView widget, Spannable text, int direction) {
            }

            @Override
            public boolean onTouchEvent(final TextView widget, Spannable text, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownY = event.getY();
                        isOnClick = true;
                        downTime = event.getEventTime();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        long time = event.getEventTime() - downTime;
                        if (isOnClick && time > 100) {
                            float x = mDownX + getScrollX();
                            int offset = getOffset(event);
                            if (offset != Integer.MIN_VALUE) {
                                if (x > getLayout().getLineMax(0)) {
                                    String selectedText = stringHelper(offset);
                                    if (selectedText.length() > 0) {
                                        Toast.makeText(mContext, selectedText, Toast.LENGTH_SHORT).show(); //TESTING

                                        mCommandView = (EditText) findViewByTag(mGlk.getView(), "_ActiveCommandViewTAG");
                                        SpannableStringBuilder output = new SpannableStringBuilder();
                                        String userInput = mCommandView.getText().toString();

                                        if (userInput.contains("<%>")) {
                                            userInput = userInput.replaceFirst("<%>", selectedText);
                                            output.append(userInput);
                                        } else if (!userInput.equals("")) {
                                            output.append(userInput + " ");
                                            output.append(selectedText);
                                        }
                                        output.setSpan(new Styles().getSpan(mGlk.getContext(), TextBufferWindow.DefaultInputStyle, false)
                                                , 0, output.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);


                                        //TextBufferWindow.this.mActiveCommand.setText(output);
                                        // mActiveCommand.setSelection(copyText.length());
                                        if (!userInput.contains("<%>") && output.subSequence(output.length() - 1, output.length()).toString().equals("$")) {
                                            mCommandView.setText(output.subSequence(0, output.length() - 1));
                                            mActiveCommand.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                                        } else
                                            mCommandView.setText(output);

                                        TextBufferWindow.this.mScrollView.fullScroll(View.FOCUS_DOWN);
                                        TextBufferWindow.this.mActiveCommand.showKeyboard();
                                        TextBufferWindow.this.mActiveCommand.selectAll();
                                    }
                                } else {
                                    Toast.makeText(mContext, "hui", Toast.LENGTH_LONG);
                                    /*String selectedText = stringHelper(offset - 1);
                                    if (selectedText.length() > 0) {
                                        Toast.makeText(mContext, selectedText, Toast.LENGTH_SHORT).show(); //TESTING
                                        //putInClipMemory(selectedText);
                                        SpannableStringBuilder copyText = new SpannableStringBuilder();
                                        copyText = copyText.append(selectedText);
                                        copyText.setSpan(new Styles().getSpan(mGlk.getContext(), TextBufferWindow.DefaultInputStyle, false)
                                                , 0, copyText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                        TextBufferWindow.this.mActiveCommand.setText(copyText);
                                        TextBufferWindow.this.mActiveCommand.setSelection(copyText.length());
                                    }*/
                                }
                            }
                            //Go to input line


                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isOnClick && (Math.abs(mDownX - event.getX()) > SCROLL_THRESHOLD
                                || Math.abs(mDownY - event.getY()) > SCROLL_THRESHOLD)) {
                            isOnClick = false;
                        }
                        break;
                    default:
                        break;
                }

                return true;
            }

            @Override
            public boolean onTrackballEvent(TextView widget, Spannable text, MotionEvent event) {
                return false;
            }
        }

        @Override
        public Parcelable onSaveInstanceState() {
            TextBufferWindow._SavedState ss = new TextBufferWindow._SavedState();
            ss.mSuperState = super.onSaveInstanceState();
            return ss;
        }

        private int bookmarkVersion = 1;
        private String nullInd = "@!nul!@";
        public String errorStateMsg = "[An error occurred saving the window contents.  The game in progress should still be playable.]";

        public void writeState(ObjectOutputStream stream) throws IOException {
            stream.writeInt(bookmarkVersion);

            Spannable ed = getEditableText();
            boolean err = false;
            int max = 48 * 1024;
            int chop = 0;

            try {
                String text = null;
                if (ed.length() > max) {
                    chop = TextUtils.indexOf(ed, '\n', ed.length() - max);
                    if (chop == -1) chop = ed.length() - max;
                    chop++;
                    text = ed.subSequence(chop, ed.length()).toString();
                }
                if (text == null) text = ed.toString();
                stream.writeUTF(text);
            } catch (Exception e) {
                err = true;
                Log.e(TAG, "failure in writeState. (1) " + e.toString());
                stream.writeUTF(errorStateMsg);
                stream.writeLong(0);
            }

            if (!err) {
                StyleSpan[] spans = ed.getSpans(0, ed.length(), StyleSpan.class);

                long spanct = 0;
                for (StyleSpan ss : spans) {
                    if (ed.getSpanStart(ss) >= chop) spanct++;
                }
                stream.writeLong(spanct);
                for (StyleSpan ss : spans) {
                    if (ed.getSpanStart(ss) >= chop) {
                        stream.writeInt(ed.getSpanStart(ss) - chop);
                        stream.writeInt(ed.getSpanEnd(ss) - chop);
                        stream.writeInt(ss.getStyle());
                        stream.writeInt(ss.getReverse());
                    }
                }
            }

            stream.writeBoolean(mTrailingCr);

            try {
                stream.writeUTF(mLastLine == null ? nullInd : mLastLine.toString());
            } catch (Exception e) {
                Log.e(TAG, "failure in writeState. (2)" + e.toString());
                stream.writeUTF(nullInd);
            }

            try {
                CharSequence w = TextBufferWindow.this.mCommandText;
                stream.writeUTF(w == null ? nullInd : w.toString());
            } catch (Exception e) {
                Log.e(TAG, "failure in writeState. (3)" + e.toString());
                stream.writeUTF(nullInd);
            }

            try {
                CharSequence w = null;
                if (TextBufferWindow.this.mPrompt != null)
                    w = TextBufferWindow.this.mPrompt.getText();

                stream.writeUTF(w == null ? nullInd : w.toString());
            } catch (Exception e) {
                Log.e(TAG, "failure in writeState. (4)" + e.toString());
                stream.writeUTF(nullInd);
            }
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            TextBufferWindow._SavedState ss = (_SavedState) state;
            super.onRestoreInstanceState(ss.mSuperState);
        }

        public void readState(ObjectInputStream stream) throws IOException {
            int version = 0;
            try {
                version = stream.readInt();
            } catch (Exception e) {
                Log.e(TAG, "failure in readState. (1)" + e.toString());
            }

            String contents = errorStateMsg;
            try {
                contents = stream.readUTF();
                if (contents == null || contents.equals(nullInd)) contents = "";
            } catch (Exception e) {
                Log.e(TAG, "failure in readState. (2)" + e.toString());
                contents = errorStateMsg;
            }

            setText(contents);

            try {
                final Editable ed = getEditableText();
                final long spanCount = stream.readLong();
                for (long i = 0; i < spanCount; i++) {
                    final int spanStart = stream.readInt();
                    final int spanEnd = stream.readInt();
                    final int spanStyle = stream.readInt();
                    final int spanReverse = stream.readInt();

                    if (spanStart != spanEnd)
                        ed.setSpan(stylehints.getSpan(mContext, spanStyle, spanReverse != 0),
                                spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                mTrailingCr = stream.readBoolean();
                mLastLine = stream.readUTF();
                if (mLastLine == null || mLastLine.equals(nullInd)) mLastLine = null;

                String text = null;

                text = stream.readUTF();
                if (text == null || text.equals(nullInd))
                    TextBufferWindow.this.mCommandText = null;
                else
                    TextBufferWindow.this.mCommandText = text;

                text = stream.readUTF();
                if (text == null || text.equals(nullInd))
                    TextBufferWindow.this.mPrompt.setText("");
                else
                    TextBufferWindow.this.mPrompt.setText(text);
            } catch (Exception e) {
                Log.e(TAG, "failure in readState. (3)" + e.toString());
            }
        }

        private _MovementMethod mMovementMethod;

        public _View(Context context) {
            super(context, null, R.attr.textBufferWindowStyle);

            setMovementMethod(mMovementMethod = new _MovementMethod());

            setPaintFlags(Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            setBackgroundResource(0);
            //setTextSize(DefaultFontSize);
            setTypeface(TextBufferWindow.this.getDefaultTypeface());
            setReadOnly(this, true);

        }

        private void setReadOnly(final TextView view, final boolean readOnly) {
            view.setFocusable(!readOnly);
            view.setFocusableInTouchMode(!readOnly);
            view.setClickable(!readOnly);
            view.setLongClickable(!readOnly);
            view.setCursorVisible(!readOnly);
        }

        private String stringHelper(int offset) {
            setSelection(offset); // touch was at end of text
            String substringStart = getText().toString().substring(getSelectionStart());
            int maxEnd = getText().toString().length() - 1;
            int nextSpaceIndex = maxEnd;
            for (int i = 0; i < substringStart.length() - 1; i++) {
                if (Character.isSpaceChar(substringStart.toCharArray()[i]) || Character.isWhitespace(substringStart.toCharArray()[i])) {
                    nextSpaceIndex = i;
                    break;
                }
            }
            int selectionEnd;
            if (nextSpaceIndex < maxEnd)
                selectionEnd = getSelectionStart() + nextSpaceIndex;
            else
                selectionEnd = maxEnd;

            String beforeStart = getText().toString().substring(0, getSelectionStart());
            int selectionStart = 0;
            for (int i = beforeStart.length() - 1; i >= 0; i--) {
                if (Character.isSpaceChar(beforeStart.toCharArray()[i]) || Character.isWhitespace(beforeStart.toCharArray()[i])) {
                    selectionStart = i;
                    break;
                }
            }
            String selectedText = getText().toString()
                    .substring(selectionStart, selectionEnd).replaceAll("[^\\p{L}\\p{N}]+", "");

            return selectedText;
        }
        //not used, send directly to CommandView
        /*private void putInClipMemory(String str) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                //TO DO: check if getAppContext returns instance of app
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) mContext.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(str);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mContext.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Text copied.", str);
                clipboard.setPrimaryClip(clip);
            }
        }*/

        private CharSequence mLastLine = null;
        private boolean mTrailingCr = false;

        public void setTextEx(CharSequence t) {
            setText("");
            appendEx(t);
        }

        public void appendEx(CharSequence t) {
            if (t == null || t.length() == 0) return;

            if (mTrailingCr) {
                mTrailingCr = false;
                append("\n");
            }

            if (mLastLine != null) {
                append(mLastLine);
                mLastLine = null;
            }

            CharSequence ct = TextBufferWindow.this.mCommandText;
            if (ct != null) {
                append(" ");

                int j = TextUtils.lastIndexOf(ct, '\n');

                if (j > -1) {
                    mTrailingCr = true;
                    ct = ct.subSequence(0, j);
                }

                append(ct);

                TextBufferWindow.this.mCommandText = null;
            }

            CharSequence buf = null;
            int i = TextUtils.lastIndexOf(t, '\n');

            if (i > -1) {
                if (mTrailingCr) {
                    mTrailingCr = false;
                    append("\n");
                }

                append(t.subSequence(0, i));
                mLastLine = t.subSequence(i, t.length());
            } else {
                mLastLine = t;
            }

            if (mLastLine.charAt(0) == '\n') {
                if (mLastLine.length() == 1)
                    ;//no prompt
                else
                    TextBufferWindow.this.setPrompt(mLastLine.subSequence(1, mLastLine.length()));
            } else {
                TextBufferWindow.this.setPrompt(mLastLine);
            }
        }

        public void print(CharSequence text) {
            final int start = length() - 1;
            appendEx(text);
            Editable e = getEditableText();
            Utils.beautify(e, start);
        }

        /* see TextBufferWindow.clear() */
        public void clear() {
            Glk.getInstance().waitForUi(
                    new Runnable() {
                        @Override
                        public void run() {
                            setTextEx("");
                        }
                    });
        }

        private int FontSize = 0;

        @Override
        public boolean onPreDraw() {
            if (FontSize != DefaultFontSize) {
                FontSize = DefaultFontSize;
                setTextSize(FontSize);
            }

            return true;
        }

        private int getOffset(MotionEvent event) {
            Layout layout = getLayout();
            if (layout == null)
                return Integer.MIN_VALUE;
            float x = event.getX() + getScrollX();
            float y = event.getY() + getScrollY();
            int line = layout.getLineForVertical((int) y);
            int offset = layout.getOffsetForHorizontal(line, x);
            return offset;
        }
    }

    public static String DefaultFontPath = null;
    public static int DefaultFontSize = 0;
    public String FontPath = null;
    public int FontSize = 0;

    public static int DefaultInputStyle = Glk.STYLE_INPUT;

    private _ScrollView mScrollView = null;
    private _CommandView mActiveCommand = null;
    private _CommandView mCommand1 = null;
    private _CommandView mCommand2 = null;
    private _PromptView mPrompt = null;
    private _HorListView mHLView = null;
    private LinearLayout mLayout = null;
    private CharSequence mCommandText = null;
    private EditText mCommandView = null;
    private Object mLineInputSpan;

    public TextBufferWindow(Glk glk, int rock) {
        super(rock);

        mGlk = glk;
        mContext = glk.getContext();
        mHandler = mGlk.getUiHandler();


        Glk.getInstance().waitForUi(
                new Runnable() {
                    @Override
                    public void run() {
                        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                2,
                                mContext.getResources().getDisplayMetrics());
                        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                3,
                                mContext.getResources().getDisplayMetrics());


                        // when window is created, style hints are fixed
                        stylehints = new Styles(_stylehints);

                        mScrollView = new _ScrollView(mContext);
                        mScrollView.setPadding(0, 0, 0, 0);
                        mScrollView.setFocusable(false);

                        LinearLayout.LayoutParams paramsDefault = new
                                LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        LinearLayout.LayoutParams paramsHLayout = new
                                LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                        LinearLayout.LayoutParams paramsPrompt = new
                                LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        LinearLayout.LayoutParams paramsCommand = new
                                LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                        LinearLayout.LayoutParams paramsLView = new
                                LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        LinearLayout.LayoutParams paramsLLayout = new
                                LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        paramsPrompt.setMargins(0, -margin, 0, 0);
                        paramsCommand.setMargins(0, -margin, 0, 0);


                        mLayout = new LinearLayout(mContext);
                        mLayout.setOrientation(LinearLayout.VERTICAL);
                        mLayout.setPadding(0, 0, 0, 0);

                        LinearLayout hl = new LinearLayout(mContext);
                        hl.setPadding(0, 0, 0, 0);
                        hl.setOrientation(LinearLayout.HORIZONTAL);

                        LinearLayout hll = new LinearLayout(mContext);
                        hll.setPadding(0, 0, 0, 0);
                        hll.setOrientation(LinearLayout.HORIZONTAL);


                        mCommand1 = new _CommandView(mContext);
                        mCommand1.setPadding(pad, 0, pad, pad);
                        mCommand1.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        mCommand1.clear();
                        mCommand1.disableInput();
                        //mCommand1.setBackgroundColor(Color.YELLOW);
                        mCommand2 = new _CommandView(mContext);
                        mCommand2.setPadding(pad, 0, pad, pad);
                        mCommand2.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        mCommand2.clear();
                        mCommand2.disableInput();
                        //mCommand2.setBackgroundColor(Color.LTGRAY);
                        ToggleCommandView();

                        mPrompt = new _PromptView(mContext);
                        mPrompt.setPadding(pad, 0, pad, pad);
                        mPrompt.setFocusable(false);

                        hl.addView(mPrompt, paramsPrompt);
                        hl.addView(mCommand1, paramsCommand);
                        hl.addView(mCommand2, paramsCommand);


                        mHLView = new _HorListView(mContext);
                        mHLView.setPadding(0, 0, 0, 0);//better than (pad,0,pad,pad)
                        final ViewGroup viewGroup = new LinearLayout(mContext);
                        final SharedPreferences sharedPreferences = mContext.getSharedPreferences("shortcutPrefs", Context.MODE_PRIVATE);
                        Set<String> keys = sharedPreferences.getAll().keySet();
                        for (final String s : keys) {
                            if (!s.matches("#.*")) {
                                CardView cardView = new CardView(mContext);
                                cardView.setMinimumHeight(140);
                                cardView.setMinimumWidth(150);
                                cardView.setRadius(10);
                                cardView.setCardBackgroundColor(R.color.shortcutsColor);
                                final TextView textView = new TextView(mContext);
                                textView.setText(s);
                                final String command = sharedPreferences.getString(s, "");
                                textView.setTag(command);
                                textView.setPadding(20, 30, 20, 0);
                                textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                                cardView.addView(textView);
                                cardView.setPadding(20, 20, 20, 20);
                                cardView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (command.endsWith("$") && !command.contains("<%>"))
                                            shortcutCommandEnter(textView);
                                        else
                                            shortcutCommand(textView);
                                    }
                                });
                                viewGroup.addView(cardView);
                            }
                        }

                        mHLView.addView(viewGroup);
                        hll.addView(mHLView, paramsLView);

                        mView = new _View(mContext);
                        mView.setPadding(pad, pad, pad, 0);
                        mView.setFocusable(false);

                        mLayout.addView(mView, paramsDefault);
                        mLayout.addView(hl, paramsHLayout);
                        mLayout.addView(hll, paramsLLayout);

                        mScrollView.addView(mLayout);
                        mStream = new _Stream();
                    }
                });
    }

    /**
     * A method for recursively finding a view given a tag. If there
     * is no view with such tag, then the parameter is not set and
     * keeps its previous value unchanged.
     *
     * @param vg  parent ViewGroup from which the iteration starts
     * @param tag Tag Object to identify the needed View
     */

    public View findViewByTag(ViewGroup vg, Object tag) {

        View result = null;

        if (vg == null)
            return null;

        for (int i = 0; i < vg.getChildCount(); i++) {
            //because some are not set and we don't like NullPtrs
            if (vg.getChildAt(i).getTag() != null) {
                if (vg.getChildAt(i).getTag().toString().equals(tag))
                    result = vg.getChildAt(i);
            }
        }
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i) instanceof ViewGroup) {
                result = findViewByTag((ViewGroup) vg.getChildAt(i), tag);
                if (result != null) break;
            }
        }
        return result;
    }

    private void animate(View v) {
        Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.press);
        v.startAnimation(animation1);
    }

    private Editable toEditable(EditText et) {
        Editable etext = et.getText();
        etext.setSpan(new Styles().getSpan(mGlk.getContext(), TextBufferWindow.DefaultInputStyle, false)
                , 0, et.getText().length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return etext;
    }

    /**
     * Directly inputs text onto Glk.select through TextWatcher
     */
    private TextView tempView = null;

    public void shortcutCommandEnter(View v) {
        mCommandView = (EditText) findViewByTag(mGlk.getView(), "_ActiveCommandViewTAG");
        tempView = (TextView) v;
        if (mCommandView != null) {
            boolean semaphore = true;
            while (semaphore) {
                animate(v);
                SpannableStringBuilder userInput = new SpannableStringBuilder(mCommandView.getText().toString());
                String userCommand = tempView.getTag().toString();
                userCommand = userCommand.substring(0, userCommand.length() - 1);
                if (!userCommand.equals(""))
                    mCommandView.setText(userInput + " " + userCommand);
                else
                    mCommandView.setText(userCommand);


                mActiveCommand.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));

                mCommandView = (EditText) findViewByTag(mGlk.getView(), "_ActiveCommandViewTAG");
                if (mCommandView != null && userCommand.equals("Inventory")) {
                    userInput.setSpan(new Styles().getSpan(mGlk.getContext(), TextBufferWindow.DefaultInputStyle, false), 0, userInput.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    mCommandView.setText(userInput);
                }
                semaphore = false;
            }
        } else {
            //Of course not (reachable)
            Toast.makeText(mGlk.getContext(), "Interpreter.mCommandView is null. Please, contact JPDOB.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets the text in the CommandView, enabling the user to proceed typing the rest
     */
    public void shortcutCommand(View v) {
        mCommandView = (EditText) findViewByTag(mGlk.getView(), "_ActiveCommandViewTAG");
        tempView = (TextView) v;
        if (mCommandView != null) {
            animate(v);
            String userInput = mCommandView.getText().toString();
            String shortcutCommand = tempView.getTag().toString();

            //shortcutCommand.setSpan(new Styles().getSpan(mGlk.getContext(), TextBufferWindow.DefaultInputStyle, false)
            //      , 0, shortcutCommand.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            if (!userInput.equals(""))
                mCommandView.setText(userInput + " " + shortcutCommand);
            else
                mCommandView.setText(shortcutCommand);
            Selection.setSelection(toEditable(mCommandView), shortcutCommand.length());
        } else {
            //Of course not (reachable)
            Toast.makeText(mGlk.getContext(), "Interpreter.mCommandView is null. Please, contact JPDOB.", Toast.LENGTH_SHORT).show();
        }
    }

    // hack to fix fatal exception from Android framework thrown by spellchecker.
    // basic idea is to hide the textview (and show another one) rather than clearing it.
    // just before we need to use the old textview again we clear it & hopefully by
    // then the idiot spellchecker bot has visited and moved on.
    //
    // Issue 41971:	setText() in afterTextChanged function may case spellChecker bug
    // http://code.google.com/p/android/issues/detail?id=41971
    public void ToggleCommandView() {
        if (mActiveCommand == mCommand1) {
            mCommand1.setVisibility(View.GONE);
            mCommand1.setTag("Inactive");

            mCommand2.clear();
            mCommand2.setVisibility(View.VISIBLE);
            mCommand2.setTag("_ActiveCommandViewTAG");
            mActiveCommand = mCommand2;
        } else {
            mCommand2.setVisibility(View.GONE);
            mCommand2.setTag("Inactive");

            mCommand1.clear();
            mCommand1.setVisibility(View.VISIBLE);
            mCommand1.setTag("_ActiveCommandViewTAG");
            mActiveCommand = mCommand1;
        }
    }

    private Typeface _typeface = null;

    public Typeface getDefaultTypeface() {
        if (_typeface == null) {
            Typeface tf = null;

            //TODO: this is broken & disabled for now

            // if (DefaultFontPath.endsWith("ttf")
            // 	|| DefaultFontPath.endsWith("otf"))
            // 	try {
            // 		tf = Typeface.createFromFile(DefaultFontPath);
            // 	} catch (Exception ex) {}
            // else if (DefaultFontPath.endsWith("Droid Sans"))
            // 	tf = Typeface.SANS_SERIF;
            // else if (DefaultFontPath.endsWith("Droid Mono"))
            // 	tf = Typeface.MONOSPACE;

            if (tf == null) tf = Typeface.SERIF;

            _typeface = tf;
        }

        return _typeface;
    }

    public void setPrompt(CharSequence p) {
        mPrompt.setText(p);
    }

    public Object makeInputSpan() {
        return stylehints.getSpan(mContext, Glk.STYLE_INPUT, false);
    }

    public void lineInputAccepted(Spannable s) {
        String result = s.toString().trim();

        mCommandText = s;
        mPrompt.setText("");

        final org.andglk.glk.Stream echo = mStream.mEchoStream;
        if (echo != null) {
            echo.putString(result);
            echo.putChar('\n');
        }

        //Log.d("Glk/TextBufferWindow", "lineInputAccepted:"+result);

        LineInputEvent lie = new LineInputEvent(this, result, mLineEventBuffer,
                mLineEventBufferLength, mLineEventBufferRock, mUnicodeEvent);
        mLineEventBufferLength = mLineEventBuffer = mLineEventBufferRock = 0;
        mGlk.postEvent(lie);
    }

    @Override
    public void cancelCharEvent() {
        mGlk.getUiHandler().post(new Runnable() {
            @Override
            public void run() {
                mActiveCommand.disableCharInput();
            }
        });
    }

    @Override
    public LineInputEvent cancelLineEvent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        ((_Stream) mStream).discardBuffers();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.clear();
            }
        });
    }

    @Override
    public void flush() {
        ((_Stream) mStream).flush();
    }

    @Override
    public int[] getSize() {
        // TODO Auto-generated method stub
        return new int[]{0, 0};
    }

    @Override
    public int getType() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView() {
        return mScrollView;
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
        //Log.d("Glk/TextBufferWindow","requestCharEvent");
        Glk.getInstance().waitForUi(
                new Runnable() {
                    @Override
                    public void run() {
                        mActiveCommand.enableCharInput();
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
    }

    @Override
    public void requestLineEvent(final String initial, final long maxlen,
                                 final int buffer, final int unicode) {
        //Log.d("Glk/TextBufferWindow","requestLineEvent");
        flush();

        Glk.getInstance().waitForUi(
                new Runnable() {
                    @Override
                    public void run() {
                        mLineEventBuffer = buffer;
                        mLineEventBufferLength = maxlen;
                        mLineEventBufferRock = retainVmArray(buffer, maxlen);
                        mUnicodeEvent = (unicode != 0);
                        mActiveCommand.enableInput();
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
    }

    @Override
    boolean styleDistinguish(int style1, int style2) {
        if (style1 == style2)
            return false;

        int res1, res2;
        res1 = getTextAppearanceId(style1);
        res2 = getTextAppearanceId(style2);
        final int[] fields = {android.R.attr.textSize, android.R.attr.textColor,
                android.R.attr.typeface, android.R.attr.textStyle};
        TypedArray ta1 = mContext.obtainStyledAttributes(res1, fields);
        TypedArray ta2 = mContext.obtainStyledAttributes(res2, fields);

        return (ta1.getDimension(0, 0) != ta2.getDimension(0, 0)) ||
                (ta1.getColor(1, 0) != ta2.getColor(1, 0)) ||
                (ta1.getString(2) != ta2.getString(2)) ||
                (ta1.getString(3) != ta2.getString(3));
    }

    public static Styles _stylehints = new Styles();
    public Styles stylehints;
}
