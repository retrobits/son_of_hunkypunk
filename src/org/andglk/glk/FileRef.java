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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

/* ugly, ugly, ugly. is there a better way? 
 * it'd be best to have a separate R file for the package. */
import org.andglk.hunkypunk.R;

public class FileRef {
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
/* moved to native code
	private final File mFile;
	private final boolean mIsText;
	
	static List<FileRef> _fileRefs = new LinkedList<FileRef>();
	private static Iterator<FileRef> _iterator;
	private static FileRef _last;

	private FileRef(File file, boolean isText, int rock) {
		super(rock);
		mFile = file;
		mIsText = isText;
		
		_fileRefs.add(this);
	}
*/
	private FileRef() {}

	/** A future which asks the user for a new filename.
	 * @author divide
	 */
	private static class FilePrompt implements Future<File> {
		private File mFile;
		private boolean mDone = false;
		private Handler mUiHandler;
		private AlertDialog mChooser;
		private Glk mGlk;
		private Context mContext;
		private File mBaseDir;
		
		public FilePrompt(final int usage, final int mode) throws NoSuchMethodException {
			mGlk = Glk.getInstance();
			mContext = mGlk.getContext();
			mUiHandler = mGlk.getUiHandler();
			
			mUiHandler.post(new Runnable() {
				@Override
				public void run() {
					/* allow user to choose existing file...
					  if (mode == FILEMODE_WRITE)
						new NewFileDialog(FilePrompt.this, usage);
					  else
					*/
						buildExistingFileDialog(usage, mode != FILEMODE_READ);
				}
			});
		}
		
		protected void buildExistingFileDialog(final int usage, final boolean allowNew) {
			mBaseDir = mGlk.getFilesDir(usage);
			
			String[] filelist = mBaseDir.list();
			final int shift = (allowNew ? 1 : 0);
			final String[] list = new String[shift + filelist.length];
			for (int i = 0; i < filelist.length; i++)
				list[shift + i] = filelist[i];
			if (allowNew)
				list[0] = mContext.getString(R.string.create_new_file);
			
			int theTitle;
			switch (usage) {
			case FILEUSAGE_SAVEDGAME:
				theTitle = R.string.pick_saved_game;
				break;
			case FILEUSAGE_TRANSCRIPT:
				theTitle = R.string.pick_transcript;
				break;
			case FILEUSAGE_INPUTRECORD:
				theTitle = R.string.pick_input_record;
				break;
			case FILEUSAGE_DATA:
				theTitle = R.string.pick_data;
				break;
			default:
				Log.e("Glk/FileRef", "not implemented file usage: " + Integer.toString(usage));
				publishResult(null);
				return;
			}
			final int title = theTitle;
			
			mChooser = new AlertDialog.Builder(mContext)
				.setItems(list, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (allowNew) { 
							if (which == 0)
								new NewFileDialog(FilePrompt.this, usage);
							else
								makeSure(list[which]);
						} else
							publishResult(new File(mBaseDir, list[which]));
					}})
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						publishResult(null);
					}})
				.setCancelable(true)
				.setTitle(title).create();
			mChooser.show();
		}

		protected void makeSure(String string) {
			final File f = new File(mBaseDir, string);
			if (f.exists()) {
				new AlertDialog.Builder(mContext)
					.setMessage(R.string.modify_warning)
					.setPositiveButton(android.R.string.yes, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mChooser.dismiss();
							publishResult(f);
						}
					})
					.setNegativeButton(android.R.string.no, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setTitle(android.R.string.dialog_alert_title)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							mChooser.show();
						}
					})
					.show();
			} else {
				publishResult(f);
			}
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
			int title = 0;
			Glk glk = Glk.getInstance();
			switch (usage & FILEUSAGE_TYPEMASK) {
			case FILEUSAGE_SAVEDGAME:
				title = R.string.new_saved_game;
				break;
			case FILEUSAGE_TRANSCRIPT:
				title = R.string.new_transcript;
				break;
			case FILEUSAGE_DATA:
				title = R.string.new_data;
				break;
			case FILEUSAGE_INPUTRECORD:
				title = R.string.new_input_record;
				break;
			default:
				Log.e("Glk", "unimplemented FileRef.createByPrompt usage " + Integer.toString(usage));
				mNewFilePrompt.publishResult(null);
				return;
			}
			
			mBaseDir = glk.getFilesDir(usage & FILEUSAGE_TYPEMASK);
			Context context = Glk.getInstance().getContext();
			mNameEdit = new EditText(context);
			mNameEdit.setHint(R.string.name);
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
				.setMessage(R.string.replace_warning)
				.setNegativeButton(android.R.string.no, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setPositiveButton(android.R.string.yes, this)
				.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						NewFileDialog.this.show();
					}
				})
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
    /* moved to native code
	public static FileRef createByPrompt(int usage, int mode, int rock) {
		try {
			Future<File> filename = new FilePrompt(usage & FILEUSAGE_TYPEMASK, mode);
			File fname = filename.get();
			//Log.d("FileRef", "got filename: " + fname.getAbsolutePath());
			if (fname != null)
				return (new FileRef(fname, (usage & ~FILEUSAGE_TYPEMASK) == FILEUSAGE_TEXTMODE, rock)).getPointer();
			else
				return null;
		} catch (Exception e) {
			Log.e("Glk/FileRef", "error while prompting for fileref", e);
			return null;
		}
	}
    */

	public static String getPathByPrompt(int usage, int mode, int rock) {
		try {
			Future<File> filename = new FilePrompt(usage & FILEUSAGE_TYPEMASK, mode);
			File fname = filename.get();
			return fname.getAbsolutePath();
		} catch (Exception e) {
			Log.e("Glk/FileRef", "error while prompting for fileref", e);
			return null;
		}
	}

	/* moved to native code
	public File getFile() {
		return mFile;
	}

	public boolean isText() {
		return mIsText;
	}
	
	public void destroy() {
		_fileRefs.remove(this);
		release();
	}
	
	static public FileRef createTemp(int usage, int rock) {
		try {
			final File file = File.createTempFile("", "");
			return new FileRef(file, (usage & ~FILEUSAGE_TYPEMASK) != FILEUSAGE_BINARYMODE, rock);
		} catch (IOException e) {
			Log.e("Glk/FileRef", "I/O error when creating temporary fileref", e);
			return null;
		}
	}
	
	static public FileRef createByName(int usage, String name, int rock) {
		return new FileRef(new File(Glk.getInstance().getFilesDir(usage & FILEUSAGE_TYPEMASK), name),
				(usage & ~FILEUSAGE_TYPEMASK) != FILEUSAGE_BINARYMODE, rock);
	}
	
	static public FileRef createFromFileRef(int usage, FileRef ref, int rock) {
		return new FileRef(new File(Glk.getInstance().getFilesDir(usage & FILEUSAGE_TYPEMASK), ref.getFile().getName()),
				(usage & ~FILEUSAGE_TYPEMASK) != FILEUSAGE_BINARYMODE, rock);
	}

	static public FileRef iterate(FileRef w) {
		if (w == null)
			_iterator = _fileRefs.iterator();
		else if (_last != w) {
			_iterator = _fileRefs.iterator();
			while (_iterator.next() != w);
		}
		if (_iterator.hasNext())
			_last = _iterator.next();
		else
			_last = null;
		return _last;
	}
	
	public void deleteFile() {
		mFile.delete();
	}
	
	public boolean doesFileExist() {
		return mFile.exists();
	}

	@Override
	public int getDispatchClass() {
		return GIDISP_CLASS_FILEREF;
	}
	*/
}
