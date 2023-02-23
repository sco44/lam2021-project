package it.unibo.cs.lam2021.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.util.List;

import it.unibo.cs.lam2021.database.dao.CategoriesDao;
import it.unibo.cs.lam2021.database.dao.LocalProductDao;
import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.database.entity.LocalProduct;

public class ProductRepository {

    private LocalProductDao mProductDao;
    private CategoriesDao mCategoriesDao;

    private LiveData<List<LocalProductWithCategory>> mAllLocalProducts;
    private LiveData<List<Category>> mAllCategories;

    public ProductRepository(Application application){
        LocalDatabase db = LocalDatabase.getDatabase(application);
        mProductDao = db.LocalProductDao();
        mCategoriesDao = db.CategoriesDao();
    }

    public LiveData<List<LocalProductWithCategory>> getAllProducts(){
        if(mAllLocalProducts == null)
            mAllLocalProducts = mProductDao.loadAllLocalProducts();

        return mAllLocalProducts;
    }

    public ListenableFuture<LocalProductWithCategory> getProductWithId(String id) {
        return mProductDao.loadLocalProductById(id);
    }

    public LiveData<List<LocalProductWithCategory>> searchProducts(String query){
        return mProductDao.searchProducts(query);
    }

    public LiveData<List<LocalProductWithCategory>> filterAndSearchProducts(String query, boolean importantFilter, boolean expiredFilter, boolean uncategorizedFilter, Integer[] categories) {
        long expiry = LocalDate.MAX.toEpochDay();
        if(expiredFilter)
            expiry = LocalDate.now().toEpochDay();

        boolean[] important = { true, false };
        if(importantFilter)
            important = new boolean[] { true };

        if(uncategorizedFilter)
            return mProductDao.filterAndSearchProductsUncategorized(query, important, expiry, categories);

        if(categories.length == 0)
            return mProductDao.filterAndSearchProducts(query, important, expiry);
        else
            return mProductDao.filterAndSearchProductsInCategories(query, important, expiry, categories);
    }

    public List<LocalProduct> getExpiringProducts(long inDays) {
        long now = LocalDate.now().toEpochDay();
        return mProductDao.getExpiringProducts(now, now + inDays);
    }

    public List<LocalProduct> getExpiredProducts() {
        return mProductDao.getExpiredProducts(LocalDate.now().toEpochDay());
    }

    public List<LocalProduct> getImportantProducts() {
        return mProductDao.getImportantProducts();
    }

    public void insertProduct(LocalProduct p){
        LocalDatabase.databaseWriteExecutor.execute(() -> mProductDao.insertLocalProducts(p));
    }
    public void updateProduct(LocalProduct p){
        LocalDatabase.databaseWriteExecutor.execute(() -> mProductDao.updateLocalProducts(p));
    }

    public void deleteProducts(LocalProduct...p){
        LocalDatabase.databaseWriteExecutor.execute(()-> mProductDao.deleteLocalProducts(p));
    }

    public LiveData<List<Category>> getAllCategories() {
        if(mAllCategories == null)
            mAllCategories = mCategoriesDao.loadAllCategories();

        return mAllCategories;
    }

    public void insertCategory(Category c){
        LocalDatabase.databaseWriteExecutor.execute(() -> mCategoriesDao.insertCategory(c));
    }

    public void updateCategory(Category c){
        LocalDatabase.databaseWriteExecutor.execute(() -> mCategoriesDao.updateCategory(c));
    }
    public void deleteCategory(Category... c){
        LocalDatabase.databaseWriteExecutor.execute(() -> mCategoriesDao.deleteCategory(c));
    }
}
