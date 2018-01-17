/*
 * Copyright (C) 2012 OpenIntents.org
 * Copyright (C) 2014-2015 George Venios
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

package com.veniosg.dir.android.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.veniosg.dir.android.FileManagerApplication;
import com.veniosg.dir.R;
import com.veniosg.dir.android.adapter.FileHolderListAdapter;
import com.veniosg.dir.mvvm.model.DirectoryHolder;
import com.veniosg.dir.android.misc.DirectoryScanner;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.android.util.Logger;
import com.veniosg.dir.android.ui.widget.WaitingViewFlipper;

import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.veniosg.dir.IntentConstants.*;
import static com.veniosg.dir.IntentConstants.ACTION_REFRESH_LIST;
import static com.veniosg.dir.IntentConstants.EXTRA_DIRECTORIES_ONLY;
import static com.veniosg.dir.IntentConstants.EXTRA_DIR_PATH;
import static com.veniosg.dir.IntentConstants.EXTRA_FILTER_MIMETYPE;
import static com.veniosg.dir.IntentConstants.EXTRA_WRITEABLE_ONLY;
import static com.veniosg.dir.android.fragment.PreferenceFragment.PREFS_THEME;
import static com.veniosg.dir.android.ui.widget.WaitingViewFlipper.PAGE_INDEX_CONTENT;
import static com.veniosg.dir.android.ui.widget.WaitingViewFlipper.PAGE_INDEX_LOADING;
import static com.veniosg.dir.android.ui.widget.WaitingViewFlipper.PAGE_INDEX_PERMISSION_DENIED;

/**
 * An {@link AbsListFragment} that displays the contents of a directory.
 * <p>
 *     Clicks do nothing.
 * </p>
 * <p>
 *     Refreshes on OnSharedPreferenceChange and when receiving
 *     a local ACTION_REFRESH_LIST broadcast with EXTRA_DIR_PATH matching this folder.
 * </p>
 * <p>
 *     Requests permissions if they're not granted.
 * </p>
 */
public abstract class FileListFragment extends AbsListFragment {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 0;
    private static final String INSTANCE_STATE_PATH = "path";
	private static final String INSTANCE_STATE_FILES = "files";
    private static final String INSTANCE_STATE_NEEDS_LOADING = "needsLoading";

    // Not an anonymous inner class because of:
	// http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
	private OnSharedPreferenceChangeListener preferenceListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// We only care for list-altering preferences. This could be dangerous though,
			// as later contributors might not see this, and have their settings not work in realtime.
			// Therefore this is commented out, since it's not likely the refresh is THAT heavy.
			// *****************
			// if (PreferenceActivity.PREFS_DISPLAYHIDDENFILES.equals(key)
			// || PreferenceActivity.PREFS_SORTBY.equals(key)
			// || PreferenceActivity.PREFS_ASCENDING.equals(key))

			// Prevent NullPointerException caused from this getting called after the activity is finished.
			if (getActivity() != null && !key.equals(PREFS_THEME)) // We're restarting, no need for refresh
				refresh();
		}
	};

	FileHolderListAdapter mAdapter;
	private DirectoryScanner mScanner;
	private ArrayList<FileHolder> mFiles = new ArrayList<>();
	private String mPath;
	private String mFilename;
    private FileObserver mFileObserver;

    private WaitingViewFlipper mFlipper;
    private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String requestPath = intent.getStringExtra(EXTRA_DIR_PATH);
            if (requestPath != null && requestPath.equals(mPath)) {
                refresh();
            }
        }
    };
    private View.OnClickListener mEmptyViewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onEmptyViewClicked();
        }
    };
    private View.OnClickListener mRequestPermissionsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            requestPermissions();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mRefreshReceiver, new IntentFilter(ACTION_REFRESH_LIST));
    }

    @Override
    public void onDestroy() {
        stopScanner();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mRefreshReceiver);
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(INSTANCE_STATE_PATH, mPath);
        outState.putInt(INSTANCE_STATE_NEEDS_LOADING, isScannerRunning() ? 1 : 0);
        outState.putParcelableArrayList(INSTANCE_STATE_FILES, mFiles);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_filelist, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

		// Set auto refresh on preference change.
		getDefaultSharedPreferences(getActivity())
				.registerOnSharedPreferenceChangeListener(preferenceListener);

		// Set list properties
		getListView().requestFocus();
		getListView().requestFocusFromTouch();

		mFlipper = (WaitingViewFlipper) view.findViewById(R.id.flipper);
        view.findViewById(R.id.empty_img).setOnClickListener(mEmptyViewClickListener);
        view.findViewById(R.id.permissions_button).setOnClickListener(mRequestPermissionsListener);

		// Get arguments
        boolean needsLoading = true;
		if (savedInstanceState == null) {
            setPath(new File(getArguments().getString(EXTRA_DIR_PATH)));
			mFilename = getArguments().getString(EXTRA_FILENAME);
		} else {
			setPath(new File(savedInstanceState.getString(INSTANCE_STATE_PATH)));
			mFiles = savedInstanceState
					.getParcelableArrayList(INSTANCE_STATE_FILES);
            needsLoading = savedInstanceState.getInt(INSTANCE_STATE_NEEDS_LOADING) != 0;
		}
		pathCheckAndFix();

        if (needsLoading) {
            refresh();
        }

        mAdapter = new FileHolderListAdapter(mFiles);
        setListAdapter(mAdapter);
	}

    /**
	 * Reloads {@link #mPath}'s contents.
	 */
	protected void refresh() {
        if (hasPermissions()) {
            showLoading(true);
            renewScanner().start();
        } else {
            requestPermissions();
        }
	}

    private boolean hasPermissions() {
	    if (getActivity() == null) return false;

        return checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        showLoading(true);
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
    }

    /**
     * Switch to permission request mode.
     */
    private void showPermissionDenied() {
        onListVisibilityChanging(false);
        mFlipper.setDisplayedChild(PAGE_INDEX_PERMISSION_DENIED);
        onListVisibilityChanging(false);
    }

    /**
     * Make the UI indicate loading.
     */
    private void showLoading(boolean loading) {
        onListVisibilityChanging(!loading);
        if (loading) {
            mFlipper.setDisplayedChildDelayed(PAGE_INDEX_LOADING);
        } else {
            mFlipper.setDisplayedChild(PAGE_INDEX_CONTENT);
        }
        onListVisibilityChanged(!loading);
    }

    /**
	 * Recreates the {@link #mScanner} using the previously set arguments and
	 * {@link #mPath}.
	 * 
	 * @return {@link #mScanner} for convenience.
	 */
	protected DirectoryScanner renewScanner() {
        // Cancel previous scanner so that it doesn't load on top of the new list.
        stopScanner();

        String filetypeFilter = getArguments().getString(EXTRA_FILTER_FILETYPE);
		String mimetypeFilter = getArguments().getString(EXTRA_FILTER_MIMETYPE);
		boolean writeableOnly = getArguments().getBoolean(EXTRA_WRITEABLE_ONLY);
		boolean directoriesOnly = getArguments().getBoolean(EXTRA_DIRECTORIES_ONLY);

		mScanner = new DirectoryScanner(new File(mPath),
                getActivity(),
				new FileListMessageHandler(),
                ((FileManagerApplication) getActivity().getApplicationContext()).getMimeTypes(),
				filetypeFilter == null ? "" : filetypeFilter,
				mimetypeFilter == null ? "" : mimetypeFilter,
                writeableOnly,
				directoriesOnly);
		return mScanner;
	}

    private void stopScanner() {
        if (hasScanner()) {
            mScanner.cancel();
        }
    }

    public boolean isScannerRunning() {
        return hasScanner()
                && mScanner.isAlive()
                && mScanner.isRunning();
    }

    protected boolean isMediaScannerDisabledForPath() {
        return mScanner.getNoMedia();
    }

    protected boolean hasScanner() {
        return mScanner != null;
    }

    private class FileListMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
                case DirectoryScanner.MESSAGE_SHOW_DIRECTORY_CONTENTS:
                    DirectoryHolder c = (DirectoryHolder) msg.obj;
                    mFiles.clear();
                    mFiles.addAll(c.listSdCard);
                    mFiles.addAll(c.listDir);
                    mFiles.addAll(c.listFile);
                    onDataReady();

                    mAdapter.notifyDataSetChanged();
                    if (getView() != null) {
                        getListView().setSelection(0);
                    }
                    showLoading(false);
                    onDataApplied();
                    break;
                case DirectoryScanner.MESSAGE_SET_PROGRESS:
                    // Irrelevant.
                    break;
                }
		}
	}

	/**
	 * @return The currently displayed directory's absolute path.
	 */
	public final String getPath() {
		return mPath;
	}

	/**
	 * This will be ignored if path doesn't pass check as valid.
	 * 
	 * @param dir The path to set.
	 */
	public final void setPath(File dir) {
        mPath = dir.getAbsolutePath();

        if (dir.exists()){
            // Observe the path
            if (mFileObserver != null) {
                mFileObserver.stopWatching();
            }
            mFileObserver = generateFileObserver(mPath);
            mFileObserver.startWatching();
		}
	}

    private FileObserver generateFileObserver(String pathToObserve) {
        return new FileObserver(pathToObserve,
                          FileObserver.CREATE
                        | FileObserver.DELETE
                        | FileObserver.CLOSE_WRITE // Removed since in case of continuous modification
                                                   // (copy/compress) we would flood with events.
                        | FileObserver.MOVED_FROM
                        | FileObserver.MOVED_TO) {
            private static final long MIN_REFRESH_INTERVAL = 2 * 1000;

            private long lastUpdate = 0;

            @Override
            public void onEvent(int event, String path) {
                if (System.currentTimeMillis() - lastUpdate <= MIN_REFRESH_INTERVAL
                        || event == 32768) { // See https://code.google.com/p/android/issues/detail?id=29546
                    return;
                }

                Logger.logV(Logger.TAG_OBSERVER, "Observed event " + event + ", refreshing list..");
                lastUpdate = System.currentTimeMillis();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    });
                }
            }
        };
    }

    private void pathCheckAndFix() {
		File dir = new File(mPath);
		// Sanity check that the path (coming from extras_dir_path) is indeed a
		// directory
		if (!dir.isDirectory() && dir.getParentFile() != null) {
			// remember the filename for picking.
			mFilename = dir.getName();
			setPath(dir.getParentFile());
		}
	}

	public String getFilename() {
		return mFilename;
	}

    /**
     * Will request a refresh for all active FileListFragment instances currently displaying "directory".
     * @param directory The directory to refresh.
     */
    public static void refresh(Context c, File directory) {
        Intent i = new Intent(ACTION_REFRESH_LIST);
        i.putExtra(EXTRA_DIR_PATH, directory.getAbsolutePath());

        LocalBroadcastManager.getInstance(c).sendBroadcast(i);
    }

    /**
     * Use this callback to handle UI state when the new list data is ready but BEFORE
     * the list is refreshed.
     */
    protected void onDataReady() {}

    /**
     * Use this callback to handle UI state when the new list data is ready and the UI
     * has been refreshed.
     */
    protected void onDataApplied() {}

    /**
     * Used to inform subclasses about list visibility changing. Can be used to
     * make the ui indicate the visible state of the fragment. This is called before the actual change.
     *
     * @param visible If the list started or stopped being visible.
     */
    protected void onListVisibilityChanging(boolean visible) {}

    /**
     * Used to inform subclasses about visible state changing. Can be used to
     * make the ui indicate the visible state of the fragment. This is called after the actual change.
     *
     * @param visible If the list started or stopped being visible.
     */
    protected void onListVisibilityChanged(boolean visible) {}

    protected void onEmptyViewClicked(){}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    refresh();
                } else {
                    showPermissionDenied();
                }
                break;
        }
    }
}
