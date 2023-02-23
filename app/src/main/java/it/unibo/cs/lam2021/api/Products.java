package it.unibo.cs.lam2021.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Products {
    @SerializedName("products")
    private List<Product> mProducts;

    @SerializedName("token")
    private String mToken;

    public List<Product> getProducts() { return mProducts; }
    public String getToken() { return mToken; }
}