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

package com.accessifiers.filebrowser.android.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import com.accessifiers.filebrowser.R;
import com.accessifiers.filebrowser.android.fragment.FileListFragment;
import com.accessifiers.filebrowser.android.fragment.PickFileListFragment;
import com.accessifiers.filebrowser.android.util.FileUtils;

import java.io.File;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.text.TextUtils.isEmpty;
import static com.accessifiers.filebrowser.IntentConstants.ACTION_PICK_DIRECTORY;
import static com.accessifiers.filebrowser.IntentConstants.ACTION_PICK_FILE;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_DIRECTORIES_ONLY;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_DIR_PATH;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_FILENAME;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_FILTER_MIMETYPE;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_IS_GET_CONTENT_INITIATED;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_TITLE;
import static com.accessifiers.filebrowser.android.fragment.PreferenceFragment.getDefaultPickFilePath;
import static com.accessifiers.filebrowser.android.fragment.PreferenceFragment.setDefaultPickFilePath;

public class IntentFilterActivity extends BaseActivity {
	private FileListFragment mFragment;

	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);

        setContentView(R.layout.activity_generic);
        setupToolbar();

		Intent intent = getIntent();
		Bundle options = intent.getExtras();
		if (options == null) options = new Bundle();
		// Add a path if path is not specified in this activity's call
		if (!options.containsKey(EXTRA_DIR_PATH)) {
			// Set a default path so that we launch a proper list.
			File defaultFile = new File(getDefaultPickFilePath(this));
			if (!defaultFile.exists()) {
				String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
				setDefaultPickFilePath(this, storagePath);
				defaultFile = new File(getDefaultPickFilePath(this));
			}
			options.putString(EXTRA_DIR_PATH, defaultFile.getAbsolutePath());
		}

		// Add a path if a path has been specified in this activity's call.
		Uri data = intent.getData();
		if (data != null && !isEmpty(data.getPath())) {
			File dataFile = FileUtils.getFile(data);
            options.putString(EXTRA_DIR_PATH, dataFile.getAbsolutePath());
			if (dataFile.isFile()){
				options.putString(EXTRA_FILENAME, dataFile.getName());
			}
		}

		// Add a mimetype filter if it was specified through the type of the intent.
		if (!options.containsKey(EXTRA_FILTER_MIMETYPE) && intent.getType() != null) {
			options.putString(EXTRA_FILTER_MIMETYPE, intent.getType());
		}

		bindData(intent, options);
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

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getActionBar().setDisplayShowHomeEnabled(false);
        }
    }

	private void bindData(Intent intent, Bundle extras) {
		// Item pickers
		if (ACTION_PICK_DIRECTORY.equals(intent.getAction())
				|| ACTION_PICK_FILE.equals(intent.getAction())
				|| ACTION_GET_CONTENT.equals(intent.getAction())) {
			if (intent.hasExtra(EXTRA_TITLE)) {
				setTitle(intent.getStringExtra(EXTRA_TITLE));
			} else {
				setTitle(R.string.pick_title);
			}

			mFragment = (PickFileListFragment) getSupportFragmentManager()
					.findFragmentByTag(PickFileListFragment.class.getName());

			// Only add if it doesn't exist
			if (mFragment == null) {
				mFragment = new PickFileListFragment();

				// Pass extras through to the list fragment. This helps centralize path resolution etc.
				extras.putBoolean(
						EXTRA_IS_GET_CONTENT_INITIATED,
						intent.getAction().equals(ACTION_GET_CONTENT));
				extras.putBoolean(
						EXTRA_DIRECTORIES_ONLY,
						intent.getBooleanExtra(EXTRA_DIRECTORIES_ONLY, false) ||
						intent.getAction().equals(ACTION_PICK_DIRECTORY));

				mFragment.setArguments(extras);
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.fragment, mFragment, PickFileListFragment.class.getName())
						.commit();
			}
		} else {
            // Don't stay alive without a UI
            finish();
        }
	}
}
