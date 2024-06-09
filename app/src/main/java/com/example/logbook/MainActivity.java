package com.example.logbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.logbook.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private AutoCompleteTextView editTextGroupNumber;
    private AutoCompleteTextView editTextDayOfWeek;
    private AutoCompleteTextView editTextLessonNumber;
    private AutoCompleteTextView editTextLessonName;
    private Button SendBtn;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Инициализация элементов пользовательского интерфейса
        editTextGroupNumber = binding.editTextGroupNumber;
        editTextDayOfWeek = binding.editTextDayOfWeek;
        editTextLessonNumber = binding.editTextLessonNumber;
        editTextLessonName = binding.editTextLessonName;
        SendBtn = binding.SendBtn;

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        // Инициализация адаптеров для AutoCompleteTextView
        setupAutoCompleteTextViews();

        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Получение значений из полей ввода
                String groupNumber = editTextGroupNumber.getText().toString();
                String dayOfWeek = editTextDayOfWeek.getText().toString();
                String lessonNumber = editTextLessonNumber.getText().toString();
                String lessonName = editTextLessonName.getText().toString();

                // Проверка значения дня недели
                if (!isValidDayOfWeek(dayOfWeek)) {
                    Toast.makeText(MainActivity.this, "Ошибка: Неверный день недели", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Проверка наличия значения номера пары
                if (lessonNumber.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Ошибка: Номер пары не может быть пустым", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Проверка значения номера пары
                int lessonNumberInt = Integer.parseInt(lessonNumber);
                if (lessonNumberInt < 1 || lessonNumberInt > 6) {
                    Toast.makeText(MainActivity.this, "Ошибка: Неверный номер пары", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Формирование пути к узлу в базе данных
                DatabaseReference lessonRef = ref.child(groupNumber).child(dayOfWeek).child(lessonNumber);

                // Установка значения узлу
                lessonRef.setValue(lessonName, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // Если возникла ошибка при установке значения
                            Toast.makeText(MainActivity.this, "Ошибка: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            // Успешно установлено новое значение
                            Toast.makeText(MainActivity.this, "Данные успешно отправлены", Toast.LENGTH_SHORT).show();
                            // Очистка полей после успешной отправки
                            clearInputFields();
                        }
                    }
                });
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // Установим активным элемент "Журнал"
        bottomNavigationView.setSelectedItemId(R.id.diary);
        // Снимите выделение с всех элементов
        bottomNavigationView.getMenu().findItem(R.id.diary).setChecked(false);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.guide) {
                    startActivity(new Intent(MainActivity.this, GuideActivity.class));
                    return true;
                } else if (itemId == R.id.diary) {
                    startActivity(new Intent(MainActivity.this, ScheduleActivity.class));
                    return true;
                } else if (itemId == R.id.settings) {
                    startActivity(new Intent(MainActivity.this, ActivitySettings.class));
                    return true;
                }
                return false;
            }
        });
    }

    // Метод для настройки AutoCompleteTextView
    private void setupAutoCompleteTextViews() {
        // Списки подсказок
        
        //На будущее: это можно закинуть в Firebase
        String[] groupNumbers = {"IKBO3322", "IKBO3422"};
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] lessonNumbers = {"1", "2", "3", "4", "5", "6"};
        String[] lessonNames = {"Выходной =)","Дизайн мобильных приложений", "Программирование на языке Питон", "Физическая культура",
                "Технология разработки программных приложений", "Проектирование баз данных", "Разработка мобильных приложений",
                "Технология разработки программных приложений (Лекция)", "Теория вероятностей и математическая статистика", "Анализ и концептуальное моделирование систем",
                "Иностарнный язык", "Проектирование баз данных (Лекция)", "Дизайн мобильных приложений (Лекция)"
        };

        // Адаптеры для AutoCompleteTextView
        ArrayAdapter<String> groupNumberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, groupNumbers);
        ArrayAdapter<String> dayOfWeekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, daysOfWeek);
        ArrayAdapter<String> lessonNumberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lessonNumbers);
        ArrayAdapter<String> lessonNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, lessonNames);

        // Установка адаптеров
        editTextGroupNumber.setAdapter(groupNumberAdapter);
        editTextDayOfWeek.setAdapter(dayOfWeekAdapter);
        editTextLessonNumber.setAdapter(lessonNumberAdapter);
        editTextLessonName.setAdapter(lessonNameAdapter);

        // Включение фильтрации в адаптерах
        editTextGroupNumber.setThreshold(0);
        editTextGroupNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editTextGroupNumber.showDropDown();
                }
            }
        });

        editTextDayOfWeek.setThreshold(0);
        editTextDayOfWeek.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editTextDayOfWeek.showDropDown();
                }
            }
        });

        editTextLessonNumber.setThreshold(0);
        editTextLessonNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editTextLessonNumber.showDropDown();
                }
            }
        });

        editTextLessonName.setThreshold(0);
        editTextLessonName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editTextLessonName.showDropDown();
                }
            }
        });


    }

    // Метод для проверки правильности дня недели
    private boolean isValidDayOfWeek(String dayOfWeek) {
        return dayOfWeek.equalsIgnoreCase("Monday") ||
                dayOfWeek.equalsIgnoreCase("Tuesday") ||
                dayOfWeek.equalsIgnoreCase("Wednesday") ||
                dayOfWeek.equalsIgnoreCase("Thursday") ||
                dayOfWeek.equalsIgnoreCase("Friday") ||
                dayOfWeek.equalsIgnoreCase("Saturday") ||
                dayOfWeek.equalsIgnoreCase("Sunday");
    }

    // Метод для очистки полей ввода
    private void clearInputFields() {
        editTextLessonNumber.setText("");
        editTextLessonName.setText("");
    }
}
