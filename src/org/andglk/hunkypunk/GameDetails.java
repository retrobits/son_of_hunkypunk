/*
	Copyright © 2009-2010 Rafał Rzepecki <divided.mind@gmail.com>

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
import java.io.RandomAccessFile;

import org.andglk.hunkypunk.HunkyPunk.Games;
import org.andglk.hunkypunk.R.id;
import org.andglk.ifdb.IFDb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameDetails extends Activity implements OnClickListener {
    private static final String TAG = "hunkypunk.GameDetails";

	private static final String[] PROJECTION = { 
		Games._ID, Games.IFID, Games.TITLE, Games.HEADLINE, Games.AUTHOR, Games.DESCRIPTION,
		Games.FIRSTPUBLISHED, Games.GENRE, Games.GROUP, Games.SERIES, Games.SERIESNUMBER, Games.FORGIVENESS,
		Games.LANGUAGE, Games.LOOKED_UP, Games.PATH
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
	private static final int PATH = 14;

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
	private File mGameFile;
	private View mRestartButton;

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
		mRestartButton = findViewById(id.restart);
		
		((Button) findViewById(R.id.open)).setOnClickListener(this);
		((Button) findViewById(R.id.remove)).setOnClickListener(this);
		mRestartButton.setOnClickListener(this);
		
		mQuery = managedQuery(game, PROJECTION, null, null, null);
		mQuery.registerContentObserver(mContentObserver);
		showData();
	}

	protected void showData() {
		mQuery.moveToFirst();
		
		if (mGameIfid == null)
			mGameIfid = mQuery.getString(IFID);
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

		mGameFile = new File(mQuery.getString(PATH));

		String terp = getTerp();
		sb.append("Interpreter: ");
		sb.append(terp);
		sb.append('\n');

		if (terp.compareTo("frotz")==0 || terp.compareTo("nitfol")==0) {
			sb.append("ZCode Version: ");
			sb.append(getZcodeVersion());
			sb.append('\n');
		}
		
		final int len = sb.length(); 
		if (len != 0)
			sb.replace(len - 1, len, ""); // remove trailing newline
		
		mDetails.setText(sb);
		
		// Uri.fromFile doesn't work for some reason
		mCover.setImageURI(Uri.parse(HunkyPunk.getCover(mQuery.getString(IFID)).getAbsolutePath()));
		
		mRestartButton.setVisibility(getBookmark().exists() ? View.VISIBLE : View.GONE);
	}

	private File getBookmark() {
		return new File(
						HunkyPunk.getGameDataDir(Uri.parse(mGameFile.getAbsolutePath()), mGameIfid), "bookmark");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mGameIfid != null && mGameFile != null)
			mRestartButton.setVisibility(getBookmark().exists() ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.restart:
			askRestartGame();
			break;
		case R.id.open:
			openGame();
		case R.id.remove:
			// TODO
		}
	}

	private String getZcodeVersion() {
		int zver = 0;
		try {
			RandomAccessFile f = new RandomAccessFile(mGameFile.getAbsolutePath(), "r");		
			zver = f.read();
			f.close();			
		} catch(Exception ex){}
		if (zver == 0) return "unknown";
		else if (zver == 70) return "unknown (blorbed)";
		else return Integer.toString(zver);
	}

	private String getTerp() {
		String fullPath = mGameFile.getAbsolutePath();
		String ext = "||";
		int dot = fullPath.lastIndexOf(".");
		if (dot > -1) ext = "|"+fullPath.substring(dot+1)+"|";

		if ("|ulx|blb|blorb|glb|gblorb|".indexOf(ext) > -1) return "git";
		else if ("|gam|t2|t3|".indexOf(ext) > -1) return "tads";
		else if ("|zblorb|zlb|".indexOf(ext) > -1) return "frotz";
		else /* *.z[1-9] */
			return (getZcodeVersion().compareTo("6")==0) ? "nitfol" : "frotz";
	}

	private void openGame() {
		Intent intent = new Intent(Intent.ACTION_VIEW, 
								   Uri.parse(mGameFile.getAbsolutePath()), 
								   this, 
								   Interpreter.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		intent.putExtra("terp", getTerp());
		intent.putExtra("ifid", mGameIfid);
		intent.putExtra("loadBookmark", true);
		startActivity(intent);
	}

	private void askRestartGame() {
		new AlertDialog.Builder(this)
			.setMessage(R.string.restart_warning)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getBookmark().delete();
					openGame();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			})
			.setTitle(android.R.string.dialog_alert_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.show();
	}
}
