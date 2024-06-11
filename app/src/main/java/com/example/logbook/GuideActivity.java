package com.example.logbook;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.logbook.databinding.ActivityAboutBinding;
import com.example.logbook.databinding.ActivityGuideBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class GuideActivity extends AppCompatActivity {

    private ActivityGuideBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuideBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.guide);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.guide) {
                    return true;
                } else if (itemId == R.id.diary) {
                    startActivity(new Intent(GuideActivity.this, ScheduleActivity.class));
                  //  overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    overridePendingTransition(0, 0);  // Отключение анимации перехода
                    return true;
                } else if (itemId == R.id.settings) {
                    startActivity(new Intent(GuideActivity.this, ActivitySettings.class));
                   // overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    overridePendingTransition(0, 0);  // Отключение анимации перехода
                    return true;
                } else if (itemId == R.id.about) {
                    startActivity(new Intent(GuideActivity.this, ActivityAbout.class));
                   // overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    overridePendingTransition(0, 0);  // Отключение анимации перехода
                    return true;
                }
                return false;
            }
        });
    }



}
