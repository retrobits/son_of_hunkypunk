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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.andglk.glk.Utils;
import org.andglk.hunkypunk.HunkyPunk.Games;
import org.andglk.ifdb.IFDb;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class GamesList extends ListActivity implements OnClickListener {
    private static final String[] PROJECTION = {
            Games._ID,
            Games.IFID,
            Games.TITLE,
            Games.AUTHOR,
            Games.PATH
    };

    protected static final String[] BEGINNER_GAMES = {
            "http://www.ifarchive.org/if-archive/games/zcode/905.z5",
            "http://www.ifarchive.org/if-archive/games/zcode/Advent.z5",
            "http://www.ifarchive.org/if-archive/games/zcode/awaken.z5",
            "http://www.ifarchive.org/if-archive/games/zcode/dreamhold.z8",
            "http://www.ifarchive.org/if-archive/games/zcode/LostPig.z8",
            "http://www.ifarchive.org/if-archive/games/zcode/shade.z5",
            "http://www.ifarchive.org/if-archive/games/tads/indigo.t3",
            "http://www.ifarchive.org/if-archive/games/competition98/tads/plant/plant.gam",
            "http://www.ifarchive.org/if-archive/games/zcode/Bronze.zblorb",
            "http://www.ifarchive.org/if-archive/games/zcode/theatre.z5",
            "http://hunkypunk.googlecode.com/files/uu1.gam"
    };

    protected static final String TAG = "HunkyPunk";


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

    private ProgressDialog progressDialog;

    private Thread downloadThread;

    protected boolean downloadCancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        mScanner = StorageManager.getInstance(this);
        mScanner.setHandler(mHandler);
        mScanner.checkExisting();

        Cursor cursor = managedQuery(Games.CONTENT_URI, PROJECTION, Games.PATH + " IS NOT NULL", null, null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor,
                new String[]{Games.TITLE, Games.AUTHOR}, new int[]{android.R.id.text1, android.R.id.text2});

        setListAdapter(adapter);

        setContentView(R.layout.games_list);
        findViewById(R.id.go_to_ifdb).setOnClickListener(this);
        findViewById(R.id.download_preselected).setOnClickListener(this);


        startScan();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(getApplication());
        inflater.inflate(R.layout.menu_main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent intent;
        switch (item.getNumericShortcut()) {
            case '1':
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            case '2':
                AlertDialog builder;
                try {
                    builder = AboutDialogBuilder.show(this);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        /** the id is not equal to the position, cause of the list is alphabetical sorted.
         * We create an array, where the positions match the ids */

        long ifIDs[] = new long[getListAdapter().getCount()];//array for the right order of the ifIDs (non-alphabetical order)

        /** matching id of each IF to the position in ListView*/
        for (int j = 0; j < getListAdapter().getCount(); j++) {
            ifIDs[j] = getListView().getItemIdAtPosition(j);
            System.out.println(ifIDs[j]);
        }

        Intent i = new Intent(Intent.ACTION_VIEW, Games.uriOfId(id), this, GameDetails.class);
        i.putExtra("position", position); //commit the position of the clicked item
        i.putExtra("ifIDs", ifIDs); //commiting the array, where the positions matches the ids
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_to_ifdb:
                startActivity(new Intent(Intent.ACTION_DEFAULT, Uri.parse("http://ifdb.tads.org")));
                break;
            case R.id.download_preselected:
                downloadPreselected();
                break;
        }
    }

    private void downloadPreselected() {
        downloadCancelled = false;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setMessage(getString(R.string.downloading_stories));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(
                new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        synchronized (downloadThread) {
                            downloadCancelled = true;
                        }
                        downloadThread.interrupt();
                    }
                }
        );
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(BEGINNER_GAMES.length);
        progressDialog.show();

        downloadThread = new Thread() {
            @Override
            public void run() {
                int i = 0;
                for (String s : BEGINNER_GAMES) {
                    synchronized (this) {
                        if (downloadCancelled)
                            return;
                    }
                    try {
                        final URL u = new URL(s);
                        final String fileName = Uri.parse(s).getLastPathSegment();
                        Utils.copyStream(u.openStream(), new FileOutputStream(new File(Paths.ifDirectory(), fileName)));
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "malformed URL when fetching " + s, e);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "file not found when fetching " + s, e);
                    } catch (IOException e) {
                        Log.e(TAG, "I/O error when fetching " + s, e);
                    }
                    progressDialog.setProgress(++i);
                }

                try {
                    mScanner.scan(Paths.ifDirectory());
                    IFDb.getInstance(getContentResolver()).lookupGames();
                } catch (IOException e) {
                    Log.e(TAG, "I/O error when fetching metadata", e);
                }

                progressDialog.dismiss();
            }
        };
        downloadThread.start();
    }
}
