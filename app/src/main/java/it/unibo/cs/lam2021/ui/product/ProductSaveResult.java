package it.unibo.cs.lam2021.ui.product;

public class ProductSaveResult {

    public ProductSaveResult(String message, boolean finish) {
        this.message = message;
        this.finish = finish;
    }

    public boolean finish;
    public String message;
}
