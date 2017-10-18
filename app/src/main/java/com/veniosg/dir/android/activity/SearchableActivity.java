/*
 * Copyright (C) 2014 George Venios
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

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.veniosg.dir.R;
import com.veniosg.dir.android.fragment.SearchListFragment;
import com.veniosg.dir.android.util.Utils;

/**
 * The activity that handles queries and shows search results.
 * Also handles search-suggestion triggered intents.
 * 
 * @author George Venios
 * 
 */
public class SearchableActivity extends BaseActivity {
    private SearchListFragment mFragment;

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleRequest();
    }

	protected void onCreate(Bundle savedInstanceState) {
		// Presentation settings
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_generic);
        setupToolbar();

        // Handle the search request.
        handleRequest();
	}

    private void handleRequest() {
        // Add fragment only if it hasn't already been added.
        mFragment = (SearchListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if(mFragment == null){
            mFragment = new SearchListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, mFragment, FRAGMENT_TAG).commit();
        }
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Utils.showHome(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}