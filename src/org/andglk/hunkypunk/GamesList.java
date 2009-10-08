package org.andglk.hunkypunk;

import org.andglk.hunkypunk.HunkyPunk.Games;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.SimpleCursorAdapter;

public class GamesList extends ListActivity {
	private static final String[] PROJECTION = {
		Games._ID,
		Games.IFID,
		Games.TITLE,
		Games.FILENAME
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		Cursor cursor = managedQuery(Games.CONTENT_URI, PROJECTION, null, null, null);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
				new String[] { Games.TITLE, Games.IFID }, new int[] { android.R.id.text1, android.R.id.text2 });
		
		setListAdapter(adapter);
		
		startCardScan();
	}

	private void startCardScan() {
		setProgressBarIndeterminateVisibility(true);
		
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				setProgressBarIndeterminateVisibility(false);
			}
		};
		
		Message msg = Message.obtain(handler);
		MediaScanner scanner = new MediaScanner(getContentResolver());
		scanner.setMessage(msg);
		scanner.start();
	}
}
