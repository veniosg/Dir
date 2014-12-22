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

import com.veniosg.dir.util.Utils;

import junit.framework.TestCase;

import java.io.File;

public class UtilsTest extends TestCase {
    public void testLastCommonDirectoryIndex() {
        File file1 = new File("/");
        File file2 = new File("/");
        assertEquals(0, Utils.lastCommonDirectoryIndex(file1, file2));

        // file1 = "/"
        file2 = new File("/sdcard/Pictures");
        assertEquals(0, Utils.lastCommonDirectoryIndex(file1, file2));

        file1 = new File("/sdcard");
        // file2 = "/sdcard/Pictures"
        assertEquals(1, Utils.lastCommonDirectoryIndex(file1, file2));

        file1 = new File("/emulated/storage/0/Pictures");
        file2 = new File("/emulated/storage/0/");
        assertEquals(3, Utils.lastCommonDirectoryIndex(file1, file2));

        file1 = new File("/emulated");
        file2 = new File("/");
        assertEquals(0, Utils.lastCommonDirectoryIndex(file1, file2));
    }
}
