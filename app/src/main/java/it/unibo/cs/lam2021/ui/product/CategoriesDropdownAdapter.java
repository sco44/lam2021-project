package it.unibo.cs.lam2021.ui.product;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.database.entity.Category;

public class CategoriesDropdownAdapter extends BaseAdapter implements Filterable {

    private final Context ctx;
    private final List<Category> categories = new ArrayList<>();
    private final int defaultTxtColor;

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Category) resultValue).getName();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    public CategoriesDropdownAdapter(@NonNull Context context, List<Category> list) {
        ctx = context;
        categories.add(new Category(null, ctx.getString(R.string.category_uncategorized_name), ctx.getString(R.string.category_uncategorized_desc), null));
        categories.addAll(list);

        TypedArray typedArray = ctx.getTheme().obtainStyledAttributes(new int[] { android.R.attr.textColorPrimary });
        defaultTxtColor = typedArray.getColor(0, Color.RED);
        typedArray.recycle();
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Category getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        Integer categoryId = categories.get(i).getId();
        return categoryId == null ? -1 : categoryId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Category c = getItem(position);

        if(convertView == null)
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_category_dropdown, null);

        TextView txtView = ((TextView) convertView);
        txtView.setText(c.getName());

        if(c.getColor() != null)
            txtView.setTextColor(c.getColor());
        else
            txtView.setTextColor(defaultTxtColor);

        return convertView;
    }

    public int getDefaultColor() {
        return defaultTxtColor;
    }

    public Category getCategoryById(Integer id) {
        for(Category c : categories) {
            if (c.getId() == null) {
                if (id == null)
                    return c;
            } else if (c.getId().intValue() == id)
                return c;
        }

        return null;
    }
}
