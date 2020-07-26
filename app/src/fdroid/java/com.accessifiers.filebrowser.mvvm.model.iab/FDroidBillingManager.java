/*
 * Copyright (C) 2018 George Venios
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
package com.accessifiers.filebrowser.mvvm.model.iab;

import android.app.Activity;
import android.content.Context;

public class FDroidBillingManager implements BillingManager {
    @Override
    public void init(Context context, OnPurchasedListener onPurchasedListener,
                     OnBillingUnavailableListener onUnavailableListener) {
        onUnavailableListener.onBillingUnavailable();
    }

    @Override
    public boolean hasPurchasedDonation() {
        return false;
    }

    @Override
    public void purchaseDonation(Activity activity) {
    }

    @Override
    public void consumePurchase(Purchase p, OnConsumedListener onConsumedListener) {
    }
}
