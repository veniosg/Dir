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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class TestUtils {
    private TestUtils(){}

    protected static void cleanDirectory(File file) {
        if(!file.exists()) return;
        for(String name:file.list()) {
            if(!name.startsWith("oi-") && !name.startsWith(".oi-")) {
                throw new RuntimeException(file + " contains unexpected file");
            }
            File child = new File(file, name);
            if(child.isDirectory())
                cleanDirectory(child);
            else
                child.delete();
        }
        file.delete();
        if(file.exists()) {
            throw new RuntimeException("Deletion of " + file + " failed");
        }
    }

    protected static void createFile(String path, String content) throws IOException {
        File file = new File(path);
        FileWriter wr = new FileWriter(file);
        wr.write(content);
        wr.close();
    }

    protected static void createDirectory(String path) throws IOException {
        File file = new File(path);
        file.mkdir();
        if(!file.exists())
            throw new IOException("Creation of " + path + " failed");
    }

    protected static void deleteDirectory(String path) {
        File file = new File(path);
        if(file.exists())
            if(file.isDirectory())
                cleanDirectory(file);
        file.delete();
    }
}
