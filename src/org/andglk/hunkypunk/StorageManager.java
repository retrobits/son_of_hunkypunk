/*
	Copyright © 2009-2010 Rafał Rzepecki <divided.mind@gmail.com>

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

package org.andglk.hunkypunk;

import java.io.File;
import java.io.IOException;

import org.andglk.babel.Babel;
import org.andglk.hunkypunk.HunkyPunk.Games;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class StorageManager {
	public static final int DONE = 0;
	public static final int INSTALLED = 1;
	public static final int INSTALL_FAILED = 2;

	private static final String TAG = "hunkypunk.MediaScanner";
	private static final String[] PROJECTION = { Games._ID, Games.PATH };
	private static final int _ID = 0;
	private static final int PATH = 1;

	private final ContentResolver mContentResolver;
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
		
		Cursor c = mContentResolver.query(Games.CONTENT_URI, PROJECTION, Games.PATH + " IS NOT NULL", null, null);
		
		while (c.moveToNext())
			if (!new File(c.getString(PATH)).exists()) {
				ContentValues cv = new ContentValues();
				cv.putNull(Games.PATH);
				mContentResolver.update(ContentUris.withAppendedId(Games.CONTENT_URI, c.getLong(_ID)), cv, null, null);
			}
		
		c.close();
	}

	public void scan(File dir) {
		if (!dir.exists() || !dir.isDirectory())
			return;
		
		final File[] files = dir.listFiles();
		if (files == null)
			return;

		for (File f : files)
			if (!f.isDirectory())
				try {
					if (f.getName().matches(".*\\.z[1-9]$")
						|| f.getName().matches(".*\\.zblorb$")
						|| f.getName().matches(".*\\.zlb$")
/* todo:
						|| f.getName().matches(".*\\.blorb$")
						|| f.getName().matches(".*\\.gblorb$")
						|| f.getName().matches(".*\\.blb$")
						|| f.getName().matches(".*\\.glb$")
						|| f.getName().matches(".*\\.ulx$")
						|| f.getName().matches(".*\\.gam$"))
*/
						checkFile(f);
				} catch (IOException e) {
					Log.w(TAG, "IO exception while checking " + f, e);
				}
			else
				scan(f);
	}

	private String checkFile(File f) throws IOException {
		String ifid = Babel.examine(f);
		
		if (ifid == null)
			return null;
		
		Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, ifid);
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);
		
		ContentValues cv = new ContentValues();
		cv.put(Games.PATH, f.getAbsolutePath());
		
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

	public void startCheckingFile(final File file) {
		new Thread() {
			@Override
			public void run() {
				try {
					String ifid;
					if ((ifid = checkFile(file)) != null) {
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
				/* seems like overkill to scan the whole sdcard...
					scan(Environment.getExternalStorageDirectory());
				*/
				scan(HunkyPunk.DIRECTORY);
				Message.obtain(mHandler, DONE).sendToTarget();
			}
		}.start();
	}
}
