/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2018 George Venios
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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder.OnItemClickListener;
import com.veniosg.dir.android.ui.widget.DividerRecyclerView;
import com.veniosg.dir.mvvm.model.FileHolder;

/**
 * Static library support version of the framework's {@link android.app.ListFragment}.
 * Used to write apps that run on platforms prior to Android 3.0.  When running
 * on Android 3.0 or above, this implementation is still used; it does not try
 * to switch to the framework's implementation.  See the framework SDK
 * documentation for a class overview.
 */
public class RecyclerViewFragment extends Fragment {
    static final int INTERNAL_EMPTY_ID = 0x00ff0001;
    static final int INTERNAL_PROGRESS_CONTAINER_ID = 0x00ff0002;
    static final int INTERNAL_LIST_CONTAINER_ID = 0x00ff0003;

    final private Handler mHandler = new Handler();

    final private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    final private OnItemClickListener mOnClickListener = this::onListItemClick;

    RecyclerView.Adapter mAdapter;
    RecyclerView mList;
    View mCustomEmptyView;
    TextView mStandardEmptyView;
    View mProgressContainer;
    View mListContainer;
    CharSequence mEmptyText;
    View mEmptyView;
    boolean mListShown;
    private final RecyclerView.AdapterDataObserver mEmptyViewUpdatingObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            ensureEmptyView(false);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            ensureEmptyView(true);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            ensureEmptyView(true);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            ensureEmptyView(true);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            ensureEmptyView(true);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            ensureEmptyView(true);
        }
    };

    public RecyclerViewFragment() {
    }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy <em>must</em> have a RecyclerView whose id
     * is {@link android.R.id#list android.R.id.list} and can optionally
     * have a sibling view id {@link android.R.id#empty android.R.id.empty}
     * that is to be shown when the list is empty.
     * <p>
     * <p>If you are overriding this method with your own custom content,
     * consider including the standard layout {@link android.R.layout#list_content}
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Context context = getActivity();

        FrameLayout root = new FrameLayout(context);

        // ------------------------------------------------------------------

        LinearLayout pframe = new LinearLayout(context);
        pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
        pframe.setOrientation(LinearLayout.VERTICAL);
        pframe.setVisibility(View.GONE);
        pframe.setGravity(Gravity.CENTER);

        ProgressBar progress = new ProgressBar(context, null,
                android.R.attr.progressBarStyleLarge);
        pframe.addView(progress, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(pframe, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ------------------------------------------------------------------

        FrameLayout lframe = new FrameLayout(context);
        lframe.setId(INTERNAL_LIST_CONTAINER_ID);

        TextView tv = new TextView(getActivity());
        tv.setId(INTERNAL_EMPTY_ID);
        tv.setGravity(Gravity.CENTER);
        lframe.addView(tv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        RecyclerView lv = new DividerRecyclerView(getActivity());
        lv.setId(android.R.id.list);
        lframe.addView(lv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        root.addView(lframe, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ------------------------------------------------------------------

        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return root;
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mListShown = false;
        mCustomEmptyView = mProgressContainer = mListContainer = null;
        mStandardEmptyView = null;
        super.onDestroyView();
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getRecyclerView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param itemView The view that was clicked within the RecyclerView
     * @param item     The data item represented by itemView
     */
    public void onListItemClick(View itemView, FileHolder item) {
    }

    /**
     * Provide the cursor for the list view.
     */
    public void setListAdapter(@NonNull RecyclerView.Adapter adapter) {
        boolean hadAdapter = mAdapter != null;
        if (hadAdapter) {
            mAdapter.unregisterAdapterDataObserver(mEmptyViewUpdatingObserver);
        }
        mAdapter = adapter;
        if (mAdapter instanceof ClickableAdapter) {
            ((ClickableAdapter) mAdapter).setOnItemClickListener(mOnClickListener);
        }
        if (mList != null) {
            mList.setAdapter(mAdapter);
            boolean shouldAnimate = getView() != null && getView().getWindowToken() != null;
            if (!mListShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter. It is now time to show it.
                setListShown(true, shouldAnimate);
            } else {
                ensureEmptyView(shouldAnimate);
            }
        }
        mAdapter.registerAdapterDataObserver(mEmptyViewUpdatingObserver);
    }

    private void ensureEmptyView(boolean animate) {
        boolean hideEmpty = false;
        if (mAdapter != null) {
            hideEmpty = !mListShown || mAdapter.getItemCount() > 0;
        }

        if (hideEmpty) {
            if (animate) {
                mEmptyView.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mEmptyView.clearAnimation();
            }
            mEmptyView.setVisibility(View.GONE);
        } else {
            if (animate) {
                mEmptyView.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mEmptyView.clearAnimation();
            }
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Get the activity's list view widget.
     */
    public RecyclerView getRecyclerView() {
        ensureList();
        return mList;
    }

    /**
     * The default content for a RecyclerViewFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     */
    public void setEmptyText(CharSequence text) {
        ensureList();
        if (mStandardEmptyView == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        mStandardEmptyView.setText(text);
        if (mEmptyText == null) {
            setEmptyView(mStandardEmptyView);
        }
        mEmptyText = text;
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * <p>
     * <p>Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with {@link #setListAdapter(RecyclerView.Adapter)}.
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     *
     * @param shown If true, the list view is shown; if false, the progress
     *              indicator.  The initial value is true.
     */
    public void setListShown(boolean shown) {
        setListShown(shown, false);
    }

    /**
     * Like {@link #setListShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     */
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown   If true, the list view is shown; if false, the progress
     *                indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     *                new state.
     */
    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
        ensureEmptyView(animate);
    }

    /**
     * Get the RecyclerView.Adapter associated with this activity's RecyclerView.
     */
    public RecyclerView.Adapter getListAdapter() {
        return mAdapter;
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof RecyclerView) {
            mList = (RecyclerView) root;
        } else {
            mStandardEmptyView = (TextView) root.findViewById(INTERNAL_EMPTY_ID);
            if (mStandardEmptyView == null) {
                mCustomEmptyView = root.findViewById(android.R.id.empty);
            } else {
                mStandardEmptyView.setVisibility(View.GONE);
            }
            mProgressContainer = root.findViewById(INTERNAL_PROGRESS_CONTAINER_ID);
            mListContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID);
            View rawRecyclerView = root.findViewById(android.R.id.list);
            if (!(rawRecyclerView instanceof RecyclerView)) {
                if (rawRecyclerView == null) {
                    throw new RuntimeException(
                            "Your content must have a RecyclerView whose id attribute is " +
                                    "'android.R.id.list'");
                }
                throw new RuntimeException(
                        "Content has view with id attribute 'android.R.id.list' "
                                + "that is not a RecyclerView class");
            }
            mList = (RecyclerView) rawRecyclerView;
            if (mCustomEmptyView != null) {
                setEmptyView(mCustomEmptyView);
            } else if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
                setEmptyView(mStandardEmptyView);
            }
        }
        mListShown = true;
        if (mAdapter != null) {
            RecyclerView.Adapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        } else {
            // We are starting without an adapter, so assume we won't
            // have our data right away and start with the progress indicator.
            if (mProgressContainer != null) {
                setListShown(false, false);
            }
        }
        mHandler.post(mRequestFocus);
    }

    private void setEmptyView(View emptyView) {
        if (emptyView != mEmptyView) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
        }
        mEmptyView = emptyView;
        ensureEmptyView(false);
    }

    public interface ClickableAdapter {
        void setOnItemClickListener(OnItemClickListener onClickListener);
    }
}
