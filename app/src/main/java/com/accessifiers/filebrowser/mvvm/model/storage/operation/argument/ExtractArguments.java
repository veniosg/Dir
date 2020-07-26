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

package com.accessifiers.filebrowser.mvvm.model.storage.operation.argument;

import android.support.annotation.NonNull;

import com.accessifiers.filebrowser.mvvm.model.FileHolder;
import com.accessifiers.filebrowser.mvvm.model.storage.operation.FileOperation;

import java.io.File;
import java.util.List;

public class ExtractArguments extends FileOperation.Arguments {
    @NonNull
    private final List<FileHolder> zipFiles;

    private ExtractArguments(@NonNull File target, @NonNull List<FileHolder> zipFiles) {
        super(target);
        this.zipFiles = zipFiles;
    }

    public static ExtractArguments extractArgs(@NonNull File target, @NonNull List<FileHolder> zipFiles) {
        return new ExtractArguments(target, zipFiles);
    }

    @NonNull
    public List<FileHolder> getZipFiles() {
        return zipFiles;
    }
}