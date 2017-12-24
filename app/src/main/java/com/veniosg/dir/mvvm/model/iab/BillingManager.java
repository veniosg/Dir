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

import com.android.billingclient.api.Purchase;

public interface BillingManager {
    void init(Context context, OnPurchasedListener onPurchasedListener);
    boolean supportsBilling();
    boolean hasPurchasedDonation();
    void purchaseDonation(Activity activity);
    void consumePurchase(Purchase p, OnConsumedListener onConsumedListener);

    interface OnPurchasedListener {
        void onPurchased(Purchase p);
    }

    interface OnConsumedListener {
        void onConsumed();
    }
}
