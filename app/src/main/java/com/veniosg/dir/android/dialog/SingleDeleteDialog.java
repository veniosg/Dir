/*
 * Copyright (C) 2018 George Venios
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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.veniosg.dir.R;
import com.veniosg.dir.mvvm.model.FileHolder;

import static com.veniosg.dir.IntentConstants.EXTRA_DIALOG_FILE_HOLDER;

public class SingleDeleteDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FileHolder holder = getArguments().getParcelable(EXTRA_DIALOG_FILE_HOLDER);

        if (holder != null) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.really_delete, holder.getName()))
                    .setPositiveButton(R.string.yes, (dialog1, which) -> {
                        new DeleteAsyncTask(getContext()).execute(holder);
                    })
                    .setNegativeButton(R.string.no, null)
                    .create();
            dialog.setIcon(R.drawable.ic_dialog_delete);
            return dialog;
        } else {
            dismiss();
            return new Dialog(getContext());
        }
    }
}