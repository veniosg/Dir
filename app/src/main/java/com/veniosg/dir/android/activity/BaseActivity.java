/*
 * Copyright (C) 2014-2016 George Venios
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toolbar;

import com.veniosg.dir.R;
import com.veniosg.dir.android.ui.Themer;

import static com.veniosg.dir.IntentConstants.ACTION_REFRESH_THEME;

abstract class BaseActivity extends FragmentActivity {
    static final String FRAGMENT_TAG = "content_fragment";

    private final BroadcastReceiver mThemeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PreferenceActivity.class.equals(BaseActivity.this.getClass())) {
                finish();
                startActivity(getIntent());
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                recreate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Themer.applyTheme(this);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mThemeReceiver, new IntentFilter(ACTION_REFRESH_THEME));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mThemeReceiver);
        super.onDestroy();
    }

    /**
     * Always call this after setContent() or it will not have any effect.
     */
    protected void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setActionBar(toolbar);
            //noinspection ConstantConditions
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
