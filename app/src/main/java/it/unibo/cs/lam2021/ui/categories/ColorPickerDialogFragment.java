package it.unibo.cs.lam2021.ui.categories;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unibo.cs.lam2021.R;

public class ColorPickerDialogFragment extends DialogFragment {

    public static final String TAG = "ColorPickerDialogFragment";

    private static final String ARG_SELECTEDCOLOR = "selectedColor";

    private static final class ColorGridAdapter extends BaseAdapter {

        private final int[] colors;

        private final int selectedColor;
        private final ColorPickerDialogFragment fragment;

        public ColorGridAdapter(ColorPickerDialogFragment fragment, int selectedColor) {
            this.fragment = fragment;
            colors = fragment.getContext().getResources().getIntArray(R.array.category_colors);
            this.selectedColor = selectedColor;
        }

        @Override
        public int getCount() {
            return colors.length;
        }

        @Override
        public Integer getItem(int i) {
            return colors[i];
        }

        @Override
        public long getItemId(int i) {
            return colors[i];
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null)
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_color_picker, viewGroup, false);

            MaterialButton button = view.findViewById(R.id.color_button);
            button.setBackgroundColor(getItem(i));
            button.setOnClickListener(v -> {
                if(fragment.getParentFragment() instanceof OnColorPickedListener)
                    ((OnColorPickedListener) fragment.getParentFragment()).onColorPicked(getItem(i));
                fragment.dismiss();
            });
            if(getItem(i) == selectedColor) {
                button.setIconResource(R.drawable.ic_check);
            }

            return view;
        }
    }

    public interface OnColorPickedListener {
        void onColorPicked(int color);
    }

    public static ColorPickerDialogFragment newInstance(int selectedColor) {
        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTEDCOLOR, selectedColor);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int selectedColor = getArguments().getInt(ARG_SELECTEDCOLOR);
        View v = getLayoutInflater().inflate(R.layout.dialog_color_picker, null);
        GridView gv = v.findViewById(R.id.color_grid);
        gv.setAdapter(new ColorGridAdapter(this, selectedColor));
        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dlg_color_title)
                .setView(v)
                .setNeutralButton(R.string.dlg_color_custom_button, (dialog, i) ->
                         CustomColorDialogFragment.newInstance(getArguments().getInt(ARG_SELECTEDCOLOR)).show(getParentFragmentManager(), CustomColorDialogFragment.TAG))
                .create();
    }
}
