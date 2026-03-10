package com.example.blood.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blood.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText searchCity, searchBloodGroup;
    private MaterialButton findDonorsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        searchCity = findViewById(R.id.search_city);
        searchBloodGroup = findViewById(R.id.search_blood_group);
        findDonorsButton = findViewById(R.id.find_donors_button);
        View header = findViewById(R.id.header_container);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Search logic
        if (findDonorsButton != null) {
            findDonorsButton.setOnClickListener(v -> {
                String city = searchCity.getText().toString().trim();
                String bloodGroup = searchBloodGroup.getText().toString().trim();

                if (city.isEmpty()) {
                    searchCity.setError("City is required");
                    return;
                }
                if (bloodGroup.isEmpty()) {
                    searchBloodGroup.setError("Blood Group is required");
                    return;
                }

                Intent intent = new Intent(SearchActivity.this, DonorResultsActivity.class);
                intent.putExtra("city", city);
                intent.putExtra("bloodGroup", bloodGroup);
                startActivity(intent);
            });
        }

        // Navigation components
        setupNavigation();

        if (header != null) {
            header.setAlpha(0f);
            header.setTranslationY(-100f);
            header.animate().alpha(1f).translationY(0f).setDuration(800).setInterpolator(new OvershootInterpolator(1.2f)).start();
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }
    }

    private void setupNavigation() {
        LinearLayout homeButton = findViewById(R.id.home_button);
        LinearLayout donorsButton = findViewById(R.id.donors_button);
        LinearLayout needButton = findViewById(R.id.need_button);
        LinearLayout menuBottomNav = findViewById(R.id.menu_bottom_nav);
        FloatingActionButton makeRequestFab = findViewById(R.id.make_request_fab);

        if (homeButton != null) homeButton.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        if (donorsButton != null) donorsButton.setOnClickListener(v -> {
            // Already here, maybe just scroll to top or refresh
        });
        if (needButton != null) needButton.setOnClickListener(v -> startActivity(new Intent(this, MakeRequestActivity.class)));
        if (menuBottomNav != null) menuBottomNav.setOnClickListener(v -> startActivity(new Intent(this, MenuActivity.class)));
        if (makeRequestFab != null) makeRequestFab.setOnClickListener(v -> startActivity(new Intent(this, MakeRequestActivity.class)));
    }
}
