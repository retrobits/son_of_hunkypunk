package org.andglk.ifdb;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.andglk.hunkypunk.HunkyPunk.Games;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;

public class IFDb {
	public class MalformedIFIDException extends Exception {
		private static final long serialVersionUID = -4210235667358229476L;

		public MalformedIFIDException(String ifid) {
			super(ifid);
		}
	}

	private static final String[] PROJECTION = { Games._ID, Games.IFID };
	private static final int IFID = 1;

	private static final String BASE_URL = "http://ifdb.tads.org/search?searchfor=ifid:";

	private static final String TAG = "IFDb";

	private static final Pattern TITLE_PATTERN = Pattern.compile("<h1>(.*)</h1>");
	private static final int TITLE = 1;
	
	private static IFDb sInstance;
	private final ContentResolver mContentResolver;

	private IFDb(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
		
	}

	public static IFDb getInstance(ContentResolver contentResolver) {
		if (sInstance == null)
			sInstance = new IFDb(contentResolver);
		else
			assert(contentResolver.equals(sInstance.mContentResolver));

		return sInstance;
	}

	public void startLookup(final Handler errorHandler) {
		new Thread() {
			@Override
			public void run() {
				try {
					lookupGames();
				} catch (IOException e) {
					Message.obtain(errorHandler).sendToTarget();
				}
			}
		}.start();
	}

	protected void lookupGames() throws IOException {
		Cursor query = mContentResolver.query(Games.CONTENT_URI, PROJECTION, 
				Games.LOOKED_UP + " IS NULL", null, null);
		
		while (query.moveToNext())
			try {
				lookup(query.getString(IFID));
			} catch (MalformedIFIDException e) {
				Log.e(TAG, "malformed ifid", e);
			} catch (IOException e) {
				Log.e(TAG, "can't connect, giving up on others", e);
				throw e;
			}
	}

	private void lookup(String ifid) throws MalformedIFIDException, IOException {
		URL url;
		try {
			url = urlOfIfid(ifid);
		} catch (MalformedURLException e) {
			throw new MalformedIFIDException(ifid);
		}
		
		HttpURLConnection.setFollowRedirects(false);
		URLConnection connection = url.openConnection();
		connection.connect();
		
		String location;
		if ((location = connection.getHeaderField("Location")) == null)
			return;
		
		Scanner scanner = new Scanner(new URL(url, location).openStream());
		
		while (scanner.findInLine(TITLE_PATTERN) == null)
			scanner.nextLine();
		
		ContentValues cv = new ContentValues();
		cv.put(Games.TITLE, scanner.match().group(TITLE));
		cv.put(Games.LOOKED_UP, true);
		mContentResolver.update(Games.uriOfIfid(ifid), cv, null, null);
	}

	private static URL urlOfIfid(String ifid) throws MalformedURLException {
		return new URL(BASE_URL + ifid);
	}
}
