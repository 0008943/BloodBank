package com.example.blood.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blood.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ManageAddressActivity extends AppCompatActivity {

    private RecyclerView rvAddresses;
    private TextView tvNoAddresses;
    private MaterialButton btnAddAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_address);

        rvAddresses = findViewById(R.id.rvAddresses);
        tvNoAddresses = findViewById(R.id.tvNoAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        ImageView btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        
        // Placeholder logic
        loadAddresses();

        btnAddAddress.setOnClickListener(v -> {
            // Logic to add address
        });
    }

    private void loadAddresses() {
        // Fetch from Firebase logic here
        tvNoAddresses.setVisibility(View.VISIBLE);
        rvAddresses.setVisibility(View.GONE);
    }
}
