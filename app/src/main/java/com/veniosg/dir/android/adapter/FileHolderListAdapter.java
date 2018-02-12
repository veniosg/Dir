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

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder;
import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder.OnItemClickListener;
import com.veniosg.dir.android.fragment.RecyclerViewFragment;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.util.List;

public class FileHolderListAdapter extends RecyclerView.Adapter<FileListViewHolder>
        implements RecyclerViewFragment.ClickableAdapter {
    private List<FileHolder> mItems;

    private OnItemToggleListener mOnItemToggleListener;
    private OnItemClickListener onItemClickListener;

    public FileHolderListAdapter(List<FileHolder> files){
		mItems = files;
		setHasStableIds(true);
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

    @Nullable
	public FileHolder getItem(int position) {
        if (position < 0 || position >= mItems.size()) return null;

		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileListViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(FileListViewHolder holder, int position) {
        holder.bind(getItem(position), onItemClickListener);
    }

    public OnItemToggleListener getOnItemToggleListener() {
        return mOnItemToggleListener;
    }

    public void setOnItemToggleListener(OnItemToggleListener mOnItemToggleListener) {
        this.mOnItemToggleListener = mOnItemToggleListener;
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener onClickListener) {
        this.onItemClickListener = onClickListener;
    }

    public interface OnItemToggleListener {
        void onItemToggle(int position);
    }
}