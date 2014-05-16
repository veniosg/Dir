/*
 * Copyright (C) 2012 OpenIntents.org
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

package com.veniosg.dir.fragment;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.activity.FileManagerActivity;
import com.veniosg.dir.adapter.FileHolderListAdapter;
import com.veniosg.dir.adapter.SearchListAdapter;
import com.veniosg.dir.loader.SearchLoader;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.view.Themer;
import com.veniosg.dir.view.WaitingViewFlipper;

import java.io.File;
import java.util.List;

/**
 * @author George Venios
 */
public class SearchListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<FileHolder>> {
    private static final int LOADER_ID = 1;
    private static final String STATE_POS = "pos";
    private static final String STATE_TOP = "top";

    private WaitingViewFlipper mFlipper;
    private SystemBarTintManager mTintManager;

    private File mRoot;
    private String mQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent();
    }

    /**
     * Make the UI indicate loading.
     */
    void setLoading(boolean loading) {
        if(loading) {
            mFlipper.setDisplayedChildDelayed(WaitingViewFlipper.PAGE_INDEX_LOADING);
        } else {
            mFlipper.setDisplayedChild(WaitingViewFlipper.PAGE_INDEX_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.filelist, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundResource(
                Themer.getThemedResourceId(getActivity(), R.attr.windowContentBackground));

        initDecorStyling(view);

        mFlipper = (WaitingViewFlipper) view.findViewById(R.id.flipper);
        ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.search_empty);
        setLoading(true);
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    ((FileHolderListAdapter) getListAdapter()).setScrolling(false);
                } else {
                    ((FileHolderListAdapter) getListAdapter()).setScrolling(true);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

        if (savedInstanceState != null) {
            getListView().setSelectionFromTop(savedInstanceState.getInt(STATE_POS),
                    savedInstanceState.getInt(STATE_TOP));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_POS, getListView().getFirstVisiblePosition());
        outState.putInt(STATE_TOP, getListView().getChildAt(0).getTop());
    }

    private void initDecorStyling(View view) {
        mTintManager = new SystemBarTintManager(getActivity());
        mTintManager.setTintResource(android.R.color.black);
        mTintManager.setStatusBarTintEnabled(true);
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop() +
                        (mTintManager.getConfig().getPixelInsetTop(false) != 0
                                ? mTintManager.getConfig().getPixelInsetTop(true)
                                : 0),
                view.getPaddingRight() + mTintManager.getConfig().getPixelInsetRight()
                , view.getPaddingBottom());

        initBottomViewPaddings(view);
    }

    void initBottomViewPaddings(View view) {
        getListView().setPadding(getListView().getPaddingLeft(), getListView().getPaddingTop(),
                getListView().getPaddingRight(),
                mTintManager.getConfig().getPixelInsetBottom());
    }

    private void handleIntent() {
        Intent intent = getActivity().getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Get the query.
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            getActivity().getActionBar().setTitle(mQuery);

            // Get the current path
            String path = intent.getBundleExtra(SearchManager.APP_DATA).getString(
                        IntentConstants.EXTRA_SEARCH_INIT_PATH);
            mRoot = new File(path);
            getActivity().getActionBar().setSubtitle(path);

            // Start the actual search
            getLoaderManager().initLoader(LOADER_ID, null, this);
            getActivity().setProgressBarIndeterminateVisibility(true);
        }
        // We're here because of a clicked suggestion
        else if(Intent.ACTION_VIEW.equals(intent.getAction())){
            browse(intent.getData());

            getActivity().finish();
        } else {
            // Intent contents error.
            getActivity().setTitle(R.string.query_error);
            setLoading(false);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        browse(Uri.parse(((FileHolder) getListAdapter().getItem(position))
                .getFile().getAbsolutePath()));
    }

    private void browse(Uri path) {
        Intent intent = new Intent(getActivity(), FileManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(path);

        startActivity(intent);
    }

    @Override
    public Loader<List<FileHolder>> onCreateLoader(int id, Bundle args) {
        return new SearchLoader(getActivity(), mRoot, mQuery);
    }

    @Override
    public void onLoadFinished(Loader<List<FileHolder>> loader, List<FileHolder> data) {
        setListAdapter(new SearchListAdapter(data));

        if (isResumed()) {
            setLoading(false);
        } else {
            setLoading(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<FileHolder>> loader) {
        setListAdapter(null);
    }
}
