package it.unibo.cs.lam2021.ui.product;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import it.unibo.cs.lam2021.barcode.BarcodeCacheHelper;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.database.entity.LocalProduct;
import it.unibo.cs.lam2021.databinding.ActivityProductBinding;

public class ProductActivity extends AppCompatActivity implements DeleteProductDialogFragment.OnProductDeleteListener {

    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            productViewModel.getProduct().setImportant(b);
            productViewModel.setItemsChanged();
        }
    };

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            LocalProduct product = productViewModel.getProduct();

            if(productViewModel.getMode() == ProductViewModel.MODE_CREATE) {
                product.setName(binding.prodName.getEditText().getText().toString());
                product.setDescription(binding.prodDesc.getEditText().getText().toString());
            }

            if (binding.prodExpiry.getEditText().getText().length() != 0) {
                try {
                    product.parseExpiryDate(binding.prodExpiry.getEditText().getText().toString(), DateFormat.getDateFormat(getApplicationContext()));
                } catch (ParseException ignored) {
                    //this should never happen
                }
            }

            if(binding.prodQuantity.getEditText().getText().length() > 0)
                product.setQuantity(Integer.parseInt(binding.prodQuantity.getEditText().getText().toString()));
            else
                product.setQuantity(1);

            productViewModel.setItemsChanged();
        }
    };

    public static final String ARG_MODE = "mode";
    public static final String ARG_IS_NEW = "isNew";     // true if product isn't already in pantry
    public static final String ARG_PRODUCT = "product";     //MODE_EDIT
    public static final String ARG_TOKEN = "token";         //MODE_CREATE
    public static final String ARG_BARCODE = "barcode";     //MODE_CREATE

    private ProductViewModel productViewModel;

    private CategoriesDropdownAdapter adapter;

    private ActivityProductBinding binding;

    public static Intent makeCreateProductIntent(Context ctx, String barcode, String token, boolean isNew) {
        Intent in = new Intent(ctx, ProductActivity.class);
        in.putExtra(ARG_MODE, ProductViewModel.MODE_CREATE);     // create new product
        in.putExtra(ARG_IS_NEW, isNew);
        in.putExtra(ARG_BARCODE, barcode);
        in.putExtra(ARG_TOKEN, token);                          // token to update online db

        return in;
    }

    public static Intent makeEditProductIntent(Context ctx, LocalProduct p, boolean isNew) {
        Intent in = new Intent(ctx, ProductActivity.class);
        in.putExtra(ProductActivity.ARG_MODE, ProductViewModel.MODE_EDIT);
        in.putExtra(ARG_IS_NEW, isNew);
        in.putExtra(ProductActivity.ARG_PRODUCT, p);

        return in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        productViewModel =
                new ViewModelProvider(this).get(ProductViewModel.class);

        if(savedInstanceState == null) {      //Activity is being created for the first time. Initialize view model
            Intent i = getIntent();
            productViewModel.setNew(i.getBooleanExtra(ARG_IS_NEW, false));
            productViewModel.setMode(i.getIntExtra(ARG_MODE, ProductViewModel.MODE_EDIT));

            // MODE_EDIT:
            // Editing an existing record inside local database
            if (productViewModel.getMode() == ProductViewModel.MODE_EDIT)
                productViewModel.setProduct(i.getParcelableExtra(ARG_PRODUCT));
            else    // MODE_CREATE: creating a new element both on online and local database
                productViewModel.setProduct(new LocalProduct(i.getStringExtra(ARG_BARCODE), i.getStringExtra(ARG_TOKEN)));        // TOKEN needed by the server
        }

        binding = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LocalProduct product = productViewModel.getProduct();

        binding.prodExpiry.getEditText().setOnClickListener(view -> {
            LocalDate date;
            String dateField = binding.prodExpiry.getEditText().getText().toString();
            if(dateField.isEmpty())
                date = LocalDate.now();
            else {
                try {
                    date = DateFormat.getDateFormat(getApplicationContext())
                            .parse(dateField)
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                } catch(ParseException e) {
                    date = LocalDate.now();
                }
            }

            int y = date.getYear();
            int m = date.getMonthValue()-1;
            int d = date.getDayOfMonth();
            new DatePickerDialog(this, (datePicker, year, month, day) -> {
                Date dt = Date.from(LocalDate.of(year, month+1, day)
                        .atStartOfDay()
                        .atZone(ZoneId.systemDefault())
                        .toInstant());
                binding.prodExpiry.getEditText().setText(DateFormat.getDateFormat(getApplicationContext()).format(dt));
            }, y, m, d).show();
        });

        String data = product.getBarcode();
        binding.prodBarcode.setImageBitmap(BarcodeCacheHelper.getInstance(this).getBarcode(data, getResources().getDimensionPixelSize(R.dimen.barcode_large_width), getResources().getDimensionPixelSize(R.dimen.barcode_large_height)));
        binding.prodBarcodeTxt.setText(data);

        binding.prodName.getEditText().setText(product.getName());
        binding.prodDesc.getEditText().setText(product.getDescription());
        if(product.getExpiry() != null)
            binding.prodExpiry.getEditText().setText(product.formatExpiryDate(DateFormat.getDateFormat(getApplicationContext())));

        binding.prodImportant.setChecked(product.isImportant());
        binding.prodQuantity.getEditText().setText(String.valueOf(product.getQuantity()));

        if(productViewModel.getMode() == ProductViewModel.MODE_EDIT) {
            binding.prodName.setEnabled(false);
            binding.prodDesc.setEnabled(false);
        } else {
            binding.prodName.post(() -> binding.prodName.getEditText().addTextChangedListener(textWatcher));
            binding.prodDesc.post(() -> binding.prodDesc.getEditText().addTextChangedListener(textWatcher));
        }

        productViewModel.getCategoriesList().observe(this, categories -> {

            adapter = new CategoriesDropdownAdapter(ProductActivity.this, categories);
            binding.prodCategoryText.setAdapter(adapter);

            Category c = adapter.getCategoryById(product.getCategoryId());
            binding.prodCategoryText.setText(c.getName());
            binding.prodCategoryText.setTextColor(c.getColor() != null ? c.getColor() : adapter.getDefaultColor());

            binding.prodCategoryText.setOnItemClickListener((parent, view, position, id) -> {
                product.setCategoryId(id != -1 ? (int) id : null);
                Integer color = adapter.getItem(position).getColor();
                binding.prodCategoryText.setTextColor(color != null ? color : adapter.getDefaultColor());
            });
        });

        binding.prodExpiry.post(() -> binding.prodExpiry.getEditText().addTextChangedListener(textWatcher));
        binding.prodQuantity.post(() -> binding.prodQuantity.getEditText().addTextChangedListener(textWatcher));
        binding.prodImportant.post(() -> binding.prodImportant.setOnCheckedChangeListener(checkedChangeListener));

        productViewModel.getSaveResultLiveData().observe(this, saveResult -> {
            if(saveResult.message != null)
                Toast.makeText(this, saveResult.message, Toast.LENGTH_SHORT).show();

            if (saveResult.finish)
                finish();
        });

        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.product, menu);
        if(productViewModel.isNew())
            menu.findItem(R.id.product_delete_local).setVisible(false);

        //Since we don't know at which time the function is getting called
        if(productViewModel.getProduct() != null) {
            if(productViewModel.getUserId().equals(productViewModel.getProduct().getUserId()))
                menu.findItem(R.id.product_delete_remote).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.product_save:
                saveProduct();
                return true;

            case R.id.product_delete_local:
                DeleteProductDialogFragment.newInstance(false)
                        .show(getSupportFragmentManager(), DeleteProductDialogFragment.TAG);

                return true;

            case R.id.product_delete_remote:
                DeleteProductDialogFragment.newInstance(true)
                        .show(getSupportFragmentManager(), DeleteProductDialogFragment.TAG);

                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(productViewModel.itemsChanged()) {
            AlertDialog dlg = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.dlg_product_back_confirm_title)
                    .setMessage(R.string.dlg_product_back_confirm_message)
                    .setPositiveButton(R.string.save, (dialogInterface, i) -> saveProduct() )
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.discard, null)
                    .create();

            dlg.setOnShowListener(dialogInterface ->
                dlg.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    dlg.dismiss();
                    finish();
                })
            );

            dlg.show();
        } else
            super.onBackPressed();
    }

    private void saveProduct() {
        productViewModel.saveProduct();
    }

    @Override
    public void onProductDelete(boolean isRemote) {
        productViewModel.deleteProduct(isRemote);
    }
}