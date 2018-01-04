/*
 * Copyright (C) 2014-2017 George Venios
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

package com.veniosg.dir.android.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.android.util.Logger;
import com.veniosg.dir.android.view.CheatSheet;
import com.veniosg.dir.mvvm.model.iab.BillingManager;

import static android.content.Intent.ACTION_SENDTO;
import static android.text.TextUtils.isEmpty;
import static android.view.View.GONE;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static com.veniosg.dir.mvvm.model.iab.BillingManagerInjector.playBillingManager;

public class AboutActivity extends BaseActivity {
    private BillingManager billingManager = playBillingManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        billingManager.init(this,
                p -> {
                    billingManager.consumePurchase(p, () -> {
                        makeText(AboutActivity.this, R.string.donation_thanks, LENGTH_SHORT).show();
                    });
                },
                () -> findViewById(R.id.donate).setVisibility(GONE));
        setupToolbar();
        setupViews();
    }

    private void setupViews() {
        try {
            ((TextView) findViewById(R.id.dirVersion)).setText(
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.log(e);
        }

        // Click listeners
        findViewById(R.id.middleText).setOnClickListener(v -> {
            viewUri("market://dev?id=8885726315648229405", "https://play.google.com/store/apps/dev?id=8885726315648229405");
        });
        findViewById(R.id.contribute).setOnClickListener(v -> {
            viewUri("https://github.com/veniosg/dir", null);
        });
        findViewById(R.id.translate).setOnClickListener(v -> {
            viewUri("http://dirapp.oneskyapp.com/collaboration/project?id=27347", null);
        });
        findViewById(R.id.donate).setOnClickListener(v -> {
            billingManager.purchaseDonation(this);
        });
        findViewById(R.id.share).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_shareSubject));
            intent.putExtra(Intent.EXTRA_TEXT,
                    "http://play.google.com/store/apps/details?id=com.veniosg.dir");

            startActivity(Intent.createChooser(intent, getString(R.string.about_share) + " " + getString(R.string.app_name)));
        });

        // CheatSheets
        CheatSheet.setup(findViewById(R.id.translate));
        CheatSheet.setup(findViewById(R.id.contribute));
        CheatSheet.setup(findViewById(R.id.donate));
        CheatSheet.setup(findViewById(R.id.share));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_about, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_contact:
                Intent i = new Intent(
                        ACTION_SENDTO,
                        Uri.fromParts("mailto", "dir-support@googlegroups.com", null)
                );

                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    makeText(AboutActivity.this,
                            R.string.send_not_available, LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_review:
                viewUri("market://details?id=com.veniosg.dir", "http://play.google.com/store/apps/details?id=com.veniosg.dir");
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    private void viewUri(@NonNull String uri, @Nullable String fallbackUrl) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        try {
            i.setData(Uri.parse(uri));
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            if (isEmpty(fallbackUrl)) {
                Logger.log(e);
                makeText(AboutActivity.this, R.string.application_not_available, LENGTH_SHORT).show();
            } else {
                viewUri(fallbackUrl, null);
            }
        }
    }
}
