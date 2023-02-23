package it.unibo.cs.lam2021.api;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public class User {
    @SerializedName("id")
    private String mId;

    @SerializedName("username")
    private String mUsername;

    @SerializedName("email")
    private String mEmail;

    @SerializedName("password")
    private String mPassword;

    @SerializedName("createdAt")
    private String mCreated;

    @SerializedName("updatedAt")
    private String mUpdated;

    private transient String mAuthorizationToken;

    public User(String email, String password) {
        mEmail = email;
        mPassword = password;
    }

    public User(String email, String username, String password) {
        mUsername = username;
        mEmail = email;
        mPassword = password;
    }

    public String getId() { return mId; }
    public String getUsername() { return mUsername; }
    public String getEmail() { return mEmail; }
    public Instant createdAt() { return Instant.parse(mCreated); }
    public Instant updatedAt() { return Instant.parse(mUpdated); }

    public String getAuthorizationToken() { return mAuthorizationToken; }
    public void setAuthorizationToken(String token) { mAuthorizationToken = token; }
}
