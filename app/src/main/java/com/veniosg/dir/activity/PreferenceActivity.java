/* 
 * Copyright (C) 2008 OpenIntents.org
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

package com.veniosg.dir.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.fragment.PreferenceFragment;
import com.veniosg.dir.fragment.SearchListFragment;
import com.veniosg.dir.fragment.SimpleFileListFragment;
import com.veniosg.dir.util.Utils;
import com.veniosg.dir.view.Themer;

public class PreferenceActivity extends BaseActivity {
    private PreferenceFragment mFragment;

    @SuppressWarnings("ConstantConditions")
    @Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        mFragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if(mFragment == null){
            mFragment = new PreferenceFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, mFragment, FRAGMENT_TAG)
                    .commit();
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Utils.showHome(this);
			return true;
        default:
            return super.onOptionsItemSelected(item);
		}
	}
}
