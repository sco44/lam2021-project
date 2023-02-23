package it.unibo.cs.lam2021.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import it.unibo.cs.lam2021.database.LocalProductWithCategory;
import it.unibo.cs.lam2021.database.entity.LocalProduct;

@Dao
public interface LocalProductDao {
    @Query("SELECT * FROM localProducts")
    @Transaction
    LiveData<List<LocalProductWithCategory>> loadAllLocalProducts();

    @Query("SELECT * FROM localProducts WHERE id = :id")
    @Transaction
    ListenableFuture<LocalProductWithCategory> loadLocalProductById(String id);

    @Query("SELECT * FROM localProducts WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR barcode = :query")
    @Transaction
    LiveData<List<LocalProductWithCategory>> searchProducts(String query);

    @Query("SELECT * FROM localProducts WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR barcode = :query) AND important IN (:important) AND expiry <= :expiry")
    @Transaction
    LiveData<List<LocalProductWithCategory>> filterAndSearchProducts(String query, boolean[] important, long expiry);

    @Query("SELECT * FROM localProducts WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR barcode = :query) AND important IN (:important) AND expiry <= :expiry AND (categoryId IS NULL OR categoryId IN (:categories))")
    @Transaction
    LiveData<List<LocalProductWithCategory>> filterAndSearchProductsUncategorized(String query, boolean[] important, long expiry, Integer[] categories);

    @Query("SELECT * FROM localProducts WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR barcode = :query) AND important IN (:important) AND expiry <= :expiry AND categoryId IN (:categories)")
    @Transaction
    LiveData<List<LocalProductWithCategory>> filterAndSearchProductsInCategories(String query, boolean[] important, long expiry, Integer[] categories);

    @Query("SELECT * FROM localProducts WHERE :now < expiry AND expiry <= :cutoff")
    List<LocalProduct> getExpiringProducts(long now, long cutoff);

    @Query("SELECT * FROM localProducts WHERE expiry <= :now")
    List<LocalProduct> getExpiredProducts(long now);

    @Query("SELECT * FROM localProducts WHERE important = 1")
    List<LocalProduct> getImportantProducts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocalProducts(LocalProduct... localProducts);

    @Update
    void updateLocalProducts(LocalProduct... localProducts);

    @Delete
    void deleteLocalProducts(LocalProduct... localProducts);
}
