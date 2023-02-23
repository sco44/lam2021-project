package it.unibo.cs.lam2021.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unibo.cs.lam2021.database.converter.Converters;
import it.unibo.cs.lam2021.database.dao.CategoriesDao;
import it.unibo.cs.lam2021.database.dao.LocalProductDao;
import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.database.entity.LocalProduct;

// bump version number if your schema changes
@Database(entities={LocalProduct.class, Category.class}, version=3)
@TypeConverters({Converters.class})

public abstract class LocalDatabase extends RoomDatabase {
    // Declare your data access objects as abstract
    public abstract LocalProductDao LocalProductDao();
    public abstract CategoriesDao CategoriesDao();

    // Database name to be used
   // public static final String NAME = "localdb";

    private static volatile LocalDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static LocalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LocalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LocalDatabase.class, "local_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
