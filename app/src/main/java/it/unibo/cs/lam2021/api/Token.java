package it.unibo.cs.lam2021.api;

import com.google.gson.annotations.SerializedName;

public class Token {
    @SerializedName("accessToken")
    private String mAccessToken;

    public String getAccessToken() { return mAccessToken; }
}