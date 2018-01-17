/*
 * Copyright (C) 2014 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.veniosg.dir.test;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.safAwareDelete;

public abstract class TestUtils {
    private TestUtils() {
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cleanDirectory(File file) {
        if (!file.exists()) return;
        for (String name : file.list()) {
            File child = new File(file, name);
            if (child.isDirectory()) {
                cleanDirectory(child);
            } else {
                child.delete();
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Deletion of " + file + " failed");
        }
    }

    public static void cleanDirectorySaf(Context context, File file) {
        if (!file.exists()) return;
        for (String name : file.list()) {
            File child = new File(file, name);
            if (child.isDirectory()) {
                cleanDirectorySaf(context, child);
            } else {
                safAwareDelete(context, file);
            }
        }
        if (!safAwareDelete(context, file)) {
            throw new RuntimeException("Deletion of " + file + " failed");
        }
    }

    public static void createFile(String path, String content) throws IOException {
        File file = new File(path);
        if (file.createNewFile() && content != null && !content.isEmpty()) {
            FileWriter wr = new FileWriter(file);
            wr.write(content);
            wr.close();
        }
    }

    public static void createDirectory(String path) throws IOException {
        File file = new File(path);
        file.mkdir();
        if (!file.exists())
            throw new IOException("Creation of " + path + " failed");
    }

    public static void deleteDirectory(String path) {
        File file = new File(path);
        if (file.exists())
            if (file.isDirectory())
                cleanDirectory(file);
        file.delete();
    }
}
