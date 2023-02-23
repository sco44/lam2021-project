package it.unibo.cs.lam2021.api;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    @POST("/users")
    Call<User> register(@Body User user);

    @POST("/auth/login")
    Call<Token> login(@Body User user);

    @GET("/users/me")
    Call<User> getUserDetails(@Header("Authorization") String authorization);

    @GET("/products")
    Call<Products> getProductsByBarcode(@Header("Authorization") String authorization, @Query("barcode") String barcode);

    @POST("/products")
    Call<Product> postProductDetails(@Header("Authorization") String authorization, @Body Product product);

    @DELETE("/products/{id}")
    Call<Product> deleteProduct(@Header("Authorization") String authorization, @Path("id") String id);

    @POST("/votes")
    Call<ProductPreference> postProductPreference(@Header("Authorization") String authorization, @Body ProductPreference preference);
}