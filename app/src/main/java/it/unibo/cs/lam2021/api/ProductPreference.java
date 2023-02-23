package it.unibo.cs.lam2021.api;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public class ProductPreference {
    @SerializedName("id")
    private String mId;

    @SerializedName("rating")
    private int mRating;

    @SerializedName("productId")
    private String mProductId;

    @SerializedName("userId")
    private String mUserId;

    @SerializedName("createdAt")
    private String mCreated;

    @SerializedName("updatedAt")
    private String mUpdated;

    @SerializedName("token")
    private String mToken;

    public ProductPreference(String productId, int rating, String token) {
        mProductId = productId;
        mRating = rating;
        mToken = token;
    }

    public String getId() { return mId; }
    public int getRating() { return mRating; }
    public String getProductId() { return mProductId; }
    public String getUserId() { return mUserId; }
    public String getToken() { return mToken; }

    public Instant createdAt() { return Instant.parse(mCreated); }
    public Instant updatedAt() { return Instant.parse(mUpdated); }
}