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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.util.MediaScannerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RenameDialog extends BaseDialogFragment {
	private FileHolder mFileHolder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolder = getArguments().getParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_text_input, null);
		final EditText v = (EditText) view.findViewById(R.id.foldername);
		v.setText(mFileHolder.getName());

		v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView text, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO)
					renameTo(text.getText().toString());
				dismiss();
				return true;
			}
		});

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_rename)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                renameTo(v.getText().toString());

                            }
                        })
                .create();
        dialog.setIcon(tintIcon(mFileHolder.getIcon()));
        return dialog;
	}

	private void renameTo(String to){
		boolean res = false;
		
		if(to.length() > 0){
			File from = mFileHolder.getFile();
			File dest = new File(mFileHolder.getFile().getParent(), to);
            List<String> paths = new ArrayList<String>();
            if(from.isDirectory()) {
                MediaScannerUtils.getPathsOfFolder(paths, from);
            }

			if(!dest.exists()){
				res = mFileHolder.getFile().renameTo(dest);

				// Inform media scanner
                if (res) {
                    if (dest.isFile()) {
                        MediaScannerUtils.informFileDeleted(getActivity().getApplicationContext(), from);
                        MediaScannerUtils.informFileAdded(getActivity().getApplicationContext(), dest);
                    } else {
                        MediaScannerUtils.informPathsDeleted(getActivity().getApplicationContext(), paths);
                        MediaScannerUtils.informFolderAdded(getActivity().getApplicationContext(), dest);
                    }
                }
			}
		}
		
		Toast.makeText(getActivity(), res ? R.string.rename_success : R.string.rename_failure, Toast.LENGTH_SHORT).show();
	}
}