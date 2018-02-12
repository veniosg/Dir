/*
 * Copyright (C) 2018 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.veniosg.dir.android.adapter.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.mvvm.model.FileHolder;

import static android.view.LayoutInflater.from;
import static com.nostra13.universalimageloader.core.ImageLoader.getInstance;
import static com.veniosg.dir.android.misc.ThumbnailHelper.requestIcon;
import static com.veniosg.dir.android.ui.Themer.getThemedResourceId;

public class FileListViewHolder extends RecyclerView.ViewHolder {
    private ImageView icon;
    private TextView primaryInfo;
    TextView secondaryInfo;
    private TextView tertiaryInfo;

    public FileListViewHolder(ViewGroup parent) {
        super(from(parent.getContext()).inflate(R.layout.item_filelist, parent, false));

        icon = itemView.findViewById(R.id.icon);
        primaryInfo = itemView.findViewById(R.id.primary_info);
        secondaryInfo = itemView.findViewById(R.id.secondary_info);
        tertiaryInfo = itemView.findViewById(R.id.tertiary_info);

        int selectorRes = getThemedResourceId(parent.getContext(), android.R.attr.listChoiceBackgroundIndicator);
        itemView.setBackgroundResource(selectorRes);
    }

    public void bind(FileHolder item, OnItemClickListener listener) {
        Context context = itemView.getContext();
        boolean isDirectory = item.getFile().isDirectory();

        getInstance().cancelDisplayTask(icon);
        primaryInfo.setText(item.getName());
        secondaryInfo.setText(item.getFormattedModificationDate(context));
        // Hide directories' size as it's irrelevant if we can't recursively find it.
        tertiaryInfo.setText(isDirectory ? "" : item.getFormattedSize(context, false));
        icon.setImageDrawable(item.getBestIcon());
        requestIcon(item, icon);

        itemView.setOnClickListener(view -> listener.onClick(itemView, item));
    }

    public interface OnItemClickListener {
        void onClick(View itemView, FileHolder item);
    }
}
