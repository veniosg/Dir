package com.veniosg.dir.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.veniosg.dir.fragment.SearchListFragment;
import com.veniosg.dir.util.Utils;
import com.veniosg.dir.view.Themer;

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
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle the search request.
        handleRequest();
	}

    private void handleRequest() {
        // Add fragment only if it hasn't already been added.
        mFragment = (SearchListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if(mFragment == null){
            mFragment = new SearchListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, mFragment, FRAGMENT_TAG).commit();
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

    @Override
    public Themer.Flavor getThemeFlavor() {
        return Themer.Flavor.TRANSLUCENT_NAV;
    }
}