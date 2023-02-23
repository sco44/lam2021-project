package it.unibo.cs.lam2021.ui.login;

import android.app.Application;
import android.util.Patterns;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import it.unibo.cs.lam2021.preferences.EncryptedPreferences;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.api.User;
import retrofit2.HttpException;

public class LoginViewModel extends AndroidViewModel {

    private final Pattern usernameRegex = Pattern.compile("[A-z0-9_.\\-]{3,}");
    private final Pattern passwordRegex = Pattern.compile("(?=.*[A-Z].*)(?=.*[a-z].*)(?=.*[0-9].*)(?=.*[\\W_-].*).{8}");

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    public LoginViewModel(Application application) {
        super(application);
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        ListenableFuture<User> loginFuture = ApiService.getInstance(getApplication()).login(email, password);
        loginFuture.addListener(() -> {
            try {
                User user = loginFuture.get();
                EncryptedPreferences.getInstance(getApplication())
                        .setUsername(user.getUsername())
                        .setUserId(user.getId())
                        .setEmail(email)
                        .setPassword(password);

                loginResult.setValue(new LoginResult());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if(t instanceof ApiService.InvalidCredentialsException)
                    loginResult.setValue(new LoginResult(getApplication().getString(R.string.error_invalid_credentials)));
                else if(t instanceof HttpException) {
                    int errorCode = ((HttpException) t).code();
                    loginResult.setValue(new LoginResult(getApplication().getString(R.string.error_http, errorCode)));
                } else
                    loginResult.setValue(new LoginResult(getApplication().getString(R.string.error_no_internet)));
            }
        }, ContextCompat.getMainExecutor(getApplication()));
    }

    public void register(String email, String username, String password) {
        ListenableFuture<User> userFuture = ApiService.getInstance(getApplication()).register(email, username, password);
        userFuture.addListener(() -> {
            try {
                userFuture.get();
                login(email, password);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if(t instanceof ApiService.AlreadyRegisteredException) {
                    loginResult.setValue(new LoginResult(getApplication().getString(R.string.error_already_registered)));
                } else if(t instanceof HttpException) {
                    int errorCode = ((HttpException) e.getCause()).code();
                    loginResult.setValue(new LoginResult(getApplication().getString(R.string.error_http, errorCode)));
                } else
                    loginResult.setValue(new LoginResult(getApplication().getString(R.string.error_no_internet)));
            }
        }, ContextCompat.getMainExecutor(getApplication()));
    }

    public void loginDataChanged(String email, String username, String password) {
        boolean registrationForm = false;
        if(loginFormState.getValue() != null) {
            registrationForm = loginFormState.getValue().isRegistrationForm();
        }

        if(!isEmailValid(email)) {
            loginFormState.setValue(new LoginFormState(registrationForm, null, getApplication().getString(R.string.invalid_email), null));
        } else if (registrationForm && !isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(true, getApplication().getString(R.string.invalid_username), null, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(registrationForm, null, null, getApplication().getString(R.string.invalid_password)));
        } else {
            loginFormState.setValue(new LoginFormState(registrationForm,true));
        }
    }

    public void registrationFormToggle() {
        LoginFormState old = loginFormState.getValue();
        if(old != null)
            loginFormState.setValue(new LoginFormState(!old.isRegistrationForm(), old));
        else
            loginFormState.setValue(new LoginFormState(true, null, null, null));
    }

    private boolean isUserNameValid(String username) {
        return username != null && usernameRegex.matcher(username).matches();
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && passwordRegex.matcher(password).find();
    }
}