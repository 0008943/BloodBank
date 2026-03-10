package com.example.blood.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blood.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BecomeDonorActivity extends AppCompatActivity {

    private TextInputEditText etBloodGroup, etPhone;
    private MaterialCheckBox cbAgreed;
    private MaterialButton btnSubmit;
    private DatabaseReference databaseReference;
    private String loggedMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_donor);

        etBloodGroup = findViewById(R.id.etBloodGroup);
        etPhone = findViewById(R.id.etPhone);
        cbAgreed = findViewById(R.id.cbAgreed);
        btnSubmit = findViewById(R.id.btnSubmit);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        loggedMobile = sharedPreferences.getString("loggedMobile", null);

        if (loggedMobile != null) {
            loadUserData();
        } else {
            finish();
        }

        btnSubmit.setOnClickListener(v -> {
            if (cbAgreed.isChecked()) {
                registerAsDonor();
            } else {
                Toast.makeText(this, "Please agree to the criteria first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(loggedMobile);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String bloodGroup = snapshot.child("bloodGroup").getValue(String.class);
                    etBloodGroup.setText(bloodGroup);
                    etPhone.setText(loggedMobile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void registerAsDonor() {
        databaseReference.child("isAvailable").setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Also update local preference to keep UI in sync
                SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("available_to_donate", true).apply();
                
                Toast.makeText(this, "You are now a registered donor!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
