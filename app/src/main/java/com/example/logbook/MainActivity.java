package com.example.logbook;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.logbook.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private AutoCompleteTextView editTextGroupNumber;
    private AutoCompleteTextView editTextDayOfWeek;
    private AutoCompleteTextView editTextLessonNumber;
    private AutoCompleteTextView editTextLessonName;
    private Button SendBtn;
    private ImageView imageViewPhoto;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    // Константы для разрешений
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    // Переменная для хранения URI выбранного изображения
    private Uri selectedImageUri;

    // Константы для SharedPreferences
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_IMAGE_URI = "imageUri";

    // Переход в активити
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                        imageViewPhoto.setImageBitmap(bitmap);
                        saveImageUri(selectedImageUri);  // Сохранение URI выбранного изображения
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });



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
        imageViewPhoto = binding.imageViewPhoto;

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        // Инициализация адаптеров для AutoCompleteTextView
        setupAutoCompleteTextViews();

        // Восстановление сохраненного изображения
        loadImage();

        // Обработчик нажатия на кнопку изменения фотографии
        Button buttonChangePhoto = findViewById(R.id.buttonChangePhoto);
        buttonChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверяем разрешения
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                } else {
                    // Если разрешения уже есть, запускаем процесс выбора изображения
                    openImageChooser();
                }
            }
        });

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
                    //overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    overridePendingTransition(0, 0);  // Отключение анимации перехода
                    return true;
                } else if (itemId == R.id.diary) {
                    startActivity(new Intent(MainActivity.this, ScheduleActivity.class));
                  //  overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    overridePendingTransition(0, 0);  // Отключение анимации перехода
                    return true;
                } else if (itemId == R.id.settings) {
                    startActivity(new Intent(MainActivity.this, ActivitySettings.class));
                  //  overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    overridePendingTransition(0, 0);  // Отключение анимации перехода
                    return true;
                } else if (itemId == R.id.about) {
                    startActivity(new Intent(MainActivity.this, ActivityAbout.class));
                  //  overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                    overridePendingTransition(0, 0);  // Отключение анимации перехода
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

    // Метод для очистки полей ввода
    private void clearInputFields() {
        editTextGroupNumber.setText("");
        editTextDayOfWeek.setText("");
        editTextLessonNumber.setText("");
        editTextLessonName.setText("");
    }

    // Метод для проверки значения дня недели
    private boolean isValidDayOfWeek(String dayOfWeek) {
        String[] validDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String validDay : validDays) {
            if (validDay.equalsIgnoreCase(dayOfWeek)) {
                return true;
            }
        }
        return false;
    }

    // Метод для открытия выбора изображения
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    // Метод для сохранения URI изображения в SharedPreferences
    private void saveImageUri(Uri imageUri) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_IMAGE_URI, imageUri.toString());
        editor.apply();
    }

    // Метод для загрузки URI изображения из SharedPreferences и его отображения
    private void loadImage() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String imageUriString = prefs.getString(PREF_IMAGE_URI, null);
        if (imageUriString != null) {
            selectedImageUri = Uri.parse(imageUriString);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                imageViewPhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
