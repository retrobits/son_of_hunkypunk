package org.andglk.hunkypunk;

import android.net.Uri;
import android.provider.BaseColumns;

public final class HunkyPunk {
	public static final String AUTHORITY = "org.andglk.hunkypunk.HunkyPunk";
	private HunkyPunk() {}
	
	public static final class Games implements BaseColumns {
		private Games() {}
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/games");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.andglk.game";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.andglk.game";
		public static final String DEFAULT_SORT_ORDER = "title ASC";
		public static final String TITLE = "title";
		public static final String IFID = "ifid";
		public static final String FILENAME = "filename";
	}
}
