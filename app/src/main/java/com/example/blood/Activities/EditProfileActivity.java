package com.example.blood.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.blood.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone;
    private AutoCompleteTextView actvBloodGroup;
    private MaterialButton btnSave;
    private ImageView btnBack, editProfileImage;
    private MaterialCardView cardProfileImage;
    private DatabaseReference databaseReference;
    private String loggedMobile;
    private ActivityResultLauncher<String> mGetContent;
    private String encodedImage = "";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        actvBloodGroup = findViewById(R.id.actvBloodGroup);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        editProfileImage = findViewById(R.id.edit_profile_image);
        cardProfileImage = findViewById(R.id.cardProfileImage);

        sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        loggedMobile = sharedPreferences.getString("loggedMobile", null);

        // Setup Image Picker
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        processImage(uri);
                    }
                });

        cardProfileImage.setOnClickListener(v -> mGetContent.launch("image/*"));

        setupNavigation();

        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bloodGroups);
        actvBloodGroup.setAdapter(adapter);

        if (loggedMobile != null) {
            loadUserData();
        } else {
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> startProfileUpdate());

        View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }
    }

    private void processImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] b = baos.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            editProfileImage.setImageBitmap(scaledBitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(loggedMobile);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etPhone.setText(loggedMobile);
                    actvBloodGroup.setText(snapshot.child("bloodGroup").getValue(String.class), false);
                    String imageStr = snapshot.child("profileImageUrl").getValue(String.class);

                    if (imageStr != null && !imageStr.isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            editProfileImage.setImageBitmap(decodedByte);
                        } catch (Exception ignored) {}
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startProfileUpdate() {
        String newName = etName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();
        String newBloodGroup = actvBloodGroup.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPhone.equals(loggedMobile)) {
            // Phone number changed - need to migrate account
            checkIfNewPhoneExists(newPhone, newName, newBloodGroup);
        } else {
            // Only other data changed
            updateProfileData(loggedMobile, newName, newBloodGroup);
        }
    }

    private void checkIfNewPhoneExists(String newPhone, String name, String bloodGroup) {
        FirebaseDatabase.getInstance().getReference("Users").child(newPhone)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            etPhone.setError("This phone number is already registered to another account");
                        } else {
                            migrateAccount(newPhone, name, bloodGroup);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void migrateAccount(String newPhone, String name, String bloodGroup) {
        DatabaseReference oldRef = FirebaseDatabase.getInstance().getReference("Users").child(loggedMobile);
        DatabaseReference newRef = FirebaseDatabase.getInstance().getReference("Users").child(newPhone);

        oldRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                    if (data != null) {
                        data.put("name", name);
                        data.put("number", newPhone);
                        data.put("bloodGroup", bloodGroup);
                        if (!encodedImage.isEmpty()) {
                            data.put("profileImageUrl", encodedImage);
                        }

                        newRef.setValue(data).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                oldRef.removeValue(); // Delete old account
                                sharedPreferences.edit().putString("loggedMobile", newPhone).apply();
                                loggedMobile = newPhone;
                                Toast.makeText(EditProfileActivity.this, "Account Migrated Successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateProfileData(String mobile, String name, String bloodGroup) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(mobile);
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bloodGroup", bloodGroup);
        if (!encodedImage.isEmpty()) {
            updates.put("profileImageUrl", encodedImage);
        }

        ref.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                finish();
            }
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
