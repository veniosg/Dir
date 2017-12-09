package com.veniosg.dir.android.adapter;

import android.view.ViewGroup;

public class SearchListViewHolder extends FileListViewHolder {
    SearchListViewHolder(ViewGroup parent) {
        super(parent);
    }

    @Override
    void bind(String filePath, OnItemClickListener listener) {
        super.bind(filePath, listener);
        secondaryInfo.setText(filePath);
    }
}
