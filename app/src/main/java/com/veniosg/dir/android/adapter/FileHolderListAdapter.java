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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.util.LongSparseArray;
import android.view.ViewGroup;

import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder;
import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder.OnItemClickListener;
import com.veniosg.dir.android.fragment.RecyclerViewFragment;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.selection.SelectionTracker;

public class FileHolderListAdapter
        extends RecyclerView.Adapter<FileListViewHolder>
        implements RecyclerViewFragment.ClickableAdapter {
    private final List<FileHolder> mItems;
    private final List<Long> mIds;
    private final LongSparseArray<Integer> mIdsToPositions;
    private SelectionTracker<Long> mSelectionTracker;
    private OnItemClickListener mOnItemClickListener;
//    private OnItemToggleListener mOnItemToggleListener;
//    private final Set<Integer> mSelectedItems = new LinkedHashSet<>();
//    private OnSelectionModeToggledListener onSelectionModeToggledListener = null;

    public FileHolderListAdapter(List<FileHolder> files) {
        mItems = files;
        mIdsToPositions = new LongSparseArray<>(mItems.size());
        mIds = new ArrayList<>(mItems.size());
        refreshIds();
        setHasStableIds(true);
        registerAdapterDataObserver(new IdUpdatingAdapterDataObserver(this));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    private FileHolder getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @NonNull
    @Override
    public FileListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileListViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListViewHolder holder, int position) {
        FileHolder item = getItem(position);
        boolean isSelected = mSelectionTracker.isSelected(item.getId());
        holder.bind(item, isSelected, mOnItemClickListener);
//        (itemView, item) -> {
//            if (isInSelectionMode()) {
//                toggleItem(holder.getAdapterPosition());
//            } else {
//                mOnItemClickListener.onClick(itemView, item);
//            }
//        });
//        holder.itemView.setOnLongClickListener(v -> {
//            toggleItem(holder.getAdapterPosition());
//            return true;
//        });
        // TODO we're never really informed about item update :thinking_face:
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener onClickListener) {
        this.mOnItemClickListener = onClickListener;
    }

    public void setSelectionTracker(SelectionTracker<Long> selectionTracker) {
        this.mSelectionTracker = selectionTracker;
    }

    public Iterable<Long> getItemIds() {
        return mIds;
    }

    @Nullable
    public FileHolder getItem(long id) {
        Integer position = mIdsToPositions.get(id);
        return position != null ? getItem(position) : null;
    }

    private void refreshIds() {
        mIds.clear();
        mIdsToPositions.clear();
        for (int p = 0; p < mItems.size(); p++) {
            long id = getItemId(p);
            mIds.add(id);
            mIdsToPositions.put(id, p);
        }
    }

//    @Override
//    public void selectAllItems() {
//        mSelectedItems.clear();
//        for (int i = 0; i < mItems.size(); i++) {
//            mSelectedItems.add(i);
//        }
//        // TODO GV update right items
//        notifyDataSetChanged();
//    }

//    @Override
//    public void clearSelection() {
//        mSelectedItems.clear();
//         TODO GV update right items
//        notifyDataSetChanged();
//    }

//    @Override
//    public void setItemSelected(int id, boolean selected) {
//        boolean wasInSelectionMode = isInSelectionMode();
//
//        if (selected) {
//            mSelectedItems.add(id);
//        } else {
//            mSelectedItems.remove(id);
//        }
//
//        boolean selectionModeToggled = wasInSelectionMode != isInSelectionMode();
//        if (onSelectionModeToggledListener != null && selectionModeToggled) {
//            if (!wasInSelectionMode) {
//                onSelectionModeToggledListener.onEnabled();
//            } else {
//                onSelectionModeToggledListener.onDisabled();
//            }
//        }
//        // TODO GV update right items
//        notifyDataSetChanged();
//    }

//    @Override
//    public void toggleItem(int id) {
//        setItemSelected(id, !mSelectedItems.contains(id));
//    }
//
//    @Override
//    public int getSelectedItemCount() {
//        return mSelectedItems.size();
//    }

//    @Override
//    public int[] getSelectedItemIds() {
//        int[] selectedItemIds = integerSetToIntArray(mSelectedItems);
//        Arrays.sort(selectedItemIds);
//        return selectedItemIds;
//    }
//
//    @Override
//    public int getFirstSelectedItemId() {
//        if (mSelectedItems.isEmpty()) {
//            return -1;
//        } else if (mSelectedItems.size() == 1) {
//            return mSelectedItems.iterator().next();
//        } else {
//            ArrayList<Integer> selectedItemsList = new ArrayList<>(mSelectedItems.size());
//            selectedItemsList.addAll(mSelectedItems);
//            sort(selectedItemsList);
//            return selectedItemsList.get(0);
//        }
//    }

//    @Override
//    public boolean isInSelectionMode() {
//        return !mSelectedItems.isEmpty();
//    }

//    @Override
//    public void setOnSelectionModeToggledListener(OnSelectionModeToggledListener listener) {
//        this.onSelectionModeToggledListener = listener;
//    }
//
//    public interface OnItemToggleListener {
//        void onItemToggle(int position);
//    }

    private static class IdUpdatingAdapterDataObserver extends AdapterDataObserver {
        private FileHolderListAdapter mAdapter;

        IdUpdatingAdapterDataObserver(FileHolderListAdapter adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public void onChanged() {
            mAdapter.refreshIds();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mAdapter.refreshIds();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mAdapter.refreshIds();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mAdapter.refreshIds();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mAdapter.refreshIds();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mAdapter.refreshIds();
        }
    }
}