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

import com.veniosg.dir.android.ui.toast.ToastDisplayer;
import com.veniosg.dir.mvvm.model.storage.access.StorageAccessManager;
import com.veniosg.dir.mvvm.model.storage.access.StorageAccessManager.AccessPermissionListener;

/**
 * Manages a {@link FileOperation} instance's write access to different kinds of storage
 * devices and internally handles write access requests needed for the operation to succeed no
 * matter the location of the files being operated on.
 */
public class FileOperationRunner {
    private final StorageAccessManager storageAccessManager;
    private final ToastDisplayer toastDisplayer;

    FileOperationRunner(StorageAccessManager storageAccessManager, ToastDisplayer toastDisplayer) {
        this.storageAccessManager = storageAccessManager;
        this.toastDisplayer = toastDisplayer;
    }

    public <O extends FileOperation<A>, A extends FileOperation.Arguments> void run(O operation, A args) {
        operation.onStartOperation(args);
        boolean success = operation.operate(args);
        boolean failedButNeedsAccess = !success && operation.needsWriteAccess();
        if (failedButNeedsAccess) {
            if (storageAccessManager.hasWriteAccess(args.getTarget())) {
                if (storageAccessManager.isSafBased()) {
                    success = operation.operateSaf(args);
                }
                operation.onResult(success, args);
            } else {
                operation.onRequestingAccess();
                storageAccessManager.requestWriteAccess(args.getTarget(), new AccessPermissionListener() {
                    @Override
                    public void granted() {
                        run(operation, args);
                    }

                    @Override
                    public void denied() {
                        operation.onAccessDenied();
                    }

                    @Override
                    public void error() {
                        toastDisplayer.grantAccessWrongDirectory();
                        run(operation, args);
                    }
                });
            }
        } else {
            operation.onResult(success, args);
        }
    }
}
