package it.unibo.cs.lam2021.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.database.entity.LocalProduct;

public class LocalProductWithCategory {
    @Embedded
    public LocalProduct product;

    @Relation(parentColumn = "categoryId", entityColumn = "id")
    public Category category;
}
