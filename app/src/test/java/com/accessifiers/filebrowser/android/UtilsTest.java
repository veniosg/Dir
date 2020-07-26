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

package com.accessifiers.filebrowser.android;

import junit.framework.TestCase;

import java.io.File;
import java.util.List;

import static com.accessifiers.filebrowser.android.util.Utils.firstDifferentItemIndex;
import static com.accessifiers.filebrowser.android.util.Utils.lastCommonDirectoryIndex;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class UtilsTest extends TestCase {
    public void testLastCommonDirectoryIndex() {
        File file1 = new File("/");
        File file2 = new File("/");
        assertEquals(0, lastCommonDirectoryIndex(file1, file2));

        file1 = new File("/");
        file2 = new File("/sdcard/Pictures");
        assertEquals(0, lastCommonDirectoryIndex(file1, file2));

        file1 = new File("/sdcard");
        file2 = new File("/sdcard/Pictures");
        assertEquals(1, lastCommonDirectoryIndex(file1, file2));

        file1 = new File("/emulated/storage/0/Pictures");
        file2 = new File("/emulated/storage/0/");
        assertEquals(3, lastCommonDirectoryIndex(file1, file2));

        file1 = new File("/emulated");
        file2 = new File("/");
        assertEquals(0, lastCommonDirectoryIndex(file1, file2));
    }

    @SuppressWarnings("unchecked")
    public void testFirstDifferentItemIndex() {
        List l1 = emptyList();
        List l2 = emptyList();
        assertEquals(-1, firstDifferentItemIndex(l1, l2));

        l1 = asList("1", "2", "3");
        l2 = asList("1", "2", "3");
        assertEquals(-1, firstDifferentItemIndex(l1, l2));

        l1 = emptyList();
        l2 = asList("1", "2", "3");
        assertEquals(0, firstDifferentItemIndex(l1, l2));

        l1 = asList("1", "2", "3");
        l2 = emptyList();
        assertEquals(0, firstDifferentItemIndex(l1, l2));

        l1 = asList("2", "3");
        l2 = asList("4", "5");
        assertEquals(0, firstDifferentItemIndex(l1, l2));

        l1 = singletonList("1");
        l2 = asList("1", "2", "3");
        assertEquals(1, firstDifferentItemIndex(l1, l2));

        l1 = asList("1", "2", "3");
        l2 = singletonList("1");
        assertEquals(1, firstDifferentItemIndex(l1, l2));

        l1 = asList("1", "5");
        l2 = asList("1", "2", "3", "4", "5");
        assertEquals(1, firstDifferentItemIndex(l1, l2));

        l1 = asList("1", "2", "3", "4", "5");
        l2 = asList("1", "5");
        assertEquals(1, firstDifferentItemIndex(l1, l2));
    }
}
