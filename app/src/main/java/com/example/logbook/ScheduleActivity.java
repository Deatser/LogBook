package com.example.logbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import com.example.logbook.databinding.ActivityScheduleBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ScheduleActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";
    private ActivityScheduleBinding binding;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ArrayList<ArrayList<String>> weeklySchedule;
    private String dayList;

    private String UserId = mAuth.getCurrentUser().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        Log.d(LOG_TAG, "Created");
        Log.d(LOG_TAG, "Current UserId: " + UserId);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Установим активным элемент "Журнал"
        bottomNavigationView.setSelectedItemId(R.id.diary);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                if (itemId == R.id.guide) {
                    startActivity(new Intent(ScheduleActivity.this, GuideActivity.class));
                    return true;
                } else if (itemId == R.id.diary) {
                    return true;
                } else if (itemId == R.id.settings) {
                    startActivity(new Intent(ScheduleActivity.this, ActivitySettings.class));
                    return true;
                }

                return false;
            }
        });


        // Установка слушателя для кнопки изменения расписания
        Button buttonEditSchedule = findViewById(R.id.buttonEditSchedule);
        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users").child(UserId);
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("root").exists()){
                    buttonEditSchedule.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        buttonEditSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Переход к активности редактирования расписания
                Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Инициализация RecyclerView для каждого дня недели

        initRecyclerViews();

        // Загрузка расписания на всю неделю
        loadWeeklySchedule();
    }


    private void initRecyclerViews() {
        // Инициализация RecyclerView для понедельника
        RecyclerView recyclerViewMonday = findViewById(R.id.recyclerViewMonday);
        recyclerViewMonday.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация RecyclerView для вторника
        RecyclerView recyclerViewTuesday = findViewById(R.id.recyclerViewTuesday);
        recyclerViewTuesday.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация RecyclerView для среды
        RecyclerView recyclerViewWednesday = findViewById(R.id.recyclerViewWednesday);
        recyclerViewWednesday.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация RecyclerView для четверга
        RecyclerView recyclerViewThursday = findViewById(R.id.recyclerViewThursday);
        recyclerViewThursday.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация RecyclerView для пятницы
        RecyclerView recyclerViewFriday = findViewById(R.id.recyclerViewFriday);
        recyclerViewFriday.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация RecyclerView для субботы
        RecyclerView recyclerViewSaturday = findViewById(R.id.recyclerViewSaturday);
        recyclerViewSaturday.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация RecyclerView для воскресенья
        RecyclerView recyclerViewSunday = findViewById(R.id.recyclerViewSunday);
        recyclerViewSunday.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadWeeklySchedule() {
        // Создаем список расписаний на всю неделю
        weeklySchedule = new ArrayList<>();



        // Создаем пустые списки расписаний для каждого дня недели
        for (int i = 0; i < 7; i++) {
            weeklySchedule.add(new ArrayList<String>());
        }

        // Массив с названиями дней недели
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i = 0; i < daysOfWeek.length; i++) {
            int dayIndex = i;

            DatabaseReference userGroupRef = FirebaseDatabase.getInstance().getReference().child("Users").child(UserId).child("group");
            userGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String userGroup = snapshot.getValue(String.class);
                        if (userGroup != null) {
                            DatabaseReference groupScheduleRef = FirebaseDatabase.getInstance().getReference(userGroup).child(daysOfWeek[dayIndex]);
                            groupScheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                        String para = childSnapshot.getValue(String.class);
                                        if (para != null && para.length() > 1) {
                                            weeklySchedule.get(dayIndex).add(para);
                                        }
                                    }
                                    // После загрузки расписания вызываем метод для отображения расписания
                                    displayWeeklySchedule();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    // Обработка ошибок при чтении данных
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Обработка ошибок при чтении данных
                }
            });
        }
    }

    private void displayWeeklySchedule() {
        // Получаем ссылки на все RecyclerView
        RecyclerView recyclerViewMonday = findViewById(R.id.recyclerViewMonday);
        RecyclerView recyclerViewTuesday = findViewById(R.id.recyclerViewTuesday);
        RecyclerView recyclerViewWednesday = findViewById(R.id.recyclerViewWednesday);
        RecyclerView recyclerViewThursday = findViewById(R.id.recyclerViewThursday);
        RecyclerView recyclerViewFriday = findViewById(R.id.recyclerViewFriday);
        RecyclerView recyclerViewSaturday = findViewById(R.id.recyclerViewSaturday);
        RecyclerView recyclerViewSunday = findViewById(R.id.recyclerViewSunday);

        // Массив с названиями дней недели
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        // Создаем адаптеры для каждого RecyclerView и устанавливаем их
        recyclerViewMonday.setAdapter(new ScheduleAdapter(weeklySchedule.get(0), daysOfWeek[0]));
        recyclerViewTuesday.setAdapter(new ScheduleAdapter(weeklySchedule.get(1), daysOfWeek[1]));
        recyclerViewWednesday.setAdapter(new ScheduleAdapter(weeklySchedule.get(2), daysOfWeek[2]));
        recyclerViewThursday.setAdapter(new ScheduleAdapter(weeklySchedule.get(3), daysOfWeek[3]));
        recyclerViewFriday.setAdapter(new ScheduleAdapter(weeklySchedule.get(4), daysOfWeek[4]));
        recyclerViewSaturday.setAdapter(new ScheduleAdapter(weeklySchedule.get(5), daysOfWeek[5]));
        recyclerViewSunday.setAdapter(new ScheduleAdapter(weeklySchedule.get(6), daysOfWeek[6]));
    }


}