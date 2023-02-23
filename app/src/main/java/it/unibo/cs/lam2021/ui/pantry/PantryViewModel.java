package it.unibo.cs.lam2021.ui.pantry;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;

import it.unibo.cs.lam2021.database.LocalProductWithCategory;
import it.unibo.cs.lam2021.database.ProductRepository;
import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.database.entity.LocalProduct;

public class PantryViewModel extends AndroidViewModel {

    public enum ProductsFilter {
        EXPIRED,
        IMPORTANT,
        UNCATEGORIZED,
        CATEGORIES
    }

    public static class SearchAndFiltersData {
        public EnumSet<ProductsFilter> filters = EnumSet.noneOf(ProductsFilter.class);
        public HashSet<Integer> categoryFilters = new HashSet<>();
        public String query = "";
    }

    //private static final Pattern BARCODE_PATTERN = Pattern.compile("^[0-9]{8}$|^[0-9]{13}$");

    private final LiveData<List<LocalProductWithCategory>> products;
    private final LiveData<List<Category>> categoriesList;
    private final MutableLiveData<SearchAndFiltersData> searchAndFilters = new MutableLiveData<>();

    private final ProductRepository repository = new ProductRepository(getApplication());

    public PantryViewModel(Application application) {
        super(application);

        searchAndFilters.setValue(new SearchAndFiltersData());
        products = Transformations.switchMap(searchAndFilters, (sf) -> {
            if(sf.filters.isEmpty() && sf.query.isEmpty())
                return Transformations.distinctUntilChanged(repository.getAllProducts());
            else if(sf.filters.isEmpty())
                return Transformations.distinctUntilChanged(repository.searchProducts(sf.query));
            else
                return Transformations.distinctUntilChanged(
                        repository.filterAndSearchProducts(
                                sf.query,
                                sf.filters.contains(ProductsFilter.IMPORTANT),
                                sf.filters.contains(ProductsFilter.EXPIRED),
                                sf.filters.contains(ProductsFilter.UNCATEGORIZED),
                                sf.categoryFilters.toArray(new Integer[0])
                        )
                );
        });

        categoriesList = Transformations.distinctUntilChanged(repository.getAllCategories());
    }

    public LiveData<List<LocalProductWithCategory>> getProducts() {
        return products;
    }

    public LiveData<List<Category>> getCategoriesList() {
        return categoriesList;
    }

    public LiveData<SearchAndFiltersData> getSearchAndFilters() {
        return searchAndFilters;
    }

    public void setSearchQuery(String q) {
        SearchAndFiltersData data = searchAndFilters.getValue();
        data.query = q;
        searchAndFilters.setValue(data);
    }

    public void toggleFilter(ProductsFilter filter) {
        SearchAndFiltersData data = searchAndFilters.getValue();
        if(data.filters.contains(filter))
            data.filters.remove(filter);
        else
            data.filters.add(filter);

        searchAndFilters.setValue(data);
    }

    public void toggleCategoryFilter(int catId) {
        SearchAndFiltersData data = searchAndFilters.getValue();
        if(data.categoryFilters.contains(catId))
            data.categoryFilters.remove(catId);
        else
            data.categoryFilters.add(catId);

        if(data.categoryFilters.isEmpty())
            data.filters.remove(ProductsFilter.CATEGORIES);
        else
            data.filters.add(ProductsFilter.CATEGORIES);

        searchAndFilters.setValue(data);
    }

    public void clearFilters() {
        SearchAndFiltersData data = searchAndFilters.getValue();
        data.filters = EnumSet.noneOf(ProductsFilter.class);
        data.categoryFilters = new HashSet<>();
        searchAndFilters.setValue(data);
    }

    public void deleteProducts(LocalProduct... ps) {
        repository.deleteProducts(ps);
    }
}