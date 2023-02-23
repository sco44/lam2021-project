package it.unibo.cs.lam2021.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import it.unibo.cs.lam2021.R;

public class ApplicationPreferences {

    public static final String AUTOMATIC_PROCESSING_ENABLED = "auto_scan";
    public static final String FAST_ADD_ENABLED = "fast_add";
    public static final String EXPIRY_NOTIFICATION_ENABLED = "expiry_notification";
    public static final String EXPIRY_NOTIFICATION_TIME = "expiry_notification_time";
    public static final String EXPIRY_NOTIFICATION_BEFORE = "expiry_notification_before";
    public static final String BUY_PRODUCTS_REMINDER_ENABLED = "buy_products_reminder";
    public static final String BUY_PRODUCTS_REMINDER_WEEKDAYS = "buy_products_reminder_weekdays";
    public static final String BUY_PRODUCTS_REMINDER_TIME = "buy_products_reminder_time";

    public static final String NETWORK_ONLINE = "network_online";

    private static int defaultHour;
    private static int defaultMinute;
    private static int defaultExpiryCutoff;

    private static ApplicationPreferences instance;

    private final SharedPreferences mPrefs;

    private ApplicationPreferences(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        defaultHour = ctx.getResources().getInteger(R.integer.def_hour);
        defaultMinute = ctx.getResources().getInteger(R.integer.def_minutes);
        defaultExpiryCutoff = ctx.getResources().getInteger(R.integer.def_expiry_cutoff);
    }

    public static ApplicationPreferences getInstance(Context ctx) {
        if(ctx == null && instance == null)
            return null;

        if(instance == null) {
            synchronized (ApplicationPreferences.class) {
                if (instance == null)
                    instance = new ApplicationPreferences(ctx.getApplicationContext());
            }
        }

        return instance;
    }

    public SharedPreferences getPreferences() { return mPrefs; }

    public boolean isAutoProcessingEnabled() {
        return mPrefs.getBoolean(AUTOMATIC_PROCESSING_ENABLED, false);
    }

    public boolean isFastAddEnabled() {
        return mPrefs.getBoolean(FAST_ADD_ENABLED, true);
    }

    public boolean isExpiryNotificationEnabled() {
        return mPrefs.getBoolean(EXPIRY_NOTIFICATION_ENABLED, false);
    }

    public LocalTime getExpiryNotificationTime() {
        int minutesFromMidnight = mPrefs.getInt(EXPIRY_NOTIFICATION_TIME, defaultHour * 60 + defaultMinute);
        return LocalTime.of(
                minutesFromMidnight / 60,
                minutesFromMidnight % 60
        );
    }

    public void setExpiryNotificationTime(int hour, int minute) {
        mPrefs.edit()
                .putInt(EXPIRY_NOTIFICATION_TIME, hour * 60 + minute)
                .apply();
    }

    public int getExpiryNotificationBefore() {
        return mPrefs.getInt(EXPIRY_NOTIFICATION_BEFORE, defaultExpiryCutoff);
    }

    public boolean isBuyProductsReminderEnabled() {
        return mPrefs.getBoolean(BUY_PRODUCTS_REMINDER_ENABLED, false);
    }

    public Set<DayOfWeek> getBuyProductsReminderWeekdays() {
        Set<String> ss = mPrefs.getStringSet(BUY_PRODUCTS_REMINDER_WEEKDAYS, null);
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        ss.forEach(d -> days.add(DayOfWeek.of(Integer.parseInt(d))));

        return days;
    }

    public LocalTime getBuyProductsReminderTime() {
        int minutesFromMidnight = mPrefs.getInt(BUY_PRODUCTS_REMINDER_TIME, defaultHour * 60 + defaultMinute);
        return LocalTime.of(
                minutesFromMidnight / 60,
                minutesFromMidnight % 60
        );
    }

    public void setBuyProductsReminderTime(int hour, int minute) {
        mPrefs.edit()
                .putInt(BUY_PRODUCTS_REMINDER_TIME, hour * 60 + minute)
                .apply();
    }

    public boolean isNetworkOnline() {
        return mPrefs.getBoolean(NETWORK_ONLINE, true);
    }

    public void setNetworkOnline(boolean online) {
        mPrefs.edit()
                .putBoolean(NETWORK_ONLINE, online)
                .apply();
    }
}
