package it.unibo.cs.lam2021.ui.product;

import android.app.Application;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;

import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.preferences.EncryptedPreferences;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.api.Product;
import it.unibo.cs.lam2021.database.ProductRepository;
import it.unibo.cs.lam2021.database.entity.Category;
import it.unibo.cs.lam2021.database.entity.LocalProduct;
import retrofit2.HttpException;

public class ProductViewModel extends AndroidViewModel {

    public static final int MODE_CREATE = 0;
    public static final int MODE_EDIT = 1;

    private int mode;
    private boolean isNew;

    private String userId;

    private LocalProduct product;
    private boolean itemsChanged = false;

    private final ProductRepository repository;

    private final LiveData<List<Category>> categoriesListLiveData;
    private final MutableLiveData<ProductSaveResult> saveResultLiveData = new MutableLiveData<>();

    public ProductViewModel(Application app) {
        super(app);

        userId = EncryptedPreferences.getInstance(app).getUserId();
        repository = new ProductRepository(app);
        categoriesListLiveData = Transformations.distinctUntilChanged(repository.getAllCategories());
    }

    public void setProduct(LocalProduct p) {
        product = p;
    }

    public LocalProduct getProduct() {
        return product;
    }

    public void setItemsChanged() {
        itemsChanged = true;
    }

    public boolean itemsChanged() {
        return itemsChanged;
    }

    public void setNew(boolean n) {
        isNew = n;
    }

    public boolean isNew() {
        return isNew;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int m) {
        mode = m;
    }

    public String getUserId() {
        return userId;
    }

    public LiveData<List<Category>> getCategoriesList() {
        return categoriesListLiveData;
    }

    public LiveData<ProductSaveResult> getSaveResultLiveData() {
        return saveResultLiveData;
    }

    public void saveProduct() {
        ListenableFuture<Product> productFuture;
        if (mode == MODE_CREATE) {
            if(product.getName().isEmpty()) {
                saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.error_product_name_empty), false));
                return;
            }

            productFuture = ApiService.getInstance(getApplication()).postProductDetails(product.getName(),
                    product.getDescription(),
                    product.getBarcode(),
                    product.getToken());
        } else
            productFuture = Futures.immediateFuture(null);

        productFuture.addListener(() -> {
            try {
                Product remoteProduct = productFuture.get();
                if(remoteProduct != null) {
                    product.setId(remoteProduct.getId());
                    product.setUserId(remoteProduct.getUserId());
                    repository.insertProduct(product);
                } else {
                    if (isNew)
                        repository.insertProduct(product);
                    else
                        repository.updateProduct(product);
                }

                if (isNew)
                    saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.product_added, product.getName()), true));
                else
                    saveResultLiveData.setValue(new ProductSaveResult(null, true));

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if (t instanceof HttpException) {
                    int errorCode = ((HttpException) t).code();
                    saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.error_http, errorCode), false));
                } else {
                    ApplicationPreferences.getInstance(getApplication()).setNetworkOnline(false);
                    saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.error_no_internet), false));
                }
            }
        }, ContextCompat.getMainExecutor(getApplication()));
    }

    public void deleteProduct(boolean isRemote) {
        if(isRemote) {
            ListenableFuture<Void> deleteFuture = ApiService.getInstance(getApplication()).deleteProduct(product.getId());
            deleteFuture.addListener(() -> {
                try {
                    deleteFuture.get();
                    repository.deleteProducts(product);
                    saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.product_deleted_remote, product.getName()), true));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    Throwable t = e.getCause();
                    if(t instanceof HttpException) {
                        int errorCode = ((HttpException) t).code();
                        saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.error_http, errorCode), false));
                    } else {
                        ApplicationPreferences.getInstance(getApplication()).setNetworkOnline(false);
                        saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.error_no_internet), false));
                    }
                }
            }, ContextCompat.getMainExecutor(getApplication()));
        } else {
            repository.deleteProducts(product);
            saveResultLiveData.setValue(new ProductSaveResult(getApplication().getString(R.string.product_deleted_local, product.getName()), true));
        }
    }
}
