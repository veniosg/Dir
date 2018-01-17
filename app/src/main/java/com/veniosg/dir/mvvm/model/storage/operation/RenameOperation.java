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

import android.content.Context;
import android.support.v4.provider.DocumentFile;

import com.veniosg.dir.android.fragment.FileListFragment;
import com.veniosg.dir.android.ui.toast.ToastDisplayer;
import com.veniosg.dir.android.util.MediaScannerUtils;
import com.veniosg.dir.mvvm.model.storage.operation.argument.RenameArguments;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.veniosg.dir.android.util.MediaScannerUtils.getPathsOfFolder;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.findFile;

public class RenameOperation extends FileOperation<RenameArguments> {
    private final Context context;
    private final ToastDisplayer toastDisplayer;
    private List<String> affectedPaths = new ArrayList<>();

    public RenameOperation(Context context, ToastDisplayer toastDisplayer) {
        this.context = context;
        this.toastDisplayer = toastDisplayer;
    }

    @Override
    public boolean operate(RenameArguments args) {
        File from = args.getFileToRename();
        File dest = args.getTarget();

        return dest.exists() || from.renameTo(dest);
    }

    @Override
    public boolean operateSaf(RenameArguments args) {
        File from = args.getFileToRename();
        File dest = args.getTarget();

        if (dest.exists()) {
            return true;
        } else {
            DocumentFile safFrom = findFile(context, from);
            return safFrom != null && safFrom.renameTo(args.getTarget().getName());
        }
    }

    @Override
    protected void onStartOperation(RenameArguments args) {
        File from = args.getFileToRename();
        if (from.isDirectory()) {
            getPathsOfFolder(affectedPaths, from);
        } else {
            affectedPaths.add(from.getAbsolutePath());
        }
    }

    @Override
    protected void onResult(boolean success, RenameArguments args) {
        if (success) {
            toastDisplayer.renameSuccess();

            File dest = args.getTarget();
            FileListFragment.refresh(context, dest.getParentFile());
            MediaScannerUtils.informPathsDeleted(context, affectedPaths);
            if (dest.isFile()) {
                MediaScannerUtils.informFileAdded(context, dest);
            } else {
                MediaScannerUtils.informFolderAdded(context, dest);
            }
        } else {
            toastDisplayer.renameFailure();
        }
    }

    @Override
    protected void onAccessDenied() {
    }

    @Override
    protected void onRequestingAccess() {
    }

    @Override
    public boolean needsWriteAccess() {
        return true;
    }
}
