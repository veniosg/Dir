package com.veniosg.dir.mvvm.model.storage.operation.ui;

import android.content.Context;

import java.io.File;

public abstract class OperationStatusDisplayerInjector {
    private static final OperationStatusDisplayer NO_OP_DISPLAYER = new OperationStatusDisplayer() {
        @Override
        public void showCopySuccess(int operationId, File destDir) {
        }

        @Override
        public void showCopyFailure(int operationId, File destDir) {
        }

        @Override
        public void showMoveProgress(int operationId, File destDir, File moving, int progress, int max) {
        }

        @Override
        public void showMoveSuccess(int operationId, File destDir) {
        }

        @Override
        public void showMoveFailure(int operationId, File destDir) {
        }

        @Override
        public void showCopyProgress(int operationId, File destDir, File copying, int progress, int max) {
        }
    };

    private OperationStatusDisplayerInjector() {
    }

    public static OperationStatusDisplayer operationStatusDisplayer(Context context) {
        return new NotificationOperationStatusDisplayer(context);
    }

    public static OperationStatusDisplayer noOpStatusDisplayer() {
        return NO_OP_DISPLAYER;
    }
}
