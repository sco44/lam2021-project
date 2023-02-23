package it.unibo.cs.lam2021;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unibo.cs.lam2021.notification.NotificationScheduler;
import it.unibo.cs.lam2021.preferences.ApplicationPreferences;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction()))
            return;

        ApplicationPreferences preferences = ApplicationPreferences.getInstance(context);

        if(preferences.isBuyProductsReminderEnabled())
            NotificationScheduler.scheduleReminderNotification(context, true);

        if(preferences.isExpiryNotificationEnabled())
            NotificationScheduler.scheduleExpiryNotification(context, true);
    }
}