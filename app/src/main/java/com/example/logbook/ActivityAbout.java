package com.example.logbook;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.logbook.databinding.ActivityAboutBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityAbout extends AppCompatActivity {

    private ActivityAboutBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.about);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.about) {
                    return true;
                } else if (itemId == R.id.diary) {
                    startActivity(new Intent(ActivityAbout.this, ScheduleActivity.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    return true;
                } else if (itemId == R.id.settings) {
                    startActivity(new Intent(ActivityAbout.this, ActivitySettings.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    return true;
                } else if (itemId == R.id.guide) {
                    startActivity(new Intent(ActivityAbout.this, GuideActivity.class));
                    overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
