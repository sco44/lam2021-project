package it.unibo.cs.lam2021.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedPreferences {
    private static EncryptedPreferences instance;

    private static final String FILE_NAME = "LAM2021AppSecure";

    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_EMAIL = "user_email";
    private static final String PREF_USER_NAME = "user_name";
    private static final String PREF_USER_PASSWORD = "user_password";
    private static final String PREF_AUTHORIZATION_TOKEN = "authorization_token";

    private final SharedPreferences preferences;

    private EncryptedPreferences(Context ctx) throws GeneralSecurityException, IOException {
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
                .build();

        MasterKey masterKey = new MasterKey.Builder(ctx)
                .setKeyGenParameterSpec(spec)
                .build();
        preferences = EncryptedSharedPreferences.create(ctx, FILE_NAME, masterKey, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
    }

    public static EncryptedPreferences getInstance(Context ctx) {
        if(ctx == null && instance == null)
            return null;

        if(instance == null) {
            synchronized (EncryptedPreferences.class) {
                if (instance == null) {
                    try {
                        instance = new EncryptedPreferences(ctx.getApplicationContext());
                    } catch(GeneralSecurityException | IOException exception) {
                        return null;
                    }
                }
            }
        }

        return instance;
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public String getUserId() {
        if(preferences != null)
            return preferences.getString(PREF_USER_ID, "");

        return null;
    }

    public String getEmail() {
        if(preferences != null) {
            return preferences.getString(PREF_USER_EMAIL, "");
        }

        return null;
    }

    public String getUsername() {
        if(preferences != null) {
            return preferences.getString(PREF_USER_NAME, "");
        }

        return null;
    }

    public String getPassword() {
        if(preferences != null) {
            return preferences.getString(PREF_USER_PASSWORD, "");
        }

        return null;
    }

    public String getAuthorizationToken() {
        if(preferences != null) {
            return preferences.getString(PREF_AUTHORIZATION_TOKEN, "");
        }

        return null;
    }

    public EncryptedPreferences setEmail(String email) {
        if(preferences != null) {
            preferences.edit()
                    .putString(PREF_USER_EMAIL, email)
                    .apply();
        }

        return this;
    }

    public EncryptedPreferences setUsername(String name) {
        if(preferences != null) {
            preferences.edit()
                    .putString(PREF_USER_NAME, name)
                    .apply();
        }

        return this;
    }

    public EncryptedPreferences setUserId(String id) {
        if(preferences != null) {
            preferences.edit()
                    .putString(PREF_USER_ID, id)
                    .apply();
        }

        return this;
    }

    public EncryptedPreferences setPassword(String psw) {
        if(preferences != null) {
            preferences.edit()
                    .putString(PREF_USER_PASSWORD, psw)
                    .apply();
        }

        return this;
    }

    public EncryptedPreferences setAuthorizationToken(String token) {
        if(preferences != null) {
            preferences.edit()
                    .putString(PREF_AUTHORIZATION_TOKEN, token)
                    .apply();
        }

        return this;
    }

    public void logout() {
        if(preferences != null) {
            preferences.edit()
                    .remove(PREF_USER_EMAIL)
                    .remove(PREF_USER_PASSWORD)
                    .remove(PREF_AUTHORIZATION_TOKEN)
                    .apply();
        }
    }
}
