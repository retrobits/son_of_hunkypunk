package org.andglk.hunkypunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.andglk.babel.Babel;
import org.andglk.glk.Utils;
import org.andglk.hunkypunk.HunkyPunk.Games;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class StorageManager {
	public static final int DONE = 0;
	public static final int INSTALLED = 1;
	public static final int INSTALL_FAILED = 2;

	private static final String TAG = "hunkypunk.MediaScanner";
	private static final String[] PROJECTION = { Games._ID, Games.FILENAME };
	private static final int _ID = 0;
	private static final int FILENAME = 1;

	private final ContentResolver mContentResolver;
	private List<File> mExtraPaths = new LinkedList<File>();
	private Handler mHandler;
	
	private StorageManager(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}
	
	private static StorageManager sInstance;
	
	public static StorageManager getInstance(ContentResolver contentResolver) {
		if (sInstance == null)
			sInstance = new StorageManager(contentResolver);
		
		assert(sInstance.mContentResolver == contentResolver);
		return sInstance;
	}

	public void setHandler(Handler h) {
		mHandler = h;
	}
	
	public void checkExisting() {
		HunkyPunk.ensureDirectoryExists();
		
		Cursor c = mContentResolver.query(Games.CONTENT_URI, PROJECTION, Games.FILENAME + " IS NOT NULL", null, null);
		
		while (c.moveToNext())
			if (!new File(HunkyPunk.DIRECTORY, c.getString(FILENAME)).exists()) {
				ContentValues cv = new ContentValues();
				cv.putNull(Games.FILENAME);
				mContentResolver.update(ContentUris.withAppendedId(Games.CONTENT_URI, c.getLong(_ID)), cv, null, null);
			}
		
		c.close();
	}

	public void scan(File dir, boolean foreign) {
		if (!dir.exists() || !dir.isDirectory())
			return;
		
		for (File f : dir.listFiles())
			if (!f.isDirectory())
				try {
					checkFile(f, foreign);
				} catch (IOException e) {
					Log.w(TAG, "IO exception while checking " + f, e);
				}
	}

	private String checkFile(File f, boolean foreign) throws IOException {
		String ifid = Babel.examine(f);
		
		if (ifid == null)
			return null;
		
		Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, ifid);
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);
		
		String filename = f.getName();
		
		// only copy file if we don't already have it
		if (foreign && (!query.moveToNext() || query.isNull(FILENAME)))
			try {
				filename = installStory(f);
			} catch (IOException e) {
				Log.e(TAG, "IO error while installing story " + f, e);
				query.close();
				throw(e);
			}
		
		ContentValues cv = new ContentValues();
		cv.put(Games.FILENAME, filename);
		
		if (query == null || query.getCount() != 1) {
			cv.put(Games.IFID, ifid);
			final String fname = f.getName();
			cv.put(Games.TITLE, fname.substring(0, fname.lastIndexOf('.')));
			mContentResolver.insert(Games.CONTENT_URI, cv);
		} else
			mContentResolver.update(uri, cv, null, null);
		
		query.close();
		return ifid;
	}

	private String installStory(File f) throws IOException {
		if (!HunkyPunk.DIRECTORY.exists())
			HunkyPunk.DIRECTORY.mkdir();
		
		String name = f.getName();
		File target;
		while ((target = new File(HunkyPunk.DIRECTORY, name)).exists())
			name = "_" + name;
		
		copyFile(f, target);

		return name;
	}

	/* shouldn't this be a library function or something?! */
	private void copyFile(File f, File target) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		FileOutputStream fos = new FileOutputStream(target);

		Utils.copyStream(fis, fos);
		
		fis.close();
		fos.close();
	}

	public void addExtraSearchPath(File file) {
		mExtraPaths.add(file);
	}

	public void startCheckingFile(final File file) {
		new Thread() {
			@Override
			public void run() {
				try {
					String ifid;
					if ((ifid = checkFile(file, true)) != null) {
						Message.obtain(mHandler, INSTALLED, ifid).sendToTarget();
						return;
					}
				} catch (IOException e) {
				}
				
				Message.obtain(mHandler, INSTALL_FAILED).sendToTarget();
			}
		}.run();
	}

	public void startScan() {
		new Thread() {
			@Override
			public void run() {
				scan(HunkyPunk.DIRECTORY, false);
				for (File dir : mExtraPaths)
					scan(dir, true);
				Message.obtain(mHandler, DONE).sendToTarget();
			}
		}.start();
	}
}
