package com.example.logbook;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.logbook.databinding.ActivitySettingsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivitySettings extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;
    private ActivitySettingsBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        EditText oldPassword = binding.oldPassword;
        EditText newPassword = binding.newPassword;
        Button confirmButton = binding.confirmButton;
        Button logoutButton = binding.logoutButton;

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = oldPassword.getText().toString();
                String newPass = newPassword.getText().toString();

                if (user != null && !oldPass.isEmpty() && !newPass.isEmpty()) {
                    // Получение учетных данных для повторной аутентификации пользователя
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

                    // Повторная аутентификация пользователя
                    user.reauthenticate(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Повторная аутентификация успешна, обновление пароля
                            user.updatePassword(newPass)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            checkAndRequestNotificationPermission();
                                        } else {
                                            Toast.makeText(ActivitySettings.this, "Ошибка изменения пароля", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Неверный старый пароль
                            Toast.makeText(ActivitySettings.this, "Неверный старый пароль", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ActivitySettings.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(ActivitySettings.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.settings);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.guide) {
                    startActivity(new Intent(ActivitySettings.this, GuideActivity.class));
                } else if (itemId == R.id.diary) {
                    startActivity(new Intent(ActivitySettings.this, ScheduleActivity.class));
                    return true;
                } else if (itemId == R.id.settings) {
                    return true;
                } else if (itemId == R.id.about) {
                    startActivity(new Intent(ActivitySettings.this, ActivityAbout.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
            } else {
                showPasswordChangeNotification();
            }
        } else {
            showPasswordChangeNotification();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPasswordChangeNotification();
            } else {
                Toast.makeText(this, "Доступ запрещён", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPasswordChangeNotification() {
        String channelId = "password_change_channel";
        String channelName = "Password Change Notifications";
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Create notification channel if Android version is Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.diary_svgrepo_com) // иконка
                .setContentTitle("Пароль изменён")
                .setContentText("Ваш пароль был успешно изменен.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}
