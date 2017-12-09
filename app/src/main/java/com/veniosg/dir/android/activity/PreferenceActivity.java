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

package com.veniosg.dir.android.activity;

import android.os.Bundle;
import android.view.MenuItem;

import com.veniosg.dir.R;
import com.veniosg.dir.android.fragment.PreferenceFragment;
import com.veniosg.dir.android.util.Utils;

public class PreferenceActivity extends BaseActivity {
    private PreferenceFragment mFragment;

    @SuppressWarnings("ConstantConditions")
    @Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        setContentView(R.layout.activity_generic);
        setupToolbar();

        mFragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if(mFragment == null){
            mFragment = new PreferenceFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment, mFragment, FRAGMENT_TAG)
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
