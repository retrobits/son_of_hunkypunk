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

package org.andglkmod.hunkypunk;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andglkmod.hunkypunk.HunkyPunk.Games;
import org.andglkmod.hunkypunk.R.id;
import org.andglkmod.ifdb.IFDb;
import org.andglkmod.glk.Utils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.text.Editable;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;

import static android.app.Activity.OVERRIDE_TRANSITION_OPEN;

public class GameDetails extends AppCompatActivity implements OnClickListener {
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
    // For SWIPE
    private static final int MIN_DISTANCE = 100;
    private static final int SCROLL_PROTECTOR = 250;
    private GestureDetector gestureDetector;

    // Modern Handler with explicit Looper
    private ContentObserver mContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Ensure showData runs on UI thread
            runOnUiThread(() -> showData());
        }
    };
    private Cursor mQuery;
    private final Object cursorLock = new Object(); // Thread safety for cursor operations
    private TextView mTitle;
    private TextView mHeadline;
    private TextView mAuthor;
    private ImageView mCover;
    private TextView mDescription;
    private View mDescriptionLayout;
    private TextView mDetails;
    private NestedScrollView mScroll;
    private AlertDialog mProgressDialog;
    
    // Modern Handler with explicit Looper and ExecutorService for background tasks
    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool();
    private Handler mLookupHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            // Progress is now handled by Material progress indicators
            switch (msg.what) {
                case IFDb.FAILURE:
                    Toast.makeText(GameDetails.this, R.string.lookup_failure, Toast.LENGTH_SHORT).show();
                    break;
                case IFDb.SUCCESS:
                    // Replace deprecated requery() with modern cursor refresh
                    refreshGameData();
                    break;
            }
        }
    };
    protected String mGameIfid;
    private Handler mInstallHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case StorageManager.INSTALLED:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    mGameIfid = (String) msg.obj;
                    show(HunkyPunk.Games.uriOfIfid(mGameIfid), null);
                    break;
                case StorageManager.INSTALL_FAILED:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    Toast.makeText(GameDetails.this, R.string.install_failure, Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }
    };
    private File mGameFile;
    private View mRestartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri game = getIntent().getData();
        if (game == null) {
            Log.e(TAG, "No game URI provided");
            finish();
            return;
        }
        
        String scheme = game.getScheme();
        if (scheme == null) {
            Log.e(TAG, "No scheme in game URI");
            finish();
            return;
        }

        if (scheme.equals(ContentResolver.SCHEME_CONTENT)
                && game.toString().indexOf("HunkyPunk/games") > 0)
            show(game, savedInstanceState);
        else
            install(game, scheme);
        gestureDetector = new GestureDetector(this, new SwipeDetector());
        //To relax the Interpreter activity; First time load of SharedPreferences
        getSharedPreferences("Night", Context.MODE_PRIVATE).getBoolean("NightOn", false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_game_details, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back navigation
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        
        Intent intent;
        switch (item.getNumericShortcut()) {
            case '1':
                intent = new Intent(this, PreferencesActivity.class);
                startActivity(intent);
                break;
            case '2':
                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                input.setText(mTitle.getText());
                input.setSelection(input.getText().length());
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.edit_title)
                        .setView(input)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Editable value = input.getText();
                                        StorageManager.getInstance(GameDetails.this)
                                                .updateGame(mGameIfid, value.toString());
                                        show(HunkyPunk.Games.uriOfIfid(mGameIfid), null);
                                    }
                                })
                        .setNegativeButton(this.getString(android.R.string.cancel), null).show();
                break;
            case '3':
                AlertDialog d = new MaterialAlertDialogBuilder(this)
                        .setPositiveButton(this.getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        StorageManager.getInstance(GameDetails.this)
                                                .deleteGame(mGameIfid);
                                        GameDetails.this.finish();
                                    }
                                })
                        .setNegativeButton(this.getString(android.R.string.cancel), null)
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage(R.string.delete_confirm)
                        .setCancelable(true)
                        .setTitle(R.string.delete_title).create();
                d.show();
                break;
            case '4':
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

    private void install(Uri game, String scheme) {
        View progressView = LayoutInflater.from(this).inflate(R.layout.material_progress_dialog, null);
        TextView messageView = progressView.findViewById(R.id.progress_message);
        if (messageView != null) {
            messageView.setText(getString(R.string.examining_file, game.getLastPathSegment()));
        }
        
        mProgressDialog = new MaterialAlertDialogBuilder(this)
                .setView(progressView)
                .setCancelable(false)
                .create();
        mProgressDialog.show();

        StorageManager mediaScanner = StorageManager.getInstance(this);
        mediaScanner.setHandler(mInstallHandler);
        mediaScanner.startInstall(game, scheme);
    }

    private void show(Uri game, Bundle savedInstanceState) {
        try {
            setContentView(R.layout.game_details);
            
            // Set up toolbar 
            Toolbar toolbar = (Toolbar)findViewById(R.id.appbar);
            if (toolbar == null) {
                Log.e(TAG, "Toolbar not found in layout");
                Toast.makeText(this, "Error loading game details", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            
            // Set up navigation click listener
            toolbar.setNavigationOnClickListener(v -> finish());

            mTitle = (TextView) findViewById(R.id.title);
            mHeadline = (TextView) findViewById(R.id.headline);
            mAuthor = (TextView) findViewById(R.id.author);
            mDescription = (TextView) findViewById(R.id.description);
            mCover = (ImageView) findViewById(R.id.cover);
            mDescriptionLayout = findViewById(R.id.description_layout);
            mDetails = (TextView) findViewById(R.id.details);
            mScroll = (NestedScrollView) findViewById(R.id.info_scroll);
            mRestartButton = findViewById(R.id.restart);

            // Check if any critical views are null
            if (mTitle == null || mHeadline == null || mAuthor == null || 
                mDescription == null || mCover == null || mDescriptionLayout == null ||
                mDetails == null || mScroll == null || mRestartButton == null) {
                Log.e(TAG, "One or more views not found in layout");
                Toast.makeText(this, "Error loading game details", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            View fabPlay = findViewById(R.id.fab_play);
            View restartBtn = findViewById(R.id.restart);
            if (fabPlay != null) {
                fabPlay.setOnClickListener(this);
            }
            if (restartBtn != null) {
                restartBtn.setOnClickListener(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up GameDetails UI", e);
            Toast.makeText(this, "Error loading game details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mQuery = getContentResolver().query(game, PROJECTION, null, null, null);
        if (mQuery != null) {
            mQuery.registerContentObserver(mContentObserver);
        }
        showData();
    }

    protected void showData() {
        // Ensure we're on the UI thread
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            runOnUiThread(() -> showData());
            return;
        }
        
        synchronized (cursorLock) {
            if (mQuery == null || mQuery.isClosed()) {
                Log.w(TAG, "showData called with null or closed cursor");
                return;
            }
            
            try {
                if (!mQuery.moveToFirst()) {
                    Log.w(TAG, "showData: cursor is empty");
                    return;
                }

                if (mGameIfid == null) {
                    mGameIfid = mQuery.getString(IFID);
                }
                
                if (mQuery.isNull(LOOKED_UP)) {
                    Toast.makeText(this, R.string.looking_up, Toast.LENGTH_SHORT).show();
                    // Progress is now handled by Material progress indicators
                    IFDb.getInstance(getContentResolver()).startLookup(this, mGameIfid, mLookupHandler);
                }

                String title = mQuery.getString(TITLE);
                if (title != null) {
                    mTitle.setText(title);
                    // Set toolbar title for better Material Design
                    try {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle(title);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Could not set toolbar title", e);
                        // Try direct toolbar access as fallback
                        try {
                            Toolbar toolbar = findViewById(R.id.appbar);
                            if (toolbar != null) {
                                toolbar.setTitle(title);
                            }
                        } catch (Exception ex) {
                            Log.w(TAG, "Could not set toolbar title via direct access", ex);
                        }
                    }
                }

                String string = mQuery.getString(HEADLINE);
                mHeadline.setText(string);
                mHeadline.setVisibility(string == null ? View.GONE : View.VISIBLE);

                string = mQuery.getString(AUTHOR);
                if (string != null) {
                    mAuthor.setText(getString(R.string.by_author, string));
                    mAuthor.setVisibility(View.VISIBLE);
                } else {
                    mAuthor.setVisibility(View.GONE);
                }

                string = mQuery.getString(DESCRIPTION);
                if (string != null) {
                    // Convert HTML br tags to proper line breaks for better formatting
                    string = string.replace("<br/>", "\n").replace("<br>", "\n").replace("<BR/>", "\n").replace("<BR>", "\n");
                    mDescription.setText(string);
                    mDescriptionLayout.setVisibility(View.VISIBLE);
                } else {
                    mDescriptionLayout.setVisibility(View.GONE);
                }

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

                String pathString = mQuery.getString(PATH);
                if (pathString != null) {
                    mGameFile = new File(pathString);

                    String terp = getTerp();
                    sb.append("Interpreter: ");
                    sb.append(terp);
                    sb.append('\n');

                    if (terp.compareTo("frotz") == 0 || terp.compareTo("nitfol") == 0) {
                        sb.append("ZCode Version: ");
                        sb.append(getZcodeVersion());
                        sb.append('\n');
                    }
                }
                
                if (mGameIfid != null) {
                    sb.append("IFID: ").append(mGameIfid);
                    sb.append('\n');
                }

                final int len = sb.length();
                if (len != 0)
                    sb.replace(len - 1, len, ""); // remove trailing newline

                mDetails.setText(sb);

                String ifid = mQuery.getString(IFID);
                if (ifid != null) {
                    File i = HunkyPunk.getCover(this, ifid);
                    if (i != null && i.exists()) {
                        try {
                            // Uri.fromFile doesn't work for some reason, using alternative approach
                            Uri imageUri = Uri.parse(i.getAbsolutePath());
                            if (imageUri != null) {
                                mCover.setImageURI(imageUri);

                                // Use modern approach to get display dimensions
                                int width, height;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
                                    android.graphics.Rect bounds = windowMetrics.getBounds();
                                    width = (int) (bounds.width() / 1.5);
                                    height = (int) (bounds.height() / 1.5);
                                } else {
                                    DisplayMetrics displayMetrics = new DisplayMetrics();
                                    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                    width = (int) (displayMetrics.widthPixels / 1.5);
                                    height = (int) (displayMetrics.heightPixels / 1.5);
                                }
                                int sz = Math.min(width, height);

                                // Use FrameLayout.LayoutParams since MaterialCardView extends FrameLayout
                                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sz, sz);
                                lp.gravity = Gravity.CENTER;
                                mCover.setLayoutParams(lp);
                            } else {
                                Log.w(TAG, "Failed to create URI for cover image: " + i.getAbsolutePath());
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Error setting cover image", e);
                        }
                    }
                }

                mRestartButton.setVisibility(getBookmark().exists() ? View.VISIBLE : View.GONE);
            } catch (Exception e) {
                Log.e(TAG, "Error in showData", e);
                Toast.makeText(this, "Error loading game data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File getBookmark() {
        if (mGameFile == null) {
            Log.w(TAG, "getBookmark: mGameFile is null");
            return new File(""); // Return empty file that won't exist
        }
        return new File(
                Paths.gameStateDir(this,Uri.parse(mGameFile.getAbsolutePath()), mGameIfid), "bookmark");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGameIfid != null && mGameFile != null)
            mRestartButton.setVisibility(getBookmark().exists() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.restart) {
            askRestartGame();
        } else if (id == R.id.fab_play) {
            openGame();
        }
    }

    private String getZcodeVersion() {
        if (mGameFile == null) {
            Log.w(TAG, "getZcodeVersion: mGameFile is null");
            return "unknown";
        }
        
        int zver = 0;
        try {
            RandomAccessFile f = new RandomAccessFile(mGameFile.getAbsolutePath(), "r");
            zver = f.read();
            f.close();
        } catch (Exception ex) {
            Log.w(TAG, "Error reading ZCode version from " + mGameFile.getAbsolutePath(), ex);
        }
        if (zver == 0) return "unknown";
        else if (zver == 70) return "unknown (blorbed)";
        else return Integer.toString(zver);
    }

    private String getTerp() {
        if (mGameFile == null) {
            Log.w(TAG, "getTerp: mGameFile is null");
            return "frotz"; // Default interpreter
        }
        
        String ext = Utils.getFileExtension(mGameFile).toLowerCase();
        ext = "|" + ext + "|";

        if ("|ulx|blb|blorb|glb|gblorb|".indexOf(ext) > -1) return "git";
        else if ("|gam|t2|t3|".indexOf(ext) > -1) return "tads";
        else /* *.z[1-9] dat zcode zblorb zblb */
            return (getZcodeVersion().compareTo("6") == 0) ? "nitfol" : "frotz";
    }

    private void openGame() {
        // Null safety check for mGameFile
        if (mGameFile == null) {
            Log.e(TAG, "openGame: mGameFile is null, cannot open game");
            Toast.makeText(this, "Game file not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Verify file exists
        if (!mGameFile.exists()) {
            Log.e(TAG, "openGame: game file does not exist: " + mGameFile.getAbsolutePath());
            Toast.makeText(this, "Game file not found on device", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(mGameFile.getAbsolutePath()),
                this,
                Interpreter.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("terp", getTerp());
        intent.putExtra("ifid", mGameIfid);
        intent.putExtra("loadBookmark", true);

        /*Fix of Theatre screen issue,
		  it prompts the user with a dialog to the automatically rotate the screen.
		  Once rotated it is loadable in any mode.*/

        //System.out.println(getResources().getDisplayMetrics().widthPixels +"|" + getResources().getDisplayMetrics().densityDpi);
        //TODO Get minimum IFs supported screen size from game file
        float screen = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        //System.out.println(mGameIfid + "|" + screen);
        if (mGameIfid != null && mGameIfid.equals("ZCODE-2-951203-A9FD") && screen <= 2.7f && !getBookmark().exists()) {
            //tested up to 2.54 = 540/213 then jumps to 3.38 = 540/160
            rotateDialog(intent);
        } else
            startActivity(intent);
    }

    private void askRestartGame() {
        new MaterialAlertDialogBuilder(this)
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

    /* swipe through Ifs */
    private class SwipeDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) {
                return false;
            }

            int position = getIntent().getIntExtra("position", -1);
            long[] ifIDs = getIntent().getLongArrayExtra("ifIDs");

            if (ifIDs == null || position == -1) {
                return false;
            }

            if (e1.getX() - e2.getX() > MIN_DISTANCE) { //swipe right
                if (++position == ifIDs.length) //increment and check if reached end
                    position = 0;
                Intent i = new Intent(Intent.ACTION_VIEW, Games.uriOfIfid(ifIDs[position] + ""), GameDetails.this, GameDetails.class);
                i.putExtra("position", position);
                i.putExtra("ifIDs", ifIDs);
                startActivity(i);
                // Use modern activity transitions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.right_left, R.anim.center);
                } else {
                    overridePendingTransition(R.anim.right_left, R.anim.center);
                }
                finish();
                return true;
            }
            if (e2.getX() - e1.getX() > MIN_DISTANCE) {//swipe left
                if (--position == -1)//decrement and check if reached begin
                    position = ifIDs.length - 1;
                Intent i = new Intent(Intent.ACTION_VIEW, Games.uriOfIfid(ifIDs[position] + ""), GameDetails.this, GameDetails.class);
                i.putExtra("position", position);
                i.putExtra("ifIDs", ifIDs);
                startActivity(i);
                // Use modern activity transitions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.left_right, R.anim.center);
                } else {
                    overridePendingTransition(R.anim.left_right, R.anim.center);
                }
                finish();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (gestureDetector != null) {
            if (gestureDetector.onTouchEvent(e))
                return true;
        }
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }

    private void rotateDialog(final Intent intent) {
        MaterialAlertDialogBuilder rotateDialog = new MaterialAlertDialogBuilder(this);
        setUpAlertTheatre(rotateDialog, intent);
    }

    private void setUpAlertTheatre(MaterialAlertDialogBuilder rotateDialog, final Intent intent) {
        rotateDialog.setTitle("Theatre")
                .setMessage(R.string.theatre_message)
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            intent.putExtra("landscape", true);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getBaseContext(), "Sorry, device screen too small to play ''Theatre''.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(intent);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Shutdown background executor
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
        
        synchronized (cursorLock) {
            // Properly close the cursor to prevent memory leaks
            if (mQuery != null && !mQuery.isClosed()) {
                mQuery.unregisterContentObserver(mContentObserver);
                mQuery.close();
                mQuery = null;
            }
        }
    }
    
    private void refreshGameData() {
        // Ensure we're on the UI thread
        if (Thread.currentThread() != getMainLooper().getThread()) {
            runOnUiThread(() -> refreshGameData());
            return;
        }
        
        synchronized (cursorLock) {
            // Close the old cursor and create a new one to get fresh data
            if (mQuery != null && !mQuery.isClosed()) {
                mQuery.unregisterContentObserver(mContentObserver);
                mQuery.close();
            }
            
            try {
                Uri game = getIntent().getData();
                mQuery = getContentResolver().query(game, PROJECTION, null, null, null);
                if (mQuery != null) {
                    mQuery.registerContentObserver(mContentObserver);
                    showData();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error refreshing game data", e);
                Toast.makeText(this, "Error refreshing game data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
