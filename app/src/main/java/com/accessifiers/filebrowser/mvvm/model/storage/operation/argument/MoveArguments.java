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

import static java.util.Collections.unmodifiableList;

public class MoveArguments extends FileOperation.Arguments {
    @NonNull
    private final List<FileHolder> filesToMove;

    private MoveArguments(@NonNull List<FileHolder> toMove, @NonNull File to) {
        super(to);
        this.filesToMove = toMove;
    }

    public static MoveArguments moveArgs(List<FileHolder> toMove, File to) {
        return new MoveArguments(toMove, to);
    }

    @NonNull
    public List<FileHolder> getFilesToMove() {
        return unmodifiableList(filesToMove);
    }
}
