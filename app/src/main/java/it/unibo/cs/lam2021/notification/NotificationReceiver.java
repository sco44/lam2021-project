package it.unibo.cs.lam2021.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent i = new Intent(context, NotificationService.class);

        if(NotificationScheduler.ACTION_EXPIRY_NOTIFICATION.equalsIgnoreCase(intent.getAction())) {
            i.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_EXPIRY);
            NotificationService.enqueueWork(context, i);
        } else if (NotificationScheduler.ACTION_REMINDER_NOTIFICATION.equalsIgnoreCase(intent.getAction())) {
            i.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_REMINDER);
            NotificationService.enqueueWork(context, i);
        }
    }
}