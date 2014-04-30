package com.veniosg.dir.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.view.ViewHolder;

import java.util.List;

/**
 * Simple adapter for displaying search results.
 *
 * @author George Venios
 */
public class SearchListAdapter extends FileHolderListAdapter {

    public SearchListAdapter(List<FileHolder> files) {
        super(files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ((ViewHolder) view.getTag()).secondaryInfo.setMaxLines(3);
        ((ViewHolder) view.getTag()).secondaryInfo.setText(
                ((FileHolder) getItem(position)).getFile().getAbsolutePath());

        return view;
    }
}