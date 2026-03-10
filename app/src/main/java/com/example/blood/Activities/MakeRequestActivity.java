package com.example.blood.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class MakeRequestActivity extends AppCompatActivity {

    private String selectedBloodGroup = "";
    private View selectedView = null;
    private TextInputEditText etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request);

        ImageView btnBack = findViewById(R.id.btnBack);
        View btnNotification = findViewById(R.id.btnNotification);
        MaterialButton findDonorButton = findViewById(R.id.find_donor_button);
        AutoCompleteTextView bloodUnitSpinner = findViewById(R.id.blood_unit_spinner);
        etLocation = findViewById(R.id.etLocation);

        // Setup Blood Unit Spinner
        String[] units = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, units);
        if (bloodUnitSpinner != null) {
            bloodUnitSpinner.setAdapter(adapter);
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

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show());
        }

        setupBloodGroupSelection();

        if (findDonorButton != null) {
            findDonorButton.setOnClickListener(v -> {
                if (selectedBloodGroup.isEmpty()) {
                    Toast.makeText(this, "Please select a blood group", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String location = etLocation.getText().toString().trim();
                
                Intent intent = new Intent(MakeRequestActivity.this, DonorResultsActivity.class);
                intent.putExtra("bloodGroup", selectedBloodGroup);
                intent.putExtra("location", location);
                startActivity(intent);
            });
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

    private void setupBloodGroupSelection() {
        int[] ids = {
                R.id.blood_group_a_pos, R.id.blood_group_a_neg,
                R.id.blood_group_b_pos, R.id.blood_group_b_neg,
                R.id.blood_group_ab_pos, R.id.blood_group_ab_neg,
                R.id.blood_group_o_pos, R.id.blood_group_o_neg
        };

        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

        for (int i = 0; i < ids.length; i++) {
            final int index = i;
            View view = findViewById(ids[i]);
            if (view != null) {
                TextView tv = view.findViewById(R.id.blood_group_text);
                if (tv != null) tv.setText(groups[i]);
                
                view.setOnClickListener(v -> {
                    if (selectedView != null) {
                        selectedView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    selectedView = v;
                    selectedBloodGroup = groups[index];
                    v.setBackgroundResource(R.drawable.selected_blood_bg);
                });
            }
        }
    }
}
