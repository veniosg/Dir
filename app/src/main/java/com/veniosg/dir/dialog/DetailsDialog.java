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

package com.veniosg.dir.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.util.FileUtils;

import java.io.File;

public class DetailsDialog extends BaseDialogFragment {
	private FileHolder mFileHolder;
	private TextView mSizeView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolder = getArguments().getParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER);
	}

    @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		File f = mFileHolder.getFile();
		// Inflate the view to display
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View v = inflater.inflate(R.layout.dialog_details, null);
		
		// Fill the views
		((TextView) v.findViewById(R.id.details_type_value)).setText((f.isDirectory() ? R.string.details_type_folder :
																		(f.isFile() ? R.string.details_type_file : R.string.details_type_other) ));
		
		mSizeView = (TextView) v.findViewById(R.id.details_size_value);
		new SizeRefreshTask().execute();
		
		String perms = (f.canRead() ? "R" : "-") + (f.canWrite() ? "W" : "-") + (FileUtils.canExecute(f) ? "X" : "-");
		((TextView) v.findViewById(R.id.details_permissions_value)).setText(perms);
		
		((TextView) v.findViewById(R.id.details_hidden_value)).setText(f.isHidden() ? R.string.yes : R.string.no);
		
		((TextView) v.findViewById(R.id.details_lastmodified_value)).setText(
                mFileHolder.getFormattedModificationDate(getActivity()));
		
		// Finally create the dialog
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
        dialog.setIcon(tintIcon(mFileHolder.getIcon()));
        return dialog;
	}
    /**
	 * This task doesn't update the text viewed to the user until it's finished, 
	 * so that the user knows the size he sees is indeed the final one.
	 * 
	 * @author George Venios
	 *
	 */
	private class SizeRefreshTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			mSizeView.setText(R.string.loading);
		}
		
		@Override
		protected String doInBackground(Void... params) {
			return mFileHolder.getFormattedSize(getActivity(), true);
		}
		
		@Override
		protected void onPostExecute(String result) {
			mSizeView.setText(result);
		}
	}
}