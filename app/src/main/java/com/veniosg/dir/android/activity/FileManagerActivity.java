/* 
 * Copyright (C) 2008 OpenIntents.org
 * Copyright (C) 2015 George Venios
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
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.android.fragment.SideNavFragment;
import com.veniosg.dir.android.fragment.SimpleFileListFragment;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.io.File;

import static android.support.v4.view.GravityCompat.START;
import static com.veniosg.dir.IntentConstants.EXTRA_FROM_OI_FILEMANAGER;
import static com.veniosg.dir.IntentConstants.EXTRA_SEARCH_INIT_PATH;
import static com.veniosg.dir.android.util.FileUtils.getFile;
import static com.veniosg.dir.android.util.FileUtils.openFile;
import static java.lang.Math.min;

public class FileManagerActivity extends BaseActivity
        implements SideNavFragment.BookmarkContract {
	private SimpleFileListFragment mFragment;
    private DrawerLayout mDrawerLayout;

    @Override
	protected void onNewIntent(Intent intent) {
		if(intent.getData() != null) {
            mFragment.openInformingPathBar(new FileHolder(getFile(intent.getData()), this),
                    true);  // We force no anim as the layout transition does not run properly when
                            // the view is not visible and consequently the buttons will stay invisible.
        }
	}
	
	/**
	 * Either open the file and finish, or navigate to the designated directory.
     * This gives FileManagerActivity the flexibility to handle file scheme data of any type.
	 * @return The folder to navigate to, if applicable. Null otherwise.
	 */
	private File resolveIntentData(){
		File data = getFile(getIntent().getData());
		if(data == null)
			return null;
		
		if (data.isFile() && !getIntent().getBooleanExtra(EXTRA_FROM_OI_FILEMANAGER, false)) {
			openFile(new FileHolder(data, this), this);

			finish();
			return null;
		} else {
            return getFile(getIntent().getData());
        }
	}
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        setContentView(R.layout.activity_filemanager);
        setupToolbar();
        setupDrawer();

        // Search when the user types.
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		// If not called by name, open on the requested location.
		File data = resolveIntentData();

		// Add fragment only if it hasn't already been added.
		mFragment = (SimpleFileListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		if (mFragment == null) {
			mFragment = new SimpleFileListFragment();
			Bundle args = new Bundle();
			if (data == null)
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
			if (icicle == null && data != null)
				mFragment.openInformingPathBar(new FileHolder(new File(data.toString()), this));
		}
	}

    @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.options_filemanager, menu);
 		return true;
 	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showBookmarks();
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerVisible(START)) {
            mDrawerLayout.closeDrawer(START);
        } else {
            if (!mFragment.pressBack()) {
                super.onBackPressed();
            }
        }
    }

	@Override
	public boolean onSearchRequested() {
		Intent searchIntent = new Intent(this, SearchableActivity.class);
		searchIntent.putExtra(EXTRA_SEARCH_INIT_PATH, mFragment.getPath());

		startActivity(searchIntent);
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

    private void setupDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        setOptimalDrawerWidth(findViewById(R.id.bookmarks));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        getActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * Calculating width based on
     * http://www.google.com/design/spec/patterns/navigation-drawer.html#navigation-drawer-specs.
     */
    private void setOptimalDrawerWidth(View drawerContainer) {
        int actionBarSize = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }

        ViewGroup.LayoutParams params = drawerContainer.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int minScreenWidth = min(displayMetrics.widthPixels, displayMetrics.heightPixels);

        params.width = min(minScreenWidth - actionBarSize, 5 * actionBarSize);
        drawerContainer.requestLayout();
    }
}
