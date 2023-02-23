package it.unibo.cs.lam2021.ui.pantry;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.database.entity.Category;

public class FiltersDialogFragment extends DialogFragment {

    public static final String TAG = "FiltersDialogFragment";

    private static final String ARG_CATEGORIES = "categories";

    private PantryViewModel pantryViewModel;

    private List<Category> categories;
    private final ArrayList<String> options = new ArrayList<>();

    private boolean[] selected;

    public static FiltersDialogFragment newInstance(ArrayList<Category> categories) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CATEGORIES, categories);
        FiltersDialogFragment fragment = new FiltersDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        Bundle args = getArguments();
        categories = args.getParcelableArrayList(ARG_CATEGORIES);

        pantryViewModel = new ViewModelProvider(getParentFragment()).get(PantryViewModel.class);

        options.add(getString(R.string.filter_important));
        options.add(getString(R.string.filter_expired));
        options.add(getString(R.string.filter_uncategorized));
        categories.forEach(c -> options.add(c.getName()));

        selected = new boolean[options.size()];

        PantryViewModel.SearchAndFiltersData searchAndFilters = pantryViewModel.getSearchAndFilters().getValue();

        if(searchAndFilters.filters.contains(PantryViewModel.ProductsFilter.IMPORTANT)) {
            selected[0] = true;
        }

        if(searchAndFilters.filters.contains(PantryViewModel.ProductsFilter.EXPIRED)) {
            selected[1] = true;
        }

        if(searchAndFilters.filters.contains(PantryViewModel.ProductsFilter.UNCATEGORIZED)) {
            selected[2] = true;
        }

        for(int i = 0; i < categories.size(); i++) {
            if(searchAndFilters.categoryFilters.contains(categories.get(i).getId())) {
                selected[3+i] = true;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dlg_filters_title)
                .setMultiChoiceItems(options.toArray(new String[0]), selected, (dialogInterface, i, b) -> {
                    if(i == 0)
                        pantryViewModel.toggleFilter(PantryViewModel.ProductsFilter.IMPORTANT);
                    else if(i == 1)
                        pantryViewModel.toggleFilter(PantryViewModel.ProductsFilter.EXPIRED);
                    else if(i == 2)
                        pantryViewModel.toggleFilter(PantryViewModel.ProductsFilter.UNCATEGORIZED);
                    else if(i >= 3)
                        pantryViewModel.toggleCategoryFilter(categories.get(i-3).getId());
                })
                .setPositiveButton(R.string.dlg_filters_apply, null)
                .setNeutralButton(R.string.dlg_filters_clear, ((dialogInterface, i) -> pantryViewModel.clearFilters()))
                .create();
    }
}
