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
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;

import org.andglk.babel.Babel;
import org.andglk.glk.Utils;
import org.andglk.hunkypunk.HunkyPunk.Games;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;

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
	private DatabaseHelper mOpenHelper;
	
	private StorageManager(Context context) {
		mContentResolver = context.getContentResolver();
		mOpenHelper = new DatabaseHelper(context);
	}
	
	private static StorageManager sInstance;
	
	public static StorageManager getInstance(Context context) {
		if (sInstance == null) sInstance = new StorageManager(context);
		
		assert(sInstance.mContentResolver == context.getContentResolver());
		return sInstance;
	}

	public void setHandler(Handler h) {
		mHandler = h;
	}

	public String gameInstalledFilePath(File f) {
		String ifid = null;
		String path = null;

		try {
			ifid = Babel.examine(f);
		}catch(Exception e){}
		
		if (ifid == null)
			return path;
		
		Uri uri = Uri.withAppendedPath(Games.CONTENT_URI, ifid);
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);		
		if (query != null || query.getCount() == 1)
			if (query.moveToNext())
				path = query.getString(PATH);			
		return path;
	}
	
	public void checkExisting() {
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
		
		if (query != null && query.getCount() == 1) 
			mContentResolver.update(uri, cv, null, null);
		
		query.close();	
	}

	public void deleteGame(String ifid) {
		String path = null;
		Uri uri = HunkyPunk.Games.uriOfIfid(ifid);
		//Log.d("StorageManager",uri.toString());
		Cursor query = mContentResolver.query(uri, PROJECTION, null, null, null);		
		if (query != null || query.getCount() == 1)
			if (query.moveToNext())
				path = query.getString(PATH);			

		if (path != null){
			File fp = new File(path);
			if (fp.exists()) fp.delete();
		}
		query.close();

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.execSQL("delete from games where ifid = '"+ifid+"'");
	}

	private String checkFile(File f) throws IOException {
		String ifid = Babel.examine(f);		
		if (ifid == null) return null;

		return checkFile(f, ifid);
	}
	private String checkFile(File f, String ifid) throws IOException {
		if (ifid == null) ifid = Babel.examine(f);
		if (ifid == null) return null;
		
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
				scan(Paths.ifDirectory());
				Message.obtain(mHandler, DONE).sendToTarget();
			}
		}.start();
	}

	public static String unknownContent = "IFID_";
	public void startInstall(final Uri game, final String scheme) {
		new Thread() {
			@Override
			public void run() {

				File fgame = null;
				File ftemp = null;
				String ifid = null;

				try {
					if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
						ftemp = File.createTempFile(unknownContent,null,Paths.tempDirectory());
						InputStream in = mContentResolver.openInputStream(game);
						OutputStream out = new FileOutputStream(ftemp);
						Utils.copyStream(in, out);
						in.close(); out.close();

						ifid = Babel.examine(ftemp);

						//TODO: obtain terp from Babel
						String ext = "zcode";					
						if (ifid.indexOf("TADS")==0) ext="gam";

						fgame = new File(Paths.tempDirectory().getAbsolutePath() 
												 + "/" + unknownContent + ifid + "." + ext);
						ftemp.renameTo(fgame);
					}
					else {
						fgame = new File(game.getPath());
					}

					String src = fgame.getAbsolutePath();
					String dst = Paths.ifDirectory().getAbsolutePath()+"/"+fgame.getName();		
					String installedPath = gameInstalledFilePath(fgame);

					if (installedPath == null || !(new File(installedPath).exists())) {
						if (!dst.equals(src)) {
							InputStream in = new FileInputStream(src);
							OutputStream out = new FileOutputStream(dst);

							Utils.copyStream(in,out);
							in.close(); out.close();
						}
					}
					else {
						dst = installedPath;
					}

					if ((ifid = checkFile(new File(dst), ifid)) != null) {
						Message.obtain(mHandler, INSTALLED, ifid).sendToTarget();
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

				Message.obtain(mHandler, INSTALL_FAILED).sendToTarget();
			}
		}.start();
	}
}
