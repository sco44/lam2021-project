package it.unibo.cs.lam2021.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.api.Product;
import it.unibo.cs.lam2021.api.ProductPreference;
import it.unibo.cs.lam2021.database.LocalProductWithCategory;
import it.unibo.cs.lam2021.database.ProductRepository;
import it.unibo.cs.lam2021.database.entity.LocalProduct;
import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.ui.product.ProductActivity;


public class RemoteProductsDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "RemoteProductsDialogFragment";

    public class ProductsListAdapter extends BaseAdapter {

        private final List<Product> mProductList;

        public ProductsListAdapter(List<Product> pl) {
            super();
            mProductList = pl;
        }

        @Override
        public int getCount() {
            return mProductList.size();
        }

        @Override
        public Product getItem(int i) {
            return mProductList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_remote_products, null);
            }

            Product p = mProductList.get(i);

            TextView name = view.findViewById(R.id.remote_prod_name);
            name.setText(p.getName());
            TextView desc = view.findViewById(R.id.remote_prod_desc);
            desc.setText(p.getDescription());

            return view;
        }
    }

    private static final String ARG_BARCODE = "barcode";
    private static final String ARG_TOKEN = "token";
    private static final String ARG_PRODUCTS = "products";

    private String barcode;
    private String token;

    private ProductsListAdapter adapter;

    public static RemoteProductsDialogFragment newInstance(String barcode, String token, List<Product> products) {
        Bundle args = new Bundle();
        args.putString(ARG_BARCODE, barcode);
        args.putString(ARG_TOKEN, token);
        args.putParcelableArrayList(ARG_PRODUCTS, new ArrayList<>(products));
        RemoteProductsDialogFragment fragment = new RemoteProductsDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        this.barcode = args.getString(ARG_BARCODE);
        this.token = args.getString(ARG_TOKEN);
        this.adapter = new ProductsListAdapter(args.getParcelableArrayList(ARG_PRODUCTS));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dlg_remote_products_title)
                .setAdapter(adapter, this)
                .setPositiveButton(R.string.dlg_remote_products_add, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(i == DialogInterface.BUTTON_POSITIVE) {
            startActivity(ProductActivity.makeCreateProductIntent(getContext(), barcode, token, true));
        } else if (i >= 0) {
            Product p = adapter.getItem(i);
            ListenableFuture<ProductPreference> preferenceFuture = ApiService.getInstance(getContext()).postProductPreference(p.getId(), 1, token);
            preferenceFuture.addListener(() -> {
                try {
                    preferenceFuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    if(e.getCause() instanceof IOException)
                        ApplicationPreferences.getInstance(getContext()).setNetworkOnline(false);
                }
            }, ContextCompat.getMainExecutor(getContext()));
            ProductRepository repository = new ProductRepository(getActivity().getApplication());
            ListenableFuture<LocalProductWithCategory> productFuture = repository.getProductWithId(p.getId());
            productFuture.addListener(() -> {
                try {
                    LocalProductWithCategory product = productFuture.get();

                    if (product == null) {  //Product doesn't exists
                        if (ApplicationPreferences.getInstance(getContext()).isFastAddEnabled()) {
                            repository.insertProduct(new LocalProduct(p));
                            Toast.makeText(getContext(), getString(R.string.product_added, p.getName()), Toast.LENGTH_SHORT).show();
                        } else
                            startActivity(ProductActivity.makeEditProductIntent(getContext(), new LocalProduct(p), true));
                    } else {
                        Toast.makeText(getContext(), getString(R.string.product_already_in_pantry, product.product.getName()), Toast.LENGTH_SHORT).show();
                        startActivity(ProductActivity.makeEditProductIntent(getContext(), product.product, false));
                    }

                    dismiss();
                } catch (InterruptedException | ExecutionException ignored) {
                    //will never happen
                }
            }, ContextCompat.getMainExecutor(getContext()));
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if(getActivity() instanceof DialogInterface.OnDismissListener)
            ((DialogInterface.OnDismissListener) getActivity()).onDismiss(dialog);
    }
}
