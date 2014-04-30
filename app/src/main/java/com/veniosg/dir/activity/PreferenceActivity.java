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
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.veniosg.dir.R;
import com.veniosg.dir.util.Utils;

public class PreferenceActivity extends android.preference.PreferenceActivity
                                implements OnSharedPreferenceChangeListener {

	private static final String PREFS_MEDIASCAN = "mediascan";
	private static final String PREFS_DISPLAYHIDDENFILES = "displayhiddenfiles";
    private static final String PREFS_DEFAULTPICKFILEPATH = "defaultpickfilepath";
	private static final String PREFS_SORTBY = "sortby";
	private static final String PREFS_ASCENDING = "ascending";
    private static final String PREFS_USEBESTMATCH = "usebestmatch";

    @SuppressWarnings("ConstantConditions")
    @Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        getWindow().setBackgroundDrawableResource(R.color.window);
        addPreferencesFromResource(R.xml.preferences);
        getActionBar().setDisplayHomeAsUpEnabled(true);
		
		/* Register the onSharedPreferenceChanged listener to update the SortBy ListPreference summary */
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		/* Set the onSharedPreferenceChanged listener summary to its initial value */
		changeListPreferenceSummaryToCurrentValue((ListPreference)findPreference("sortby"));
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

	public static boolean getMediaScanFromPreference(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
					.getBoolean(PREFS_MEDIASCAN, false);
	}

	static void setDisplayHiddenFiles(Context context, boolean enabled) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREFS_DISPLAYHIDDENFILES, enabled);
		editor.commit();
	}

	public static boolean getDisplayHiddenFiles(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREFS_DISPLAYHIDDENFILES, true);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals("sortby")){
			changeListPreferenceSummaryToCurrentValue((ListPreference)findPreference(key));
		}
	}
	
	private void changeListPreferenceSummaryToCurrentValue(ListPreference listPref){
		listPref.setSummary(listPref.getEntry());
	}
	

	public static int getSortBy(Context context) {
		/* entryValues must be a string-array while we need integers */
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
								 .getString(PREFS_SORTBY, "1"));
	}
	
	public static boolean getAscending(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(PREFS_ASCENDING, true);
	}

    public static void setDefaultPickFilePath(Context context, String path) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREFS_DEFAULTPICKFILEPATH, path);
        editor.commit();
    }

    public static String getDefaultPickFilePath(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREFS_DEFAULTPICKFILEPATH, Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/");
    }

    public static boolean getUseBestMatch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PreferenceActivity.PREFS_USEBESTMATCH, true);
    }

}
