package it.unibo.cs.lam2021.barcode;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Size;

public class BarcodeData {

    //Relative BoundingRect
    final private RectF rect;
    final private String barcode;

    //BarcodeData(code, boundingRect, imageSize);
    public BarcodeData(String brcode, Rect br, Size wh) {

        rect = new RectF(((float) br.left) / wh.getWidth(),
                ((float) br.top) / wh.getHeight(),
                ((float) br.right) / wh.getWidth(),
                ((float) br.bottom) / wh.getHeight());
        //points = p;
        barcode = brcode;

    }

    //translate the relative rect into a rect suitable for the given size
    public Rect translateRect(Size size, boolean flipHorizontal) {
        if(flipHorizontal) {
            RectF flippedRect = new RectF(rect);
            float width = rect.width();
            flippedRect.right = Math.abs(rect.left - 1.0f);
            flippedRect.left = flippedRect.right - width;
            return new Rect((int) (flippedRect.left * size.getWidth()), (int) (flippedRect.top * size.getHeight()), (int) (flippedRect.right * size.getWidth()), (int) (flippedRect.bottom * size.getHeight()));
        }
        else
            return new Rect((int) (rect.left * size.getWidth()), (int) (rect.top * size.getHeight()), (int) (rect.right * size.getWidth()), (int) (rect.bottom * size.getHeight()));
    }

    public String getRawCode() {
        return barcode;
    }
}
