package it.unibo.cs.lam2021.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.databinding.FragmentCategoriesBinding;

public class CategoriesFragment extends Fragment {

    private CategoriesViewModel categoriesViewModel;
    private FragmentCategoriesBinding binding;

    private CategoriesListAdapter adapter;

    private ActionMode actionMode;

    private class SelectionActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.selection, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.delete_item:
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(getString(R.string.dlg_delete_title, getResources().getQuantityString(R.plurals.category, categoriesViewModel.getSelectedItemsCount(), categoriesViewModel.getSelectedItemsCount())))
                            .setMessage(R.string.dlg_delete_message)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                                categoriesViewModel.deleteCategory(
                                        categoriesViewModel.getSelection().getValue()
                                                .stream().map(adapter::getItem).toArray(Category[]::new)
                                );
                                categoriesViewModel.clearSelection();
                            })
                            .setNegativeButton(R.string.no, null)
                            .create().show();
                    return true;

                case R.id.select_all:
                    categoriesViewModel.selectAll();
                    updateTitle();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            categoriesViewModel.clearSelection();
            actionMode = null;
        }

        public void updateTitle() {
            actionMode.setTitle(getResources().getQuantityString(R.plurals.category, categoriesViewModel.getSelectedItemsCount(), categoriesViewModel.getSelectedItemsCount()));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        categoriesViewModel =
                new ViewModelProvider(this).get(CategoriesViewModel.class);

        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postponeEnterTransition();

        adapter = new CategoriesListAdapter(getContext());

        binding.categoriesList.setAdapter(adapter);
        binding.categoriesList.setOnItemClickListener((adapterView, v, position, id) -> {
            if(categoriesViewModel.isSelectionModeActive()) {
                categoriesViewModel.toggleItemSelection(position);
            } else {
                Category c = (Category) adapterView.getAdapter().getItem(position);
                CategoryDialogFragment.newInstance(CategoryDialogFragment.MODE_EDIT, new Category(c)).show(getChildFragmentManager(), CategoryDialogFragment.TAG);
            }
        });

        binding.categoriesList.setOnItemLongClickListener((adapterView, v, position, id) -> {
            if (!categoriesViewModel.isSelectionModeActive()) {
                categoriesViewModel.setItemSelected(position, true);
            } else {
                binding.categoriesList.performItemClick(v, position, id);
            }

            return true;
        });

        categoriesViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if(categories == null)
                return;

            if(categories.isEmpty())
                binding.categoriesEmpty.setVisibility(View.VISIBLE);
            else
                binding.categoriesEmpty.setVisibility(View.GONE);

            adapter.setCategories(categories);
        });

        categoriesViewModel.getSelection().observe(getViewLifecycleOwner(), selection -> {
            adapter.setSelection(selection);

            if(selection.isEmpty()) {
                if(actionMode != null) {
                    actionMode.finish();
                    binding.categoriesList.setDrawSelectorOnTop(true);

                }
            } else {
                if(actionMode == null) {
                    ((AppCompatActivity) getActivity()).startSupportActionMode(new SelectionActionModeCallback());
                    binding.categoriesList.setDrawSelectorOnTop(false);
                }

                actionMode.setTitle(getResources().getQuantityString(R.plurals.category, selection.size(), selection.size()));
            }
        });

        startPostponedEnterTransition();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.categories, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.category_new) {
            CategoryDialogFragment.newInstance(CategoryDialogFragment.MODE_CREATE, null).show(getChildFragmentManager(), CategoryDialogFragment.TAG);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}