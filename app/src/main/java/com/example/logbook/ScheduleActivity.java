package com.example.logbook;

import java.util.Calendar;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.logbook.databinding.ActivityScheduleBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.common.value.qual.StringVal;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity {

    final String LOG_TAG = "myLogs";
    private ActivityScheduleBinding binding;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ArrayList<ArrayList<String>> weeklySchedule;
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
                if (snapshot.child("root").exists()) {
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
                Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Инициализация RecyclerView
        RecyclerView recyclerViewSchedule = findViewById(R.id.recyclerViewSchedule);
        recyclerViewSchedule.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация RadioGroup и RadioButton
        RadioGroup radioGroupDays = findViewById(R.id.radioGroupDays);

        radioGroupDays.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Получаем выбранную радиокнопку
                RadioButton checkedRadioButton = findViewById(checkedId);
                String selectedDay = checkedRadioButton.getText().toString();
                // Отображаем расписание для выбранного дня недели
                displayScheduleForSelectedDay(selectedDay);
            }
        });

        // Загрузка расписания на всю неделю с использованием обратного вызова
        loadWeeklySchedule(new OnScheduleLoadListener() {
            @Override
            public void onScheduleLoaded() {
                // Отображение расписания на текущий день после загрузки данных
                displayScheduleForToday();
            }
        });
    }

    private void loadWeeklySchedule(OnScheduleLoadListener listener) {
        // Создаем список расписаний на всю неделю
        weeklySchedule = new ArrayList<>();
        // Создаем пустые списки расписаний для каждого дня недели
        for (int i = 0; i < 7; i++) {
            weeklySchedule.add(new ArrayList<String>());
        }

        // Массив с названиями дней недели
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        int daysToLoad = daysOfWeek.length;
        int[] daysLoaded = {0};  // Using an array to keep track of the count within the inner class

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
                                    daysLoaded[0]++;
                                    if (daysLoaded[0] == daysToLoad) {
                                        listener.onScheduleLoaded();
                                    }
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

    private void displayScheduleForToday() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                RadioButton radioSunday = binding.radioSunday;
                radioSunday.setChecked(true);
                displayScheduleForSelectedRadio(6);
                break;
            case Calendar.MONDAY:
                RadioButton radioMonday = binding.radioMonday;
                radioMonday.setChecked(true);
                displayScheduleForSelectedRadio(0);
                break;
            case Calendar.TUESDAY:
                RadioButton radioTuesday = binding.radioTuesday;
                radioTuesday.setChecked(true);
                displayScheduleForSelectedRadio(1);
                break;
            case Calendar.WEDNESDAY:
                RadioButton radioWednesday = binding.radioWednesday;
                radioWednesday.setChecked(true);
                displayScheduleForSelectedRadio(2);
                break;
            case Calendar.THURSDAY:
                RadioButton radioThursday = binding.radioThursday;
                radioThursday.setChecked(true);
                displayScheduleForSelectedRadio(3);
                break;
            case Calendar.FRIDAY:
                RadioButton radioFriday = binding.radioFriday;
                radioFriday.setChecked(true);
                displayScheduleForSelectedRadio(4);
                break;
            case Calendar.SATURDAY:
                RadioButton radioSaturday = binding.radioSaturday;
                radioSaturday.setChecked(true);
                displayScheduleForSelectedRadio(5);
                break;
            default:
                // Handle any unexpected values
                break;
        }
    }

    private void displayScheduleForSelectedDay(String selectedDay) {
        switch (selectedDay) {
            case "Пн":
                displayScheduleForSelectedRadio(0);
                break;
            case "Вт":
                displayScheduleForSelectedRadio(1);
                break;
            case "Ср":
                displayScheduleForSelectedRadio(2);
                break;
            case "Чт":
                displayScheduleForSelectedRadio(3);
                break;
            case "Пт":
                displayScheduleForSelectedRadio(4);
                break;
            case "Сб":
                displayScheduleForSelectedRadio(5);
                break;
            case "Вс":
                displayScheduleForSelectedRadio(6);
                break;
            default:
                // Handle any unexpected values
                break;
        }
    }

    private void displayScheduleForSelectedRadio(int dayIndex) {
        // Получаем ссылку на RecyclerView
        RecyclerView recyclerViewSchedule = findViewById(R.id.recyclerViewSchedule);
        // Создаем адаптер для расписания выбранного дня
        ScheduleAdapter adapter = new ScheduleAdapter(weeklySchedule.get(dayIndex), getResources().getStringArray(R.array.scheduleTimes));
        recyclerViewSchedule.setAdapter(adapter);
    }

    // Listener interface to handle schedule load completion
    interface OnScheduleLoadListener {
        void onScheduleLoaded();
    }
}
