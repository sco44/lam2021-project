package it.unibo.cs.lam2021.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.database.ProductRepository;
import it.unibo.cs.lam2021.database.entity.LocalProduct;
import it.unibo.cs.lam2021.ui.SplashActivity;

public class NotificationService extends JobIntentService {

    private static final String EXPIRY_CHANNEL_ID = "LAM2021Exp";
    private static final String REMINDER_CHANNEL_ID = "LAM2021Rem";

    public static final int EXPIRING_NOTIFICATION_ID = 200;
    public static final int EXPIRED_NOTIFICATION_ID = 201;
    public static final int REMINDER_NOTIFICATION_ID = 202;

    private static final int NOTIFICATION_CONTENT_REQUEST_CODE = 90;

    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final int NOTIFICATION_EXPIRY = 0;
    public static final int NOTIFICATION_REMINDER = 1;

    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, NotificationService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        ApplicationPreferences prefs = ApplicationPreferences.getInstance(this);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationChannel expiryChannel = new NotificationChannel(EXPIRY_CHANNEL_ID, getString(R.string.notification_channel_expiry_name), NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel reminderChannel = new NotificationChannel(REMINDER_CHANNEL_ID, getString(R.string.notification_channel_reminder_name), NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(expiryChannel);
        notificationManager.createNotificationChannel(reminderChannel);

        int notificationType = intent.getIntExtra(NOTIFICATION_TYPE, -1);

        if(notificationType == -1)      //this should never happen
            return;

        ProductRepository repository = new ProductRepository(getApplication());

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                NOTIFICATION_CONTENT_REQUEST_CODE,
                new Intent(this, SplashActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        if(notificationType == NOTIFICATION_EXPIRY) {
            List<LocalProduct> products = repository.getExpiringProducts(prefs.getExpiryNotificationBefore());
            if(products.size() > 0) {
                StringBuilder notificationText = new StringBuilder();
                StringBuilder notificationBigText = new StringBuilder();
                products.forEach(p -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), p.getExpiry());
                    notificationText.append(p.getName())
                            .append(", ");

                    notificationBigText.append(" \u2022 ")
                            .append(getString(R.string.expiring_notification_content_row, p.getName(),
                                    getResources().getQuantityString(R.plurals.days, (int) days, days)))
                            .append("\n");
                });
                notificationText.setLength(notificationText.length() - 2);
                notificationBigText.setLength(notificationBigText.length() - 1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, EXPIRY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_barcode_scan)
                        .setContentTitle(getString(R.string.expiring_notification_title))
                        .setContentText(notificationText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBigText))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
                notificationManager.notify(EXPIRING_NOTIFICATION_ID, builder.build());
            }

            products = repository.getExpiredProducts();
            if(products.size() > 0) {
                StringBuilder notificationText = new StringBuilder();
                StringBuilder notificationBigText = new StringBuilder();
                products.forEach(p -> {
                    long days = ChronoUnit.DAYS.between(p.getExpiry(), LocalDate.now());
                    notificationText.append(p.getName())
                            .append(", ");

                    notificationBigText.append(" \u2022 ");
                    if(days == 0)
                        notificationBigText.append(getString(R.string.expired_notification_content_row_today, p.getName()));
                    else
                        notificationBigText.append(getString(R.string.expired_notification_content_row, p.getName(),
                                getResources().getQuantityString(R.plurals.days, (int) days, days)));

                    notificationBigText.append("\n");
                });
                notificationText.setLength(notificationText.length() - 2);
                notificationBigText.setLength(notificationBigText.length() - 1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, EXPIRY_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_barcode_scan)
                        .setContentTitle(getString(R.string.expired_notification_title))
                        .setContentText(notificationText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBigText))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
                notificationManager.notify(EXPIRED_NOTIFICATION_ID, builder.build());
            }

            NotificationScheduler.scheduleExpiryNotification(this, false);
        } else if(notificationType == NOTIFICATION_REMINDER) {
            List<LocalProduct> products = repository.getImportantProducts();
            if(products.size() > 0) {
                StringBuilder notificationText = new StringBuilder();
                StringBuilder notificationBigText = new StringBuilder();
                products.forEach(p -> {
                    notificationText.append(p.getName())
                            .append(", ");

                    notificationBigText.append(" \u2022 ")
                            .append(p.getName())
                            .append("\n");
                });
                notificationText.setLength(notificationText.length() - 2);
                notificationBigText.setLength(notificationBigText.length() - 1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, REMINDER_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_barcode_scan)
                        .setContentTitle(getString(R.string.reminder_notification_title))
                        .setContentText(notificationText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBigText))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
                notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build());
            }

            NotificationScheduler.scheduleReminderNotification(this, false);
        }
    }
}
