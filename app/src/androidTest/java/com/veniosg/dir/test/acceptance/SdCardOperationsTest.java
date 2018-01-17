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

package com.veniosg.dir.test.acceptance;

import android.content.Context;

import com.veniosg.dir.android.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.createDirectory;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.createFile;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.safAwareDelete;
import static com.veniosg.dir.test.TestUtils.cleanDirectorySaf;

public class SdCardOperationsTest extends FileOperationsTest {
    @Override
    protected File getStorageRoot(Context context) {
        List<String> extPaths = FileUtils.getExtSdCardPaths(context);
        if (extPaths.isEmpty()) {
            throw new IllegalStateException("Can't test SD Card operations without an SD Card attached.");
        }
        return new File(extPaths.get(0));
    }

    @Override
    protected void setUpFiles(Context context, File testDirectory, File testChildDirectory, File testCopyDestination, File testChildFile) throws IOException {
        createDirectory(context, testDirectory);
        createDirectory(context, testChildDirectory);
        createDirectory(context, testCopyDestination);
        createFile(context, testChildFile, "*/*");
    }

    @Override
    protected void tearDownFiles(Context context, File testExtractedDirectory, File testDirectory, File compressedFile) {
        safAwareDelete(context, testExtractedDirectory);
        safAwareDelete(context, compressedFile);
        cleanDirectorySaf(context, testDirectory);
        safAwareDelete(context, testDirectory);
    }
}
