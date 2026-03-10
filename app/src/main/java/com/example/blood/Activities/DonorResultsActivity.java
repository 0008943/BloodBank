package com.example.blood.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blood.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DonorResultsActivity extends AppCompatActivity {

    private RecyclerView rvDonors;
    private TextView tvNoDonors;
    private DonorAdapter adapter;
    private List<User> donorList;
    private String targetBloodGroup;
    private String targetCity;
    private String targetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_results);

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
        }

        @Override
        public int getItemCount() { return donors.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, location, bloodGroup;
            ImageView avatar;
            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.donor_name);
                location = v.findViewById(R.id.donor_location);
                bloodGroup = v.findViewById(R.id.blood_group_text);
                avatar = v.findViewById(R.id.donor_avatar);
            }
        }
    }
}
