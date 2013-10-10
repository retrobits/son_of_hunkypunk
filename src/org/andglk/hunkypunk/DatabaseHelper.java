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

package org.andglk.hunkypunk;

import org.andglk.hunkypunk.HunkyPunk.Games;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "hunky_punk.db";
	private static final int DATABASE_VERSION = 2;

	public static final String GAMES_TABLE_NAME = "games";	
	public static final int GAMES = 1;
	public static final int GAME_ID = 2;
	public static final int GAME_IFID = 3;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final String query =
			"CREATE TABLE " + GAMES_TABLE_NAME + " ("
			+ Games._ID + " INTEGER PRIMARY KEY, "
			+ Games.IFID + " TEXT UNIQUE NOT NULL, "

			+ Games.TITLE + " TEXT, "
			+ Games.AUTHOR + " TEXT, "
			+ Games.LANGUAGE + " TEXT, "
			+ Games.HEADLINE + " TEXT, "
			+ Games.FIRSTPUBLISHED + " TEXT, "
			+ Games.GENRE + " TEXT, "
			+ Games.GROUP + " TEXT, "
			+ Games.DESCRIPTION + " TEXT, "
			+ Games.SERIES + " TEXT, "
			+ Games.SERIESNUMBER + " INTEGER, "
			+ Games.FORGIVENESS + " TEXT, "

			+ Games.PATH + " TEXT, "
			+ Games.LOOKED_UP
			+ ");";
		db.execSQL(query);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1) {
			/* we don't need the old FILENAME column but there's nothing to be done */
			final String query = 
				"ALTER TABLE " + GAMES_TABLE_NAME + 
				" ADD COLUMN "
				+ Games.PATH + " TEXT;";
			db.execSQL(query);
			/* PATHs will get filled automatically on next scan */
		}
	}
}
	
