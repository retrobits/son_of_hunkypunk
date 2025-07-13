/*
	Copyright © 2025 Son of Hunky Punk Contributors

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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity to handle modern file selection using Storage Access Framework
 * for adding games to Hunky Punk on Android API 29+
 */
public class DocumentPickerActivity extends AppCompatActivity {
    private static final String TAG = "DocumentPickerActivity";
    private static final int PICK_GAME_FILE_REQUEST = 1001;
    private static final int PICK_MULTIPLE_GAME_FILES_REQUEST = 1002;
    
    // Supported Interactive Fiction file extensions
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        "z1", "z2", "z3", "z4", "z5", "z6", "z7", "z8",
        "Z1", "Z2", "Z3", "Z4", "Z5", "Z6", "Z7", "Z8",
        "dat", "DAT", "gam", "GAM", "t2", "T2", "t3", "T3",
        "zcode", "zblorb", "ZBLORB", "zblb", "ZBLB",
        "ulx", "blb", "blorb", "glb", "gblorb"
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Immediately start file picker
        openFilePicker();
    }

    private void openFilePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            
            // Set specific MIME types for better filtering
            String[] mimeTypes = {
                "application/octet-stream",     // General binary files
                "application/x-zmachine",       // Z-machine files
                "application/x-tads",           // TADS files
                "application/x-blorb",          // Blorb files
                "*/*"                           // Fallback for all files
            };
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            
            // Allow multiple files selection on newer Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_MULTIPLE_GAME_FILES_REQUEST);
            } else {
                startActivityForResult(intent, PICK_GAME_FILE_REQUEST);
            }
        } else {
            // Fallback for older Android versions
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_GAME_FILE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_MULTIPLE_GAME_FILES_REQUEST && data.getClipData() != null) {
                // Handle multiple files
                int count = data.getClipData().getItemCount();
                List<Uri> selectedUris = new ArrayList<>();
                
                for (int i = 0; i < count; i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    selectedUris.add(uri);
                }
                
                processSelectedFiles(selectedUris);
            } else if (data.getData() != null) {
                // Handle single file
                List<Uri> selectedUris = new ArrayList<>();
                selectedUris.add(data.getData());
                processSelectedFiles(selectedUris);
            }
        } else {
            // User cancelled or error occurred
            finish();
        }
    }

    private void processSelectedFiles(List<Uri> uris) {
        int successCount = 0;
        int totalCount = uris.size();
        
        for (Uri uri : uris) {
            if (isGameFile(uri) && copyFileToGameDirectory(uri)) {
                successCount++;
            }
        }
        
        // Show result to user
        if (successCount > 0) {
            String message = getResources().getQuantityString(
                R.plurals.games_added_successfully, successCount, successCount);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
            // Trigger rescan of games directory
            StorageManager scanner = StorageManager.getInstance(this);
            scanner.scan(Paths.ifDirectory(this));
            
            // Return to games list
            Intent intent = new Intent(this, GamesList.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.no_valid_games_selected, Toast.LENGTH_LONG).show();
        }
        
        finish();
    }

    private boolean isGameFile(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName == null) return false;
        
        String extension = getFileExtension(fileName);
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting filename", e);
            }
        }
        
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        
        return result;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    private boolean copyFileToGameDirectory(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName == null) return false;
        
        File targetDir = Paths.ifDirectory(this);
        File targetFile = new File(targetDir, fileName);
        
        // Check if file already exists - if so, try with a number suffix
        if (targetFile.exists()) {
            String nameWithoutExt = fileName;
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                nameWithoutExt = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            }
            
            int counter = 1;
            do {
                String newFileName = nameWithoutExt + "_" + counter + extension;
                targetFile = new File(targetDir, newFileName);
                counter++;
            } while (targetFile.exists() && counter < 100);
            
            if (targetFile.exists()) {
                Log.w(TAG, "Could not find unique filename for: " + fileName);
                return false;
            }
        }
        
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            
            if (inputStream == null) {
                Log.e(TAG, "Could not open input stream for: " + uri);
                return false;
            }
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            Log.i(TAG, "Successfully copied file: " + targetFile.getName() + " (" + totalBytes + " bytes)");
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Error copying file: " + fileName, e);
            if (targetFile.exists()) {
                targetFile.delete();
            }
            return false;
        }
    }
}
