package it.unibo.cs.lam2021.ui.product;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.unibo.cs.lam2021.R;

public class DeleteProductDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = "DeleteProductDialogFragment";

    private static final String ARG_REMOTE = "isRemote";

    public interface OnProductDeleteListener {
        void onProductDelete(boolean isRemote);
    }

    private boolean isRemote;

    public static DeleteProductDialogFragment newInstance(boolean isRemote) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_REMOTE, isRemote);
        DeleteProductDialogFragment fragment = new DeleteProductDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        isRemote = args.getBoolean(ARG_REMOTE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(getContext())
                .setTitle(isRemote ? R.string.dlg_delete_remote_title : R.string.dlg_delete_local_title)
                .setMessage(isRemote ? R.string.dlg_delete_remote_message : R.string.dlg_delete_local_message)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if(getActivity() instanceof OnProductDeleteListener && i == DialogInterface.BUTTON_POSITIVE)
            ((OnProductDeleteListener) getActivity()).onProductDelete(isRemote);
    }

}
