package it.unibo.cs.lam2021.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.unibo.cs.lam2021.database.entity.Category;

@Dao
public interface CategoriesDao {

    @Query("SELECT * FROM categories")
    LiveData<List<Category>> loadAllCategories();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(Category... c);

    @Update
    void updateCategory(Category... c);

    @Delete
    void deleteCategory(Category... c);
}
