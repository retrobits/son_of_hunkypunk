package org.andglk.hunkypunk;

import java.io.File;

import org.andglk.Nitfol;
import org.andglk.R;
import org.andglk.hunkypunk.HunkyPunk.Games;
import org.andglk.ifdb.IFDb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameDetails extends Activity implements OnClickListener {
	private static final String[] PROJECTION = { 
		Games._ID, Games.IFID, Games.TITLE, Games.HEADLINE, Games.AUTHOR, Games.DESCRIPTION,
		Games.FIRSTPUBLISHED, Games.GENRE, Games.GROUP, Games.SERIES, Games.SERIESNUMBER, Games.FORGIVENESS,
		Games.LANGUAGE, Games.LOOKED_UP, Games.FILENAME
	};
	private static final int IFID = 1;
	private static final int TITLE = 2;
	private static final int HEADLINE = 3;
	private static final int AUTHOR = 4;
	private static final int DESCRIPTION = 5;
	private static final int FIRSTPUBLISHED = 6;
	private static final int GENRE = 7;
	private static final int GROUP = 8;
	private static final int SERIES = 9;
	private static final int SERIESNUMBER = 10;
	private static final int FORGIVENESS = 11;
	private static final int LANGUAGE = 12;
	private static final int LOOKED_UP = 13;
	private static final int FILENAME = 14;

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
	private TextView mDescription;
	private View mDescriptionLayout;
	private TextView mDetails;
	private ProgressDialog mProgressDialog;
	protected Uri mGameUri;
	private Handler mLookupHandler = new Handler() {
		public void handleMessage(Message msg) {
			setProgressBarIndeterminateVisibility(false);
			switch (msg.what) {
			case IFDb.FAILURE:
				Toast.makeText(GameDetails.this, R.string.lookup_failure, Toast.LENGTH_SHORT).show();
				break;
			case IFDb.SUCCESS:
				mQuery.requery();
			}
		}
	};
	protected String mGameIfid;
	private Handler mInstallHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case StorageManager.INSTALLED:
				mProgressDialog.dismiss();
				mGameIfid = (String) msg.obj;
				show(HunkyPunk.Games.uriOfIfid(mGameIfid));
				break;
			case StorageManager.INSTALL_FAILED:
				mProgressDialog.dismiss();
				Toast.makeText(GameDetails.this, R.string.install_failure, Toast.LENGTH_SHORT).show();
				finish();
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Uri game = getIntent().getData();
		if (game.getScheme().equals(ContentResolver.SCHEME_FILE))
			install(game);
		else
			show(game);
	}

	private void install(Uri game) {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.examining_file, game.getLastPathSegment()));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
		
		StorageManager mediaScanner = StorageManager.getInstance(getContentResolver());
		mediaScanner.setHandler(mInstallHandler);
		mediaScanner.startCheckingFile(new File(game.getPath()));
	}

	private void show(Uri game) {
		setContentView(R.layout.game_details);
		mTitle = (TextView) findViewById(R.id.title);
		mHeadline = (TextView) findViewById(R.id.headline);
		mAuthor = (TextView) findViewById(R.id.author);
		mDescription = (TextView) findViewById(R.id.description);
		mCover = (ImageView) findViewById(R.id.cover);
		mDescriptionLayout = findViewById(R.id.description_layout);
		mDetails = (TextView) findViewById(R.id.details);
		
		((Button) findViewById(R.id.open)).setOnClickListener(this);
		((Button) findViewById(R.id.remove)).setOnClickListener(this);
		
		mQuery = managedQuery(game, PROJECTION, null, null, null);
		mQuery.registerContentObserver(mContentObserver);
		showData();
	}

	protected void showData() {
		mQuery.moveToFirst();
		
		if (mQuery.isNull(LOOKED_UP)) {
			Toast.makeText(this, R.string.looking_up, Toast.LENGTH_SHORT).show();
			setProgressBarIndeterminateVisibility(true);
			IFDb.getInstance(getContentResolver()).startLookup(mGameIfid, mLookupHandler);
		}
		
		mTitle.setText(mQuery.getString(TITLE));
		String string = mQuery.getString(HEADLINE);
		mHeadline.setText(string);
		mHeadline.setVisibility(string == null ? View.GONE : View.VISIBLE);
		
		string = mQuery.getString(AUTHOR);
		mAuthor.setText(getString(R.string.by_author, string));
		mAuthor.setVisibility(string == null ? View.GONE : View.VISIBLE);
		
		string = mQuery.getString(DESCRIPTION);
		mDescription.setText(string);
		mDescriptionLayout.setVisibility(string == null ? View.GONE : View.VISIBLE);
		
		StringBuilder sb = new StringBuilder();
		if ((string = mQuery.getString(FIRSTPUBLISHED)) != null) {
			sb.append(getString(R.string.first_published_s, string));
			sb.append('\n');
		}
		if ((string = mQuery.getString(GENRE)) != null) {
			sb.append(getString(R.string.genre_s, string));
			sb.append('\n');
		}
		if ((string = mQuery.getString(GROUP)) != null) {
			sb.append(getString(R.string.group_s, string));
			sb.append('\n');
		}
		if ((string = mQuery.getString(SERIES)) != null) {
			sb.append(getString(R.string.series_s, string));
			sb.append('\n');
		}
		if ((string = mQuery.getString(SERIESNUMBER)) != null) {
			sb.append(getString(R.string.seriesnumber_s, string));
			sb.append('\n');
		}
		if ((string = mQuery.getString(FORGIVENESS)) != null) {
			sb.append(getString(R.string.forgiveness_s, string));
			sb.append('\n');
		}
		if ((string = mQuery.getString(LANGUAGE)) != null) {
			sb.append(getString(R.string.language_s, string));
			sb.append('\n');
		}
		
		final int len = sb.length(); 
		if (len != 0)
			sb.replace(len - 1, len, ""); // remove trailing newline
		
		mDetails.setText(sb);
		
		// Uri.fromFile doesn't work for some reason
		mCover.setImageURI(Uri.parse(HunkyPunk.getCover(mQuery.getString(IFID)).getAbsolutePath()));
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.open:
			Intent intent = new Intent(Intent.ACTION_VIEW, 
					Uri.withAppendedPath(HunkyPunk.DIRECTORY_URI, mQuery.getString(FILENAME)), this, Nitfol.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		case R.id.remove:
			// TODO
		}
	}
}
