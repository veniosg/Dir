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
import com.accessifiers.filebrowser.mvvm.model.storage.operation.CreateDirectoryOperation;

import java.io.File;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static com.accessifiers.filebrowser.IntentConstants.EXTRA_DIR_PATH;
import static com.accessifiers.filebrowser.mvvm.model.storage.operation.FileOperationRunnerInjector.operationRunner;
import static com.accessifiers.filebrowser.mvvm.model.storage.operation.argument.CreateDirectoryArguments.createDirectoryArguments;

public class CreateDirectoryDialog extends DialogFragment {
    private CharSequence text;
    private Context c;

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
        v.setHint(R.string.folder_name);

        v.setOnEditorActionListener((text, actionId, event) -> {
            if (actionId == IME_ACTION_GO) createFolder(text.getText(), getActivity());
            return true;
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_new_folder)
                .setView(view)
                .setPositiveButton(android.R.string.ok,
                        (dialog1, which) -> createFolder(v.getText(), getActivity())
                )
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setIcon(R.drawable.ic_dialog_folder);
        return dialog;
    }

    private void createFolder(final CharSequence text, Context c) {
        if (text.length() != 0) {
            //noinspection ConstantConditions
            File parentPath = new File(getArguments().getString(EXTRA_DIR_PATH));
            File tbc = new File(parentPath, text.toString());
            if (!tbc.exists()) {
                operationRunner(c).run(new CreateDirectoryOperation(c), createDirectoryArguments(tbc));
                dismiss();
            }
        }
    }
}