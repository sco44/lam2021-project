package it.unibo.cs.lam2021.ui.categories;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import it.unibo.cs.lam2021.R;

public class CustomColorDialogFragment extends DialogFragment {

    public static final String TAG = "CustomColorDialogFragment";

    private static final String ARG_SELECTEDCOLOR = "selectedColor";

    private Color color = new Color();

    private EditText colorView;
    private Slider sliderRed;
    private Slider sliderGreen;
    private Slider sliderBlue;

    private final Slider.OnChangeListener colorChangeListener = new Slider.OnChangeListener() {
        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            switch(slider.getId()) {
                case R.id.sliderRed:
                    color = Color.valueOf(value/255.0f, color.green(), color.blue());
                    break;
                case R.id.sliderGreen:
                    color = Color.valueOf(color.red(), value/255.0f, color.blue());
                    break;
                case R.id.sliderBlue:
                    color = Color.valueOf(color.red(), color.green(), value/255.0f);
                    break;
            }

            colorView.setBackgroundTintList(ColorStateList.valueOf(color.toArgb()));
            if(!colorView.isFocused())
                colorView.setText(String.format("%06X", color.toArgb() & 0x00FFFFFF));
            colorView.setTextColor(color.luminance() > 0.33f ? Color.BLACK : Color.WHITE);
        }
    };

    private final Slider.OnSliderTouchListener sliderTouchListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
            colorView.removeTextChangedListener(colorTextWatcher);

            if(colorView.hasFocus()) {
                colorView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(colorView.getWindowToken(), 0);
            }
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            colorView.addTextChangedListener(colorTextWatcher);
        }
    };

    private final LabelFormatter labelFormatter = value -> String.valueOf((int) value);

    private final TextWatcher colorTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.length() == 0) {
                sliderRed.setValue(0);
                sliderGreen.setValue(0);
                sliderBlue.setValue(0);
                return;
            }

            int c = Integer.parseInt(s.toString(), 16);

            sliderRed.setValue(Color.red(c));
            sliderGreen.setValue(Color.green(c));
            sliderBlue.setValue(Color.blue(c));
        }
    };

    public static CustomColorDialogFragment newInstance(int selectedColor) {
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTEDCOLOR, selectedColor);
        CustomColorDialogFragment fragment = new CustomColorDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Color selectedColor = Color.valueOf(getArguments().getInt(ARG_SELECTEDCOLOR));
        View v = getLayoutInflater().inflate(R.layout.dialog_custom_color_picker, null);
        colorView = v.findViewById(R.id.colorView);
        colorView.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
        sliderRed = v.findViewById(R.id.sliderRed);
        sliderGreen = v.findViewById(R.id.sliderGreen);
        sliderBlue = v.findViewById(R.id.sliderBlue);

        sliderRed.addOnChangeListener(colorChangeListener);
        sliderRed.setLabelFormatter(labelFormatter);
        sliderRed.addOnSliderTouchListener(sliderTouchListener);
        sliderGreen.addOnChangeListener(colorChangeListener);
        sliderGreen.setLabelFormatter(labelFormatter);
        sliderGreen.addOnSliderTouchListener(sliderTouchListener);
        sliderBlue.addOnChangeListener(colorChangeListener);
        sliderBlue.setLabelFormatter(labelFormatter);
        sliderBlue.addOnSliderTouchListener(sliderTouchListener);

        sliderRed.setValue(selectedColor.red()*255);
        sliderGreen.setValue(selectedColor.green()*255);
        sliderBlue.setValue(selectedColor.blue()*255);

        colorView.addTextChangedListener(colorTextWatcher);

        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dlg_color_title)
                .setView(v)
                .setPositiveButton(android.R.string.ok, (dialog, i) -> {
                    if(getParentFragment() instanceof ColorPickerDialogFragment.OnColorPickedListener)
                        ((ColorPickerDialogFragment.OnColorPickedListener) getParentFragment()).onColorPicked(color.toArgb());
                })
                .create();
    }
}

