package it.unibo.cs.lam2021.ui.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unibo.cs.lam2021.R;

public class NumberPickerDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "NumberPickerDialogFragment";

    private static final String ARG_INITIALVALUE = "initialValue";
    private static final String ARG_PREFERENCE = "preference";

    public interface OnValueSetListener {
        void onValueSet(String preference, int value);
    }

    private int initialValue;
    private String preference;

    private NumberPicker picker;

    public static NumberPickerDialogFragment newInstance(String preference, int initialValue) {
        Bundle args = new Bundle();
        args.putString(ARG_PREFERENCE, preference);
        args.putInt(ARG_INITIALVALUE, initialValue);
        NumberPickerDialogFragment fragment = new NumberPickerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        initialValue = args.getInt(ARG_INITIALVALUE);
        preference = args.getString(ARG_PREFERENCE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        picker = new NumberPicker(getContext());
        picker.setMinValue(1);
        picker.setMaxValue(30);
        picker.setValue(initialValue);
        return new MaterialAlertDialogBuilder(getContext())
                .setMessage(R.string.dlg_number_picker_message)
                .setView(picker)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(i == Dialog.BUTTON_POSITIVE && getActivity() instanceof OnValueSetListener)
            ((OnValueSetListener)getActivity()).onValueSet(preference, picker.getValue());
    }
}
