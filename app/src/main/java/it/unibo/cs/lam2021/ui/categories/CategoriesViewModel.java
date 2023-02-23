package it.unibo.cs.lam2021.ui.categories;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import it.unibo.cs.lam2021.database.ProductRepository;
import it.unibo.cs.lam2021.database.entity.Category;

public class CategoriesViewModel extends AndroidViewModel {

    private final LiveData<List<Category>> categories;
    private final MutableLiveData<HashSet<Integer>> selection = new MutableLiveData<>();

    private final ProductRepository repository = new ProductRepository(getApplication());

    public CategoriesViewModel(Application application) {
        super(application);

        categories = repository.getAllCategories();
        selection.setValue(new HashSet<>());
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }
    public MutableLiveData<HashSet<Integer>> getSelection() {
        return selection;
    }

    public void setSelection(HashSet<Integer> selection) {
        this.selection.setValue(selection);
    }

    public boolean isSelectionModeActive() {
        return !this.selection.getValue().isEmpty();
    }

    public int getSelectedItemsCount() {
        return selection.getValue().size();
    }

    public void setItemSelected(int i, boolean sel) {
        HashSet<Integer> newSelection = selection.getValue();
        if(sel) newSelection.add(i);
        else    newSelection.remove(i);

        selection.setValue(newSelection);
    }

    public void toggleItemSelection(int i) {
        HashSet<Integer> newSelection = selection.getValue();
        if(newSelection.contains(i))
            newSelection.remove(i);
        else
            newSelection.add(i);

        selection.setValue(newSelection);
    }

    public void selectAll() {
        HashSet<Integer> newSelection = selection.getValue();
        IntStream.range(0, categories.getValue().size()).forEach(newSelection::add);
        selection.setValue(newSelection);
    }

    public void clearSelection() {
        HashSet<Integer> newSelection = selection.getValue();
        newSelection.clear();
        selection.setValue(newSelection);
    }

    public void insertCategory(Category c) {
        repository.insertCategory(c);
    }

    public void updateCategory(Category c) {
        repository.updateCategory(c);
    }

    public void deleteCategory(Category...c) {
        repository.deleteCategory(c);
    }
}