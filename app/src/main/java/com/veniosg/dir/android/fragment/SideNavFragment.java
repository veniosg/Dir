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

package com.veniosg.dir.android.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.android.activity.AboutActivity;
import com.veniosg.dir.android.activity.PreferenceActivity;
import com.veniosg.dir.android.adapter.BookmarkListAdapter;
import com.veniosg.dir.android.provider.BookmarkProvider;
import com.veniosg.dir.android.ui.widget.WaitingViewFlipper;

import java.io.File;

import static android.view.View.GONE;
import static com.veniosg.dir.android.ui.Themer.getThemedResourceId;
import static com.veniosg.dir.android.ui.Themer.setStatusBarColour;

public class SideNavFragment extends AbsListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private WaitingViewFlipper mFlipper;
    private View.OnClickListener mSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), PreferenceActivity.class);
            startActivity(intent);
        }
    };
    private View.OnClickListener mAboutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, null);
    }

    @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

        mFlipper = (WaitingViewFlipper) view.findViewById(R.id.flipper);

        setLoading(true);
        view.findViewById(R.id.empty_img).setVisibility(GONE);
        ((TextView) view.findViewById(R.id.empty_text)).setText(R.string.bookmark_empty);

        if (getListView() instanceof GridView) {
            ((GridView) getListView()).setNumColumns(1);
        }
        setListAdapter(new BookmarkListAdapter(getActivity(), null));
        setListChoiceListener();
        view.setBackgroundResource(getThemedResourceId(getActivity(), android.R.attr.colorBackground));

        view.findViewById(R.id.action_settings).setOnClickListener(mSettingsClickListener);
        view.findViewById(R.id.action_about).setOnClickListener(mAboutClickListener);
        getLoaderManager().initLoader(0, null, this);
	}

    @Override
	public void onListItemClick(AbsListView l, View v, int position, long id) {
		Cursor c = ((Cursor) getListAdapter().getItem(position));
		((BookmarkContract) getActivity()).onBookmarkSelected(c.getString(
                c.getColumnIndex(BookmarkProvider.PATH)));
	}

    /**
     * Make the UI indicate loading.
     */
    void setLoading(boolean loading) {
        if (loading) {
            mFlipper.setDisplayedChildDelayed(WaitingViewFlipper.PAGE_INDEX_LOADING);
        } else {
            mFlipper.setDisplayedChild(WaitingViewFlipper.PAGE_INDEX_CONTENT);
        }
    }

    private void setListChoiceListener() {
        getListView().setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                menu.clear();
                mode.getMenuInflater().inflate(R.menu.cab_bookmarks, menu);

                if (getListView().getCheckedItemCount() != 1) {
                    menu.removeItem(R.id.menu_open_parent);
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {
                setStatusBarColour(getActivity(), false);
            }

            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                setStatusBarColour(getActivity(), true);
                return true;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        long[] ids = getListView().getCheckedItemIds();
                        for (long id : ids) {
                            getListView().getContext().getContentResolver().delete(BookmarkProvider.CONTENT_URI,
                                    BookmarkProvider._ID + "=?", new String[]{"" + id});
                        }
                        mode.finish();
                        return true;

                    case R.id.menu_open_parent:
                        int pos = 0;
                        SparseBooleanArray checked = getListView().getCheckedItemPositions();
                        for (int i = 0; i < getListView().getCount(); i++) {
                            if (checked.get(i)) {
                                pos = i;
                            }
                        }

                        String path = ((Cursor) getListAdapter().getItem(pos)).getString(2);
                        ((BookmarkContract) getActivity()).onBookmarkSelected(
                                new File(path).getParent());
                        mode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode,
                                                  int position, long id, boolean checked) {
                if (getListView().getCheckedItemCount() != 0) {

                    mode.setTitle(getListView().getCheckedItemCount() + " " + getString(R.string.selected));

                    // Force actions' refresh
                    mode.invalidate();
                }
            }
        });
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), BookmarkProvider.CONTENT_URI, null, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ((BookmarkListAdapter) getListAdapter()).swapCursor(cursor);
        setLoading(false);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        ((BookmarkListAdapter) getListAdapter()).swapCursor(null);
        setLoading(false);
    }

    public static interface BookmarkContract {
        void onBookmarkSelected(String path);

        void showBookmarks();
    }
}
