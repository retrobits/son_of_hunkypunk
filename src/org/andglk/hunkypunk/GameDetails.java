package org.andglk.hunkypunk;

import org.andglk.R;
import org.andglk.hunkypunk.HunkyPunk.Games;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class GameDetails extends Activity {
	private static final String[] PROJECTION = { Games._ID, Games.IFID, Games.TITLE, Games.HEADLINE, Games.AUTHOR };
	private static final int IFID = 1;
	private static final int TITLE = 2;
	private static final int HEADLINE = 3;
	private static final int AUTHOR = 4;

	private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			showData();
		}
	};
	private Cursor mQuery;
	private TextView mTitle;
	private TextView mHeadline;
	private TextView mAuthor;
	private ImageView mCover;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.game_details_header);
		mTitle = (TextView) findViewById(R.id.title);
		mHeadline = (TextView) findViewById(R.id.headline);
		mAuthor = (TextView) findViewById(R.id.author);
		mCover = (ImageView) findViewById(R.id.cover);
		
		Uri game = getIntent().getData();
		mQuery = managedQuery(game, PROJECTION, null, null, null);
		
		if (mQuery != null) {
			mQuery.registerContentObserver(mContentObserver);
			showData();
		}
	}

	protected void showData() {
		mQuery.moveToFirst();
		
		mTitle.setText(mQuery.getString(TITLE));
		String string = mQuery.getString(HEADLINE);
		mHeadline.setText(string);
		mHeadline.setVisibility(string == null ? View.GONE : View.VISIBLE);
		mAuthor.setText(getString(R.string.by_author, mQuery.getString(AUTHOR)));
		
		// Uri.fromFile doesn't work for some reason
		mCover.setImageURI(Uri.parse(HunkyPunk.getCover(mQuery.getString(IFID)).getAbsolutePath()));
	}
}
