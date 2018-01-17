package com.veniosg.dir.mvvm.model.storage.operation;

import android.content.Context;

import com.veniosg.dir.android.ui.toast.ToastDisplayer;
import com.veniosg.dir.mvvm.model.storage.access.ExternalStorageAccessManager;

public abstract class FileOperationRunnerInjector {
    private FileOperationRunnerInjector() {
    }

    /**
     * Builds a default instance of {@link FileOperationRunner}.
     */
    public static FileOperationRunner operationRunner(Context c) {
        return new FileOperationRunner(new ExternalStorageAccessManager(c), new ToastDisplayer(c));
    }
}
