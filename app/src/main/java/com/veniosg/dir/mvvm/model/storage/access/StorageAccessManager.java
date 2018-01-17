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

import android.support.annotation.NonNull;

import java.io.File;

public interface StorageAccessManager {
    boolean hasWriteAccess(@NonNull File fileInStorage);
    void requestWriteAccess(@NonNull File fileInStorage, @NonNull AccessPermissionListener listener);
    boolean isSafBased();

    interface AccessPermissionListener {
        void granted();
        void denied();

        /**
         * Called when the grant succeeded but the file is still not writable.
         * This can mean that the grant happened on the wrong directory.
         */
        void error();
    }
}
