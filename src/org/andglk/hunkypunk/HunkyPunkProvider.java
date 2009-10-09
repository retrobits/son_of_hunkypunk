package org.andglk.hunkypunk;

import java.util.HashMap;

import org.andglk.hunkypunk.HunkyPunk.Games;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class HunkyPunkProvider extends ContentProvider {
	private static final String DATABASE_NAME = "hunky_punk.db";
	private static final int DATABASE_VERSION = 2;
	private static final String GAMES_TABLE_NAME = "games";
	
	private static HashMap<String, String> sGamesProjectionMap;
	
	private static final int GAMES = 1;
	private static final int GAME_ID = 2;
	private static final int GAME_IFID = 3;
	
	private static final UriMatcher sUriMatcher;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(
				"CREATE TABLE " + GAMES_TABLE_NAME + " ("
					+ Games._ID + " INTEGER PRIMARY KEY, "
					+ Games.IFID + " TEXT UNIQUE NOT NULL, "
					+ Games.FILENAME + " TEXT, "
					+ Games.TITLE + " TEXT, "
					+ Games.LOOKED_UP
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// no public version yet, so no sense in transforming data
			db.execSQL("DROP TABLE " + GAMES_TABLE_NAME + ";");
			onCreate(db);
		}
	}
	
	private DatabaseHelper mOpenHelper;
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db;
		db = mOpenHelper.getReadableDatabase();
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		qb.setTables(GAMES_TABLE_NAME);
		qb.setProjectionMap(sGamesProjectionMap);
		switch (sUriMatcher.match(uri)) {
		case GAMES:
			break;
		case GAME_ID:
			qb.appendWhere(Games._ID + " = " + Long.toString(ContentUris.parseId(uri)));
			break;
		case GAME_IFID:
			qb.appendWhere(Games.IFID + " = ");
			qb.appendWhereEscapeString(uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		
		String orderBy;
		if (TextUtils.isEmpty(sortOrder))
			orderBy = HunkyPunk.Games.DEFAULT_SORT_ORDER;
		else
			orderBy = sortOrder;
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case GAMES:
			return Games.CONTENT_TYPE;

		case GAME_ID:
		case GAME_IFID:
			return Games.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (sUriMatcher.match(uri) != GAMES)
			throw new IllegalArgumentException("Unknown URI " + uri);

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(GAMES_TABLE_NAME, Games.IFID, values);
		if (rowId > 0) {
			Uri gameUri = ContentUris.withAppendedId(Games.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(gameUri, null);
			return gameUri;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = 0;
		StringBuilder sb = null;
		switch (sUriMatcher.match(uri)) {
		case GAMES:
			count = db.delete(GAMES_TABLE_NAME, selection, selectionArgs);
			break;
		case GAME_ID:
			sb = new StringBuilder(Games._ID + " = " + Long.toString(ContentUris.parseId(uri)));
			break;
		case GAME_IFID:
			sb = new StringBuilder(Games.IFID + " = \'" + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()) + "\'");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		if (sb != null) {
			if (selection != null)
				sb.append(" AND (" + selection + ")");
			count = db.delete(GAMES_TABLE_NAME, sb.toString(), selectionArgs);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = 0;
		StringBuilder sb = null;
		switch (sUriMatcher.match(uri)) {
		case GAMES:
			count = db.update(GAMES_TABLE_NAME, values, selection, selectionArgs);
			break;
		case GAME_ID:
			sb = new StringBuilder(Games._ID + " = " + Long.toString(ContentUris.parseId(uri)));
			break;
		case GAME_IFID:
			sb = new StringBuilder(Games.IFID + " = " + DatabaseUtils.sqlEscapeString(uri.getLastPathSegment()));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		if (sb != null) {
			if (selection != null)
				sb.append(" AND (" + selection + ")");
			count = db.update(GAMES_TABLE_NAME, values, sb.toString(), selectionArgs);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(HunkyPunk.AUTHORITY, "games", GAMES);
		sUriMatcher.addURI(HunkyPunk.AUTHORITY, "games/#", GAME_ID);
		sUriMatcher.addURI(HunkyPunk.AUTHORITY, "games/*", GAME_IFID);
		
		sGamesProjectionMap = new HashMap<String, String>();
		sGamesProjectionMap.put(Games._ID, Games._ID);
		sGamesProjectionMap.put(Games.IFID, Games.IFID);
		sGamesProjectionMap.put(Games.FILENAME, Games.FILENAME);
		sGamesProjectionMap.put(Games.TITLE, Games.TITLE);
	}
}
