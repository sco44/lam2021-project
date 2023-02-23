package it.unibo.cs.lam2021.ui.settings;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;

import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.notification.NotificationScheduler;

public class SettingsActivity extends AppCompatActivity implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener, NumberPickerDialogFragment.OnValueSetListener {

    private ApplicationPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appPreferences = ApplicationPreferences.getInstance(this);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference p) {
        switch(p.getKey()) {
            case ApplicationPreferences.BUY_PRODUCTS_REMINDER_TIME:
                new TimePickerDialog(this,
                        (timePicker, h, m) -> appPreferences.setBuyProductsReminderTime(h, m),
                        appPreferences.getBuyProductsReminderTime().getHour(),
                        appPreferences.getBuyProductsReminderTime().getMinute(),
                        true)
                        .show();
                return true;

            case ApplicationPreferences.EXPIRY_NOTIFICATION_BEFORE:
                NumberPickerDialogFragment.newInstance(ApplicationPreferences.EXPIRY_NOTIFICATION_BEFORE, appPreferences.getExpiryNotificationBefore())
                        .show(getSupportFragmentManager(), NumberPickerDialogFragment.TAG);
                return true;

            case ApplicationPreferences.EXPIRY_NOTIFICATION_TIME:
                new TimePickerDialog(this, (timePicker, h, m) -> appPreferences.setExpiryNotificationTime(h, m),
                        appPreferences.getExpiryNotificationTime().getHour(),
                        appPreferences.getExpiryNotificationTime().getMinute(),
                        true)
                        .show();

                return true;

            default:
                return false;
        }
    }

    @Override
    public void onValueSet(String preference, int value) {
        appPreferences.getPreferences().edit()
            .putInt(preference, value)
            .apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            switch(s) {
                case ApplicationPreferences.BUY_PRODUCTS_REMINDER_ENABLED:
                case ApplicationPreferences.BUY_PRODUCTS_REMINDER_TIME:
                    if (appPreferences.isBuyProductsReminderEnabled())
                        NotificationScheduler.scheduleReminderNotification(this, true);
                    else
                        NotificationScheduler.cancelReminderNotification(this);

                    break;

                case ApplicationPreferences.EXPIRY_NOTIFICATION_ENABLED:
                case ApplicationPreferences.EXPIRY_NOTIFICATION_TIME:
                    if(appPreferences.isExpiryNotificationEnabled())
                        NotificationScheduler.scheduleExpiryNotification(this, true);
                    else
                        NotificationScheduler.cancelExpiryNotification(this);

                    break;
            }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            findPreference(ApplicationPreferences.EXPIRY_NOTIFICATION_TIME).setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference(ApplicationPreferences.EXPIRY_NOTIFICATION_BEFORE).setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());
            findPreference(ApplicationPreferences.BUY_PRODUCTS_REMINDER_TIME).setOnPreferenceClickListener((Preference.OnPreferenceClickListener) getActivity());

            ApplicationPreferences.getInstance(getContext()).getPreferences().registerOnSharedPreferenceChangeListener((SharedPreferences.OnSharedPreferenceChangeListener) getActivity());

            MultiSelectListPreference daysOfTheWeek = findPreference(ApplicationPreferences.BUY_PRODUCTS_REMINDER_WEEKDAYS);
            EnumSet<DayOfWeek> weekdays = EnumSet.allOf(DayOfWeek.class);
            ArrayList<String> names = new ArrayList<>(7);
            weekdays.forEach(d -> {
                String display = d.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
                display = display.substring(0, 1).toUpperCase() + display.substring(1).toLowerCase();
                names.add(display);
            });
            daysOfTheWeek.setEntries(names.toArray(new String[0]));
        }
    }
}