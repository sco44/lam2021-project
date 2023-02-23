package it.unibo.cs.lam2021.ui.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unibo.cs.lam2021.preferences.EncryptedPreferences;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.ui.login.LoginActivity;

public class LogoutConfirmDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static String TAG = "LogoutConfirmDialogFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dlg_logout_title)
                .setMessage(R.string.dlg_logout_text)
                .setPositiveButton(R.string.yes, this)
                .setNegativeButton(R.string.no, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                EncryptedPreferences.getInstance(getContext()).logout();
                ApiService.getInstance(getContext()).setAuthorization("");
                getContext().startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finishAffinity();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }
}
