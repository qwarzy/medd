package com.example.med.notifications;

import android.content.Context;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    public static void scheduleMorningReminder(Context context) {
        // Утреннее напоминание в 9:00
        long delay = getDelayToNextTime(9, 0);

        PeriodicWorkRequest morningReminder = new PeriodicWorkRequest.Builder(
                ReminderWorker.class,
                24, TimeUnit.HOURS
        ).setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "morning_brush_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                morningReminder
        );
    }

    public static void scheduleEveningReminder(Context context) {
        // Вечернее напоминание в 21:00
        long delay = getDelayToNextTime(21, 0);

        PeriodicWorkRequest eveningReminder = new PeriodicWorkRequest.Builder(
                ReminderWorker.class,
                24, TimeUnit.HOURS
        ).setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "evening_brush_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                eveningReminder
        );
    }

    private static long getDelayToNextTime(int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();

        next.set(Calendar.HOUR_OF_DAY, targetHour);
        next.set(Calendar.MINUTE, targetMinute);
        next.set(Calendar.SECOND, 0);

        if (next.before(now)) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }

        return next.getTimeInMillis() - now.getTimeInMillis();
    }
}