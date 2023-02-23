package it.unibo.cs.lam2021.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.stream.IntStream;

public class BarcodeCacheHelper {

    private final File cacheDir;

    private static BarcodeCacheHelper instance;

    private BarcodeCacheHelper(Context ctx) {
        cacheDir = new File(ctx.getCacheDir(), "barcodes");
        cacheDir.mkdir();
    }

    public static BarcodeCacheHelper getInstance(Context ctx) {
        if(ctx == null && instance == null)
            return null;

        if(instance == null) {
            synchronized (BarcodeCacheHelper.class) {
                if (instance == null) {
                    instance = new BarcodeCacheHelper(ctx.getApplicationContext());
                }
            }
        }

        return instance;
    }

    //could return null bitmap
    @Nullable
    public Bitmap getBarcode(String data, int width, int height) {
        File tmp = new File(cacheDir, data + "-" + width + "x" + height);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(tmp.toString());
            if(bitmap != null)
                return bitmap;
        } catch (Exception ignored) {
        }

        BarcodeFormat fmt;
        if (data.length() == 8) fmt = BarcodeFormat.EAN_8;
        else if (data.length() == 13) fmt = BarcodeFormat.EAN_13;
        else return null;

        HashMap hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.MARGIN, 0);
        MultiFormatWriter writer = new MultiFormatWriter();

        try {
            BitMatrix bm = writer.encode(data, fmt, width, height, hintMap);
            bitmap = Bitmap.createBitmap(IntStream.range(0, height)
                            .flatMap(h -> IntStream.range(0, width).map(w -> bm.get(w, h) ? Color.BLACK : Color.WHITE))
                            .collect(() -> IntBuffer.allocate(width * height), IntBuffer::put, IntBuffer::put)
                            .array(),
                    width, height, Bitmap.Config.ARGB_8888);
            tmp.createNewFile();
            FileOutputStream fileOut = new FileOutputStream(tmp);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
        } catch (Exception ignored) {
            //---
        }

        return bitmap;
    }
}
