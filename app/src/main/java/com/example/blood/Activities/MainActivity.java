package com.example.blood.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blood.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView tvHelloName, tvUserLocation, tvBannerDays, tvBannerTitle;
    private ImageView userAvatar, btnNotification;
    private DatabaseReference databaseReference;
    private String loggedMobile;
    private CardView findDonorsCard, requestBloodCard, bloodInstructionsCard;
    private LinearLayout userInfoLayout, donorsButton, needButton, menuBottomNav;
    private FusedLocationProviderClient fusedLocationClient;
    private BarChart bloodRequestsChart;
    private MaterialButton btnBecomeDonor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvHelloName = findViewById(R.id.tvHelloName);
        tvUserLocation = findViewById(R.id.tvUserLocation);
        tvBannerDays = findViewById(R.id.tvBannerDays);
        tvBannerTitle = findViewById(R.id.tvBannerTitle);
        btnBecomeDonor = findViewById(R.id.btnBecomeDonor);
        userAvatar = findViewById(R.id.user_avatar);
        RecyclerView donorRecyclerView = findViewById(R.id.donor_recycler_view);
        FloatingActionButton makeRequestFab = findViewById(R.id.make_request_fab);
        menuBottomNav = findViewById(R.id.menu_bottom_nav);
        btnNotification = findViewById(R.id.btnNotification);
        findDonorsCard = findViewById(R.id.find_donors_card);
        requestBloodCard = findViewById(R.id.request_blood_card);
        bloodInstructionsCard = findViewById(R.id.blood_instructions_card);
        userInfoLayout = findViewById(R.id.user_info_layout);
        donorsButton = findViewById(R.id.donors_button);
        needButton = findViewById(R.id.need_button);
        bloodRequestsChart = findViewById(R.id.blood_requests_chart);

        setupChart();

        if (donorRecyclerView != null) {
            donorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        loggedMobile = sharedPreferences.getString("loggedMobile", null);

        if (loggedMobile != null) {
            loadUserData();
            fetchBloodRequests();
            updateLiveLocation();
        }

        setClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        animateView(findViewById(R.id.header_container), -100f, 0);
        animateView(findViewById(R.id.banner_scroll), 100f, 200);
        animateView(findViewById(R.id.menu_grid), 100f, 400);
        animateView(findViewById(R.id.blood_needed_card), 100f, 500);
        animateView(findViewById(R.id.donation_request_header), 100f, 600);
        
        if (makeRequestFab != null) {
            makeRequestFab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MakeRequestActivity.class)));
        }
    }

    private void setupChart() {
        if (bloodRequestsChart == null) return;
        bloodRequestsChart.getDescription().setEnabled(false);
        bloodRequestsChart.setDrawGridBackground(false);
        bloodRequestsChart.setDrawBarShadow(false);
        bloodRequestsChart.setDrawValueAboveBar(true);
        bloodRequestsChart.getLegend().setEnabled(false);
        bloodRequestsChart.setPinchZoom(false);
        bloodRequestsChart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = bloodRequestsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(8);

        bloodRequestsChart.getAxisLeft().setDrawGridLines(false);
        bloodRequestsChart.getAxisLeft().setAxisMinimum(0f);
        bloodRequestsChart.getAxisRight().setEnabled(false);
    }

    private void updateLiveLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && loggedMobile != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(loggedMobile);
                Map<String, Object> locationUpdate = new HashMap<>();
                locationUpdate.put("latitude", location.getLatitude());
                locationUpdate.put("longitude", location.getLongitude());
                userRef.updateChildren(locationUpdate);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLiveLocation();
        }
    }

    private void loadUserData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(loggedMobile);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String imageStr = snapshot.child("profileImageUrl").getValue(String.class);
                    String nextDonation = snapshot.child("nextDonation").getValue(String.class);

                    tvHelloName.setText("Hello " + (name != null ? name : "User") + "!");
                    tvUserLocation.setText("User ID: " + loggedMobile);

                    updateBannerCountdown(nextDonation);

                    if (imageStr != null && !imageStr.isEmpty()) {
                        try {
                            byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            userAvatar.setImageBitmap(decodedByte);
                        } catch (Exception e) {
                            userAvatar.setImageResource(R.drawable.ic_person_cartoon);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateBannerCountdown(String nextDonation) {
        if (tvBannerDays == null || tvBannerTitle == null) return;

        if (nextDonation == null || nextDonation.equals("Not available") || nextDonation.isEmpty()) {
            tvBannerTitle.setText("You are eligible to");
            tvBannerDays.setText("Donate Now!");
            if (btnBecomeDonor != null) btnBecomeDonor.setText("Become a Donor");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date donationDate = sdf.parse(nextDonation);
            Date today = new Date();

            if (donationDate != null && donationDate.after(today)) {
                long diffInMs = donationDate.getTime() - today.getTime();
                long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs);
                
                tvBannerTitle.setText("You can become a Blood Donor in");
                tvBannerDays.setText(diffInDays + (diffInDays == 1 ? " Day" : " Days"));
                if (btnBecomeDonor != null) btnBecomeDonor.setText("View Schedule");
            } else {
                tvBannerTitle.setText("You are eligible to");
                tvBannerDays.setText("Donate Now!");
                if (btnBecomeDonor != null) btnBecomeDonor.setText("Become a Donor");
            }
        } catch (Exception e) {
            tvBannerTitle.setText("Check your eligibility");
            tvBannerDays.setText("Today");
        }
    }

    private void fetchBloodRequests() {
        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("donationRequests");
        requestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> bloodGroupCounts = new HashMap<>();
                String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
                for (String g : groups) bloodGroupCounts.put(g, 0);

                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    String bloodGroup = requestSnapshot.child("bloodGroup").getValue(String.class);
                    if (bloodGroup != null && bloodGroupCounts.containsKey(bloodGroup)) {
                        Integer currentCount = bloodGroupCounts.get(bloodGroup);
                        bloodGroupCounts.put(bloodGroup, (currentCount != null ? currentCount : 0) + 1);
                    }
                }
                updateChart(bloodGroupCounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void updateChart(Map<String, Integer> counts) {
        if (bloodRequestsChart == null) return;
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

        for (int i = 0; i < groups.length; i++) {
            Integer count = counts.get(groups[i]);
            entries.add(new BarEntry(i, count != null ? count.floatValue() : 0f));
            labels.add(groups[i]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Blood Requests");
        dataSet.setColor(Color.parseColor("#F43232"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        bloodRequestsChart.setData(data);
        bloodRequestsChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        bloodRequestsChart.invalidate(); // Refresh the chart
    }

    private void setClickListeners() {
        userAvatar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EditProfileActivity.class)));
        userInfoLayout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MenuActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NotificationActivity.class)));
        findDonorsCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        requestBloodCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MakeRequestActivity.class)));
        bloodInstructionsCard.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BloodInstructionsActivity.class)));
        donorsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        needButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MakeRequestActivity.class)));
        menuBottomNav.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MenuActivity.class)));
        if (btnBecomeDonor != null) {
            btnBecomeDonor.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BecomeDonorActivity.class)));
        }
    }

    private void animateView(View view, float translationY, int delay) {
        if (view != null) {
            view.setAlpha(0f);
            view.setTranslationY(translationY);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(800)
                    .setStartDelay(delay)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
        }
    }
}
