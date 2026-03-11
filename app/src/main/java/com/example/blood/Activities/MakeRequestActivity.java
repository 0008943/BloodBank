package com.example.blood.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blood.R;
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
import java.util.List;
import java.util.Locale;

public class MakeRequestActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private RequestAdapter adapter;
    private List<DonationRequest> requestList;
    private DatabaseReference databaseReference;
    private String loggedMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        loggedMobile = sharedPreferences.getString("loggedMobile", null);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnNotification = findViewById(R.id.btnNotification);
        FloatingActionButton fabAddRequest = findViewById(R.id.make_request_fab);

        rvRequests = findViewById(R.id.rvRequests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        requestList = new ArrayList<>();
        adapter = new RequestAdapter(requestList);
        rvRequests.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("donationRequests");

        fetchRequests();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }
        
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        if (fabAddRequest != null) {
            fabAddRequest.setOnClickListener(v -> {
                Toast.makeText(this, "Add New Request Clicked", Toast.LENGTH_SHORT).show();
            });
        }

        setupNavigation();
    }

    private void fetchRequests() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DonationRequest request = dataSnapshot.getValue(DonationRequest.class);
                    if (request != null) {
                        requestList.add(request);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MakeRequestActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        findViewById(R.id.home_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        findViewById(R.id.donors_button).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        findViewById(R.id.need_button).setOnClickListener(v -> { /* Already here */ });
        findViewById(R.id.menu_bottom_nav).setOnClickListener(v -> {
            startActivity(new Intent(this, MenuActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
        private List<DonationRequest> requests;

        public RequestAdapter(List<DonationRequest> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DonationRequest request = requests.get(position);
            
            holder.tvBloodGroup.setText(request.getBloodGroup());
            holder.tvTitle.setText("Emergency " + request.getBloodGroup() + " Blood Needed");
            holder.tvLocation.setText(request.getLocation());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.tvDate.setText(sdf.format(new Date(request.getTimestamp())));

            if ("accepted".equals(request.getStatus())) {
                holder.btnDecline.setVisibility(View.GONE);
                if (loggedMobile != null && loggedMobile.equals(request.getUserMobile())) {
                    holder.btnAccept.setText("Track Donor");
                    holder.btnAccept.setOnClickListener(v -> {
                        Intent intent = new Intent(MakeRequestActivity.this, TrackingActivity.class);
                        intent.putExtra("donorMobile", request.getAcceptedBy());
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    });
                } else if (loggedMobile != null && loggedMobile.equals(request.getAcceptedBy())) {
                    holder.btnAccept.setText("Accepted (You)");
                    holder.btnAccept.setEnabled(false);
                } else {
                    holder.btnAccept.setText("Accepted");
                    holder.btnAccept.setEnabled(false);
                }
            } else {
                holder.btnDecline.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Accept");
                holder.btnAccept.setEnabled(true);
                
                if (loggedMobile != null && loggedMobile.equals(request.getUserMobile())) {
                    holder.btnAccept.setEnabled(false);
                    holder.btnDecline.setEnabled(false);
                }

                holder.btnAccept.setOnClickListener(v -> acceptRequest(request));
                holder.btnDecline.setOnClickListener(v -> Toast.makeText(MakeRequestActivity.this, "Request Declined", Toast.LENGTH_SHORT).show());
            }
        }

        private void acceptRequest(DonationRequest request) {
            if (loggedMobile == null) return;
            
            DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("donationRequests").child(request.getRequestId());
            reqRef.child("status").setValue("accepted");
            reqRef.child("acceptedBy").setValue(loggedMobile).addOnSuccessListener(aVoid -> {
                Toast.makeText(MakeRequestActivity.this, "Request Accepted! Location sharing started.", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvBloodGroup, tvTitle, tvLocation, tvDate;
            MaterialButton btnAccept, btnDecline;

            ViewHolder(View v) {
                super(v);
                tvBloodGroup = v.findViewById(R.id.tvBloodGroup);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvLocation = v.findViewById(R.id.tvLocation);
                tvDate = v.findViewById(R.id.tvDate);
                btnAccept = v.findViewById(R.id.btnAccept);
                btnDecline = v.findViewById(R.id.btnDecline);
            }
        }
    }
}
