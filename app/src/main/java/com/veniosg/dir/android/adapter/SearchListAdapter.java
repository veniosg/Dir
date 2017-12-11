/*
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

package com.veniosg.dir.android.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.veniosg.dir.android.adapter.FileListViewHolder.OnItemClickListener;

import java.util.List;

import static java.util.Collections.emptyList;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListViewHolder> {
    private List<String> data = emptyList();
    private OnItemClickListener onItemClickListener;

    public SearchListAdapter(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void notifyDataAppended(@NonNull List<String> updatedData) {
        int oldItemCount = this.data.size();
        this.data = updatedData;

        int newItemCount = updatedData.size() - oldItemCount;
        notifyItemRangeInserted(oldItemCount, newItemCount);
    }

    public void notifyResultsCleared() {
        notifyItemRangeRemoved(0, data.size());
        data = emptyList();
    }

    @Override
    public SearchListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SearchListViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(SearchListViewHolder holder, int position) {
        holder.bind(data.get(position), onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}