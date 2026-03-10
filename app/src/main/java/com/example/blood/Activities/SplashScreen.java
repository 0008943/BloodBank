package com.example.blood.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blood.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

                String loggedMobile = sharedPreferences.getString("loggedMobile", null);
                
                if (loggedMobile != null) {
                    // User is logged in, go to Home
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                } else {
                    // Not logged in, check if it's the first run for onboarding
                    boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
                    if (isFirstRun) {
                        startActivity(new Intent(SplashScreen.this, OnboardingActivity.class));
                    } else {
                        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                    }
                }
                finish();
            }
        }, 2500);
    }
}
