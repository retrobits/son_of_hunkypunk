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

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.content.Context;
import android.widget.Toast;

import java.io.File;

public abstract class Paths {
    private static File ifDirectory;

    public static File cardDirectory() {
        return new File(Environment.getExternalStorageDirectory().getPath());
    }
    
    public static File appDirectory(Context c) {
        return c.getExternalFilesDir(null);
    }

    public static File oldAppDirectory() {
        File f = new File(cardDirectory(), "Android/data/org.andglkmod.hunkypunk");
        return f;
    }

    public static File gameStateRootDirectory(Context c) {
        File f = new File(appDirectory(c), "gamestate");
        if (!f.exists()) f.mkdir();
        return f;
    }

    public static File coverDirectory(Context c) {
        File f = new File(appDirectory(c), "covers");
        if (!f.exists()) f.mkdir();
        return f;
    }

    public static File tempDirectory(Context c) {
        File f = new File(appDirectory(c), "temp");
        if (!f.exists()) f.mkdir();
        return f;
    }

    public static File fontDirectory(Context c) {
        File f = new File(appDirectory(c), "Fonts");
        if (!f.exists()) f.mkdir();
        return f;
    }

    public static File gameStateDir(Context c, Uri uri, String ifid) {
        return gameStateDir(c,uri.getPath(),ifid);
    }
    public static File gameStateDir(Context c, String path, String ifid) {
        File fGame = new File(path);

        File fData = Paths.gameStateRootDirectory(c);

        //search
        String dirName = fGame.getName()+"."+ifid;
        GameDataDirFilter filter = new GameDataDirFilter(ifid);
        File[] fs = fData.listFiles(filter);
        if (fs != null && fs.length>0)
            dirName = fs[0].getName();

        File f = new File(fData, dirName);
        if (!f.exists()) f.mkdir();

        return f;
    }

    public static File oldGameStateDir(Uri uri, String ifid) {
        return oldGameStateDir(uri.getPath(),ifid);
    }
    public static File oldGameStateDir(String path, String ifid) {
        File fGame = new File(path);

        File fData = Paths.oldAppDirectory();

        //search
        String dirName = fGame.getName()+"."+ifid;
        GameDataDirFilter filter = new GameDataDirFilter(ifid);
        File[] fs = fData.listFiles(filter);
        if (fs != null && fs.length>0)
            dirName = fs[0].getName();

        return new File(fData, dirName);
    }

    public static boolean isIfDirectoryValid(Context c) {
        // App-specific storage is always available and doesn't require permissions
        File ifDir = ifDirectory(c);
        return ifDir.exists() || ifDir.mkdirs();
    }

    public static File ifDirectory(Context c) {
        // Always use app-specific storage - no permissions required
        File f = defaultIfDirectory(c);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    public static File defaultIfDirectory(Context c) {
        File a = appDirectory(c);
        File f = new File(a, "Interactive Fiction");
        return f;
    }

    /**
     * Get the public Documents directory path for better user accessibility.
     * This provides a user-accessible location for game files.
     */
    public static File getPublicGamesDirectory() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File gamesDir = new File(documentsDir, "Interactive Fiction");
            if (!gamesDir.exists()) {
                gamesDir.mkdirs();
            }
            return gamesDir;
        } else {
            // Fallback for older Android versions
            File sdcard = Environment.getExternalStorageDirectory();
            File gamesDir = new File(sdcard, "Documents/Interactive Fiction");
            if (!gamesDir.exists()) {
                gamesDir.mkdirs();
            }
            return gamesDir;
        }
    }

    /**
     * Get a user-friendly path description for the games directory
     */
    public static String getGameDirectoryDescription(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return "Use the 'Add Games' button to select game files from any location on your device.";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return "Games are stored in app-specific storage. Use 'Add Games' button to import files.";
        } else {
            return getPublicGamesDirectory().getAbsolutePath();
        }
    }

    public static void setIfDirectory(File file) {
        ifDirectory = file;
    }
    
    public static File transcriptDirectory(Context c) {
        File f = new File(ifDirectory(c), "transcripts");
        if (!f.exists()) f.mkdir();
        return f;
    }
}