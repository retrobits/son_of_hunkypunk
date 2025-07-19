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

package org.andglkmod.hunkypunk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andglkmod.babel.Babel;
import org.andglkmod.glk.Utils;
import org.andglkmod.hunkypunk.HunkyPunk.Games;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;

public class StorageManager {
	public static final int DONE = 0;
	public static final int INSTALLED = 1;
	public static final int INSTALL_FAILED = 2;

	private static final String TAG = "hunkypunk.MediaScanner";
	private static final String[] PROJECTION = { Games._ID, Games.PATH };
	private static final String[] PROJECTION2 = {Games._ID, Games.PATH,Games.IFID, Games.TITLE};

	private static final String[] PROJECTION3 = {Games.IFID, Games.TITLE, Games.PATH};

	private static final int _ID = 0;
	private static final int PATH = 1;

	private final ContentResolver mContentResolver;
	private Handler mHandler;
	private DatabaseHelper mOpenHelper;
	private Context mContext;
	
	// Modern thread pool for background operations
	private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool();
	
	private StorageManager(Context context) {
		mContext = context;
		mContentResolver = mContext.getContentResolver();
		mOpenHelper = new DatabaseHelper(mContext);
	}
	
	private static StorageManager sInstance;
	
	public static StorageManager getInstance(Context context) {
		try {
			if (sInstance == null) {
				sInstance = new StorageManager(context.getApplicationContext());
			}
		} catch (AssertionError ae) {
			// Ignore assertion and create instance
			sInstance = new StorageManager(context.getApplicationContext());
			Log.w(TAG, "AssertionError in getInstance, created new instance", ae);
		}
		return sInstance;
	}

	public void setHandler(Handler h) {
		mHandler = h;
	}

	public String gameInstalledFilePath(File f) {
		String ifid = null;
		String path = null;

		try {
			if (f != null && f.exists() && f.canRead() && f.length() > 0) {
				ifid = Babel.examine(f);
			}
		} catch (Exception e) {
			Log.w(TAG, "Failed to examine file: " + f, e);
		} catch (Error e) {
			Log.e(TAG, "Native error examining file: " + f, e);
		}
		
		if (ifid == null)
			return path;
		
		Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, ifid);
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);		
		if (query != null) {
			try {
				if (query.getCount() == 1 && query.moveToNext()) {
					path = query.getString(PATH);
				}
			} finally {
				query.close();
			}
		}
		return path;
	}
	
	public void checkExisting() {
		Cursor c = mContentResolver.query(Games.CONTENT_URI, PROJECTION, Games.PATH + " IS NOT NULL", null, null);
		if (c == null) {
			Log.w(TAG, "checkExisting: cursor is null");
			return;
		}
		
		try {
			while (c.moveToNext()) {
				if (!new File(c.getString(PATH)).exists()) {
					ContentValues cv = new ContentValues();
					cv.putNull(Games.PATH);
					mContentResolver.update(ContentUris.withAppendedId(Games.CONTENT_URI, c.getLong(_ID)), cv, null, null);
				}
			}
		} finally {
			c.close();
		}
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
					
					String g = f.getName().toLowerCase();
					if (
						/* zcode: frotz, nitfol */
						g.matches(".*\\.z[1-9]$")
						|| g.matches(".*\\.dat$")
						|| g.matches(".*\\.zcode$")
						|| g.matches(".*\\.zblorb$")
						|| g.matches(".*\\.zlb$")

						/* tads */
						|| g.matches(".*\\.gam$")
						|| g.matches(".*\\.t2$")
						|| g.matches(".*\\.t3$")

						/* glulx */
						/*
						|| g.matches(".*\\.blorb$")
						|| g.matches(".*\\.gblorb$")
						|| g.matches(".*\\.blb$")
						|| g.matches(".*\\.glb$")
						|| g.matches(".*\\.ulx$")
						*/
						)
						checkFile(f);
				} catch (IOException e) {
					Log.w(TAG, "IO exception while checking " + f, e);
				}
			else
				scan(f);
	}

	public void updateGame(String ifid, String title) {
		Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, ifid);
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);
		
		ContentValues cv = new ContentValues();
		cv.put(Games.TITLE, title);
		
		if (query != null) {
			try {
				if (query.getCount() == 1) {
					mContentResolver.update(uri, cv, null, null);
				}
			} finally {
				query.close();
			}
		}
	}

	public void deleteGame(String ifid) {
		String path = null;
		Uri uri = HunkyPunk.Games.uriOfIfid(ifid);
		//Log.d("StorageManager",uri.toString());
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);
		
		if (query != null) {
			try {
				if (query.getCount() == 1 && query.moveToNext()) {
					path = query.getString(PATH);
				}
			} finally {
				query.close();
			}
		}

		if (path != null) {
			File fp = new File(path);
			if (fp.exists()) fp.delete();
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		try {
			db.execSQL("delete from games where ifid = ?", new String[]{ifid});
		} finally {
			db.close();
		}
	}

	private String checkFile(File f) throws IOException {
		if (f == null || !f.exists() || !f.canRead()) {
			return null;
		}
		
		String ifid = null;
		try {
			ifid = Babel.examine(f);
		} catch (Exception e) {
			Log.w(TAG, "Failed to examine file: " + f, e);
			return null;
		} catch (Error e) {
			Log.e(TAG, "Native error examining file: " + f, e);
			return null;
		}
		
		if (ifid == null) return null;

		return checkFile(f, ifid);
	}
	private String checkFile(File f, String ifid) throws IOException {
		if (f == null || !f.exists() || !f.canRead()) {
			return null;
		}
		
		if (ifid == null) {
			try {
				ifid = Babel.examine(f);
			} catch (Exception e) {
				Log.w(TAG, "Failed to examine file: " + f, e);
				return null;
			} catch (Error e) {
				Log.e(TAG, "Native error examining file: " + f, e);
				return null;
			}
		}
		
		if (ifid == null) return null;
		
		Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, ifid);
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);
		
		ContentValues cv = new ContentValues();
		cv.put(Games.PATH, f.getAbsolutePath());
		
		if (query == null || query.getCount() != 1) {
			cv.put(Games.IFID, ifid);
			final String fname = f.getName();
			int lastDot = fname.lastIndexOf('.');
			if (lastDot > 0) {
				cv.put(Games.TITLE, fname.substring(0, lastDot));
			} else {
				cv.put(Games.TITLE, fname);
			}
			mContentResolver.insert(Games.CONTENT_URI, cv);
		} else {
			mContentResolver.update(uri, cv, null, null);
		}
		
		if (query != null) {
			query.close();
		}
		return ifid;
	}

	public void startCheckingFile(final File file) {
		backgroundExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					String ifid;
					if ((ifid = checkFile(file)) != null) {
						if (mHandler != null) {
							Message.obtain(mHandler, INSTALLED, ifid).sendToTarget();
						}
						return;
					}
				} catch (IOException e) {
					Log.w(TAG, "Error checking file: " + file, e);
				}
				
				if (mHandler != null) {
					Message.obtain(mHandler, INSTALL_FAILED).sendToTarget();
				}
			}
		});
	}

	public boolean alreadyScanning = false;
	public void startScan() {
		alreadyScanning = true;
		backgroundExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					scan(Paths.ifDirectory(mContext));
					if (mHandler != null) {
						Message.obtain(mHandler, DONE).sendToTarget();
					}
				} catch (Exception e) {
					Log.e(TAG, "Error during scan", e);
				} finally {
					alreadyScanning = false;
				}
			}
		});
	}

	public static String unknownContent = "IFID_";
	public void startInstall(final Uri game, final String scheme) {
		backgroundExecutor.execute(new Runnable() {
			@Override
			public void run() {

				File fgame = null;
				File ftemp = null;
				String ifid = null;

				try {
					if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
						ftemp = File.createTempFile(unknownContent,null,Paths.tempDirectory(mContext));
						
						try (InputStream in = mContentResolver.openInputStream(game);
						     FileOutputStream out = new FileOutputStream(ftemp)) {
							
							if (in == null) {
								Log.e(TAG, "Could not open input stream for: " + game);
								if (mHandler != null) {
									Message.obtain(mHandler, INSTALL_FAILED).sendToTarget();
								}
								return;
							}
							
							Utils.copyStream(in, out);
						}

						try {
							ifid = Babel.examine(ftemp);
						} catch (Exception e) {
							Log.w(TAG, "Failed to examine temp file: " + ftemp, e);
							ifid = null;
						} catch (Error e) {
							Log.e(TAG, "Native error examining temp file: " + ftemp, e);
							ifid = null;
						}

						//TODO: obtain terp from Babel
						String ext = "zcode";					
						if (ifid != null && ifid.indexOf("TADS")==0) ext="gam";

						fgame = new File(Paths.tempDirectory(mContext).getAbsolutePath()
												 + "/" + unknownContent + ifid + "." + ext);
						ftemp.renameTo(fgame);
					}
					else {
						fgame = new File(game.getPath());
					}

					String src = fgame.getAbsolutePath();
					String dst = Paths.ifDirectory(mContext).getAbsolutePath()+"/"+fgame.getName();
					String installedPath = gameInstalledFilePath(fgame);

					if (installedPath == null || !(new File(installedPath).exists())) {
						if (!dst.equals(src)) {
							try (FileInputStream in = new FileInputStream(src);
							     FileOutputStream out = new FileOutputStream(dst)) {
								Utils.copyStream(in, out);
							}
						}
					}
					else {
						dst = installedPath;
					}

					if ((ifid = checkFile(new File(dst), ifid)) != null) {
						if (mHandler != null) {
							Message.obtain(mHandler, INSTALLED, ifid).sendToTarget();
						}
						return;
					}
				} catch (Exception e){
					Log.i("HunkyPunk/StorageManager",e.toString());
				} finally {
					if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
						if (ftemp != null && ftemp.exists()) ftemp.delete();
						if (fgame != null && fgame.exists()) fgame.delete();
					}
				}

				if (mHandler != null) {
					Message.obtain(mHandler, INSTALL_FAILED).sendToTarget();
				}
			}
		});
	}
	//added for Swipe
	//creates an array with pathes of all games
	public File[] getFiles(File dir) {
		return (dir.listFiles());
	}

	public String[] getIfIdArray(File dir) {
		String path = null;
		File[] x = dir.listFiles();
		String[] gameArray = new String[getFiles(dir).length - 1];
		String[] ifIdArray = new String[gameArray.length];
		for (int i = 0; i < (gameArray.length); i++) {
			try {
				if (x[i] != null && x[i].exists() && x[i].canRead()) {
					path = Babel.examine(x[i]);
				}
			} catch (Exception e) {
				Log.w(TAG, "Failed to examine file: " + x[i], e);
				continue;
			} catch (Error e) {
				Log.e(TAG, "Native error examining file: " + x[i], e);
				continue;
			}

			if (path == null) continue;

			Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, path);

			Cursor query = mContentResolver.query(uri, PROJECTION2, null, null, null);

			if (query != null) {
				try {
					if (query.moveToFirst()) {
						gameArray[i] = query.getString(3);
					}
				} finally {
					query.close();
				}
			}
		}
		Arrays.sort(gameArray);
		for (int i = 0; i < (ifIdArray.length); i++) {
			String gameTitle = gameArray[i];

			for (int j = 0; j < (ifIdArray.length); j++) {
				try {
					if (x[j] != null && x[j].exists() && x[j].canRead()) {
						path = Babel.examine(x[j]);
					}
				} catch (Exception e) {
					Log.w(TAG, "Failed to examine file: " + x[j], e);
					continue;
				} catch (Error e) {
					Log.e(TAG, "Native error examining file: " + x[j], e);
					continue;
				}
				
				if (path == null) continue;
				
				Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, path);
				Cursor query = mContentResolver.query(uri, PROJECTION3, null, null, null);
				if (query != null) {
					try {
						if (query.moveToFirst()) {
							if (query.getString(1).equals(gameTitle))
								ifIdArray[i] = query.getString(0);
						}
					} finally {
						query.close();
					}
				}
			}


		}
		return ifIdArray;
	}
	
	/**
	 * Shutdown the background executor service. Should be called when the application is terminating.
	 * This is a modern best practice to properly clean up background threads.
	 */
	public void shutdown() {
		if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
			backgroundExecutor.shutdown();
		}
	}
}