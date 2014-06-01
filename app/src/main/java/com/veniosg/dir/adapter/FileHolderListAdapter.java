package com.veniosg.dir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.veniosg.dir.FileManagerApplication;
import com.veniosg.dir.R;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.view.ViewHolder;

import java.util.List;

public class FileHolderListAdapter extends BaseAdapter {
    private List<FileHolder> mItems;
	private int mItemLayoutId = R.layout.item_filelist;

	// Thumbnail specific
    private boolean scrolling = false;
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
	public Object getItem(int position) {
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
    View newView(Context context){
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

		holder.icon.setImageDrawable(item.getBestIcon());
		holder.primaryInfo.setText(item.getName());
		holder.secondaryInfo.setText(item.getFormattedModificationDate(convertView.getContext()));
		// Hide directories' size as it's irrelevant if we can't recursively find it.
		holder.tertiaryInfo.setText(item.getFile().isDirectory()? "" : item.getFormattedSize(
                convertView.getContext(), false));

        // Enable Gmail-like item-selection pattern
        holder.icon.setTag(position);
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemToggleListener != null) {
                    Integer position = (Integer) v.getTag();
                    mOnItemToggleListener.onItemToggle(position);
                }
            }
        });
        // Only override clicks if a listener exists.
        holder.icon.setClickable(mOnItemToggleListener != null);

        if(shouldLoadIcon(item)){
            ((FileManagerApplication) convertView.getContext().getApplicationContext())
                    .getThumbnailLoader().loadImage(item, holder.icon);
        }

		return convertView;
	}

	/**
	 * Inform this adapter about scrolling state of list so that lists don't lag due to cache ops.
	 * @param isScrolling True if the ListView is still scrolling.
	 */
	public void setScrolling(boolean isScrolling){
		scrolling = isScrolling;
		if(!isScrolling)
			notifyDataSetChanged();
	}

    public OnItemToggleListener getOnItemToggleListener() {
        return mOnItemToggleListener;
    }

    public void setOnItemToggleListener(OnItemToggleListener mOnItemToggleListener) {
        this.mOnItemToggleListener = mOnItemToggleListener;
    }

    private boolean shouldLoadIcon(FileHolder item){
		return !scrolling && item.getFile().isFile() && !item.getMimeType().equals("video/mpeg");
	}

    public interface OnItemToggleListener {
        public void onItemToggle(int position);
    }
}