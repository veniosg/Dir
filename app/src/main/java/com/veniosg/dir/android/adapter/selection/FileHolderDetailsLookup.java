package com.veniosg.dir.android.adapter.selection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.MotionEvent;
import android.view.View;

import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder;

import androidx.recyclerview.selection.ItemDetailsLookup;

public class FileHolderDetailsLookup extends ItemDetailsLookup<Long> {
    @NonNull
    private final RecyclerView mRecyclerView;

    public FileHolderDetailsLookup(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<Long> getItemDetails(@NonNull MotionEvent e) {
        View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            ViewHolder holder = mRecyclerView.getChildViewHolder(view);
            if (holder instanceof FileListViewHolder) {
                return ((FileListViewHolder) holder).getItemDetails();
            }
        }
        return null;

    }
}
