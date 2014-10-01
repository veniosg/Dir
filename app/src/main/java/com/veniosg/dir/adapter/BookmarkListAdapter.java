package com.veniosg.dir.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.veniosg.dir.FileManagerApplication;
import com.veniosg.dir.R;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.util.Utils;
import com.veniosg.dir.view.ViewHolder;

import java.io.File;

/**
 * @author George Venios
 */
public class BookmarkListAdapter extends CursorAdapter {
	// Thumbnail specific
    private boolean scrolling = false;
    
	public BookmarkListAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
	}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder viewHolder;
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_filelist, null);

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
        ViewHolder holder = (com.veniosg.dir.view.ViewHolder) convertView.getTag();
        FileHolder item = new FileHolder(new File(cursor.getString(2)), convertView.getContext());

        holder.icon.setImageDrawable(item.getBestIcon());
        holder.primaryInfo.setText(item.getName());
        holder.secondaryInfo.setMaxLines(3);
        holder.secondaryInfo.setText(item.getFile().getAbsolutePath());
        // Hide directories' size as it's irrelevant if we can't recursively find it.
        holder.tertiaryInfo.setText(item.getFile().isDirectory()? "" : item.getFormattedSize(
                convertView.getContext(), false));

        if(shouldLoadIcon(item)){
            Utils.loadThumbnail(convertView.getContext(), item.getFile(), holder.icon, item.getBestIcon());
        }
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

	private boolean shouldLoadIcon(FileHolder item){
		return !scrolling && item.getFile().isFile() && !item.getMimeType().equals("video/mpeg");
	}
}