package it.unibo.cs.lam2021.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import it.unibo.cs.lam2021.api.Product;

@Entity(tableName = "localProducts", foreignKeys = {
        @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.SET_NULL
        )
})

public class LocalProduct implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LocalProduct createFromParcel(Parcel in) {
            return new LocalProduct(in);
        }

        public LocalProduct[] newArray(int size) {
            return new LocalProduct[size];
        }
    };

    @PrimaryKey
    @NonNull
    private String id;

    private String name;
    private String description;
    private String barcode;

    private LocalDate expiry;

    @ColumnInfo(defaultValue = "NULL", index = true)
    @Nullable
    private Integer categoryId;

    private boolean important;       //notify when it runs out
    private int quantity;
    private String userId;

    @Ignore
    private String token;

    // ROOM constructor
    public LocalProduct() {

    }

    //Create local product from remote product
    @Ignore
    public LocalProduct(Product p) {
        id = p.getId();
        name = p.getName();
        description = p.getDescription();
        barcode = p.getBarcode();
        userId = p.getUserId();

        token = p.getToken();

        quantity = 1;
        expiry = null;
        important = false;
    }

    // ACTIVITY constructor
    @Ignore
    public LocalProduct(String barcode, String token) {
        this(new Product("", "", barcode, token));
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBarcode() { return barcode; }
    public String getToken() { return token; }
    public Integer getCategoryId() { return categoryId; }
    public LocalDate getExpiry() { return expiry; }
    public boolean isImportant() { return important; }
    public int getQuantity() { return quantity; }
    public String getUserId() { return userId; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setToken(String token) { this.token = token; }
    public void setImportant(boolean i) { important = i; }
    public void setCategoryId(Integer id) { categoryId = id; }
    public void setExpiry(LocalDate expiry) { this.expiry = expiry; }
    public void setQuantity(int q) { quantity = q; }
    public void setUserId(String userId) { this.userId = userId; }

    public String formatExpiryDate(DateFormat fmt) {
        Date d = Date.from(expiry
                .atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
        return fmt.format(d);
    }

    public void parseExpiryDate(String txt, DateFormat fmt) throws ParseException {
        Date d = fmt.parse(txt);
        expiry = d.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    // ---------------
    // PARCELABLE
    public LocalProduct(Parcel p) {
        id = p.readString();
        name = p.readString();
        description = p.readString();
        barcode = p.readString();
        int catId = p.readInt();
        categoryId = (catId == -1 ? null : catId);
        long exp = p.readLong();
        expiry = (exp == 0 ? null : LocalDate.ofEpochDay(exp));
        quantity = p.readInt();
        userId = p.readString();
        important = p.readInt() > 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(barcode);
        parcel.writeInt(categoryId == null ? -1 : categoryId);
        parcel.writeLong(expiry == null ? 0 : expiry.toEpochDay());
        parcel.writeInt(quantity);
        parcel.writeString(userId);
        parcel.writeInt(important ? 1 : 0);
    }
}
