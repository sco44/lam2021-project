package it.unibo.cs.lam2021.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unibo.cs.lam2021.R;

public class NoInternetDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "NoInternetDialogFragment";

    private static final String ARG_WITH_CANCEL_BUTTON = "withCancelButton";
    private static final String ARG_MESSAGE = "message";

    public interface OnActionListener {
        void onNoInternetRetry(DialogInterface dialog);
        void onNoInternetCancel(DialogInterface dialog);
        void onNoInternetOfflineMode(DialogInterface dialog);
    }

    private int message;
    private boolean withCancelButton;

    public static NoInternetDialogFragment newInstance(int message, boolean withCancelButton) {
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE, message);
        args.putBoolean(ARG_WITH_CANCEL_BUTTON, withCancelButton);
        NoInternetDialogFragment fragment = new NoInternetDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        message = args.getInt(ARG_MESSAGE);
        withCancelButton = args.getBoolean(ARG_WITH_CANCEL_BUTTON);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.no_internet_dlg_title)
                .setMessage(message)
                .setPositiveButton(R.string.retry, null);
        if(withCancelButton)
            builder.setNegativeButton(R.string.cancel, null);
        builder.setNeutralButton(R.string.offline_mode, null);
        setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(dialogInterface -> {
            getDialog().findViewById(android.R.id.button1).setOnClickListener(this);
            if(withCancelButton)
                getDialog().findViewById(android.R.id.button2).setOnClickListener(this);
            getDialog().findViewById(android.R.id.button3).setOnClickListener(this);
        });
        return dialog;
    }

    @Override
    public void onClick(View v) {
        if(!(getActivity() instanceof OnActionListener))
            return;

        OnActionListener listener = (OnActionListener) getActivity();
        switch(v.getId()) {
            case android.R.id.button1:
                listener.onNoInternetRetry(getDialog());
                break;

            case android.R.id.button2:
                listener.onNoInternetCancel(getDialog());
                break;

            case android.R.id.button3:
                listener.onNoInternetOfflineMode(getDialog());
                break;
        }
    }
}
