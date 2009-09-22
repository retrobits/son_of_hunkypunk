package org.andglk;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

public class FileRef extends CPointed {
	public final static int FILEUSAGE_DATA = 0x00;
	public final static int FILEUSAGE_SAVEDGAME = 0x01;
	public final static int FILEUSAGE_TRANSCRIPT = 0x02;
	public final static int FILEUSAGE_INPUTRECORD = 0x03;
	public final static int FILEUSAGE_TYPEMASK = 0x0f;

	public final static int FILEUSAGE_TEXTMODE = 0x100;
	public final static int FILEUSAGE_BINARYMODE = 0x000;

	public final static int FILEMODE_WRITE = 0x01;
	public final static int FILEMODE_READ = 0x02;
	public final static int FILEMODE_READWRITE = 0x03;
	public final static int FILEMODE_WRITEAPPEND = 0x05;

	private FileRef(String filename, int rock) {
		super(rock);
	}

	/** A future which asks the user for a new filename.
	 * @author divide
	 */
	private static class NewFilePrompt implements Future<String> {
		private String mFilename;
		private boolean mDone = false;
		private Handler mUiHandler;
		
		public NewFilePrompt(final int usage) {
			mUiHandler = Glk.getInstance().getUiHandler();
			mUiHandler.post(new Runnable() {
				@Override
				public void run() {
					new NewFileDialog(NewFilePrompt.this, usage);
				}
			});
		}
		
		@Override
		public synchronized boolean cancel(boolean mayInterrupt) {
			return false;
		}

		@Override
		public synchronized String get() throws InterruptedException, ExecutionException {
			wait();
			return mFilename;
		}

		@Override
		public synchronized String get(long arg0, TimeUnit arg1)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			wait(TimeUnit.MILLISECONDS.convert(arg0, arg1));
			if (!mDone)
				throw new TimeoutException();
			return mFilename;
		}

		@Override
		public synchronized boolean isCancelled() {
			return false;
		}

		@Override
		public synchronized boolean isDone() {
			return mDone;
		}
		
		private synchronized void publishResult(String filename) {
			mFilename = filename;
			mDone = true;
			notify();
		}
	}
	private static class NewFileDialog extends AlertDialog implements OnClickListener, OnCancelListener {
		private EditText mNameEdit;
		private final NewFilePrompt mNewFilePrompt;

		/** Create a new file prompt. 
		 * The user will be prompted for a name for a new file 
		 * to be placed in appropriate directory according to usage.
		 * The filename will then be set as the result of {@link NewFilePrompt}.
		 * If user cancels, null will be set.
		 * 
		 * If file with given name already exists, the user is warned 
		 * and asked to confirm overwriting first.
		 * @note this must be created in the main thread;
		 *
		 * @param newFilePrompt future to place the computation in
		 * @param usage
		 */
		protected NewFileDialog(NewFilePrompt newFilePrompt, final int usage) {
			super(Glk.getInstance().getContext());
			mNewFilePrompt = newFilePrompt;
			int title = 0, hint = 0;
			switch (usage & FILEUSAGE_TYPEMASK) {
			case FILEUSAGE_SAVEDGAME:
				title = R.string.saved_game;
				hint = R.string.new_saved_game_hint;
				break;
			default:
				Log.w("Glk", "unimplemented FileRef.createByPrompt usage " + Integer.toString(usage));
				mNewFilePrompt.publishResult(null);
				return;
			}
			
			Context context = Glk.getInstance().getContext();
			mNameEdit = new EditText(context);
			mNameEdit.setHint(hint);
			setTitle(title);
			setButton(AlertDialog.BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
			setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);
			setView(mNameEdit);
			setCancelable(true);
			setOnCancelListener(this);
			show();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which == BUTTON_NEGATIVE) {
				cancel();
				return;
			}
			
			mNewFilePrompt.publishResult(mNameEdit.getText().toString());
		}

		@Override
		public synchronized void onCancel(DialogInterface dialog) {
			mNewFilePrompt.publishResult(null);
		}
	}

	/** Create a FileRef by prompt. Should be called from C code in its thread.
	 * 
	 * @param usage intended file usage bitfield
	 * @param mode intended file open mode, one of FILEMODE_WRITE, FILEMODE_READ, 
	 * FILEMODE_READWRITE and FILEMODE_WRITEAPPEND
	 * @param rock rock value to store in this fileref
	 * @return C pointer to a reference to the new fileref or 0 if canceled or errored.
	 */
	public static int createByPrompt(int usage, int mode, int rock) {
		Future<String> filename;
		
		switch (mode) {
		case FILEMODE_WRITE:
			filename = new NewFilePrompt(usage);
			break;
		default:
			Log.w("Glk", "unimplemented FileRef.createByPrompt filemode " + Integer.toString(mode));
			return 0;
		}
		
		try {
			String fname = filename.get();
			Log.d("FileRef", "got filename: " + fname);
			if (fname != null)
				return (new FileRef(fname, rock)).getPointer();
			else
				return 0;
		} catch (Exception e) {
			Log.e("Glk/FileRef", "error while prompting for fileref", e);
			return 0;
		}
	}
}
