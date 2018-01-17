package com.veniosg.dir.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.io.File;

import static android.view.LayoutInflater.from;
import static com.veniosg.dir.android.misc.ThumbnailHelper.requestIcon;
import static com.veniosg.dir.android.ui.Themer.getThemedResourceId;

public class FileListViewHolder extends RecyclerView.ViewHolder {
    private ImageView icon;
    private TextView primaryInfo;
    TextView secondaryInfo;
    private TextView tertiaryInfo;

    FileListViewHolder(ViewGroup parent) {
        super(from(parent.getContext()).inflate(R.layout.item_filelist, parent, false));

        icon = (ImageView) itemView.findViewById(R.id.icon);
        primaryInfo = (TextView) itemView.findViewById(R.id.primary_info);
        secondaryInfo = (TextView) itemView.findViewById(R.id.secondary_info);
        tertiaryInfo = (TextView) itemView.findViewById(R.id.tertiary_info);

        int selectorRes = getThemedResourceId(parent.getContext(), android.R.attr.listChoiceBackgroundIndicator);
        itemView.setBackgroundResource(selectorRes);
    }

    void bind(String filePath, OnItemClickListener listener) {
        Context context = itemView.getContext();
        FileHolder item = new FileHolder(new File(filePath), context);
        boolean isDirectory = item.getFile().isDirectory();

        primaryInfo.setText(item.getName());
        secondaryInfo.setText(item.getFormattedModificationDate(context));
        tertiaryInfo.setText(isDirectory ? "" : item.getFormattedSize(context, false));
        icon.setImageDrawable(item.getBestIcon());
        requestIcon(item, icon);

        itemView.setOnClickListener(view -> listener.onClick(itemView, item));
    }

    public interface OnItemClickListener {
        public void onClick(View itemView, FileHolder item);
    }
}
