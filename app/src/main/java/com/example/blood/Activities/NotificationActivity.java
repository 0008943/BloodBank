package com.example.blood.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvNotifications = findViewById(R.id.rvNotifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        rvNotifications.setAdapter(adapter);

        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String loggedMobile = sharedPreferences.getString("loggedMobile", null);

        if (loggedMobile != null) {
            fetchNotifications(loggedMobile);
        }

        setupNavigation();
    }

    private void fetchNotifications(String mobile) {
        databaseReference = FirebaseDatabase.getInstance().getReference("notifications").child(mobile);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    notificationList.add(notification);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<Notification> notifications;

        public NotificationAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification notification = notifications.get(position);
            holder.tvName.setText(notification.getName());
            holder.tvMessage.setText(notification.getMessage());
            holder.tvTime.setText(notification.getTime());

            String imageStr = notification.getAvatarUrl();
            if (imageStr != null && !imageStr.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.ivAvatar.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    holder.ivAvatar.setImageResource(R.drawable.ic_person_cartoon);
                }
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_person_cartoon);
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMessage, tvTime;
            ImageView ivAvatar;

            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvMessage = v.findViewById(R.id.tvMessage);
                tvTime = v.findViewById(R.id.tvTime);
                ivAvatar = v.findViewById(R.id.ivAvatar);
            }
        }
    }
}
