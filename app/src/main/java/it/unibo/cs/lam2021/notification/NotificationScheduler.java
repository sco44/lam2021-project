package it.unibo.cs.lam2021.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

import it.unibo.cs.lam2021.preferences.ApplicationPreferences;

public class NotificationScheduler {

    public static final String ACTION_EXPIRY_NOTIFICATION = "it.unibo.cs.LAM2021.intent.action.EXPIRY_NOTIFICATION";
    public static final String ACTION_REMINDER_NOTIFICATION = "it.unibo.cs.LAM2021.intent.action.REMINDER_NOTIFICATION";

    public static final int EXPIRY_NOTIFICATION_REQUEST_CODE = 100;
    public static final int REMINDER_NOTIFICATION_REQUEST_CODE = 110;

    private static void setAlarm(Context context, LocalDateTime time, String action, int requestCode) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        long millis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Intent i = new Intent(context, NotificationReceiver.class);
        i.setAction(action);

        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi);
    }

    private static void cancelAlarm(Context context, String action, int requestCode) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        Intent i = new Intent(context, NotificationReceiver.class);
        i.setAction(action);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        if(pi != null)
            alarmManager.cancel(pi);
    }

    //firstTime = true in boot receiver and settings
    //firstTime = false in notification receiver
    public static void scheduleReminderNotification(Context context, boolean firstTime) {
        ApplicationPreferences prefs = ApplicationPreferences.getInstance(context);

        Set<DayOfWeek> days = prefs.getBuyProductsReminderWeekdays();
        if(days.isEmpty())
            return;

        LocalTime time = prefs.getBuyProductsReminderTime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime trigger = null;

        for(int i = (firstTime ? 0 : 1); i <= 7; i++) {
            if(days.contains(now.plusDays(i).getDayOfWeek())) {
                if(i == 0 && now.toLocalTime().isAfter(time))       //prevent sending notification on configuration change
                    continue;

                trigger = LocalDateTime.of(now.plusDays(i).toLocalDate(), time);
                break;
            }
        }

        setAlarm(context,
                trigger,
                NotificationScheduler.ACTION_REMINDER_NOTIFICATION,
                NotificationScheduler.REMINDER_NOTIFICATION_REQUEST_CODE);
    }

    public static void scheduleExpiryNotification(Context context, boolean firstTime) {
        ApplicationPreferences prefs = ApplicationPreferences.getInstance(context);

        LocalTime time = prefs.getExpiryNotificationTime();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime trigger;
        if(firstTime)
            trigger = now.plusDays(now.toLocalTime().isAfter(time) ? 1 : 0).with(time);
        else
            trigger = now.plusDays(1).with(time);


        setAlarm(context,
                trigger,
                NotificationScheduler.ACTION_EXPIRY_NOTIFICATION,
                NotificationScheduler.EXPIRY_NOTIFICATION_REQUEST_CODE);
    }

    public static void cancelReminderNotification(Context context) {
        NotificationScheduler.cancelAlarm(context,
                NotificationScheduler.ACTION_REMINDER_NOTIFICATION,
                NotificationScheduler.REMINDER_NOTIFICATION_REQUEST_CODE);
    }

    public static void cancelExpiryNotification(Context context) {
        NotificationScheduler.cancelAlarm(context,
                NotificationScheduler.ACTION_EXPIRY_NOTIFICATION,
                NotificationScheduler.EXPIRY_NOTIFICATION_REQUEST_CODE);
    }
}
