package it.unibo.cs.lam2021.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category implements Parcelable {

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private Integer id;
    private String name;
    private String description;
    private Integer color;

    public Category() { }

    @Ignore
    public Category(Integer id) {
        this.id = id;
    }

    @Ignore
    public Category(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Ignore
    public Category(Integer id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.description = desc;
    }

    public Category(Integer id, String name, String desc, Integer c) {
        this.id = id;
        this.name = name;
        this.description = desc;
        this.color = c;
    }

    @Ignore
    public Category(Category c) {
        this(c.id, c.name, c.description, c.color);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getColor() { return color; }

    public void setId(Integer id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setColor(Integer color) { this.color = color; }

    // ---------------
    // PARCELABLE
    public Category(Parcel p) {
        int catId = p.readInt();
        id = catId != -1 ? catId : null;
        name = p.readString();
        description = p.readString();
        color = p.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id != null ? id : -1);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeInt(color);
    }
}
