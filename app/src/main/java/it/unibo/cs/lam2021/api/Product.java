package it.unibo.cs.lam2021.api;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public class Product implements Parcelable {

    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @SerializedName("id")
    protected String mId;

    @SerializedName("name")
    protected String mName;

    @SerializedName("description")
    protected String mDescription;

    @SerializedName("barcode")
    protected String mBarcode;

    @SerializedName("userId")
    protected String mUserId;

    @SerializedName("test")
    protected boolean mTest;

    @SerializedName("createdAt")
    protected String mCreated;

    @SerializedName("updatedAt")
    protected String mUpdated;

    @SerializedName("token")
    protected String mToken;

    public Product(String name, String description, String barcode) {
        mName = name;
        mDescription = description;
        mBarcode = barcode;
        mTest = false;
    }

    public Product(String name, String description, String barcode, String token) {
        mName = name;
        mDescription = description;
        mBarcode = barcode;
        mToken = token;
        mTest = false;
    }

    public Product(Product p) {
        this.mId = p.mId;
        this.mName = p.mName;
        this.mDescription = p.mDescription;
        this.mBarcode = p.mBarcode;
        this.mToken = p.mToken;
        this.mTest = p.mTest;
        this.mCreated = p.mCreated;
        this.mUpdated = p.mUpdated;
        this.mUserId = p.mUserId;
    }

    public String getId() { return mId; }
    public String getName() { return mName; }
    public String getDescription() { return mDescription; }
    public String getBarcode() { return mBarcode; }
    public String getUserId() { return mUserId; }
    public String getToken() { return mToken; }

    public void setName(String name) { mName = name; }
    public void setDescription(String desc) { mDescription = desc; }
    public void setBarcode(String barcode) { mBarcode = barcode; }
    public void setToken(String token) { mToken = token; }

    public Instant createdAt() { return Instant.parse(mCreated); }
    public Instant updatedAt() { return Instant.parse(mUpdated); }

    Product(Parcel parcel) {
        mId = parcel.readString();
        mName = parcel.readString();
        mDescription = parcel.readString();
        mBarcode = parcel.readString();
        mUserId = parcel.readString();
        mTest = parcel.readInt() > 0;
        mCreated = parcel.readString();
        mUpdated = parcel.readString();
        mToken = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeString(mDescription);
        parcel.writeString(mBarcode);
        parcel.writeString(mUserId);
        parcel.writeInt(mTest ? 1 : 0);
        parcel.writeString(mCreated);
        parcel.writeString(mUpdated);
        parcel.writeString(mToken);
    }
}
