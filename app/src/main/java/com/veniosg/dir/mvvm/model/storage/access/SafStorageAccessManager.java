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

package com.veniosg.dir.mvvm.model.storage.access;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriPermission;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.provider.DocumentFile;

import com.veniosg.dir.android.activity.SafPromptActivity;

import java.io.File;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.support.v4.provider.DocumentFile.fromTreeUri;
import static com.veniosg.dir.IntentConstants.ACTION_STORAGE_ACCESS_RESULT;
import static com.veniosg.dir.IntentConstants.EXTRA_STORAGE_ACCESS_GRANTED;
import static com.veniosg.dir.android.util.FileUtils.getExternalStorageRoot;
import static com.veniosg.dir.android.util.FileUtils.isWritable;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.areSameFile;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.createFile;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.safAwareDelete;
import static java.lang.String.format;
import static java.util.Locale.ROOT;

/**
 * Uses the Storage Access Framework to request and persist access permissions to external storage.
 */
class SafStorageAccessManager implements StorageAccessManager {
    private final Context context;

    SafStorageAccessManager(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean hasWriteAccess(@NonNull File fileInStorage) {
        boolean grantedBefore = permissionGrantedForParentOf(fileInStorage);

        return grantedBefore || checkWriteAccess(fileInStorage);
    }

    @Override
    public void requestWriteAccess(@NonNull final File fileInStorage,
                                   @NonNull final AccessPermissionListener listener) {
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // We're assuming broadcast will happen in onDestroy(), and only once, regardless of SAF result
                localBroadcastManager.unregisterReceiver(this);

                if (hasWriteAccess(fileInStorage)) {
                    listener.granted();
                } else {
                    boolean granted = intent.getBooleanExtra(EXTRA_STORAGE_ACCESS_GRANTED, false);

                    if (!granted) {
                        listener.denied();
                    } else {
                        listener.error();
                    }
                }
            }
        }, new IntentFilter(ACTION_STORAGE_ACCESS_RESULT));
        Intent safPromptIntent = new Intent(context, SafPromptActivity.class);
        safPromptIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(safPromptIntent);
    }

    @Override
    public boolean isSafBased() {
        return true;
    }

    private boolean permissionGrantedForParentOf(@NonNull File fileInStorage) {
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();

        for (UriPermission permission : permissions) {
            String storageRoot = getExternalStorageRoot(fileInStorage, context);
            DocumentFile grantedDocFile = fromTreeUri(context, permission.getUri());
            boolean grantedOnAncestor = areSameFile(storageRoot, grantedDocFile);
            if (permission.isWritePermission() && grantedOnAncestor) return true;
        }
        return false;
    }

    private boolean checkWriteAccess(File fileInStorage) {
        File fileParent = fileInStorage.getParentFile();
        // Reached root, can't write
        if (fileParent == null) return false;
        // Recur until we find a parent that exists
        if (!fileParent.exists()) return checkWriteAccess(fileParent);

        File tmpFile = generateDummyFileIn(fileParent);

        boolean writable = false;
        if (isWritable(tmpFile)) writable = true;

        DocumentFile document;
        if (!writable) {
            // Java said not writable, confirm with SAF
            document = createFile(context, tmpFile, "image/png");

            if (document != null) {
                writable = document.canWrite() && tmpFile.exists();
            }
        }

        // Cleanup
        safAwareDelete(context, tmpFile);
        return writable;
    }

    @NonNull
    private File generateDummyFileIn(File parent) {
        File dummyFile;
        int i = 0;
        do {
            String fileName = format(ROOT, "WriteAccessCheck%d", i++);
            dummyFile = new File(parent, fileName);
        } while (dummyFile.exists());
        return dummyFile;
    }
}
