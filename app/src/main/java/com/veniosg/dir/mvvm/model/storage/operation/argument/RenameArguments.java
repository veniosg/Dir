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

package com.veniosg.dir.mvvm.model.storage.operation.argument;

import android.support.annotation.NonNull;

import com.veniosg.dir.mvvm.model.storage.operation.FileOperation;

import java.io.File;

public class RenameArguments extends FileOperation.Arguments {
    @NonNull
    private final File fileToRename;

    private RenameArguments(@NonNull File fileToRename, @NonNull String newName) {
        super(new File(fileToRename.getParent(), newName));
        this.fileToRename = fileToRename;
    }

    public static RenameArguments renameArguments(@NonNull File fileToRename, @NonNull String newName) {
        return new RenameArguments(fileToRename, newName);
    }

    @NonNull
    public File getFileToRename() {
        return fileToRename;
    }
}