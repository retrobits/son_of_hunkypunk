package org.andglk.hunkypunk;

import java.io.File;

import org.andglk.R;
import org.andglk.hunkypunk.HunkyPunk.Games;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class GamesList extends ListActivity {
	private static final String[] PROJECTION = {
		Games._ID,
		Games.IFID,
		Games.TITLE,
		Games.FILENAME
	};
	private MediaScanner mScanner;
	private ProgressDialog mProgressDialog;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MediaScanner.DONE:
				setProgressBarIndeterminateVisibility(false);
				break;
			case MediaScanner.INSTALLED:
				mProgressDialog.dismiss();
				Toast.makeText(GamesList.this, R.string.install_success, Toast.LENGTH_SHORT).show();
				break;
			case MediaScanner.INSTALL_FAILED:
				mProgressDialog.dismiss();
				Toast.makeText(GamesList.this, R.string.install_failure, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		mScanner = new MediaScanner(getContentResolver());
		mScanner.setHandler(mHandler);
		mScanner.checkExisting();

		Cursor cursor = managedQuery(Games.CONTENT_URI, PROJECTION, Games.FILENAME + " IS NOT NULL", null, null);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
				new String[] { Games.TITLE, Games.IFID }, new int[] { android.R.id.text1, android.R.id.text2 });
		
		setListAdapter(adapter);

		Uri uri = getIntent().getData();
		if (uri != null && uri.getScheme().equals(ContentResolver.SCHEME_FILE))
			tryToInstall(uri);
		else
			startScan();
	}

	private void tryToInstall(Uri uri) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.examining_file, uri.getLastPathSegment()));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		
		mScanner.startCheckingFile(new File(uri.getPath()));
	}

	private void startScan() {
		setProgressBarIndeterminateVisibility(true);
		
		mScanner.start();
	}
}
