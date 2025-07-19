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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import android.os.Looper;
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
import androidx.appcompat.widget.Toolbar;

public class GamesList extends AppCompatActivity implements OnClickListener {
    private ListView mListView;
    
    protected ListView getListView() {
        return mListView;
    }
    
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
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StorageManager.DONE:
                    // Progress is now handled by Material progress indicators
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
    private Cursor gamesCursor;
    private final Object cursorLock = new Object(); // Thread safety for cursor operations


    private void requestStoragePermissions() {
        // Modern Android approach: Use app-specific storage (no permissions required)
        // and Storage Access Framework for user file selection
        
        // For Android 10+ (API 29+), we rely entirely on:
        // 1. App-specific storage (no permissions required)
        // 2. Storage Access Framework for file selection
        // 3. Media store APIs where appropriate
        
        // No legacy storage permissions needed in modern Android
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, refresh the view
                verifyIfDirectory(this);
            } else {
                // Permission denied, inform user that app uses app-specific storage
                Toast.makeText(this, 
                    "Storage permission denied. The app will use app-specific storage. " +
                    "Use 'Add Games' button to import game files.", 
                    Toast.LENGTH_LONG).show();
                verifyIfDirectory(this);
            }
        }
    }

    private void verifyIfDirectory(Context c)
    {
        Log.d(TAG, "Verifying IF directory: " + Paths.ifDirectory(c));
        Log.d(TAG, "Directory valid: " + Paths.isIfDirectoryValid(c));
        
        // App-specific storage is always available in modern Android
        if (Paths.isIfDirectoryValid(c)){
            findViewById(R.id.go_to_prefs).setVisibility(View.INVISIBLE);
            findViewById(R.id.go_to_prefs_msg).setVisibility(View.INVISIBLE);
            findViewById(R.id.go_to_ifdb).setVisibility(View.VISIBLE);
            findViewById(R.id.go_to_ifdb_msg).setVisibility(View.VISIBLE);
            findViewById(R.id.download_preselected).setVisibility(View.VISIBLE);
        } else {
            // This should rarely happen with app-specific storage
            findViewById(R.id.go_to_prefs).setVisibility(View.VISIBLE);
            findViewById(R.id.go_to_prefs_msg).setVisibility(View.VISIBLE);
            findViewById(R.id.go_to_ifdb).setVisibility(View.INVISIBLE);
            findViewById(R.id.go_to_ifdb_msg).setVisibility(View.INVISIBLE);
            findViewById(R.id.download_preselected).setVisibility(View.INVISIBLE);
        }

        // Clean up database entries for games that no longer exist
        DatabaseHelper mOpenHelper = new DatabaseHelper(this);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Log.d(TAG, "Adapter count in verifyIfDirectory: " + (adapter != null ? adapter.getCount() : "null"));
        
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                Cursor cur = (Cursor) adapter.getItem(i);
                if (cur != null && cur.getString(4) != null) {
                    Log.d(TAG, "Found game path: " + cur.getString(4));
                    // Check if the file still exists
                    File gameFile = new File(cur.getString(4));
                    if (!gameFile.exists()) {
                        Log.d(TAG, "Deleting game with missing file: " + cur.getString(4));
                        db.execSQL("delete from games where ifid = '" + cur.getString(1) + "'");
                    }
                }
            }
        }
        db.close();

        // Refresh the games list
        startScan();
        refreshGamesList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called - refreshing adapter");
        
        // Ensure cursor refresh happens on UI thread
        runOnUiThread(() -> {
            refreshGamesList();
            verifyIfDirectory(this);
        });
    }

    private void refreshGamesList() {
        Log.d(TAG, "Refreshing games list...");
        
        // Ensure we're on the UI thread for all cursor operations
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            runOnUiThread(() -> refreshGamesList());
            return;
        }
        
        synchronized (cursorLock) {
            // Close previous cursor if it exists
            if (gamesCursor != null && !gamesCursor.isClosed()) {
                try {
                    gamesCursor.close();
                } catch (Exception e) {
                    Log.w(TAG, "Error closing previous cursor", e);
                }
            }
            
            // Create a new cursor to get fresh data using modern approach
            try {
                gamesCursor = getContentResolver().query(Games.CONTENT_URI, PROJECTION, Games.PATH + " IS NOT NULL", null, null);
                Log.d(TAG, "New cursor count: " + (gamesCursor != null ? gamesCursor.getCount() : "null"));
            } catch (Exception e) {
                Log.e(TAG, "Error creating new cursor", e);
                return;
            }
            
            // Debug: Check cursor data safely
            if (gamesCursor != null && gamesCursor.getCount() > 0) {
                try {
                    if (gamesCursor.moveToFirst()) {
                        for (int i = 0; i < Math.min(3, gamesCursor.getCount()); i++) {
                            String title = gamesCursor.getString(2);
                            String path = gamesCursor.getString(4);
                            Log.d(TAG, "Game " + i + ": TITLE=" + title + ", PATH=" + path);
                            if (!gamesCursor.moveToNext()) break;
                        }
                        gamesCursor.moveToFirst(); // Reset cursor position
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing cursor data", e);
                }
            }
            
            if (adapter != null && mListView != null) {
                try {
                    adapter.changeCursor(gamesCursor);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter updated with new cursor. Count: " + adapter.getCount());
                    
                    // Update button visibility after data change
                    updateAddButtonVisibility();
                    
                    // Force ListView to update its empty view state
                    if (mListView.getEmptyView() != null) {
                        Log.d(TAG, "ListView empty view state: isEmpty=" + adapter.isEmpty() + 
                              ", emptyViewVisibility=" + mListView.getEmptyView().getVisibility());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating adapter with new cursor", e);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display with modern API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (controller != null) {
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }

        // Handle storage permissions based on Android version
        requestStoragePermissions();

        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        // Initialize StorageManager with crash guard for AssertionError
        try {
            mScanner = StorageManager.getInstance(this);
            mScanner.setHandler(mHandler);
            mScanner.checkExisting();
        } catch (AssertionError ae) {
            Log.w(TAG, "AssertionError initializing StorageManager, skipping storage scan", ae);
            mScanner = null;
        }

        /** This part creates the list of Ifs */
        gamesCursor = getContentResolver().query(Games.CONTENT_URI, PROJECTION, Games.PATH + " IS NOT NULL", null, null);
        Log.d(TAG, "Database query returned cursor with count: " + (gamesCursor != null ? gamesCursor.getCount() : "null"));
        
        adapter = new SimpleCursorAdapter(this, R.layout.game_list_item, gamesCursor,
                new String[]{Games.TITLE, Games.AUTHOR}, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        
        Log.d(TAG, "Adapter created with count: " + (adapter != null ? adapter.getCount() : "null"));
        
        setContentView(R.layout.games_list);
        
        // Initialize ListView
        mListView = findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> 
            onListItemClick(mListView, view, position, id));
        
        // Set up the empty view relationship
        View emptyView = findViewById(android.R.id.empty);
        mListView.setEmptyView(emptyView);
        
        // Update button visibility based on game count
        updateAddButtonVisibility();
        
        Log.d(TAG, "ListView setup complete. Adapter count: " + adapter.getCount() + 
              ", ListView visibility: " + mListView.getVisibility() + 
              ", Empty view visibility: " + emptyView.getVisibility());
        
        Toolbar toolbar = (Toolbar)findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.go_to_ifdb).setOnClickListener(this);
        findViewById(R.id.go_to_prefs).setOnClickListener(this);
        findViewById(R.id.download_preselected).setOnClickListener(this);
        findViewById(R.id.add_games_button).setOnClickListener(this);
        findViewById(R.id.add_games_button_empty).setOnClickListener(this);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        synchronized (cursorLock) {
            // Properly close the cursor to prevent memory leaks
            if (gamesCursor != null && !gamesCursor.isClosed()) {
                gamesCursor.close();
                gamesCursor = null;
            }
        }
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        return super.onOptionsItemSelected(item);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        /** the id is not equal to the position, cause of the list is alphabetical sorted.
         * We create an array, where the positions match the ids */

        long ifIDs[] = new long[mListView.getAdapter().getCount()];//array for the right order of the ifIDs (non-alphabetical order)

        /** matching id of each IF to the position in ListView*/
        for (int j = 0; j < mListView.getAdapter().getCount(); j++) {
            ifIDs[j] = getListView().getItemIdAtPosition(j);
            //System.out.println(ifIDs[j]);
        }

        Intent i = new Intent(Intent.ACTION_VIEW, Games.uriOfId(id), this, GameDetails.class);
        i.putExtra("position", position); //commit the position of the clicked item
        i.putExtra("ifIDs", ifIDs); //commiting the array, where the positions matches the ids
        startActivity(i);
    }

    private void startScan() {
        Log.d(TAG, "startScan called, mScanner: " + mScanner);
        if (mScanner != null && !mScanner.alreadyScanning) {
            Log.d(TAG, "Starting scan...");
            // Progress is now handled by Material progress indicators
            mScanner.startScan();
        } else {
            Log.d(TAG, "Scan not started - mScanner null or already scanning");
        }
    }

    private void startLookup() {
        IFDb ifdb = IFDb.getInstance(getContentResolver());
        ifdb.startLookup(this, new Handler(Looper.getMainLooper()) {
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
        } else if (id == R.id.add_games_button || id == R.id.add_games_button_empty) {
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

        progressDialog = new MaterialAlertDialogBuilder(this)
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
                    final int progress = ++i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressIndicator.setProgress(progress);
                        }
                    });
                }

                try {
                    if (mScanner != null) {
                        Log.d(TAG, "Download complete, starting scan of directory: " + Paths.ifDirectory(c));
                        mScanner.scan(Paths.ifDirectory(c));
                    } else {
                        Log.w(TAG, "mScanner is null, cannot scan directory");
                    }
                    IFDb.getInstance(getContentResolver()).lookupGames(c);
                } catch (IOException e) {
                    Log.e(TAG, "I/O error when fetching metadata", e);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        // Refresh the games list after download
                        Log.d(TAG, "Download finished, refreshing games list");
                        refreshGamesList();
                    }
                });
            }
        };

        downloadThread.start();
    }

    private void updateAddButtonVisibility() {
        View addButton = findViewById(R.id.add_games_button);
        if (addButton != null) {
            // Show the button when there are games in the list
            boolean hasGames = adapter != null && adapter.getCount() > 0;
            addButton.setVisibility(hasGames ? View.VISIBLE : View.GONE);
        }
    }
}
