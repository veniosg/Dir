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

import com.veniosg.dir.android.fragment.FileListFragment;
import com.veniosg.dir.android.ui.toast.ToastDisplayer;
import com.veniosg.dir.mvvm.model.storage.operation.argument.CreateDirectoryArguments;

import java.io.File;

import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.createDirectory;

public class CreateDirectoryOperation extends FileOperation<CreateDirectoryArguments> {
    private final Context context;
    private final ToastDisplayer toastDisplayer;

    public CreateDirectoryOperation(Context context) {
        this.context = context;
        this.toastDisplayer = new ToastDisplayer(context);
    }

    @Override
    public boolean operate(CreateDirectoryArguments args) {
        File dest = args.getTarget();

        return dest.exists() || dest.mkdirs();
    }

    @Override
    public boolean operateSaf(CreateDirectoryArguments args) {
        File dest = args.getTarget();

        return dest.exists() || createDirectory(context, dest) != null;
    }

    @Override
    public void onStartOperation(CreateDirectoryArguments args) {
    }

    @Override
    public void onResult(boolean success, CreateDirectoryArguments args) {
        if (success) {
            toastDisplayer.createDirectorySuccess();
            FileListFragment.refresh(context, args.getTarget().getParentFile());
        } else {
            toastDisplayer.createDirectoryFailure();
        }
    }

    @Override
    public void onAccessDenied() {
    }

    @Override
    public void onRequestingAccess() {
    }

    @Override
    public boolean needsWriteAccess() {
        return true;
    }
}
