/*
 * Copyright © 2025 HunkyPunk Contributors
 * 
 * Modern Android 16 permissions helper for HunkyPunk
 */

package org.andglkmod.hunkypunk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionsHelper {
    
    private static final int PERMISSIONS_REQUEST_CODE = 1001;
    
    /**
     * Get required permissions based on Android API level
     */
    public static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        
        // Always needed
        permissions.add(Manifest.permission.INTERNET);
        
        // Storage permissions for different API levels
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6+
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) { // Android 10 and below
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        
        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        
        return permissions.toArray(new String[0]);
    }
    
    /**
     * Check if all required permissions are granted
     */
    public static boolean hasAllPermissions(Context context) {
        String[] permissions = getRequiredPermissions();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Request missing permissions
     */
    public static void requestPermissions(Activity activity) {
        String[] permissions = getRequiredPermissions();
        List<String> missingPermissions = new ArrayList<>();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        
        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity, 
                missingPermissions.toArray(new String[0]), 
                PERMISSIONS_REQUEST_CODE);
        }
    }
    
    /**
     * Check if storage permission is granted
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
}
