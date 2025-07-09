/*
    Thread-safe crash guard for cursor and database operations.
    Prevents IllegalStateException crashes in cursor operations.
 */

package org.andglkmod.hunkypunk;

import android.database.Cursor;
import android.os.Looper;
import android.util.Log;

public class CrashGuard {
    private static final String TAG = "CrashGuard";
    
    /**
     * Safely closes a cursor, checking for null and closed state
     */
    public static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            try {
                cursor.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing cursor", e);
            }
        }
    }
    
    /**
     * Safely moves cursor to first position with error handling
     */
    public static boolean safeMoveToFirst(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return false;
        }
        
        try {
            return cursor.moveToFirst();
        } catch (Exception e) {
            Log.w(TAG, "Error moving cursor to first", e);
            return false;
        }
    }
    
    /**
     * Safely gets string from cursor with error handling
     */
    public static String safeGetString(Cursor cursor, int columnIndex) {
        if (cursor == null || cursor.isClosed()) {
            return null;
        }
        
        try {
            return cursor.getString(columnIndex);
        } catch (Exception e) {
            Log.w(TAG, "Error getting string from cursor", e);
            return null;
        }
    }
    
    /**
     * Safely gets long from cursor with error handling
     */
    public static long safeGetLong(Cursor cursor, int columnIndex) {
        if (cursor == null || cursor.isClosed()) {
            return 0;
        }
        
        try {
            return cursor.getLong(columnIndex);
        } catch (Exception e) {
            Log.w(TAG, "Error getting long from cursor", e);
            return 0;
        }
    }
    
    /**
     * Checks if we're on the UI thread
     */
    public static boolean isUIThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    
    /**
     * Safely checks if cursor is null or closed
     */
    public static boolean isCursorValid(Cursor cursor) {
        return cursor != null && !cursor.isClosed();
    }
}
