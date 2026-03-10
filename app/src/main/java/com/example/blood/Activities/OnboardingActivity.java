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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.blood.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView btnNext, tvSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        tvSkip = findViewById(R.id.tvSkip);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() + 1 < adapter.getItemCount()) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                finishOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> finishOnboarding());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == adapter.getItemCount() - 1) {
                    btnNext.setText("Finish");
                } else {
                    btnNext.setText("Next");
                }
            }
        });
    }

    private void finishOnboarding() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isFirstRun", false).apply();
        startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
        finish();
    }

    class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId;
            if (viewType == 0) {
                layoutId = R.layout.onboarding_page1;
            } else if (viewType == 1) {
                layoutId = R.layout.onboarding_page2;
            } else {
                layoutId = R.layout.onboarding_page3;
            }
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Content is defined in layout XML files
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
