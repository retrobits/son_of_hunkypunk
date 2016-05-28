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

import java.io.File;

import android.net.Uri;
import android.provider.BaseColumns;

public final class HunkyPunk {

	public static final String AUTHORITY = "org.andglk.hunkypunk.HunkyPunk";
	public static final Uri IF_DIRECTORY_URI = Uri.fromFile(Paths.ifDirectory());

	private HunkyPunk() {}
	
	public static final class Games implements BaseColumns {
		private Games() {}

		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/games");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.andglk.game";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.andglk.game";
		public static final String DEFAULT_SORT_ORDER = "lower(title) ASC";
		
		public static final String IFID = "ifid";

		public static final String TITLE = "title";
		public static final String AUTHOR = "author";

		public static final String PATH = "path";
		public static final String LOOKED_UP = "looked_up";
		
		public static final String LANGUAGE = "language";
		public static final String HEADLINE = "headline";
		public static final String FIRSTPUBLISHED = "first_published";
		public static final String GENRE = "genre";
		public static final String GROUP = "collection";
		public static final String DESCRIPTION = "description";
		public static final String SERIES = "series";
		public static final String SERIESNUMBER = "seriesnumber";
		public static final String FORGIVENESS = "forgiveness";
		
		public static Uri uriOfIfid(String ifid) {
			return CONTENT_URI.buildUpon().appendPath(ifid).build();
		}
		public static Uri uriOfId(long id) {
			return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
		}
	}

	public static File getCover(String ifid) {
		return new File(Paths.coverDirectory(), ifid);
	}
	
	public static File getGameDataDir(Uri uri, String ifid) {
		File fGame = new File(uri.getPath());

		File fData = Paths.dataDirectory();

		//search
		String dirName = fGame.getName()+"."+ifid;
		GameDataDirFilter filter = new GameDataDirFilter(ifid);		
		File[] fs = fData.listFiles(filter);
		if (fs != null && fs.length>0)
			dirName = fs[0].getName();

		File f = new File(fData, dirName);
		if (!f.exists()) f.mkdir();
		
		return f;
	}
}
