package com.example.blood.Activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.blood.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String donorMobile;
    private Marker donorMarker;
    private TextView tvDonorName;
    private DatabaseReference donorRef;
    private ValueEventListener donorLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        donorMobile = getIntent().getStringExtra("donorMobile");
        tvDonorName = findViewById(R.id.tvDonorName);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (donorMobile == null || donorMobile.isEmpty()) {
            Toast.makeText(this, "Donor information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        trackDonorLocation();
    }

    private void trackDonorLocation() {
        donorRef = FirebaseDatabase.getInstance().getReference("Users").child(donorMobile);
        donorLocationListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    Double lat = snapshot.child("latitude").getValue(Double.class);
                    Double lng = snapshot.child("longitude").getValue(Double.class);

                    tvDonorName.setText("Donor: " + (name != null ? name : "Unknown"));

                    if (lat != null && lng != null) {
                        LatLng donorLatLng = new LatLng(lat, lng);
                        if (donorMarker == null) {
                            donorMarker = mMap.addMarker(new MarkerOptions()
                                    .position(donorLatLng)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(donorLatLng, 15f));
                        } else {
                            donorMarker.setPosition(donorLatLng);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrackingActivity.this, "Failed to track donor", Toast.LENGTH_SHORT).show();
            }
        };
        donorRef.addValueEventListener(donorLocationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (donorRef != null && donorLocationListener != null) {
            donorRef.removeEventListener(donorLocationListener);
        }
    }
}
