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
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static com.veniosg.dir.test.TestUtils.cleanDirectory;

public class InternalStorageOperationsTest extends FileOperationsTest {
    @Override
    protected File getStorageRoot(Context context) {
        return Environment.getExternalStorageDirectory();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void setUpFiles(Context context, File testDirectory, File testChildDirectory,
                              File testCopyDestination, File testChildFile) throws IOException {
        testDirectory.mkdir();
        testChildDirectory.mkdir();
        testCopyDestination.mkdir();
        testChildFile.createNewFile();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void tearDownFiles(Context context, File testExtractedDirectory, File testDirectory, File compressedFile) {
        if (testExtractedDirectory.exists()) {
            testExtractedDirectory.renameTo(new File(testDirectory, "extracted"));
        }
        if (compressedFile.exists()) {
            compressedFile.renameTo(new File(testDirectory, "compressed"));
        }
        cleanDirectory(testDirectory);
        testDirectory.delete();
    }
}
