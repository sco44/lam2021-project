package it.unibo.cs.lam2021.ui.scanner;

import static android.view.View.GONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.DisplayOrientedMeteringPointFactory;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.unibo.cs.lam2021.barcode.BarcodeData;
import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.api.Products;
import it.unibo.cs.lam2021.api.User;
import it.unibo.cs.lam2021.databinding.ActivityScannerBinding;
import it.unibo.cs.lam2021.ui.NoInternetDialogFragment;
import it.unibo.cs.lam2021.ui.RemoteProductsDialogFragment;
import it.unibo.cs.lam2021.ui.product.ProductActivity;
import retrofit2.HttpException;

public class ScannerActivity extends AppCompatActivity implements NoInternetDialogFragment.OnActionListener, DialogInterface.OnDismissListener {

    private static final String LENS_FACING = "lensFacing"; // bundle tag

    private ScannerViewModel scannerViewModel;

    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;
    private Preview preview;
    private ImageAnalysis imageAnalysis;
    private Executor analysisThread;

    private Integer lensFacing = CameraSelector.LENS_FACING_BACK; //default facing

    private ActivityScannerBinding binding;

    private Camera camera;

    private final ApplicationPreferences applicationPreferences = ApplicationPreferences.getInstance(this);

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onLost(Network network) {
            applicationPreferences.setNetworkOnline(false);
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener networkChangedListener = (sharedPreferences, s) -> {
        if(s.equals(ApplicationPreferences.NETWORK_ONLINE)) {
            if(!sharedPreferences.getBoolean(ApplicationPreferences.NETWORK_ONLINE, false))
                showNoInternetDialog();
        }
    };

    private final Observer<BarcodeData> barcodeDataObserver = new Observer<BarcodeData>() {
        @Override
        public void onChanged(BarcodeData barcodeData) {
            if (barcodeData != null) {
                binding.scanButton.removeCallbacks(scanTimeout);
                processBarCode(barcodeData.getRawCode());
                scannerViewModel.getBarcode().removeObserver(this);
                binding.scanButton.setEnabled(true);
            }
        }
    };

    private final Runnable scanTimeout = () -> {
        scannerViewModel.getBarcode().removeObserver(barcodeDataObserver);
        Toast.makeText(this, R.string.no_barcode_found, Toast.LENGTH_SHORT).show();
        binding.scanButton.setEnabled(true);
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if(isGranted)
                        init();
                    else {
                        Toast.makeText(this, R.string.camera_permission_error, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            init();
        else if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.camera_permission_rationale_title)
                    .setMessage(R.string.camera_permission_rationale_message)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    )
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(false)
                    .create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnShowListener(dialogInterface ->
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
                    dialogInterface.dismiss();
                    finish();
                })
            );
            dialog.show();
        } else
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        binding = ActivityScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        scannerViewModel = new ViewModelProvider(this)
                .get(ScannerViewModel.class);

        if(applicationPreferences.isAutoProcessingEnabled())
            binding.scanButton.setVisibility(GONE);

        analysisThread = Executors.newSingleThreadExecutor();
        ListenableFuture <ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();

                preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                imageAnalysis = new ImageAnalysis.Builder()
                        //.setTargetResolution(new Size(1280, 720))
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(analysisThread, scannerViewModel.getAnalyzer());

                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

                camera.getCameraInfo().getTorchState().observe(this, (state) -> {
                    if (state > 0)
                        binding.flashButton.setImageResource(R.drawable.ic_flash_on);
                    else
                        binding.flashButton.setImageResource(R.drawable.ic_flash_off);
                });

                binding.flashButton.setEnabled(camera.getCameraInfo().hasFlashUnit());

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        scannerViewModel.getBarcode().observe(this, barcode -> {
            if (barcode != null) {
                //prevents the creation of a new dialog on screen rotation
                if(getSupportFragmentManager().findFragmentByTag(RemoteProductsDialogFragment.TAG) != null)
                    return;

                Size wh = new Size(binding.drawSurface.getMeasuredWidth(), binding.drawSurface.getMeasuredHeight());
                binding.drawSurface.highlight(barcode.translateRect(wh, lensFacing == CameraSelector.LENS_FACING_FRONT));

                if (applicationPreferences.isAutoProcessingEnabled()) {
                    if(imageAnalysis != null)
                        imageAnalysis.clearAnalyzer(); //prevents multiple scans
                    processBarCode(barcode.getRawCode());
                }
            } else
                binding.drawSurface.clear();
        });

        binding.scanButton.setOnClickListener(view -> {
            BarcodeData b = scannerViewModel.getBarcode().getValue();

            if (b == null) {
                binding.scanButton.setEnabled(false);
                scannerViewModel.getBarcode().observe(this, barcodeDataObserver);
                binding.scanButton.postDelayed(scanTimeout, 1000);
            } else
                processBarCode(b.getRawCode());
        });

        binding.getRoot().setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MeteringPoint meteringPoint = new DisplayOrientedMeteringPointFactory(binding.previewView.getDisplay(), camera.getCameraInfo(), binding.previewView.getWidth(), binding.previewView.getHeight()).createPoint(motionEvent.getX(), motionEvent.getY());
                    FocusMeteringAction action = new FocusMeteringAction.Builder(meteringPoint).build();
                    ListenableFuture<FocusMeteringResult> focusFuture = camera.getCameraControl().startFocusAndMetering(action);
                    binding.drawSurface.focus(new Point((int) motionEvent.getX(), (int) motionEvent.getY()));
                    focusFuture.addListener(() ->
                        binding.drawSurface.clearFocus()
                    , ContextCompat.getMainExecutor(this));

                    return true;

                case MotionEvent.ACTION_UP:
                    view.performClick();
                    return true;

                default:
                    return false;
            }
        });

        binding.flashButton.setOnClickListener(view -> camera.getCameraControl().enableTorch(camera.getCameraInfo().getTorchState().getValue() == 0));

        binding.flipCamera.setOnClickListener(view -> {
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                binding.flipCamera.setImageResource(R.drawable.ic_camera_front);
                lensFacing = CameraSelector.LENS_FACING_FRONT;
            } else {
                binding.flipCamera.setImageResource(R.drawable.ic_camera_rear);
                lensFacing = CameraSelector.LENS_FACING_BACK;
            }

            cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build();

            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

            binding.flashButton.setEnabled(camera.getCameraInfo().hasFlashUnit());
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LENS_FACING, lensFacing);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        lensFacing = savedInstanceState.getInt(LENS_FACING, lensFacing);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(imageAnalysis != null)
            imageAnalysis.setAnalyzer(analysisThread, scannerViewModel.getAnalyzer());

        if(binding != null) {
            binding.drawSurface.clear();
            binding.drawSurface.setVisibility(View.VISIBLE);
        }

        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        applicationPreferences.getPreferences().registerOnSharedPreferenceChangeListener(networkChangedListener);

        if(!applicationPreferences.isNetworkOnline()) {
            showNoInternetDialog();
            return;
        } else {
            Network network = connectivityManager.getActiveNetwork();
            if(network != null) {
                NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                    applicationPreferences.setNetworkOnline(false);
            } else
                applicationPreferences.setNetworkOnline(false);
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback);

        if (camera != null)
            camera.getCameraControl().enableTorch(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(binding != null)
            binding.drawSurface.setVisibility(GONE);

        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.unregisterNetworkCallback(networkCallback);

        applicationPreferences.getPreferences().unregisterOnSharedPreferenceChangeListener(networkChangedListener);
    }

    public void processBarCode(String code) {
        ListenableFuture<Products> productsFuture = ApiService.getInstance(this).getProductsByBarcode(code);
        productsFuture.addListener(() -> {
            try {
                Products ps = productsFuture.get();

                if (ps.getProducts().size() > 0) {
                    binding.drawSurface.setVisibility(GONE);
                    RemoteProductsDialogFragment.newInstance(code, ps.getToken(), ps.getProducts())
                            .show(getSupportFragmentManager(), RemoteProductsDialogFragment.TAG);
                } else {
                    startActivity(ProductActivity.makeCreateProductIntent(ScannerActivity.this, code, ps.getToken(), true));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if(t instanceof HttpException) {
                    int errorCode = ((HttpException) e.getCause()).code();
                    Toast.makeText(this, getString(R.string.error_http, errorCode), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                    ApplicationPreferences.getInstance(this).setNetworkOnline(false);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public void showNoInternetDialog() {
        NoInternetDialogFragment.newInstance(
                R.string.no_internet_dlg_message_scanner,
                false)
                .show(getSupportFragmentManager(), NoInternetDialogFragment.TAG);
    }

    @Override
    public void onNoInternetRetry(DialogInterface dialog) {
        dialog.dismiss();
        ListenableFuture<User> userDetails = ApiService.getInstance(this).getUserDetails();
        userDetails.addListener(() -> {
            try {
                userDetails.get();
                ApplicationPreferences.getInstance(ScannerActivity.this).setNetworkOnline(true);
                dialog.dismiss();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if(t instanceof HttpException) {
                    ApplicationPreferences.getInstance(ScannerActivity.this).setNetworkOnline(true);

                    int errorCode = ((HttpException) e.getCause()).code();
                    Toast.makeText(this, getString(R.string.error_http, errorCode), Toast.LENGTH_LONG).show();

                    dialog.dismiss();
                } else
                    Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(ScannerActivity.this));
    }

    @Override
    public void onNoInternetCancel(DialogInterface dialog) {
        //not needed
    }

    @Override
    public void onNoInternetOfflineMode(DialogInterface dialog) {
        dialog.dismiss();       //Prevent activity leak
        finish();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (imageAnalysis != null && applicationPreferences.isAutoProcessingEnabled())
            imageAnalysis.setAnalyzer(analysisThread, scannerViewModel.getAnalyzer());

        binding.drawSurface.clear();
        binding.drawSurface.setVisibility(View.VISIBLE);
    }
}