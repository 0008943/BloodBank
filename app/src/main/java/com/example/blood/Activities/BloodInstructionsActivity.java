package com.example.blood.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BloodInstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_instructions);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Navigation components
        LinearLayout homeButton = findViewById(R.id.home_button);
        LinearLayout donorsButton = findViewById(R.id.donors_button);
        LinearLayout needButton = findViewById(R.id.need_button);
        LinearLayout menuBottomNav = findViewById(R.id.menu_bottom_nav);
        FloatingActionButton makeRequestFab = findViewById(R.id.make_request_fab);

        // Set Click Listeners for Navigation
        if (homeButton != null) homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        if (donorsButton != null) donorsButton.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        if (needButton != null) needButton.setOnClickListener(v -> startActivity(new Intent(this, MakeRequestActivity.class)));
        if (menuBottomNav != null) menuBottomNav.setOnClickListener(v -> startActivity(new Intent(this, MenuActivity.class)));
        if (makeRequestFab != null) makeRequestFab.setOnClickListener(v -> startActivity(new Intent(this, MakeRequestActivity.class)));
    }
}
