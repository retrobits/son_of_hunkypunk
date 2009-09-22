package org.andglk;

import java.io.File;
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
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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
	private final File mFile;
	private final boolean mIsText;

	private FileRef(File file, boolean isText, int rock) {
		super(rock);
		mFile = file;
		mIsText = isText;
	}

	/** A future which asks the user for a new filename.
	 * @author divide
	 */
	private static class FilePrompt implements Future<File> {
		private File mFile;
		private boolean mDone = false;
		private Handler mUiHandler;
		
		public FilePrompt(final int usage, final int mode) throws NoSuchMethodException {
			mUiHandler = Glk.getInstance().getUiHandler();
			
			switch (mode) {
			case FILEMODE_WRITE:
			case FILEMODE_READ:
			case FILEMODE_WRITEAPPEND:
				break;
			default:
				throw new NoSuchMethodException("filemode not implemented: " + Integer.toString(mode));
			}

			mUiHandler.post(new Runnable() {
				@Override
				public void run() {
					switch (mode) {
					case FILEMODE_WRITE:
						new NewFileDialog(FilePrompt.this, usage);
						break;
					case FILEMODE_READ:
						try {
								buildExistingFileDialog(usage, false);
							} catch (NoSuchMethodException e) {
								publishResult(null);
							}
						break;
					case FILEMODE_WRITEAPPEND:
						try {
							buildExistingFileDialog(usage, true);
						} catch (NoSuchMethodException e) {
							publishResult(null);
						}
					break;
					}
				}
			});
		}
		
		protected void buildExistingFileDialog(final int usage, final boolean allowNew) throws NoSuchMethodException {
			Glk glk = Glk.getInstance();
			final File baseDir = glk.getFilesDir(usage);
			
			String[] filelist = baseDir.list();
			final int shift = (allowNew ? 1 : 0);
			final String[] list = new String[shift + filelist.length];
			for (int i = 0; i < filelist.length; i++)
				list[shift + i] = filelist[i];
			if (allowNew)
				list[0] = glk.getContext().getString(R.string.create_new_file);
			
			int theTitle;
			switch (usage) {
			case FILEUSAGE_SAVEDGAME:
				theTitle = R.string.pick_saved_game;
				break;
			case FILEUSAGE_TRANSCRIPT:
				theTitle = R.string.pick_transcript_append;
				// TODO when not appending
				break;
			default:
				// TODO
				throw new NoSuchMethodException("picking file of not implemented usage: ");
			}
			final int title = theTitle;
			
			new AlertDialog.Builder(glk.getContext())
				.setItems(list, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (allowNew && which == 0)
							new NewFileDialog(FilePrompt.this, usage);
						else
							publishResult(new File(baseDir, list[shift + which]));
					}})
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						publishResult(null);
					}})
				.setCancelable(true)
				.setTitle(title)
				.show();
		}

		@Override
		public synchronized boolean cancel(boolean mayInterrupt) {
			return false;
		}

		@Override
		public synchronized File get() throws InterruptedException, ExecutionException {
			wait();
			return mFile;
		}

		@Override
		public synchronized File get(long arg0, TimeUnit arg1)
				throws InterruptedException, ExecutionException,
				TimeoutException {
			wait(TimeUnit.MILLISECONDS.convert(arg0, arg1));
			if (!mDone)
				throw new TimeoutException();
			return mFile;
		}

		@Override
		public synchronized boolean isCancelled() {
			return false;
		}

		@Override
		public synchronized boolean isDone() {
			return mDone;
		}
		
		private synchronized void publishResult(File theFile) {
			mFile = theFile;
			mDone = true;
			notify();
		}
	}
	private static class NewFileDialog extends AlertDialog implements OnClickListener, OnCancelListener {
		private EditText mNameEdit;
		private final FilePrompt mNewFilePrompt;
		private File mBaseDir;

		/** Create a new file prompt. 
		 * The user will be prompted for a name for a new file 
		 * to be placed in appropriate directory according to usage.
		 * The filename will then be set as the result of {@link FilePrompt}.
		 * If user cancels, null will be set.
		 * 
		 * If file with given name already exists, the user is warned 
		 * and asked to confirm overwriting first.
		 * @note this must be created in the main thread;
		 *
		 * @param newFilePrompt future to place the computation in
		 * @param usage
		 */
		protected NewFileDialog(FilePrompt newFilePrompt, final int usage) {
			super(Glk.getInstance().getContext());
			mNewFilePrompt = newFilePrompt;
			int title = 0, hint = 0;
			Glk glk = Glk.getInstance();
			switch (usage & FILEUSAGE_TYPEMASK) {
			case FILEUSAGE_SAVEDGAME:
				title = R.string.saved_game;
				hint = R.string.new_saved_game_hint;
				break;
			case FILEUSAGE_TRANSCRIPT:
				title = R.string.new_transcript;
				hint = R.string.name;
				break;
			default:
				Log.w("Glk", "unimplemented FileRef.createByPrompt usage " + Integer.toString(usage));
				mNewFilePrompt.publishResult(null);
				return;
			}
			
			mBaseDir = glk.getFilesDir(usage & FILEUSAGE_TYPEMASK);
			Context context = Glk.getInstance().getContext();
			mNameEdit = new EditText(context);
			mNameEdit.setHint(hint);
			mNameEdit.setSingleLine(true);
			mNameEdit.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					filenameConfirmed(NewFileDialog.this);
					return true;
				}
			});
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

			filenameConfirmed(dialog);
		}

		private void filenameConfirmed(DialogInterface dialog) {
			File theFile = new File(mBaseDir, mNameEdit.getText().toString());
			if (!theFile.exists() || dialog != this) {
				dismiss();
				mNewFilePrompt.publishResult(theFile);
			} else {
				show();
				confirmOverwrite();
			}
		}

		private void confirmOverwrite() {
			new AlertDialog.Builder(getContext())
				.setCancelable(true)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.already_exists)
				.setNegativeButton(android.R.string.no, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						NewFileDialog.this.show();
					}
				})
				.setPositiveButton(android.R.string.yes, this)
				.setTitle(android.R.string.dialog_alert_title)
				.show();
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
		try {
			Future<File> filename = new FilePrompt(usage & FILEUSAGE_TYPEMASK, mode);
			File fname = filename.get();
			Log.d("FileRef", "got filename: " + fname.getAbsolutePath());
			if (fname != null)
				return (new FileRef(fname, (usage & ~FILEUSAGE_TYPEMASK) == FILEUSAGE_TEXTMODE, rock)).getPointer();
			else
				return 0;
		} catch (Exception e) {
			Log.e("Glk/FileRef", "error while prompting for fileref", e);
			return 0;
		}
	}

	public File getFile() {
		return mFile;
	}

	public boolean isText() {
		return mIsText;
	}
}
