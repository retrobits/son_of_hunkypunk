package org.andglk;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
	private final String mFilename;

	private FileRef(String filename, int rock) {
		super(rock);
		mFilename = filename;
	}
	
	private static class FilenamePrompt implements Future<String> {
		private String mFilename;
		private boolean mDone = false;
		@Override
		public boolean cancel(boolean arg0) {
			// TODO Auto-generated method stub
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
		public boolean isCancelled() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public synchronized boolean isDone() {
			return mDone;
		}
		
		public synchronized void publishResult(String filename) {
			mFilename = filename;
			mDone = true;
			notify();
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
		FilenamePrompt filename;
		switch (mode) {
		case FILEMODE_WRITE:
			filename = askFilenameForWrite(usage);
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

	private static FilenamePrompt askFilenameForWrite(final int usage) {
		final FilenamePrompt fp = new FilenamePrompt();
		Glk.getInstance().getUiHandler().post(new Runnable() {
		@Override
			public void run() {
				int title = 0, hint = 0;
				switch (usage & FILEUSAGE_TYPEMASK) {
				case FILEUSAGE_SAVEDGAME:
					title = R.string.saved_game;
					hint = R.string.new_saved_game_hint;
					break;
				default:
					Log.w("Glk", "unimplemented FileRef.createByPrompt usage " + Integer.toString(usage));
					fp.publishResult(null);
					return;
				}
				
				assert (Glk.getInstance() != null);
				final EditText nameEdit = new EditText(Glk.getInstance().getContext());
				nameEdit.setHint(hint);
				new AlertDialog.Builder(Glk.getInstance().getContext())
					.setTitle(title)
					.setPositiveButton(android.R.string.ok, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							fp.publishResult(nameEdit.getText().toString());
						}
					})
					.setNegativeButton(android.R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							fp.publishResult(null);
						}
					})
					.setView(nameEdit)
					.show();
			}	
		});
		return fp;
	}

	public String getFilename() {
		return mFilename;
	}
}
