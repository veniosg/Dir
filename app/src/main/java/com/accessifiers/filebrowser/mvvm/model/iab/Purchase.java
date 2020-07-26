package com.accessifiers.filebrowser.mvvm.model.iab;

/**
 * Store-agnostic version of play billing's Purchase class.
 */
@SuppressWarnings("WeakerAccess")
public class Purchase {
    private String sku;
    private String purchaseToken;

    Purchase(String sku, String purchaseToken) {
    }

    public String getSku() {
        return sku;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }
}
