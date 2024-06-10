package com.example.logbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.example.logbook.databinding.ActivityLoginBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        // Инициализация FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Установка слушателя для кнопки входа
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Проверка, вошел ли пользователь ранее
         if (mAuth.getCurrentUser() != null) {
             //Пользователь уже вошел в аккаунт
            navigateToScheduleActivity();
         }
    }

    private void loginUser() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        // Проверка на пустые поля
       if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Аутентификация пользователя с помощью Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Вход выполнен успешно
                            Toast.makeText(LoginActivity.this, "Добро пожаловать!", Toast.LENGTH_SHORT).show();

                            ref.child("Users").child(mAuth.getCurrentUser().getUid()).child("email").setValue(email);



                            navigateToScheduleActivity();
                        } else {
                            // Ошибка входа
                            Toast.makeText(LoginActivity.this, "Не удалось войти по причине: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToScheduleActivity() {
        Intent intent = new Intent(LoginActivity.this, ScheduleActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        finish(); // Закрываем LoginActivity
    }
}
