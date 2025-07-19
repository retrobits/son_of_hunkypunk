package org.andglkmod.hunkypunk;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install splash screen for Android 12+ BEFORE calling super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
            
            // Customize the splash screen - keep it brief
            splashScreen.setKeepOnScreenCondition(() -> false);
        }
        
        // ALWAYS call super.onCreate() for all Android versions
        super.onCreate(savedInstanceState);
        
        // Start main activity with appropriate timing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For modern Android, transition immediately
            startMainActivity();
        } else {
            // For older versions, show legacy splash briefly
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::startMainActivity, 1200);
        }
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(this, GamesList.class);
        startActivity(intent);
        finish();
    }
}
