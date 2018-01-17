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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.android.ui.ViewHolder;

import java.util.List;

import static com.nostra13.universalimageloader.core.ImageLoader.getInstance;
import static com.veniosg.dir.android.misc.ThumbnailHelper.requestIcon;

public class FileHolderListAdapter extends BaseAdapter {
    private List<FileHolder> mItems;
	private int mItemLayoutId = R.layout.item_filelist;

	private OnItemToggleListener mOnItemToggleListener;

    public FileHolderListAdapter(List<FileHolder> files){
		mItems = files;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
    @Nullable
	public Object getItem(int position) {
        if (position < 0 || position >= mItems.size()) return null;

		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Set the layout to be used for item drawing.
	 * @param resId The item layout id. 0 to reset.
	 */
	public void setItemLayout(int resId){
		if(resId > 0)
			mItemLayoutId = resId;
		else
			mItemLayoutId = R.layout.item_filelist;
	}

	/**
	 * Creates a new list item view, along with it's ViewHolder set as a tag.
	 * @return The new view.
	 */
	private View newView(Context context){
		View view = LayoutInflater.from(context).inflate(mItemLayoutId, null);

		ViewHolder holder = new ViewHolder();
		holder.icon = (ImageView) view.findViewById(R.id.icon);
		holder.primaryInfo = (TextView) view.findViewById(R.id.primary_info);
		holder.secondaryInfo = (TextView) view.findViewById(R.id.secondary_info);
		holder.tertiaryInfo = (TextView) view.findViewById(R.id.tertiary_info);

		view.setTag(holder);
		return view;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileHolder item = mItems.get(position);
		if(convertView == null)
			convertView = newView(parent.getContext());
		ViewHolder holder = (ViewHolder) convertView.getTag();

        getInstance().cancelDisplayTask(holder.icon);
		holder.icon.setImageDrawable(item.getBestIcon());
		holder.primaryInfo.setText(item.getName());
		holder.secondaryInfo.setText(item.getFormattedModificationDate(convertView.getContext()));
		// Hide directories' size as it's irrelevant if we can't recursively find it.
		holder.tertiaryInfo.setText(item.getFile().isDirectory()? "" : item.getFormattedSize(
                convertView.getContext(), false));

        requestIcon(item, holder.icon);

		return convertView;
	}

    public OnItemToggleListener getOnItemToggleListener() {
        return mOnItemToggleListener;
    }

    public void setOnItemToggleListener(OnItemToggleListener mOnItemToggleListener) {
        this.mOnItemToggleListener = mOnItemToggleListener;
    }

    private boolean shouldLoadIcon(FileHolder item){
		return item.getFile().isFile() && !item.getMimeType().equals("video/mpeg");
	}

    public interface OnItemToggleListener {
        public void onItemToggle(int position);
    }
}