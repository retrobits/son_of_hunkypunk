package org.andglk.hunkypunk;

import org.andglk.hunkypunk.HunkyPunk.Games;
import org.andglk.ifdb.IFDb;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class GamesList extends ListActivity {
	private static final String[] PROJECTION = {
		Games._ID,
		Games.IFID,
		Games.TITLE,
		Games.AUTHOR,
		Games.FILENAME
	};

	private StorageManager mScanner;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case StorageManager.DONE:
				setProgressBarIndeterminateVisibility(false);
				startLookup();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		mScanner = StorageManager.getInstance(getContentResolver());
		mScanner.setHandler(mHandler);
		mScanner.checkExisting();

		Cursor cursor = managedQuery(Games.CONTENT_URI, PROJECTION, Games.FILENAME + " IS NOT NULL", null, null);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
				new String[] { Games.TITLE, Games.AUTHOR }, new int[] { android.R.id.text1, android.R.id.text2 });
		
		setListAdapter(adapter);

		startScan();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i = new Intent(Intent.ACTION_VIEW, Games.uriOfId(id), this, GameDetails.class);
		startActivity(i);
	}

	private void startScan() {
		setProgressBarIndeterminateVisibility(true);
		
		mScanner.startScan();
	}

	private void startLookup() {
		IFDb ifdb = IFDb.getInstance(getContentResolver());
		ifdb.startLookup(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Toast.makeText(GamesList.this, R.string.ifdb_connection_error, Toast.LENGTH_LONG).show();
			}
		});
	}
}
