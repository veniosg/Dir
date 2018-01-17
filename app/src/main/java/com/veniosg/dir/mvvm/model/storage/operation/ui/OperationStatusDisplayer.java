package com.veniosg.dir.mvvm.model.storage.operation.ui;

import java.io.File;

public interface OperationStatusDisplayer {
    void showCopyProgress(int operationId, File destDir, File copying, int progress, int max);
    void showCopySuccess(int operationId, File destDir);
    void showCopyFailure(int operationId, File destDir);

    void showMoveProgress(int operationId, File destDir, File moving, int progress, int max);
    void showMoveSuccess(int operationId, File destDir);
    void showMoveFailure(int operationId, File destDir);
}
