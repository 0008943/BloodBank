package com.example.blood.Activities;

import android.os.Bundle;
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

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvNoHistory;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        tvNoHistory = findViewById(R.id.tvNoHistory);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        // Since we don't have a specific history node yet, this is a placeholder
        // In a real app, you'd fetch from "history/loggedMobile"
        tvNoHistory.setVisibility(View.VISIBLE);
        rvHistory.setVisibility(View.GONE);
    }

    private class HistoryItem {
        String title, location, date;
        HistoryItem(String title, String location, String date) {
            this.title = title;
            this.location = location;
            this.date = date;
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryItem> items;
        HistoryAdapter(List<HistoryItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItem item = items.get(position);
            holder.title.setText(item.title);
            holder.location.setText(item.location);
            holder.date.setText(item.date);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, location, date;
            ViewHolder(View v) {
                super(v);
                title = v.findViewById(R.id.tvTitle);
                location = v.findViewById(R.id.tvLocation);
                date = v.findViewById(R.id.tvDate);
            }
        }
    }
}
