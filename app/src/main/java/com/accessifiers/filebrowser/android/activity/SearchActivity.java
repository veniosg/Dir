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

package com.accessifiers.filebrowser.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.accessifiers.filebrowser.android.fragment.SearchListFragment;

import static com.accessifiers.filebrowser.android.util.Utils.showHome;

public class SearchActivity extends BaseActivity {
    private SearchListFragment mFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleRequest();
    }

    protected void onCreate(Bundle savedInstanceState) {
        // Presentation settings
        super.onCreate(savedInstanceState);

        setupToolbar();

        // Handle the search request.
        handleRequest();
    }

    private void handleRequest() {
        // Add fragment only if it hasn't already been added.
        mFragment = (SearchListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (mFragment == null) {
            mFragment = new SearchListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, mFragment, FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showHome(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}