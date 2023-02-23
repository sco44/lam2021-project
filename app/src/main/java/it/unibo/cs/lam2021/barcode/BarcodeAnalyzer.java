package it.unibo.cs.lam2021.barcode;

import android.media.Image;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

    private final BarcodeScanner scanner;
    private final MutableLiveData<BarcodeData> barcodeData = new MutableLiveData<>();

    public BarcodeAnalyzer() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13 | Barcode.FORMAT_EAN_8)
                .build();

        scanner = BarcodeScanning.getClient(options);
    }

    public LiveData<BarcodeData> getBarcodeLiveData() {
        return barcodeData;
    }

    @Override
    @ExperimentalGetImage
    public void analyze(@NonNull ImageProxy image) {
        Image mediaImage = image.getImage();
        if(mediaImage != null) {
            int rotation = image.getImageInfo().getRotationDegrees();
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, rotation);
            Size imageSize;
            if(rotation == 0 || rotation == 180)
                imageSize = new Size(image.getWidth(), image.getHeight());
            else
                imageSize = new Size(image.getHeight(), image.getWidth());

            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if(!barcodes.isEmpty()) {
                            Barcode b = barcodes.get(0);
                            if(validateCode(b.getRawValue()))       //sometimes mlKit scans invalid codes
                                barcodeData.setValue(new BarcodeData(b.getRawValue(), b.getBoundingBox(), imageSize));
                            else
                                barcodeData.setValue(null);
                        } else
                            barcodeData.setValue(null);

                        image.close();
                    })
                    .addOnFailureListener(e -> image.close());
        }
    }

    public static boolean validateCode(String code) {
        char[] array = code.toCharArray();

        if(array.length != 8 && array.length != 13)
            return false;

        int mod = (array.length == 13 ? 1 : 0);
        int odd = 0, even = 0;
        for(int i = array.length-2; i >= 0; i--) {
            if(i % 2 == mod)
                odd += (array[i] - '0');
            else
                even += (array[i] - '0');
        }

        return (10 - (odd * 3 + even) % 10) % 10 == array[array.length-1] - '0';
    }
}
