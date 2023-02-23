package it.unibo.cs.lam2021.ui.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
class LoginFormState {
    @Nullable
    private final String usernameError;
    @Nullable
    private final String passwordError;
    @Nullable
    private final String emailError;
    private final boolean isDataValid;
    private final boolean isRegistrationForm;

    LoginFormState(boolean isRegistrationForm, @Nullable String usernameError, @Nullable String emailError, @Nullable String passwordError) {
        this.usernameError = usernameError;
        this.passwordError = passwordError;
        this.emailError = emailError;
        this.isDataValid = false;
        this.isRegistrationForm = isRegistrationForm;
    }

    LoginFormState(boolean isRegistrationForm, boolean isDataValid) {
        this.usernameError = null;
        this.emailError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
        this.isRegistrationForm = isRegistrationForm;
    }

    LoginFormState(boolean isRegistrationForm, LoginFormState old) {
        this.usernameError = old.usernameError;
        this.emailError = old.emailError;
        this.passwordError = old.passwordError;
        this.isDataValid = old.isDataValid;
        this.isRegistrationForm = isRegistrationForm;
    }

    boolean isRegistrationForm() { return isRegistrationForm; }

    @Nullable
    String getUsernameError() {
        return usernameError;
    }

    @Nullable
    String getEmailError() { return emailError; }

    @Nullable
    String getPasswordError() {
        return passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}