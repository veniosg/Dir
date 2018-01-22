/*
 * Copyright (C) 2017 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.veniosg.dir.mvvm.model.iab;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.veniosg.dir.R;

import java.lang.annotation.Retention;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.android.billingclient.api.BillingClient.BillingResponse.BILLING_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponse.ITEM_ALREADY_OWNED;
import static com.android.billingclient.api.BillingClient.BillingResponse.ITEM_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponse.OK;
import static com.android.billingclient.api.BillingClient.BillingResponse.SERVICE_UNAVAILABLE;
import static com.android.billingclient.api.BillingClient.BillingResponse.USER_CANCELED;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;
import static com.veniosg.dir.android.util.Logger.TAG_BILLING;
import static com.veniosg.dir.android.util.Logger.logV;
import static java.lang.String.format;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import static java.util.Locale.ENGLISH;

public class PlayBillingManager implements BillingManager {
    private static final String SKU_DONATION = "donation";
    private OnBillingUnavailableListener onBillingUnavailableListener;

    @Retention(SOURCE)
    @IntDef({CONNECTED, CONNECTING, DISCONNECTED})
    private @interface ConnectionStatus {
    }

    private static final int CONNECTED = 0;
    private static final int CONNECTING = 1;
    private static final int DISCONNECTED = 2;

    @Nullable
    private String requestedSku = null;
    @ConnectionStatus
    private int connectionStatus = DISCONNECTED;
    private BillingClient billingClient;
    private List<Runnable> onConnectedRunnables = new LinkedList<>();

    @SuppressWarnings("Convert2Lambda")
    @Override
    public void init(Context context,
                     OnPurchasedListener onPurchasedListener,
                     OnBillingUnavailableListener onBillingUnavailableListener) {
        if (billingClient != null) return;

        // We don't request billing permission pre-M
        if (SDK_INT < M) {
            onBillingUnavailableListener.onBillingUnavailable();
            return;
        }

        this.onBillingUnavailableListener = onBillingUnavailableListener;
        billingClient = BillingClient.newBuilder(context.getApplicationContext())
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@BillingResponse int responseCode, @Nullable List<Purchase> purchases) {
                        if (responseCode == OK) {
                            logV(TAG_BILLING, "Purchases updated");
                            if (purchases != null) {
                                logV(TAG_BILLING, purchases.toString());
                                for (Purchase p : purchases) {
                                    onPurchasedListener.onPurchased(
                                            new com.veniosg.dir.mvvm.model.iab.Purchase(p.getSku(),
                                                    p.getPurchaseToken()));
                                }
                            } else {
                                logV(TAG_BILLING, "No purchases");
                                makeText(context, R.string.purchase_cancelled, LENGTH_SHORT).show();
                            }
                        } else if (responseCode == USER_CANCELED) {
                            logV(TAG_BILLING, "User cancelled the purchase");
                        } else if (responseCode == ITEM_ALREADY_OWNED) {
                            logV(TAG_BILLING, "Item already owned... Maybe we didn't consume properly?");
                        } else if (responseCode != ITEM_UNAVAILABLE) {
                            logV(TAG_BILLING, "Item unavailable");
                            // Play store shows appropriate errors in this case.
                        } else {
                            logV(TAG_BILLING, format(ENGLISH, "onPurchasesUpdated responded: %d", responseCode));
                            if (requestedSku != null) { // User-triggered flow
                                makeText(context, R.string.purchase_cancelled, LENGTH_SHORT).show();
                            }
                        }
                        requestedSku = null;
                    }
                })
                .build();
        startConnection();
    }

    @Override
    public boolean hasPurchasedDonation() {
        return hasPurchased(SKU_DONATION);
    }

    @Override
    public void purchaseDonation(Activity activity) {
        purchase(activity, SKU_DONATION);
    }

    private void purchase(Activity activity, @NonNull String sku) {
        runWhenConnected(() -> {
            BillingFlowParams params = BillingFlowParams.newBuilder()
                    .setType(INAPP)
                    .setSku(sku)
                    .build();
            requestedSku = sku;
            billingClient.launchBillingFlow(activity, params);
        });
    }

    private boolean hasPurchased(@NonNull String sku) {
        Purchase.PurchasesResult purchasesStatus = billingClient.queryPurchases(INAPP);
        if (purchasesStatus.getResponseCode() == OK) {
            for (Purchase p : purchasesStatus.getPurchasesList()) {
                if (sku.equals(p.getSku())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void consumePurchase(com.veniosg.dir.mvvm.model.iab.Purchase p,
                                final OnConsumedListener onConsumedListener) {
        logV(TAG_BILLING, format(ENGLISH, "Consuming purchase: %s", p.getSku()));
        billingClient.consumeAsync(p.getPurchaseToken(), (responseCode, outToken) -> {
            if (responseCode == OK) {
                logV(TAG_BILLING, "Purchase consumed");
                onConsumedListener.onConsumed();
            } else {
                logV(TAG_BILLING, format(ENGLISH, "Error (%d) consuming, retrying...", responseCode));
                consumePurchase(p, onConsumedListener);
            }
        });
    }

    private void runWhenConnected(Runnable runnable) {
        if (connectionStatus == CONNECTED) {
            runnable.run();
        } else if (connectionStatus == DISCONNECTED) {
            onConnectedRunnables.add(runnable);
            startConnection();
        } else {
            onConnectedRunnables.add(runnable);
        }
    }

    private void startConnection() {
        logV(TAG_BILLING, "Connecting to billing service");
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
                if (responseCode == OK) {
                    logV(TAG_BILLING, "Connected to billing service");
                    connectionStatus = CONNECTED;

                    Iterator<Runnable> iterator = onConnectedRunnables.iterator();
                    while (iterator.hasNext()) {
                        Runnable next = iterator.next();
                        next.run();

                        iterator.remove();
                    }
                } else {
                    logV(TAG_BILLING, "Failed to connect to billing service");
                    connectionStatus = DISCONNECTED;

                    if (responseCode == SERVICE_UNAVAILABLE || responseCode == BILLING_UNAVAILABLE) {
                        logV(TAG_BILLING, "Billing service unavailable");
                        onBillingUnavailableListener.onBillingUnavailable();
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                connectionStatus = DISCONNECTED;
            }
        });
    }
}
