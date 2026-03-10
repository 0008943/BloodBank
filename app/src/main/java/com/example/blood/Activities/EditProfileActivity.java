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

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        loggedMobile = sharedPreferences.getString("loggedMobile", null);

        if (loggedMobile != null) {
            loadUserData();
        } else {
            finish();
        }

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> updateProfile());

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
            
            // Resize image to keep database small
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] b = baos.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            
            editProfileImage.setImageBitmap(scaledBitmap);
            Toast.makeText(this, "Image Prepared!", Toast.LENGTH_SHORT).show();
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
                        byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        editProfileImage.setImageBitmap(decodedByte);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String bloodGroup = actvBloodGroup.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Name required");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bloodGroup", bloodGroup);
        if (!encodedImage.isEmpty()) {
            updates.put("profileImageUrl", encodedImage);
        }

        databaseReference.updateChildren(updates).addOnCompleteListener(task -> {
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
