package it.unibo.cs.lam2021.ui.scanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import it.unibo.cs.lam2021.barcode.BarcodeAnalyzer;
import it.unibo.cs.lam2021.barcode.BarcodeData;

public class ScannerViewModel extends ViewModel {

    private final BarcodeAnalyzer analyzer;
    private final LiveData<BarcodeData> barcode;

    public ScannerViewModel() {
        analyzer = new BarcodeAnalyzer();
        barcode = analyzer.getBarcodeLiveData();
    }

    public BarcodeAnalyzer getAnalyzer() {
        return analyzer;
    }

    public LiveData<BarcodeData> getBarcode() {
        return barcode;
    }
}
