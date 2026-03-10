package com.example.blood.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.blood.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEt, cityEt, mobileEt, bloodGroupEt, passwordEt;
    private Button btnSignUp;
    private ImageView btnBack;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        nameEt = findViewById(R.id.register_name);
        cityEt = findViewById(R.id.register_city);
        mobileEt = findViewById(R.id.register_mobile);
        bloodGroupEt = findViewById(R.id.register_blood_group);
        passwordEt = findViewById(R.id.register_password);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.btnBack);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEt.getText().toString().trim();
                String city = cityEt.getText().toString().trim();
                String mobile = mobileEt.getText().toString().trim();
                String bloodGroup = bloodGroupEt.getText().toString().trim();
                String password = passwordEt.getText().toString().trim();

                if (isValid(name, city, mobile, bloodGroup, password)) {
                    registerUser(name, city, mobile, bloodGroup, password);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean isValid(String name, String city, String mobile, String bloodGroup, String password) {
        if (name.isEmpty()) { nameEt.setError("Name is required"); return false; }
        if (city.isEmpty()) { cityEt.setError("City is required"); return false; }
        if (mobile.isEmpty()) { mobileEt.setError("Mobile number is required"); return false; }
        if (mobile.length() != 10) { mobileEt.setError("Mobile number must be 10 digits"); return false; }
        if (bloodGroup.isEmpty()) { bloodGroupEt.setError("Blood Group is required"); return false; }
        if (password.isEmpty()) { passwordEt.setError("Password is required"); return false; }
        return true;
    }

    private void registerUser(String name, String city, String mobile, String bloodGroup, String password) {

        String userId = String.valueOf(100000 + new Random().nextInt(900000));

        User user = new User(name, city, mobile, bloodGroup, password, userId);

        databaseReference.child(mobile).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isFirstRun", false);
                    editor.putString("loggedMobile", mobile); 
                    editor.apply();

                    Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Firebase Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
