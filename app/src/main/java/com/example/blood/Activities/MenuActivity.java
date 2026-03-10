package com.example.blood.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.blood.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private TextView profileName, profileUserId, tvLifeSaved, tvBloodGroup, tvNextDonationDate;
    private ImageView profileImage;
    private DatabaseReference databaseReference;
    private SwitchMaterial switchAvailable, switchNotification, switchTracking;
    private SharedPreferences sharedPreferences;
    private String loggedMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        loggedMobile = sharedPreferences.getString("loggedMobile", null);

        profileName = findViewById(R.id.profile_name);
        profileUserId = findViewById(R.id.profile_user_id);
        tvLifeSaved = findViewById(R.id.tvLifeSaved);
        tvBloodGroup = findViewById(R.id.tvBloodGroup);
        tvNextDonationDate = findViewById(R.id.tvNextDonationDate);
        profileImage = findViewById(R.id.profile_image);

        switchAvailable = findViewById(R.id.switchAvailable);
        switchNotification = findViewById(R.id.switchNotification);
        switchTracking = findViewById(R.id.switchTracking);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnEditProfile = findViewById(R.id.btnEditProfile);
        View headerContainer = findViewById(R.id.header_container);
        View statsCard = findViewById(R.id.stats_card);
        View logoutItem = findViewById(R.id.menu_item_logout);
        View historyItem = findViewById(R.id.menu_item_history);

        // Set initial states from SharedPreferences (default false/off for first open)
        switchAvailable.setChecked(sharedPreferences.getBoolean("available_to_donate", false));
        switchNotification.setChecked(sharedPreferences.getBoolean("notifications_enabled", false));
        switchTracking.setChecked(sharedPreferences.getBoolean("tracking_enabled", false));

        setupSwitchListeners();
        setupNavigation();

        if (loggedMobile != null) {
            loadUserData(loggedMobile);
        } else {
            Toast.makeText(this, "User session not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (headerContainer != null) {
            headerContainer.setAlpha(0f);
            headerContainer.setTranslationY(-100f);
            headerContainer.animate().alpha(1f).translationY(0f).setDuration(800).setInterpolator(new OvershootInterpolator(1.2f)).start();
        }

        if (statsCard != null) {
            statsCard.setAlpha(0f);
            statsCard.setScaleX(0.8f);
            statsCard.setScaleY(0.8f);
            statsCard.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800).setStartDelay(300).setInterpolator(new OvershootInterpolator(0.8f)).start();
        }

        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnEditProfile != null) btnEditProfile.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EditProfileActivity.class)));
        if (historyItem != null) historyItem.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, HistoryActivity.class)));

        if (logoutItem != null) {
            logoutItem.setOnClickListener(v -> {
                sharedPreferences.edit().remove("loggedMobile").apply();
                finishAffinity();
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

    private void setupSwitchListeners() {
        switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("available_to_donate", isChecked).apply();
            updateFirebaseSetting("isAvailable", isChecked);
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply();
        });

        switchTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("tracking_enabled", isChecked).apply();
            updateFirebaseSetting("trackingEnabled", isChecked);
        });
    }

    private void updateFirebaseSetting(String key, boolean value) {
        if (loggedMobile != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(loggedMobile);
            userRef.child(key).setValue(value);
        }
    }

    private void loadUserData(String mobile) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mobile);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String bloodGroup = snapshot.child("bloodGroup").getValue(String.class);
                    Long lifeSaved = snapshot.child("lifeSaved").getValue(Long.class);
                    String nextDonation = snapshot.child("nextDonation").getValue(String.class);
                    String imageStr = snapshot.child("profileImageUrl").getValue(String.class);

                    profileName.setText(name != null ? name : "User");
                    profileUserId.setText("User ID: " + mobile);
                    tvLifeSaved.setText((lifeSaved != null ? lifeSaved : 0) + " life saved");
                    tvBloodGroup.setText((bloodGroup != null ? bloodGroup : "N/A") + " Group");
                    tvNextDonationDate.setText(nextDonation != null ? nextDonation : "Contact Admin");

                    if (imageStr != null && !imageStr.isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profileImage.setImageBitmap(decodedByte);
                        } catch (Exception e) {
                            profileImage.setImageResource(R.drawable.ic_person_cartoon);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupNavigation() {
        findViewById(R.id.home_button).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.donors_button).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.make_request_fab).setOnClickListener(v -> startActivity(new Intent(this, MakeRequestActivity.class)));
        findViewById(R.id.need_button).setOnClickListener(v -> startActivity(new Intent(this, MakeRequestActivity.class)));
        findViewById(R.id.menu_bottom_nav).setOnClickListener(v -> startActivity(new Intent(this, MenuActivity.class)));
    }
}
