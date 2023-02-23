package it.unibo.cs.lam2021.ui.categories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.R;

public class CategoriesListAdapter extends BaseAdapter {

    private final Context ctx;

    private List<Category> categories = new ArrayList<>();
    private HashSet<Integer> selection = new HashSet<>();

    public CategoriesListAdapter(Context ctx) {
        this.ctx = ctx;
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
        return categories.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null) {
            view = LayoutInflater.from(ctx).inflate(R.layout.item_category_list, viewGroup, false);
        }

        TextView color = view.findViewById(R.id.cat_list_color);
        TextView name = view.findViewById(R.id.cat_list_name);
        TextView desc = view.findViewById(R.id.cat_list_desc);

        color.setBackgroundColor(categories.get(i).getColor());
        name.setText(categories.get(i).getName());
        desc.setText(categories.get(i).getDescription());

        view.setActivated(selection.contains(i));

        return view;
    }

    public void setCategories(List<Category> cats) {
        this.categories = cats;
        notifyDataSetChanged();
    }

    public void setSelection(HashSet<Integer> sel) {
        this.selection = sel;
        notifyDataSetChanged();
    }
}
