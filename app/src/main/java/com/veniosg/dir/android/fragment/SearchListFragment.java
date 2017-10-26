/*
 * Copyright (C) 2012 OpenIntents.org
 * Copyright (C) 2017 George Venios
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

import android.app.SearchManager;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.android.activity.FileManagerActivity;
import com.veniosg.dir.android.adapter.FileListViewHolder.OnItemClickListener;
import com.veniosg.dir.android.adapter.SearchListAdapter;
import com.veniosg.dir.android.view.widget.WaitingViewFlipper;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.mvvm.model.search.SearchState;
import com.veniosg.dir.mvvm.viewmodel.SearchViewModel;

import java.io.File;
import java.util.List;

import static android.app.SearchManager.APP_DATA;
import static android.arch.lifecycle.ViewModelProviders.of;
import static android.view.View.GONE;
import static com.veniosg.dir.IntentConstants.EXTRA_SEARCH_INIT_PATH;
import static com.veniosg.dir.android.util.FileUtils.openFile;
import static com.veniosg.dir.android.view.Themer.getThemedResourceId;
import static com.veniosg.dir.android.view.widget.WaitingViewFlipper.PAGE_INDEX_CONTENT;
import static com.veniosg.dir.android.view.widget.WaitingViewFlipper.PAGE_INDEX_LOADING;

public class SearchListFragment extends Fragment {
//    private static final String STATE_POS = "pos";
//    private static final String STATE_TOP = "top";

    private WaitingViewFlipper mFlipper;
    private TextView mEmptyTextView;
    private ImageView mEmptyImageView;
    private RecyclerView mRecyclerView;
    private SearchViewModel viewModel;
    private final OnItemClickListener onItemClickListener = (itemView, item) -> {
        if (item.getFile().isDirectory()) {
            browse(Uri.parse(item.getFile().getAbsolutePath()));
        } else {
            openFile(item, getActivity());
        }
    };
    private final SearchListAdapter adapter = new SearchListAdapter(onItemClickListener);
    private final Observer<SearchState> resultObserver = searchState -> {
        if (searchState == null) {
            showLoading(false);
        } else {
            List<String> results = searchState.results();

            adapter.notifyDataAppended(results);
            showLoading(!results.isEmpty());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = of(this).get(SearchViewModel.class);
        handleIntent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.fragment_filelist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setBackgroundResource(
                getThemedResourceId(getActivity(), android.R.attr.colorBackground));

        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);
        mFlipper = (WaitingViewFlipper) view.findViewById(R.id.flipper);
        mEmptyTextView = (TextView) view.findViewById(R.id.empty_text);
        mEmptyImageView = (ImageView) view.findViewById(R.id.empty_img);

        setupStaticViews(view);
        setupList();
        showLoading(true);
        restoreScroll(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // TODO still needed?
//        int top = 0;
//        if (mRecyclerView.getChildCount() != 0) {
//            top = mRecyclerView.getChildAt(0).getTop();
//        }
//
//        LinearLayoutManager linearLayoutMgr = (LinearLayoutManager) mRecyclerView.getLayoutManager();
//        outState.putInt(STATE_POS, linearLayoutMgr.findFirstVisibleItemPosition());
//        outState.putInt(STATE_TOP, top);
    }

    private void restoreScroll(Bundle savedInstanceState) {
        // TODO still needed?
//        if (savedInstanceState != null) {
//            int index = savedInstanceState.getInt(STATE_POS);
//            int top = savedInstanceState.getInt(STATE_TOP);
//            ScrollPosition scrollPosition = new ScrollPosition(index, top);
//            scrollToPosition(mRecyclerView, scrollPosition, true);
//        }
    }

    private void setupStaticViews(View view) {
        mEmptyTextView.setText(R.string.search_empty);
        ((TextView) view.findViewById(R.id.loading_text)).setText(R.string.searching);
        mEmptyImageView.setVisibility(GONE);
    }

    private void setupList() {
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Make the UI indicate loading.
     */
    void showLoading(boolean loading) {
        if (loading) {
            mFlipper.setDisplayedChildDelayed(PAGE_INDEX_LOADING);
        } else {
            mFlipper.setDisplayedChild(PAGE_INDEX_CONTENT);
        }
    }

    private void handleIntent() {
        Intent intent = getActivity().getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Get the query.
            String query = intent.getStringExtra(SearchManager.QUERY);
            getActivity().getActionBar().setTitle(query);

            // Get the current path
            String path = intent.getBundleExtra(APP_DATA).getString(EXTRA_SEARCH_INIT_PATH);
            File root = new File(path);
            getActivity().getActionBar().setSubtitle(path);

            // Init search
            viewModel.init(root);
            viewModel.getLiveResults().observe(this, resultObserver);
            viewModel.updateQuery(query);
        } else {
            // Intent contents error.
            getActivity().setTitle(R.string.query_error);
            showLoading(false);
        }
    }

    private void browse(FileHolder file) {
        if (file.getFile().isDirectory()) {
            Intent intent = new Intent(getActivity(), FileManagerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setData(Uri.parse(file.getFile().getAbsolutePath()));

            startActivity(intent);
        } else {
            openFile(file, getContext());
        }
    }

    private void browse(Uri path) {
        Intent intent = new Intent(getActivity(), FileManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(path);

        startActivity(intent);
    }


}
