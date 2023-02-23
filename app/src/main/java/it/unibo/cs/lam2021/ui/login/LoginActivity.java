package it.unibo.cs.lam2021.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.databinding.ActivityLoginBinding;
import it.unibo.cs.lam2021.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    private final View.OnClickListener toggleClickListener = v -> {
        loginViewModel.registrationFormToggle();
        loginViewModel.loginDataChanged(binding.email.getText().toString(), binding.username.getText().toString(),
                binding.password.getText().toString());
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this)
                .get(LoginViewModel.class);

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null)
                return;

            String usernameError = loginFormState.getUsernameError();
            String passwordError = loginFormState.getPasswordError();
            String emailError = loginFormState.getEmailError();

            binding.login.setEnabled(loginFormState.isDataValid());
            binding.usernameLayout.setError(usernameError);
            binding.emailLayout.setError(emailError);
            binding.passwordLayout.setError(passwordError);

            binding.usernameLayout.setErrorEnabled(usernameError != null);
            binding.emailLayout.setErrorEnabled(emailError != null);
            binding.passwordLayout.setErrorEnabled(passwordError != null);

            if (loginFormState.isRegistrationForm()) {
                binding.usernameLayout.setVisibility(View.VISIBLE);
                binding.description.setText(R.string.description_login);
                binding.registrationToggle.setText(R.string.action_sign_in);
                binding.login.setText(R.string.action_register);
            } else {
                binding.usernameLayout.setVisibility(View.GONE);
                binding.description.setText(R.string.description_register);
                binding.registrationToggle.setText(R.string.action_register);
                binding.login.setText(R.string.action_sign_in);
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null)
                return;

            binding.loading.setVisibility(View.GONE);
            if (!loginResult.getResult())
                showLoginFailed(loginResult.getMessage());
            else {
                loginSuccessful();
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(binding.email.getText().toString(), binding.username.getText().toString(),
                        binding.password.getText().toString());
            }
        };

        binding.username.post(() -> binding.username.addTextChangedListener(afterTextChangedListener));
        binding.email.post(() -> binding.email.addTextChangedListener(afterTextChangedListener));
        binding.password.post(() -> binding.password.addTextChangedListener(afterTextChangedListener));
        binding.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if(loginViewModel.getLoginFormState().getValue().isRegistrationForm()) {
                    binding.loading.setVisibility(View.VISIBLE);
                    loginViewModel.register(binding.email.getText().toString(),
                            binding.username.getText().toString(),
                            binding.password.getText().toString());
                }
                else loginViewModel.login(binding.email.getText().toString(),
                        binding.password.getText().toString());
            }
            return false;
        });

        binding.login.setOnClickListener(v -> {
            binding.loading.setVisibility(View.VISIBLE);
            String email = binding.email.getText().toString();
            String password = binding.password.getText().toString();

            InputMethodManager imm = getSystemService(InputMethodManager.class);
            if(imm.isActive()) {
                View focus = getCurrentFocus();
                if (focus == null)
                    focus = new View(this);
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }

            if(loginViewModel.getLoginFormState().getValue().isRegistrationForm()) {
                String username = binding.username.getText().toString();
                loginViewModel.register(email, username, password);
            } else
                loginViewModel.login(email, password);
        });

        binding.registrationToggle.setOnClickListener(toggleClickListener);
        binding.description.setOnClickListener(toggleClickListener);
    }

    private void loginSuccessful() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    private void showLoginFailed(String errorString) {
        Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
    }
}