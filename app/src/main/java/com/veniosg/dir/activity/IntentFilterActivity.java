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

package com.veniosg.dir.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.fragment.FileListFragment;
import com.veniosg.dir.fragment.PickFileListFragment;
import com.veniosg.dir.fragment.PreferenceFragment;
import com.veniosg.dir.util.FileUtils;

import java.io.File;

public class IntentFilterActivity extends BaseActivity {
	private FileListFragment mFragment;

	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

        setContentView(R.layout.activity_generic);
        setupToolbar();

		Intent intent = getIntent();

		// Initialize arguments
		Bundle extras = intent.getExtras();
		if (extras == null)
			extras = new Bundle();
		// Add a path if path is not specified in this activity's call
		if (!extras.containsKey(IntentConstants.EXTRA_DIR_PATH)) {
			// Set a default path so that we launch a proper list.
			File defaultFile = new File(
                    PreferenceFragment.getDefaultPickFilePath(this));
			if (!defaultFile.exists()) {
                PreferenceFragment.setDefaultPickFilePath(this, Environment
						.getExternalStorageDirectory().getAbsolutePath());
				defaultFile = new File(
                        PreferenceFragment.getDefaultPickFilePath(this));
			}
			extras.putString(IntentConstants.EXTRA_DIR_PATH,
					defaultFile.getAbsolutePath());
		}

		// Add a path if a path has been specified in this activity's call.
		File data = FileUtils.getFile(getIntent().getData());
		if (data != null) {			
			File dir = FileUtils.getPathWithoutFilename(data);		
			if (dir != null) {
				extras.putString(IntentConstants.EXTRA_DIR_PATH,
						data.getAbsolutePath());
			}
			if (dir != data){
				// data is a file
				extras.putString(IntentConstants.EXTRA_FILENAME, data.getName());
			}
		}

		// Add a mimetype filter if it was specified through the type of the
		// intent.
		if (!extras.containsKey(IntentConstants.EXTRA_FILTER_MIMETYPE)
				&& intent.getType() != null)
			extras.putString(IntentConstants.EXTRA_FILTER_MIMETYPE,
					intent.getType());

		// Actually fill the ui
		chooseListType(intent, extras);
	}

	private void chooseListType(Intent intent, Bundle extras) {
		// Item pickers
		if ( IntentConstants.ACTION_PICK_DIRECTORY.equals(intent.getAction())
				|| IntentConstants.ACTION_PICK_FILE.equals(intent.getAction())
				|| Intent.ACTION_GET_CONTENT.equals(intent.getAction())){
			if (intent.hasExtra(IntentConstants.EXTRA_TITLE))
				setTitle(intent.getStringExtra(IntentConstants.EXTRA_TITLE));
			else
				setTitle(R.string.pick_title);

			mFragment = (PickFileListFragment) getSupportFragmentManager()
					.findFragmentByTag(PickFileListFragment.class.getName());

			// Only add if it doesn't exist
			if (mFragment == null) {
				mFragment = new PickFileListFragment();

				// Pass extras through to the list fragment. This helps
				// centralize the path resolving, etc.
				extras.putBoolean(
						IntentConstants.EXTRA_IS_GET_CONTENT_INITIATED,
						intent.getAction().equals(Intent.ACTION_GET_CONTENT));
				extras.putBoolean(
						IntentConstants.EXTRA_DIRECTORIES_ONLY,
						intent.getAction().equals(
								IntentConstants.ACTION_PICK_DIRECTORY));

				mFragment.setArguments(extras);
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.fragment, mFragment,
								PickFileListFragment.class.getName()).commit();
			}
		}
	}

    @Override
    public void onBackPressed() {
        if (mFragment instanceof PickFileListFragment) {
            if (!((PickFileListFragment) mFragment).pressBack()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
