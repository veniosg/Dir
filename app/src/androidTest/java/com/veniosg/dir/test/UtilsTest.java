package com.veniosg.dir.test;

import com.veniosg.dir.util.Utils;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author George Venios
 */
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
