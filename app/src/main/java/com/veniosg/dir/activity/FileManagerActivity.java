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

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.fragment.NavigationFragment;
import com.veniosg.dir.fragment.SimpleFileListFragment;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.util.FileUtils;

import java.io.File;

import static android.view.Gravity.START;

public class FileManagerActivity extends BaseActivity
        implements NavigationFragment.BookmarkContract {
	private SimpleFileListFragment mFragment;
    private DrawerLayout mDrawerLayout;

    @Override
	protected void onNewIntent(Intent intent) {
		if(intent.getData() != null)
			mFragment.openInformingPathBar(new FileHolder(FileUtils.getFile(intent.getData()), this),
                    true);  // We force no anim as the layout transition does not run properly
                            // when the view is not visible and consequently the buttons will stay invisible.
	}
	
	/**
	 * Either open the file and finish, or navigate to the designated directory.
     * This gives FileManagerActivity the flexibility to handle file scheme data of any type.
	 * @return The folder to navigate to, if applicable. Null otherwise.
	 */
	private File resolveIntentData(){
		File data = FileUtils.getFile(getIntent().getData());
		if(data == null)
			return null;
		
		if(data.isFile() && ! getIntent().getBooleanExtra(IntentConstants.EXTRA_FROM_OI_FILEMANAGER, false)){
			FileUtils.openFile(new FileHolder(data, this), this);

			finish();
			return null;
		}
		else
			return FileUtils.getFile(getIntent().getData());
	}
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        setContentView(R.layout.activity_filemanager);
        setupToolbar();

        getActionBar().setHomeAsUpIndicator(R.drawable.ic_logo);

        // Search when the user types.
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		// If not called by name, open on the requested location.
		File data = resolveIntentData();

		// Add fragment only if it hasn't already been added.
		mFragment = (SimpleFileListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		if (mFragment == null) {
			mFragment = new SimpleFileListFragment();
			Bundle args = new Bundle();
			if(data == null)
				args.putString(IntentConstants.EXTRA_DIR_PATH,
                        Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                                ? Environment.getExternalStorageDirectory().getAbsolutePath()
                                : "/");
			else
				args.putString(IntentConstants.EXTRA_DIR_PATH, data.toString());
			mFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().add(R.id.fragment, mFragment, FRAGMENT_TAG).commit();
		} else {
			// If we didn't rotate and data wasn't null.
			if(icicle == null && data!=null)
				mFragment.openInformingPathBar(new FileHolder(new File(data.toString()), this));
		}

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
	}

    @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = new MenuInflater(this);
 		inflater.inflate(R.menu.options_filemanager, menu);

 		return true;
 	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case android.R.id.home:
                mFragment.browseToHome();
                return true;
            default:
                return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(START)) {
            mDrawerLayout.closeDrawer(START);
        } else {
            if (!mFragment.pressBack()) {
                super.onBackPressed();
            }
        }
    }

	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		appData.putString(IntentConstants.EXTRA_SEARCH_INIT_PATH, mFragment.getPath());
		startSearch(null, false, appData, false);
		
		return true;
	}

    @Override
    public void onBookmarkSelected(String path) {
        mFragment.openInformingPathBar(new FileHolder(new File(path), this));
        mFragment.closeActionMode();
        mDrawerLayout.closeDrawer(START);
    }

    @Override
    public void showBookmarks() {
        mDrawerLayout.openDrawer(START);
    }
}
