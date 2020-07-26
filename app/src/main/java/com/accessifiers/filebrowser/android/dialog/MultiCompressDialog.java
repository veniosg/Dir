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

package com.accessifiers.filebrowser.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.accessifiers.filebrowser.R;
import com.accessifiers.filebrowser.android.dialog.OverwriteFileDialog.Overwritable;
import com.accessifiers.filebrowser.android.service.ZipService;
import com.accessifiers.filebrowser.mvvm.model.FileHolder;

import java.io.File;
import java.util.List;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_DIALOG_FILE_HOLDER;

public class MultiCompressDialog extends DialogFragment implements Overwritable {
	private List<FileHolder> mFileHolders;
	private Context appContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolders = getArguments().getParcelableArrayList(EXTRA_DIALOG_FILE_HOLDER);
		if (getContext() != null) appContext = getContext().getApplicationContext();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
	
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_text_input, null);
		final EditText v = view.findViewById(R.id.textinput);
		v.setHint(R.string.compressed_file_name);
		
		v.setOnEditorActionListener((text, actionId, event) -> {
               if (actionId == IME_ACTION_GO) compress(v.getText().toString());
               dismiss();
               return true;
            });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_compress)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
						(dialog1, which) -> compress(v.getText().toString()))
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        return dialog;
	}
	
	private void compress(final String zipname){
		this.zipname = zipname;
		tbcreated = new File(mFileHolders.get(0).getFile().getParent() + File.separator + zipname + ".zip");
		if (tbcreated.exists()) {
			this.zipname = zipname;
			OverwriteFileDialog dialog = new OverwriteFileDialog();
			dialog.setTargetFragment(this, 0);
			dialog.show(getFragmentManager(), "OverwriteFileDialog");
		} else {
			Context context = getContext();
			if (context == null) context = appContext;
			ZipService.compressTo(context, mFileHolders, tbcreated);
		}
	}

	private File tbcreated;
	private String zipname;
	
	@Override
	public void overwrite() {
		tbcreated.delete();
		compress(zipname);
	}
}