/*
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

package com.veniosg.dir.android.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.veniosg.dir.android.adapter.viewholder.BookmarkListViewHolder;
import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder;
import com.veniosg.dir.android.fragment.RecyclerViewFragment;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.io.File;

import androidx.recyclerview.selection.SelectionTracker;

import static com.veniosg.dir.android.fragment.RecyclerViewFragment.ClickableAdapter;

public class BookmarkListAdapter extends CursorAdapter<FileListViewHolder>
        implements ClickableAdapter, RecyclerViewFragment.SelectableAdapter<Long> {
    private FileListViewHolder.OnItemClickListener mOnItemClickListener;
    private SelectionTracker<Long> mSelectionTracker;

    public BookmarkListAdapter(Context context, Cursor cursor) {
        super(cursor);
    }

    @NonNull
    @Override
    public FileListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookmarkListViewHolder(parent);
    }

    @Override
    protected void onBindViewHolder(FileListViewHolder holder, Cursor cursor) {
        FileHolder item = new FileHolder(new File(cursor.getString(2)), holder.itemView.getContext());
        boolean isSelected = isSelected(item);

        holder.bind(item, isSelected, mOnItemClickListener);
    }

    @Override
    public void setOnItemClickListener(FileListViewHolder.OnItemClickListener onClickListener) {
        this.mOnItemClickListener = onClickListener;
    }

    @Override
    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.mSelectionTracker = selectionTracker;
    }

    private boolean isSelected(FileHolder item) {
        return mSelectionTracker != null && mSelectionTracker.isSelected(item.getId());
    }
}
