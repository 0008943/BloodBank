package com.example.blood.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blood.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonorResultsActivity extends AppCompatActivity {

    private RecyclerView rvDonors;
    private TextView tvNoDonors;
    private DonorAdapter adapter;
    private List<User> donorList;
    private String targetBloodGroup;
    private String targetCity;
    private String targetLocation;
    private String loggedMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_results);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        loggedMobile = sharedPreferences.getString("loggedMobile", null);

        targetBloodGroup = getIntent().getStringExtra("bloodGroup");
        targetCity = getIntent().getStringExtra("city");
        targetLocation = getIntent().getStringExtra("location");

        rvDonors = findViewById(R.id.rvDonors);
        tvNoDonors = findViewById(R.id.tvNoDonors);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        if (rvDonors != null) {
            rvDonors.setLayoutManager(new LinearLayoutManager(this));
            donorList = new ArrayList<>();
            adapter = new DonorAdapter(donorList);
            rvDonors.setAdapter(adapter);
            searchDonors();
        }
    }

    private void searchDonors() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    showNoDonors();
                    return;
                }
                
                donorList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        // Don't show the logged in user in results
                        if (loggedMobile != null && loggedMobile.equals(user.number)) continue;

                        boolean matchBlood = targetBloodGroup == null || targetBloodGroup.isEmpty() || 
                                           (user.getBloodGroup() != null && user.getBloodGroup().equalsIgnoreCase(targetBloodGroup));
                        
                        boolean matchCity = targetCity == null || targetCity.isEmpty() || 
                                          (user.getCity() != null && user.getCity().equalsIgnoreCase(targetCity));
                        
                        boolean matchLocation = targetLocation == null || targetLocation.isEmpty() || 
                                              (user.getCity() != null && user.getCity().equalsIgnoreCase(targetLocation));

                        if (matchBlood && (matchCity || matchLocation)) {
                            donorList.add(user);
                        }
                    }
                }

                if (donorList.isEmpty()) {
                    showNoDonors();
                } else {
                    showDonors();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showNoDonors();
            }
        });
    }

    private void showNoDonors() {
        if (tvNoDonors != null) tvNoDonors.setVisibility(View.VISIBLE);
        if (rvDonors != null) rvDonors.setVisibility(View.GONE);
    }

    private void showDonors() {
        if (tvNoDonors != null) tvNoDonors.setVisibility(View.GONE);
        if (rvDonors != null) rvDonors.setVisibility(View.VISIBLE);
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void showRequestDialog(User donor) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_send_request);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTargetDonor = dialog.findViewById(R.id.tvTargetDonor);
        TextInputEditText etRequestMessage = dialog.findViewById(R.id.etRequestMessage);
        MaterialButton btnSendRequest = dialog.findViewById(R.id.btnSendRequest);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);

        tvTargetDonor.setText("To: " + donor.getName());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSendRequest.setOnClickListener(v -> {
            String message = etRequestMessage.getText().toString().trim();
            sendRequestToFirebase(donor, message);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void sendRequestToFirebase(User donor, String message) {
        if (loggedMobile == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("DirectRequests");
        String requestId = requestsRef.push().getKey();

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("requestId", requestId);
        requestData.put("senderMobile", loggedMobile);
        requestData.put("receiverMobile", donor.number);
        requestData.put("message", message);
        requestData.put("status", "pending");
        requestData.put("timestamp", System.currentTimeMillis());

        if (requestId != null) {
            requestsRef.child(requestId).setValue(requestData).addOnSuccessListener(aVoid -> {
                Toast.makeText(DonorResultsActivity.this, "Request sent to " + donor.getName(), Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(DonorResultsActivity.this, "Failed to send request", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.ViewHolder> {
        private List<User> donors;

        public DonorAdapter(List<User> donors) { this.donors = donors; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.donor_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User donor = donors.get(position);
            holder.name.setText(donor.getName() != null ? donor.getName() : "Unknown");
            holder.location.setText(donor.getCity() != null ? donor.getCity() : "Unknown Location");
            holder.bloodGroup.setText(donor.getBloodGroup() != null ? donor.getBloodGroup() : "N/A");

            String imageStr = donor.getProfileImageUrl();
            if (imageStr != null && !imageStr.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.avatar.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    holder.avatar.setImageResource(R.drawable.ic_person_cartoon);
                }
            } else {
                holder.avatar.setImageResource(R.drawable.ic_person_cartoon);
            }

            holder.btnCall.setOnClickListener(v -> {
                if (donor.number != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + donor.number));
                    startActivity(intent);
                } else {
                    Toast.makeText(DonorResultsActivity.this, "Mobile number not available", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnMessage.setOnClickListener(v -> {
                if (donor.number != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("smsto:" + donor.number));
                    intent.putExtra("sms_body", "Hello " + donor.getName() + ", I need blood for an emergency. Can you help?");
                    startActivity(intent);
                } else {
                    Toast.makeText(DonorResultsActivity.this, "Mobile number not available", Toast.LENGTH_SHORT).show();
                }
            });

            holder.itemView.setOnClickListener(v -> showRequestDialog(donor));
        }

        @Override
        public int getItemCount() { return donors.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, location, bloodGroup;
            ImageView avatar;
            MaterialButton btnCall, btnMessage;

            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.donor_name);
                location = v.findViewById(R.id.donor_location);
                bloodGroup = v.findViewById(R.id.blood_group_text);
                avatar = v.findViewById(R.id.donor_avatar);
                btnCall = v.findViewById(R.id.btnCall);
                btnMessage = v.findViewById(R.id.btnMessage);
            }
        }
    }
}
