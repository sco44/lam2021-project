package it.unibo.cs.lam2021.ui.pantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.database.entity.LocalProduct;
import it.unibo.cs.lam2021.databinding.FragmentPantryBinding;

public class PantryFragment extends Fragment implements SearchView.OnQueryTextListener {

    final private static class PantryItemDetailsLookup extends ItemDetailsLookup<Long> {
        private final RecyclerView mRecyclerView;

        PantryItemDetailsLookup(RecyclerView view) {
            mRecyclerView = view;
        }

        @Nullable
        @Override
        public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
            View v = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if(v != null) {
                RecyclerView.ViewHolder vh = mRecyclerView.getChildViewHolder(v);
                if (vh instanceof PantryAdapter.ViewHolder) {
                    return ((PantryAdapter.ViewHolder) vh).getItemDetails();
                }
            }
            return null;
        }
    }

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
                    Selection<Long> sel = selectionTracker.getSelection();
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(getString(R.string.dlg_delete_title, getResources().getQuantityString(R.plurals.product, sel.size(), sel.size())))
                            .setMessage(R.string.dlg_delete_message)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                                List<LocalProduct> toRemove = new ArrayList<>();
                                sel.iterator().forEachRemaining((o) -> toRemove.add(pantryViewModel.getProducts().getValue().get(o.intValue()).product));
                                pantryViewModel.deleteProducts(toRemove.toArray(new LocalProduct[0]));
                                selectionTracker.clearSelection();
                            })
                            .setNegativeButton(R.string.no, null)
                            .create().show();

                    return true;

                case R.id.select_all:
                    LongStream.range(0, adapter.getItemCount()).forEach((id) -> selectionTracker.select(id));
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectionTracker.clearSelection();
            actionMode = null;
        }
    }

    private ActionMode actionMode;

    private PantryViewModel pantryViewModel;

    private FragmentPantryBinding binding;
    private SearchView searchView;
    private SelectionTracker<Long> selectionTracker;

    private PantryAdapter adapter;

    Map<Integer, String> categoriesMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        pantryViewModel =
                new ViewModelProvider(this).get(PantryViewModel.class);

        binding = FragmentPantryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postponeEnterTransition();
        adapter = new PantryAdapter(getContext());
        binding.pantryRecycler.setAdapter(adapter);
        selectionTracker = new SelectionTracker.Builder<>(
                "pantry",
                binding.pantryRecycler,
                new StableIdKeyProvider(binding.pantryRecycler),
                new PantryItemDetailsLookup(binding.pantryRecycler),
                StorageStrategy.createLongStorage())
                .build();

        adapter.setSelectionTracker(selectionTracker);

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver<Long>() {
            @Override
            public void onItemStateChanged(@NonNull Long key, boolean selected) {
                super.onItemStateChanged(key, selected);
                Selection<Long> sel = selectionTracker.getSelection();
                if(!sel.isEmpty()) {
                    if (actionMode == null)
                        ((AppCompatActivity) getActivity()).startSupportActionMode(new SelectionActionModeCallback());
                    actionMode.setTitle(getResources().getQuantityString(R.plurals.product, sel.size(), sel.size()));
                } else {
                    if (actionMode != null)
                        actionMode.finish();
                }
            }
        });

        binding.pantryRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        pantryViewModel.getProducts().observe(getViewLifecycleOwner(), localProducts ->  {
            adapter.setProducts(localProducts);
            if(localProducts.size() == 0) {
                PantryViewModel.SearchAndFiltersData searchAndFilters = pantryViewModel.getSearchAndFilters().getValue();
                if(searchAndFilters.filters.isEmpty() && searchAndFilters.query.isEmpty())
                    binding.pantryEmpty.setText(R.string.pantry_empty);
                else
                    binding.pantryEmpty.setText(R.string.pantry_no_products_found);

                binding.pantryEmpty.setVisibility(View.VISIBLE);
            }
            else
                binding.pantryEmpty.setVisibility(View.GONE);
        });

        pantryViewModel.getCategoriesList().observe(getViewLifecycleOwner(), categoriesList ->
            categoriesMap = categoriesList.stream().collect(Collectors.toMap(Category::getId, Category::getName))
        );

        pantryViewModel.getSearchAndFilters().observe(getViewLifecycleOwner(), searchAndFilters -> {
            adapter.setSearchQuery(searchAndFilters.query);

            if(searchAndFilters.filters.isEmpty()) {
                binding.filterFrame.setVisibility(View.GONE);
            } else {
                StringBuilder txt = new StringBuilder(getString(R.string.banner_active_filters)).append(":\n");
                if(searchAndFilters.filters.contains(PantryViewModel.ProductsFilter.IMPORTANT)) {
                    txt.append("  - ").append(getString(R.string.banner_filter_important)).append("\n");
                }
                if(searchAndFilters.filters.contains(PantryViewModel.ProductsFilter.EXPIRED)) {
                    txt.append("  - ").append(getString(R.string.banner_filter_expired)).append("\n");
                }
                if(searchAndFilters.filters.contains(PantryViewModel.ProductsFilter.UNCATEGORIZED)) {
                    txt.append("  - ").append(getString(R.string.banner_filter_uncategorized)).append("\n");
                }
                if(searchAndFilters.filters.contains(PantryViewModel.ProductsFilter.CATEGORIES) && categoriesMap != null) {
                    txt.append("  - ").append(getString(R.string.banner_filter_categories)).append(":\n");
                    for(Integer c : searchAndFilters.categoryFilters) {
                        txt.append("    - ").append(categoriesMap.get(c)).append("\n");
                    }
                }
                txt.setLength(txt.length()-1);
                binding.filterFrame.setVisibility(View.VISIBLE);
                binding.filterAlert.setText(txt);
            }
        });

        binding.filterRemove.setOnClickListener(v -> pantryViewModel.clearFilters());

        startPostponedEnterTransition();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pantry, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) searchItem.getActionView();
        String query = pantryViewModel.getSearchAndFilters().getValue().query;
        if(!query.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(query, false);
            searchView.clearFocus();
        }

        searchView.post(() -> searchView.setOnQueryTextListener(this) );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.filters_menu) {
            pantryViewModel.getCategoriesList().observe(getViewLifecycleOwner(), new Observer<List<Category>>() {
                @Override
                public void onChanged(List<Category> categories) {
                    if(categories == null)
                        return;

                    ArrayList<Category> arr = new ArrayList<>(categories);
                    FiltersDialogFragment.newInstance(arr).show(getChildFragmentManager(), FiltersDialogFragment.TAG);

                    pantryViewModel.getCategoriesList().removeObserver(this);
                }
            });
            return true;
        }

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        pantryViewModel.setSearchQuery(s);
        return true;
    }

}