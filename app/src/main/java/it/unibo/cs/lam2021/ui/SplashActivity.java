package it.unibo.cs.lam2021.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.barcode.BarcodeCacheHelper;
import it.unibo.cs.lam2021.preferences.EncryptedPreferences;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.api.User;
import it.unibo.cs.lam2021.ui.login.LoginActivity;
import it.unibo.cs.lam2021.ui.main.MainActivity;
import retrofit2.HttpException;


public class SplashActivity extends AppCompatActivity implements NoInternetDialogFragment.OnActionListener {

    private EncryptedPreferences prefs;
    private ListenableFuture<User> userFuture;

    private Bundle animationBundle;

    private final Runnable userResponseHandler = new Runnable() {
        @Override
        public void run() {
            try {
                User user = userFuture.get();
                prefs.setUsername(user.getUsername())
                        .setUserId(user.getId())
                        .setAuthorizationToken(user.getAuthorizationToken());

                ApplicationPreferences.getInstance(SplashActivity.this).setNetworkOnline(true);
                startActivity(new Intent(SplashActivity.this, MainActivity.class), animationBundle);
                finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if (t instanceof ApiService.InvalidCredentialsException) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class), animationBundle);
                    finish();
                }
                else if (t instanceof HttpException) {
                    int errorCode = ((HttpException) t).code();
                    Toast.makeText(SplashActivity.this, getString(R.string.error_http, errorCode), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    NoInternetDialogFragment.newInstance(
                            R.string.no_internet_dlg_message_splash,
                            true).show(getSupportFragmentManager(), NoInternetDialogFragment.TAG);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = EncryptedPreferences.getInstance(this);
        if(prefs == null) {
            Toast.makeText(getApplicationContext(), R.string.error_encrypted_preferences, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        ApplicationPreferences.getInstance(this);
        BarcodeCacheHelper.getInstance(this);

        animationBundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();

        String email = prefs.getEmail();
        String password = prefs.getPassword();

        if(!email.isEmpty() || !password.isEmpty()) {
            userFuture = ApiService.getInstance(this).login(prefs.getEmail(), prefs.getPassword());
            userFuture.addListener(userResponseHandler, ContextCompat.getMainExecutor(this));
        } else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class), animationBundle);
            finish();
        }
    }

    @Override
    public void onNoInternetRetry(DialogInterface dialog) {
        dialog.dismiss();
        userFuture = ApiService.getInstance(this).login(prefs.getEmail(), prefs.getPassword());
        userFuture.addListener(userResponseHandler, ContextCompat.getMainExecutor(SplashActivity.this));
    }

    @Override
    public void onNoInternetCancel(DialogInterface dialog) {
        dialog.dismiss();
        finish();
    }

    @Override
    public void onNoInternetOfflineMode(DialogInterface dialog) {
        dialog.dismiss();
        ApplicationPreferences.getInstance(SplashActivity.this).setNetworkOnline(false);
        startActivity(new Intent(SplashActivity.this, MainActivity.class), animationBundle);
        finish();
    }
}