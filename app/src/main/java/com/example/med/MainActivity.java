package com.example.med;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.med.database.AppDatabase;
import com.example.med.models.BrushingRecord;
import com.example.med.models.Visit;
import com.example.med.notifications.ReminderScheduler;
import com.example.med.notifications.NotificationHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private TextView streakText;
    private RecyclerView visitRecyclerView;
    private VisitAdapter visitAdapter;
    private AppDatabase db;
    private List<Visit> visitList = new ArrayList<>();

    private Button brushNowButton, datePickerButton, timePickerButton, addVisitButton;
    private EditText doctorNameInput, visitNotesInput;

    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Запрашиваем разрешение на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        db = AppDatabase.getInstance(this);

        // Запускаем напоминания в 9:00 и 21:00
        ReminderScheduler.scheduleMorningReminder(this);
        ReminderScheduler.scheduleEveningReminder(this);

        // Инициализация UI
        streakText = findViewById(R.id.streakText);
        visitRecyclerView = findViewById(R.id.visitRecyclerView);
        visitRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        brushNowButton = findViewById(R.id.brushNowButton);
        datePickerButton = findViewById(R.id.datePickerButton);
        timePickerButton = findViewById(R.id.timePickerButton);
        addVisitButton = findViewById(R.id.addVisitButton);
        doctorNameInput = findViewById(R.id.doctorNameInput);
        visitNotesInput = findViewById(R.id.visitNotesInput);

        visitAdapter = new VisitAdapter(visitList, visit -> deleteVisit(visit));
        visitRecyclerView.setAdapter(visitAdapter);

        // Кнопка "Я почистил зубы"
        brushNowButton.setOnClickListener(v -> markBrushed());

        // Выбор даты и времени
        datePickerButton.setOnClickListener(v -> showDatePicker());
        timePickerButton.setOnClickListener(v -> showTimePicker());
        addVisitButton.setOnClickListener(v -> addVisit());

        loadData();
        loadBrushingStats();
    }

    private void markBrushed() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        boolean isMorning = hour < 12;

        // Проверяем, не отмечал ли уже в это время сегодня
        new Thread(() -> {
            List<BrushingRecord> records = db.brushingDao().getAllRecords();
            Calendar today = Calendar.getInstance();
            int todayYear = today.get(Calendar.YEAR);
            int todayDay = today.get(Calendar.DAY_OF_YEAR);

            boolean alreadyBrushed = false;
            for (BrushingRecord r : records) {
                Calendar recordCal = Calendar.getInstance();
                recordCal.setTime(r.date);
                if (recordCal.get(Calendar.YEAR) == todayYear &&
                        recordCal.get(Calendar.DAY_OF_YEAR) == todayDay &&
                        r.isMorning == isMorning) {
                    alreadyBrushed = true;
                    break;
                }
            }

            if (alreadyBrushed) {
                runOnUiThread(() -> {
                    String timeOfDay = isMorning ? "утром" : "вечером";
                    Toast.makeText(this, "Вы уже отмечали чистку " + timeOfDay + " сегодня!", Toast.LENGTH_SHORT).show();
                });
            } else {
                BrushingRecord record = new BrushingRecord(new Date(), isMorning);
                db.brushingDao().insert(record);
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Отлично! Зубы чистые и здоровые!", Toast.LENGTH_SHORT).show();
                    loadBrushingStats();

                    // Отправляем подтверждение
                    NotificationHelper helper = new NotificationHelper(this);
                    helper.showReminder("🪅 Спасибо!", "Вы позаботились о здоровье своих зубов!");
                });
            }
        }).start();
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    datePickerButton.setText(String.format("📅 %02d.%02d.%d", dayOfMonth, month + 1, year));
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minute);
                    timePickerButton.setText(String.format("🕐 %02d:%02d", hourOfDay, minute));
                },
                selectedCalendar.get(Calendar.HOUR_OF_DAY),
                selectedCalendar.get(Calendar.MINUTE),
                true);
        timePicker.show();
    }

    private void addVisit() {
        String doctorName = doctorNameInput.getText().toString().trim();
        String notes = visitNotesInput.getText().toString().trim();

        if (doctorName.isEmpty()) {
            Toast.makeText(this, "Введите имя врача", Toast.LENGTH_SHORT).show();
            return;
        }

        Visit visit = new Visit(selectedCalendar.getTime(), doctorName, notes);

        new Thread(() -> {
            db.visitDao().insert(visit);
            runOnUiThread(() -> {
                Toast.makeText(this, "✅ Визит добавлен", Toast.LENGTH_SHORT).show();
                doctorNameInput.setText("");
                visitNotesInput.setText("");
                loadData();
            });
        }).start();
    }

    private void deleteVisit(Visit visit) {
        new Thread(() -> {
            db.visitDao().delete(visit);
            runOnUiThread(() -> {
                Toast.makeText(this, "🗑 Визит удален", Toast.LENGTH_SHORT).show();
                loadData();
            });
        }).start();
    }

    private void loadData() {
        new Thread(() -> {
            List<Visit> visits = db.visitDao().getAllVisits();
            runOnUiThread(() -> {
                visitList.clear();
                visitList.addAll(visits);
                visitAdapter.updateData(visitList);
            });
        }).start();
    }

    private void loadBrushingStats() {
        new Thread(() -> {
            List<BrushingRecord> records = db.brushingDao().getAllRecords();
            Calendar today = Calendar.getInstance();
            int todayYear = today.get(Calendar.YEAR);
            int todayDay = today.get(Calendar.DAY_OF_YEAR);

            int morningCount = 0;
            int eveningCount = 0;

            for (BrushingRecord r : records) {
                Calendar recordCal = Calendar.getInstance();
                recordCal.setTime(r.date);
                if (recordCal.get(Calendar.YEAR) == todayYear &&
                        recordCal.get(Calendar.DAY_OF_YEAR) == todayDay) {
                    if (r.isMorning) morningCount++;
                    else eveningCount++;
                }
            }

            final int morning = morningCount;
            final int evening = eveningCount;
            runOnUiThread(() -> {
                streakText.setText(String.format("🪅 Чисток сегодня: 🌅 %d | 🌙 %d", morning, evening));
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Уведомления разрешены", Toast.LENGTH_SHORT).show();
        }
    }
}