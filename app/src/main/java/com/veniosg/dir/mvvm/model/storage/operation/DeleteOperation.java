/*
 * Copyright (C) 2018 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.veniosg.dir.mvvm.model.storage.operation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.provider.DocumentFile;

import com.veniosg.dir.R;
import com.veniosg.dir.android.fragment.FileListFragment;
import com.veniosg.dir.android.ui.toast.ToastDisplayer;
import com.veniosg.dir.android.util.MediaScannerUtils;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.mvvm.model.storage.operation.argument.DeleteArguments;

import java.io.File;
import java.util.List;

import static com.veniosg.dir.android.util.FileUtils.delete;
import static com.veniosg.dir.android.util.FileUtils.getPathsUnder;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.findFile;

public class DeleteOperation extends FileOperation<DeleteArguments> {
    private final Context context;
    private final ToastDisplayer toastDisplayer;
    private final Handler mainThreadHandler;
    private ProgressDialog dialog;

    public DeleteOperation(Context context) {
        this.mainThreadHandler = new Handler(context.getMainLooper());
        this.toastDisplayer = new ToastDisplayer(context);
        this.context = context.getApplicationContext();

        runOnUi(() -> dialog = new ProgressDialog(context));
    }

    @Override
    public boolean operate(DeleteArguments args) {
        boolean allSucceeded = true;

        for (FileHolder fh : args.getVictims()) {
            File tbd = fh.getFile();
            List<String> paths = getPathsUnder(tbd);

            boolean deleted = delete(tbd);
            allSucceeded &= deleted;

            if (deleted) MediaScannerUtils.informPathsDeleted(context, paths);
        }
        return allSucceeded;
    }

    @Override
    public boolean operateSaf(DeleteArguments args) {
        boolean allSucceeded = true;

        for (FileHolder fh : args.getVictims()) {
            DocumentFile tbd = findFile(context, fh.getFile());
            List<String> paths = getPathsUnder(fh.getFile());

            boolean deleted = tbd != null && tbd.delete();
            allSucceeded &= deleted;

            if (deleted) MediaScannerUtils.informPathsDeleted(context, paths);
        }
        return allSucceeded;
    }

    @Override
    public void onStartOperation(DeleteArguments args) {
        runOnUi(() -> {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.setMessage(context.getString(R.string.deleting));
            dialog.setIndeterminate(true);
            dialog.show();
        });
    }

    @Override
    public void onResult(boolean success, DeleteArguments args) {
        runOnUi(() -> {
            if (success) {
                toastDisplayer.deleteSuccess();
                FileListFragment.refresh(context, args.getTarget());
            } else {
                toastDisplayer.deleteFailure();
            }

            dialog.dismiss();
        });
    }

    @Override
    public void onAccessDenied() {
        runOnUi(() -> dialog.dismiss());
    }

    @Override
    public void onRequestingAccess() {
        runOnUi(() -> dialog.cancel());
    }

    @Override
    public boolean needsWriteAccess() {
        return true;
    }

    private void runOnUi(Runnable runnable) {
        if (Looper.myLooper() != context.getMainLooper()) {
            mainThreadHandler.post(runnable);
        } else {
            runnable.run();
        }
    }
}
