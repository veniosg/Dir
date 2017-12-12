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

package com.veniosg.dir.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.io.File;

import static android.graphics.BitmapFactory.decodeFile;
import static android.view.LayoutInflater.from;
import static com.veniosg.dir.android.util.FileUtils.canExecute;
import static com.veniosg.dir.android.util.Utils.getChildAtFromEnd;
import static com.veniosg.dir.android.util.Utils.getLastChild;
import static com.veniosg.dir.android.util.Utils.isImage;
import static java.lang.String.valueOf;

public class DetailsDialog extends DialogFragment {
	private FileHolder mFileHolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolder = getArguments().getParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER);
	}

    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final File f = mFileHolder.getFile();
		final View v = from(getActivity()).inflate(R.layout.dialog_details, null);
        final ViewGroup container = (ViewGroup) v.findViewById(R.id.details_container);

        boolean isDirectory = f.isDirectory();
        String folderStr = getString(R.string.details_type_folder);
        String otherStr = getString(R.string.details_type_other);
		String perms = (f.canRead() ? "R" : "-") + (f.canWrite() ? "W" : "-") + (canExecute(f) ? "X" : "-");
        String mimeType = mFileHolder.getMimeType();

        String typeValue = isDirectory ? folderStr : (f.isFile() ? mimeType : otherStr);
        String hiddenValue = getString(f.isHidden() ? R.string.yes : R.string.no);
        String lastModifiedValue = mFileHolder
                .getFormattedModificationDate(getActivity()).toString();

        addSizeDetailsItem(container);
        addDetailsItem(container, R.string.details_type, typeValue);
        if (isDirectory) {
            String[] fList = f.list();
            if (fList != null) {
                addDetailsItem(container, R.string.details_items, valueOf(fList.length));
            }
        } else if (isImage(mimeType)) {
            addResolutionDetailsItem(container);
        }
        addDetailsItem(container, R.string.details_lastmodified, lastModifiedValue);
        addDetailsItem(container, R.string.details_hidden, hiddenValue);
        addDetailsItem(container, R.string.details_permissions, perms);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(mFileHolder.getName())
                .setView(v)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dismiss();
                            }
                        }
                )
                .create();
        return dialog;
	}

    private void addDetailsItem(ViewGroup container, int titleResId, String value) {
        from(container.getContext()).inflate(R.layout.item_details, container);

        ((TextView) getChildAtFromEnd(container, 1)).setText(titleResId);
        ((TextView) getLastChild(container)).setText(value);
    }

    private void addSizeDetailsItem(ViewGroup container) {
        from(container.getContext()).inflate(R.layout.item_details, container);
        TextView valueView = (TextView) getLastChild(container);

        ((TextView) getChildAtFromEnd(container, 1)).setText(R.string.details_size);
        valueView.setText(R.string.loading);
        new SizeRefreshTask((TextView) getLastChild(container)).execute();
    }

    private void addResolutionDetailsItem(ViewGroup container) {
        from(container.getContext()).inflate(R.layout.item_details, container);
        TextView valueView = (TextView) getLastChild(container);

        ((TextView) getChildAtFromEnd(container, 1)).setText(R.string.details_resolution);
        valueView.setText(R.string.loading);
        new ResolutionRefreshTask(valueView).execute();
    }

    private abstract class RefreshTask extends AsyncTask<Void, Void, String> {
        private final TextView mView;

        RefreshTask(TextView view) {
            mView = view;
        }

        @Override
        protected void onPostExecute(String result) {
            mView.setText(result);
        }
    }

    private class SizeRefreshTask extends RefreshTask {
        SizeRefreshTask(TextView view) {
            super(view);
        }

        @Override
        protected String doInBackground(Void... params) {
            return mFileHolder.getFormattedSize(getActivity(), true);
        }
    }

    private class ResolutionRefreshTask extends RefreshTask {
        ResolutionRefreshTask(TextView view) {
            super(view);
        }

        @Override
        protected String doInBackground(Void... params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            decodeFile(mFileHolder.getFile().getAbsolutePath(), options);
            return "" + options.outHeight + 'x' + options.outWidth;
        }
    }
}
