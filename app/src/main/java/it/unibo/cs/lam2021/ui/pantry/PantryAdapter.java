package it.unibo.cs.lam2021.ui.pantry;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import it.unibo.cs.lam2021.barcode.BarcodeCacheHelper;
import it.unibo.cs.lam2021.database.LocalProductWithCategory;
import it.unibo.cs.lam2021.database.entity.LocalProduct;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.ui.product.ProductActivity;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    private List<LocalProductWithCategory> products = new ArrayList<>();

    private SelectionTracker<Long> selectionTracker;

    private String searchQuery = "";

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView name;
        private final TextView description;
        private final TextView code;
        private final TextView expiry;
        private final TextView categoryColor;
        private final ImageView important;
        private final CardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            card = itemView.findViewById(R.id.pantry_card);
            name = itemView.findViewById(R.id.pantry_item_name);
            description = itemView.findViewById(R.id.pantry_item_desc);
            code = itemView.findViewById(R.id.pantry_item_code);
            expiry = itemView.findViewById(R.id.pantry_item_expiry);
            important = itemView.findViewById(R.id.pantry_item_important);
            categoryColor = itemView.findViewById(R.id.pantry_cat_color);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent i = ProductActivity.makeEditProductIntent(view.getContext(), products.get(getAdapterPosition()).product, false);
            view.getContext().startActivity(i);
        }

        public TextView getNameView() {
            return name;
        }

        public TextView getDescriptionView() {
            return description;
        }

        public TextView getCodeView() {
            return code;
        }

        public TextView getExpiryView() {
            return expiry;
        }

        public TextView getCategoryColorView() {
            return categoryColor;
        }

        public ImageView getImportantView() {
            return important;
        }

        public CardView getCardView() {
            return card;
        }

        public ItemDetailsLookup.ItemDetails<Long> getItemDetails() {
            return new ItemDetailsLookup.ItemDetails<Long>() {
                @Override
                public int getPosition() {
                    return getAdapterPosition();
                }

                @Override
                public Long getSelectionKey() {
                    return getItemId();
                }
            };
        }
    }

    private final Context ctx;
    private final int searchHighlightColor;

    public PantryAdapter(Context context) {
        ctx = context;
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[] { R.attr.colorPantrySearchHighlight });
        searchHighlightColor = array.getColor(0, Color.GRAY);
        array.recycle();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalProductWithCategory productWithCategory = products.get(position);
        LocalProduct p = productWithCategory.product;
        if (!searchQuery.isEmpty()) {
            String name = p.getName();
            String desc = p.getDescription();
            if (StringUtils.containsIgnoreCase(name, searchQuery)) {
                Spannable nameSpan = new SpannableString(name);
                nameSpan.setSpan(new BackgroundColorSpan(searchHighlightColor), StringUtils.indexOfIgnoreCase(name, searchQuery), StringUtils.indexOfIgnoreCase(name, searchQuery) + searchQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.getNameView().setText(nameSpan);
            } else {
                holder.getNameView().setText(name);
            }

            if (StringUtils.containsIgnoreCase(desc, searchQuery)) {
                Spannable descSpan = new SpannableString(desc);
                descSpan.setSpan(new BackgroundColorSpan(searchHighlightColor), StringUtils.indexOfIgnoreCase(desc, searchQuery), StringUtils.indexOfIgnoreCase(desc, searchQuery) + searchQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.getDescriptionView().setText(descSpan);
            } else {
                holder.getDescriptionView().setText(desc);
            }
        } else {
            holder.getNameView().setText(p.getName());
            holder.getDescriptionView().setText(p.getDescription());
        }

        BitmapDrawable barcodeDrawable = new BitmapDrawable(ctx.getResources(), BarcodeCacheHelper.getInstance(ctx).getBarcode(p.getBarcode(),
                ctx.getResources().getDimensionPixelSize(R.dimen.barcode_small_width),
                ctx.getResources().getDimensionPixelSize(R.dimen.barcode_small_height)));

        holder.getCodeView().setText(p.getBarcode());
        holder.getCodeView().setCompoundDrawablesWithIntrinsicBounds(null, barcodeDrawable, null, null);

        if (p.getExpiry() != null)
            holder.getExpiryView().setText(ctx.getString(R.string.pantry_fragment_exp_format, p.formatExpiryDate(DateFormat.getDateFormat(ctx))));

        if(productWithCategory.category == null)
            holder.getCategoryColorView().setBackgroundColor(Color.TRANSPARENT);
        else
            holder.getCategoryColorView().setBackgroundColor(productWithCategory.category.getColor());

        if(p.isImportant())
            holder.getImportantView().setVisibility(View.VISIBLE);
        else
            holder.getImportantView().setVisibility(View.GONE);

        setSelection(holder, position);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        setSelection(holder, (int) holder.getItemId());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void setSelection(ViewHolder holder, int position) {
        Context ctx = holder.getCodeView().getContext();

        if(selectionTracker.isSelected((long) position)) {
            holder.getCardView().setActivated(true);
            holder.getCodeView().setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_check_wide, 0, 0);
        } else {
            LocalProduct p = products.get(position).product;
            holder.getCardView().setActivated(false);
            BitmapDrawable barcodeDrawable = new BitmapDrawable(ctx.getResources(), BarcodeCacheHelper.getInstance(ctx).getBarcode(p.getBarcode(),
                    ctx.getResources().getDimensionPixelSize(R.dimen.barcode_small_width),
                    ctx.getResources().getDimensionPixelSize(R.dimen.barcode_small_height)));
            holder.getCodeView().setCompoundDrawablesWithIntrinsicBounds(null, barcodeDrawable, null, null);
        }
    }

    public void setSelectionTracker(SelectionTracker<Long> trk) {
        selectionTracker = trk;
    }

    public void setProducts(List<LocalProductWithCategory> prods) {
        products = prods;
        notifyDataSetChanged();
    }

    public void setSearchQuery(String q) {
        searchQuery = q;
        notifyDataSetChanged();
    }

    public void clearSearchQuery() {
        searchQuery = "";
    }
}
