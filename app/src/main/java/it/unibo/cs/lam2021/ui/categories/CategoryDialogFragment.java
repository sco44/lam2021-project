package it.unibo.cs.lam2021.ui.categories;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.database.entity.Category;

public class CategoryDialogFragment extends DialogFragment implements View.OnClickListener, ColorPickerDialogFragment.OnColorPickedListener {

    public static final String TAG = "CategoryDialogFragment";

    public static final int MODE_EDIT = 0;
    public static final int MODE_CREATE = 1;

    private static final String ARG_MODE = "mode";
    private static final String ARG_CATEGORY = "category";

    private static final String COLOR = "color";

    private CategoriesViewModel categoriesViewModel;

    private Category category;
    private int mode;

    private EditText name;
    private EditText description;
    private Button color;

    public static CategoryDialogFragment newInstance(int mode, Category c) {
        CategoryDialogFragment fragment = new CategoryDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);
        if(c != null) args.putParcelable(ARG_CATEGORY, c);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mode = getArguments().getInt(ARG_MODE);

        if(mode == MODE_EDIT)
            category = getArguments().getParcelable(ARG_CATEGORY);
        else {
            int[] categoryColors = getContext().getResources().getIntArray(R.array.category_colors);
            category = new Category(null, "", "", categoryColors[(int) (Math.random() * categoryColors.length)]);
        }
        categoriesViewModel = new ViewModelProvider(getParentFragment()).get(CategoriesViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_category, null);

        name = ((TextInputLayout) view.findViewById(R.id.cat_dlg_name)).getEditText();
        description = ((TextInputLayout) view.findViewById(R.id.cat_dlg_desc)).getEditText();
        color = view.findViewById(R.id.cat_dlg_color);

        if(savedInstanceState != null)
            category = savedInstanceState.getParcelable(ARG_CATEGORY);

        AlertDialog dlg = new MaterialAlertDialogBuilder(getContext())
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.delete, null)
                .create();
        if(mode == MODE_CREATE)
            dlg.setTitle(R.string.dlg_category_new);
        else
            dlg.setTitle(R.string.dlg_category_edit);

        name.setText(category.getName());
        description.setText(category.getDescription());
        color.setBackgroundColor(category.getColor());
        color.setTextColor(Color.valueOf(category.getColor()).luminance() > 0.33f ? Color.BLACK : Color.WHITE);
        color.setOnClickListener(v -> ColorPickerDialogFragment
                .newInstance(category.getColor())
                .show(CategoryDialogFragment.this.getChildFragmentManager(), ColorPickerDialogFragment.TAG));

        //Custom button listener prevents dialog closing if name is empty
        dlg.setOnShowListener(dialogInterface -> {
            if(mode == MODE_CREATE)
                dlg.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);    //hide delete button

            dlg.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(CategoryDialogFragment.this);
            dlg.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(CategoryDialogFragment.this);
        });

        return dlg;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == android.R.id.button1) {
            String nameStr = name.getText().toString();
            if (nameStr.isEmpty()) {
                Toast.makeText(getContext(), R.string.dlg_category_no_name_error, Toast.LENGTH_SHORT).show();
            } else {
                category.setName(nameStr);
                category.setDescription(description.getText().toString());

                if(mode == MODE_CREATE)
                    categoriesViewModel.insertCategory(category);
                else
                    categoriesViewModel.updateCategory(category);

                getDialog().dismiss();
            }
        } else {
            new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.dlg_delete_category_title)
                    .setMessage(R.string.dlg_delete_message)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        categoriesViewModel.deleteCategory(category);
                        Toast.makeText(getContext(), getString(R.string.dlg_category_deleted, category.getName()), Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    })
                    .setNegativeButton(R.string.no, null)
                    .create()
                    .show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_CATEGORY, category);
    }

    @Override
    public void onColorPicked(int clr) {
        color.setBackgroundColor(clr);
        color.setTextColor(Color.valueOf(clr).luminance() > 0.33f ? Color.BLACK : Color.WHITE);
        category.setColor(clr);
    }
}
