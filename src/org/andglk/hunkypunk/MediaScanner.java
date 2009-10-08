package org.andglk.hunkypunk;

import java.io.File;
import java.io.IOException;

import org.andglk.babel.Babel;
import org.andglk.hunkypunk.HunkyPunk.Games;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.util.Log;

public class MediaScanner extends Thread {
	private static final String TAG = "hunkypunk.MediaScanner";
	private static final String[] PROJECTION = { Games._ID, Games.FILENAME };
	private Message mMessage;
	private final ContentResolver mContentResolver;
	
	public MediaScanner(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	public void setMessage(Message msg) {
		mMessage = msg;
	}
	
	@Override
	public void run() {
		scan(new File("/sdcard/Interactive Fiction"));
		scan(new File("/sdcard/download"));
		mMessage.sendToTarget();
	}

	private void scan(File dir) {
		if (!dir.exists() || !dir.isDirectory())
			return;
		
		for (File f : dir.listFiles())
			try {
				checkFile(f);
			} catch (IOException e) {
				Log.w(TAG, "IO exception while checking " + f, e);
			}
	}

	private void checkFile(File f) throws IOException {
		String ifid = Babel.examine(f);
		
		if (ifid == null)
			return;
		
		Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, ifid);
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);
		
		ContentValues cv = new ContentValues();
		cv.put(Games.FILENAME, f.getName());
		
		if (query == null || query.getCount() == 0) {
			cv.put(Games.IFID, ifid);
			cv.put(Games.TITLE, f.getName());
			mContentResolver.insert(Games.CONTENT_URI, cv);
		} else
			mContentResolver.update(uri, cv, null, null);
	}
}
