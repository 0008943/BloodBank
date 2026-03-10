package com.example.blood.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blood.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        View header = findViewById(R.id.header_container);
        ImageView btnBack = findViewById(R.id.btnBack);
        View cityCard = findViewById(R.id.search_city).getParent().getParent() instanceof View ? (View) findViewById(R.id.search_city).getParent().getParent() : null;
        View bloodCard = findViewById(R.id.search_blood_group).getParent().getParent() instanceof View ? (View) findViewById(R.id.search_blood_group).getParent().getParent() : null;
        View searchButtonCard = findViewById(R.id.find_donors_button).getParent() instanceof View ? (View) findViewById(R.id.find_donors_button).getParent() : null;

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

        if (header != null) {
            header.setAlpha(0f);
            header.setTranslationY(-100f);
            header.animate().alpha(1f).translationY(0f).setDuration(800).setInterpolator(new OvershootInterpolator(1.2f)).start();
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        if (cityCard != null) {
            cityCard.setAlpha(0f);
            cityCard.setTranslationY(100f);
            cityCard.animate().alpha(1f).translationY(0f).setDuration(700).setStartDelay(300).setInterpolator(new OvershootInterpolator(0.8f)).start();
        }

        if (bloodCard != null) {
            bloodCard.setAlpha(0f);
            bloodCard.setTranslationY(100f);
            bloodCard.animate().alpha(1f).translationY(0f).setDuration(700).setStartDelay(450).setInterpolator(new OvershootInterpolator(0.8f)).start();
        }

        if (searchButtonCard != null) {
            searchButtonCard.setAlpha(0f);
            searchButtonCard.setScaleX(0.5f);
            searchButtonCard.setScaleY(0.5f);
            searchButtonCard.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).setStartDelay(600).setInterpolator(new OvershootInterpolator(1.5f)).start();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }
}
