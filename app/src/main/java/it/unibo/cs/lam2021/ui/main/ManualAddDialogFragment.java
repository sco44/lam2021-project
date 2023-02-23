package it.unibo.cs.lam2021.ui.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.api.Products;
import it.unibo.cs.lam2021.barcode.BarcodeAnalyzer;
import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.ui.RemoteProductsDialogFragment;
import it.unibo.cs.lam2021.ui.product.ProductActivity;
import retrofit2.HttpException;

public class ManualAddDialogFragment extends DialogFragment implements View.OnClickListener  {

    public static String TAG = "ManualAddDialogFragment";

    private static final String CODE_TEXT = "codeText";

    private TextInputLayout code;
    private Button addButton;

    private String codeText;

    private final TextWatcher barcodeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            codeText = s.toString();

            if(s.length() == 8) {
                if(BarcodeAnalyzer.validateCode(s.toString()))
                    addButton.setEnabled(true);
            } else if(s.length() == 13) {
                if(!BarcodeAnalyzer.validateCode(s.toString())) {
                    code.setErrorEnabled(true);
                    code.setError(getContext().getString(R.string.insert_invalid_barcode));
                    addButton.setEnabled(false);
                } else {
                    code.setErrorEnabled(false);
                    code.setError(null);
                    addButton.setEnabled(true);
                }
            } else {
                code.setErrorEnabled(false);
                code.setError(null);
                addButton.setEnabled(false);
            }
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dlg = new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.dlg_manual_title)
                .setView(R.layout.dialog_manual_add)
                .setPositiveButton(R.string.dlg_manual_add, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dlg.setOnShowListener(dialog -> {
            code = dlg.findViewById(R.id.code);
            addButton = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
            code.getEditText().addTextChangedListener(barcodeWatcher);
            addButton.setEnabled(false);
            addButton.setOnClickListener(this);
            if(savedInstanceState != null) {
                codeText = savedInstanceState.getString(CODE_TEXT);
                code.getEditText().setText(codeText);
            }
        });

        return dlg;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CODE_TEXT, codeText);
    }

    @Override
    public void onClick(View v) {
        ListenableFuture<Products> productsFuture = ApiService.getInstance(getContext()).getProductsByBarcode(codeText);
        productsFuture.addListener(() -> {
            try {
                Products ps = productsFuture.get();
                int size = ps.getProducts().size();

                getDialog().dismiss();
                if (size > 0)
                    RemoteProductsDialogFragment.newInstance(codeText, ps.getToken(), ps.getProducts()).show(getParentFragmentManager(), RemoteProductsDialogFragment.TAG);
                else
                    getContext().startActivity(ProductActivity.makeCreateProductIntent(getContext(), codeText, ps.getToken(), true));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();

                if (t instanceof HttpException) {
                    int errorCode = ((HttpException) e.getCause()).code();
                    Toast.makeText(getContext(), getContext().getString(R.string.error_http, errorCode), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), getContext().getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                    ApplicationPreferences.getInstance(getContext()).setNetworkOnline(false);
                }
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }
}
