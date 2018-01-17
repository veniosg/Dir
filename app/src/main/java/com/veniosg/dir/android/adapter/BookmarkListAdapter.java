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

package com.veniosg.dir.android.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.android.misc.ThumbnailHelper;
import com.veniosg.dir.android.ui.ViewHolder;

import java.io.File;

import static android.view.View.INVISIBLE;

public class BookmarkListAdapter extends CursorAdapter {
	public BookmarkListAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
	}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = LayoutInflater.from(context).inflate(R.layout.item_filelist, null);

        viewHolder = new ViewHolder();
        viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
        viewHolder.primaryInfo = (TextView) view.findViewById(R.id.primary_info);
        viewHolder.secondaryInfo = (TextView) view.findViewById(R.id.secondary_info);
        viewHolder.tertiaryInfo = (TextView) view.findViewById(R.id.tertiary_info);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View convertView, Context context, Cursor cursor) {
        ViewHolder holder = (com.veniosg.dir.android.ui.ViewHolder) convertView.getTag();
        FileHolder item = new FileHolder(new File(cursor.getString(2)), convertView.getContext());

        holder.icon.setImageDrawable(item.getBestIcon());
        holder.primaryInfo.setText(item.getName());
        holder.secondaryInfo.setMaxLines(3);
        holder.secondaryInfo.setText(item.getFile().getAbsolutePath());
        holder.tertiaryInfo.setVisibility(INVISIBLE);

        ThumbnailHelper.requestIcon(item, holder.icon);
    }
}
