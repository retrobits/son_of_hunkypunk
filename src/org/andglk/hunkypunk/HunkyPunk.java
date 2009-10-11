package org.andglk.hunkypunk;

import java.io.File;

import android.net.Uri;
import android.provider.BaseColumns;

public final class HunkyPunk {
	public static final String AUTHORITY = "org.andglk.hunkypunk.HunkyPunk";
	public static final File DIRECTORY = new File("/sdcard/Interactive Fiction");
	public static final Uri DIRECTORY_URI = Uri.fromFile(DIRECTORY);
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

		public static final String FILENAME = "filename";
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
}
