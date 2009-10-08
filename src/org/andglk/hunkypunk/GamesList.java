package org.andglk.hunkypunk;

import org.andglk.hunkypunk.HunkyPunk.Games;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class GamesList extends ListActivity {
	private static final String[] PROJECTION = {
		Games._ID,
		Games.IFID,
		Games.TITLE,
		Games.LAST_LOCATION
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		Cursor cursor = managedQuery(Games.CONTENT_URI, PROJECTION, null, null, null);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor,
				new String[] { Games.TITLE }, new int[] { android.R.id.text1 });
		setListAdapter(adapter);
	}
}
