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
import android.widget.Toast;

import com.accessifiers.filebrowser.IntentConstants;
import com.accessifiers.filebrowser.R;
import com.accessifiers.filebrowser.android.ui.toast.ToastDisplayer;
import com.accessifiers.filebrowser.mvvm.model.FileHolder;
import com.accessifiers.filebrowser.mvvm.model.storage.operation.RenameOperation;

import java.io.File;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static com.accessifiers.filebrowser.mvvm.model.storage.operation.FileOperationRunnerInjector.operationRunner;
import static com.accessifiers.filebrowser.mvvm.model.storage.operation.argument.RenameArguments.renameArguments;

public class RenameDialog extends DialogFragment {
    private FileHolder mFileHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileHolder = getArguments().getParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_text_input, null);
        final EditText v = view.findViewById(R.id.textinput);
        v.setText(mFileHolder.getName());

        v.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == IME_ACTION_GO) {
                tryRename(tv.getText().toString());
            }
            return true;
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_rename)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok,
                        (dialog1, which) -> tryRename(v.getText().toString()))
                .create();
        dialog.setIcon(mFileHolder.getIcon());
        return dialog;
    }

    private void tryRename(String newName) {
        File destFile = new File(mFileHolder.getFile().getParent(), newName);
        if (destFile.exists()) {
            Toast.makeText(getContext(), R.string.file_exists, Toast.LENGTH_SHORT).show();
        } else {
            renameTo(destFile.getName());
            dismiss();
        }
    }

    private void renameTo(String newName) {
        boolean res = false;

        if (newName.length() > 0) {
            Context c = getContext();
            operationRunner(c).run(
                    new RenameOperation(c, new ToastDisplayer(c)),
                    renameArguments(mFileHolder.getFile(), newName));
        }
    }
}