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

package org.andglkmod.hunkypunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import org.andglkmod.glk.Utils;
import org.andglkmod.hunkypunk.HunkyPunk.Games;
import org.andglkmod.ifdb.IFDb;

import android.Manifest;
import androidx.appcompat.app.AlertDialog;
import android.app.ListActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.app.AppCompatCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import java.util.regex.Pattern;

public class GamesList extends ListActivity implements OnClickListener, AppCompatCallback {
    private static final String[] PROJECTION = {
            Games._ID,
            Games.IFID,
            Games.TITLE,
            Games.AUTHOR,
            Games.PATH
    };

    protected static final String[] BEGINNER_GAMES = {
            "https://www.ifarchive.org/if-archive/games/zcode/905.z5",
            "https://www.ifarchive.org/if-archive/games/zcode/Advent.z5",
            "https://www.ifarchive.org/if-archive/games/zcode/awaken.z5",
            "https://www.ifarchive.org/if-archive/games/zcode/dreamhold.z8",
            "https://www.ifarchive.org/if-archive/games/zcode/LostPig.z8",
            "https://www.ifarchive.org/if-archive/games/zcode/shade.z5",
            "https://www.ifarchive.org/if-archive/games/tads/indigo.t3",
            "https://www.ifarchive.org/if-archive/games/competition98/tads/plant/plant.gam",
            "https://www.ifarchive.org/if-archive/games/zcode/Bronze.zblorb",
            "https://www.ifarchive.org/if-archive/games/zcode/theatre.z5",
            "https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/hunkypunk/uu1.gam"
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

    private AlertDialog progressDialog;
    private LinearProgressIndicator progressIndicator;

    private Thread downloadThread;

    protected boolean downloadCancelled;

    private SimpleCursorAdapter adapter;


    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - Use Manage External Storage for legacy compatibility
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0-10 - Use traditional storage permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        // For Android < 6.0, permissions are granted at install time
    }

    private void verifyIfDirectory(Context c)
    {
        if (Paths.isIfDirectoryValid(c)){
            findViewById(R.id.go_to_prefs).setVisibility(View.INVISIBLE);
            findViewById(R.id.go_to_prefs_msg).setVisibility(View.INVISIBLE);
            findViewById(R.id.go_to_ifdb).setVisibility(View.VISIBLE);
            findViewById(R.id.go_to_ifdb_msg).setVisibility(View.VISIBLE);
            findViewById(R.id.download_preselected).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.go_to_prefs).setVisibility(View.VISIBLE);
            findViewById(R.id.go_to_prefs_msg).setVisibility(View.VISIBLE);
            findViewById(R.id.go_to_ifdb).setVisibility(View.INVISIBLE);
            findViewById(R.id.go_to_ifdb_msg).setVisibility(View.INVISIBLE);
            findViewById(R.id.download_preselected).setVisibility(View.INVISIBLE);
        }

        /** gets the If-Path from SharedPrefences, which could be changed at the last session */
        //String path = getSharedPreferences("ifPath", Context.MODE_PRIVATE).getString("ifPath", "");
        //if (!path.equals(""))
        //    Paths.setIfDirectory(new File(path));

        /** deletes all Ifs, which are not in the current Path, in other words, it delets the
         * Ifs from the older Directory*/
        DatabaseHelper mOpenHelper = new DatabaseHelper(this);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        for (int i = 0; i < adapter.getCount(); i++) {
            Cursor cur = (Cursor) adapter.getItem(i);
            if (!Pattern.matches(".*" + Paths.ifDirectory(this) + ".*", cur.getString(4))) {
                db.execSQL("delete from games where ifid = '" + cur.getString(1) + "'");
            }
        }
        db.close();

        /** helps to refresh the View, when come back from preferences */
        startScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyIfDirectory(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Handle storage permissions based on Android version
        requestStoragePermissions();

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        // Initialize StorageManager with crash guard for AssertionError
        try {
            mScanner = StorageManager.getInstance(this);
            mScanner.setHandler(mHandler);
        } catch (AssertionError ae) {
            Log.w(TAG, "AssertionError initializing StorageManager, skipping storage scan", ae);
            mScanner = null;
        }
        mScanner.checkExisting();

        /** This part creates the list of Ifs */
        Cursor cursor = managedQuery(Games.CONTENT_URI, PROJECTION, Games.PATH + " IS NOT NULL", null, null);
        adapter = new SimpleCursorAdapter(this, R.layout.game_list_item, cursor,
                new String[]{Games.TITLE, Games.AUTHOR}, new int[]{android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);

        AppCompatDelegate delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.games_list);
        //setContentView(R.layout.games_list);
        Toolbar toolbar = (Toolbar)findViewById(R.id.appbar);
        delegate.setSupportActionBar(toolbar);

        findViewById(R.id.go_to_ifdb).setOnClickListener(this);
        findViewById(R.id.go_to_prefs).setOnClickListener(this);
        findViewById(R.id.download_preselected).setOnClickListener(this);
        findViewById(R.id.add_games_button).setOnClickListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);
        SharedPreferences sharedShortcuts = getSharedPreferences("shortcuts", MODE_PRIVATE);
        SharedPreferences sharedShortcutIDs = getSharedPreferences("shortcutIDs", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("firstStart", true)) {
            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
            SharedPreferences.Editor shortcutEditor = sharedShortcuts.edit();
            SharedPreferences.Editor shortcutIDEditor = sharedShortcutIDs.edit();

            String[] defaults = new String[]{"look", "examine", "take", "inventory", "ask", "drop", "tell", "again", "open", "close", "give", "show"};
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < defaults.length; i++)
                list.add(defaults[i]);
            Collections.sort(list);


            for (int i = 0; i < list.size(); i++) {
                // Append a space to the stored command so default shortcuts include a trailing space
                shortcutEditor.putString(list.get(i), list.get(i) + " ");
                shortcutIDEditor.putString(i + "", list.get(i));
            }
            prefEditor.putBoolean("firstStart", false);
            shortcutIDEditor.commit();
            shortcutEditor.commit();
            prefEditor.commit();
        }
        else if (sharedPreferences.getBoolean("performUpgrade", true)) {
            SharedPreferences.Editor prefEditor = sharedPreferences.edit();

            File old = Paths.oldAppDirectory();
            if (old.exists() && old.getPath() != Paths.appDirectory(this).getPath())
            {
                //copy covers
                String oldCovers = new File(old,"covers").getPath();
                File oldCoversFile = new File(oldCovers);
                if (oldCoversFile.exists()) {
                    // Ensure the destination directory exists
                    File coverParent = Paths.coverDirectory(this).getParentFile();
                    if (!coverParent.exists() && !coverParent.mkdirs()) {
                        android.util.Log.w("GamesList", "Failed to create covers directory: " + coverParent.getAbsolutePath());
                    } else {
                        copyFileOrDirectory(oldCovers, coverParent.getPath());
                    }
                }

                //copy existing games with state data
                DatabaseHelper mOpenHelper = new DatabaseHelper(this);
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                for (int i = 0; i < adapter.getCount(); i++) {
                    Cursor c = (Cursor) adapter.getItem(i);
                    String ifid = c.getString(1);
                    String gamepath = c.getString(4);
                    File oldgame = Paths.oldGameStateDir(gamepath, ifid);
                    File newgame = Paths.gameStateDir(this, gamepath, ifid);
                    File newsavegames = new File(newgame, "savegames");
                    if (oldgame.exists() && !newsavegames.exists()) {
                        copyFileOrDirectory(oldgame.getPath(), newgame.getParentFile().getPath());
                    }
                }
            }

            prefEditor.putBoolean("performUpgrade", false);
            prefEditor.commit();
        }

        verifyIfDirectory(this);
    }

    public void onSupportActionModeStarted(androidx.appcompat.view.ActionMode mode) {}

    public void onSupportActionModeFinished(androidx.appcompat.view.ActionMode mode) {}

    @Nullable
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback)
    {
        return null;
    }

    public static void copyFileOrDirectory(String srcDir, String dstDir) {
        try {
            File src = new File(srcDir);
            if (!src.exists()) {
                return; // Source doesn't exist, nothing to copy
            }
            
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {
                // Create destination directory if it doesn't exist
                if (!dst.exists() && !dst.mkdirs()) {
                    throw new IOException("Failed to create directory: " + dst.getAbsolutePath());
                }

                String files[] = src.list();
                if (files != null) {
                    int filesLength = files.length;
                    for (int i = 0; i < filesLength; i++) {
                        String src1 = (new File(src, files[i]).getPath());
                        String dst1 = dst.getPath();
                        copyFileOrDirectory(src1, dst1);
                    }
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        new MenuInflater(getApplication()).inflate(R.menu.menu_main, menu);
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
                    builder = DialogBuilder.showAboutDialog(this);
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
            //System.out.println(ifIDs[j]);
        }

        Intent i = new Intent(Intent.ACTION_VIEW, Games.uriOfId(id), this, GameDetails.class);
        i.putExtra("position", position); //commit the position of the clicked item
        i.putExtra("ifIDs", ifIDs); //commiting the array, where the positions matches the ids
        startActivity(i);
    }

    private void startScan() {
        if (!mScanner.alreadyScanning) {
            setProgressBarIndeterminateVisibility(true);
            mScanner.startScan();
        }
    }

    private void startLookup() {
        IFDb ifdb = IFDb.getInstance(getContentResolver());
        ifdb.startLookup(this, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(GamesList.this, R.string.ifdb_connection_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.go_to_ifdb) {
            startActivity(new Intent(Intent.ACTION_DEFAULT, Uri.parse("http://ifdb.tads.org")));
        } else if (id == R.id.download_preselected) {
            downloadPreselected();
        } else if (id == R.id.go_to_prefs) {
            startActivity(new Intent(this, PreferencesActivity.class));
        } else if (id == R.id.add_games_button) {
            openDocumentPicker();
        }
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(this, DocumentPickerActivity.class);
        startActivity(intent);
    }

    private void downloadPreselected() {
        downloadCancelled = false;

        View progressView = LayoutInflater.from(this).inflate(R.layout.material_progress_dialog, null);
        TextView messageView = progressView.findViewById(R.id.progress_message);
        progressIndicator = progressView.findViewById(R.id.progress_linear);
        
        messageView.setText(getString(R.string.downloading_stories));
        progressIndicator.setVisibility(View.VISIBLE);
        progressIndicator.setMax(BEGINNER_GAMES.length);
        progressView.findViewById(R.id.progress_circular).setVisibility(View.GONE);

        progressDialog = new AlertDialog.Builder(this)
                .setView(progressView)
                .setCancelable(true)
                .setOnCancelListener(
                        new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                synchronized (downloadThread) {
                                    downloadCancelled = true;
                                }
                                downloadThread.interrupt();
                            }
                        }
                )
                .create();
        progressDialog.show();

        final Context c = this;
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
                        Utils.copyStream(u.openStream(), new FileOutputStream(new File(Paths.ifDirectory(c), fileName)));
                    } catch (MalformedURLException e) {
                        Log.e(TAG, "malformed URL when fetching " + s, e);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "file not found when fetching " + s, e);
                    } catch (IOException e) {
                        Log.e(TAG, "I/O error when fetching " + s, e);
                    }
                    progressIndicator.setProgress(++i);
                }

                try {
                    mScanner.scan(Paths.ifDirectory(c));
                    IFDb.getInstance(getContentResolver()).lookupGames(c);
                } catch (IOException e) {
                    Log.e(TAG, "I/O error when fetching metadata", e);
                }

                progressDialog.dismiss();
            }
        };

        downloadThread.start();
    }
}
