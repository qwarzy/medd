package com.example.med.notifications;

import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.Calendar;

public class ReminderWorker extends Worker {
    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationHelper helper = new NotificationHelper(getApplicationContext());

        // Определяем, утро сейчас или вечер
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String title;
        String message;

        if (hour < 12) {
            title = "🌅 Доброе утро!";
            message = "Самое время почистить зубы после завтрака! Нажмите на уведомление, чтобы отметить.";
        } else {
            title = "🌙 Добрый вечер!";
            message = "Не забудьте почистить зубы перед сном! Нажмите на уведомление, чтобы отметить.";
        }

        helper.showReminder(title, message);
        return Result.success();
    }
}