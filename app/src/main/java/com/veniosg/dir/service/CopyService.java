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

package com.veniosg.dir.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StatFs;

import com.veniosg.dir.fragment.FileListFragment;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.util.FileUtils;
import com.veniosg.dir.util.MediaScannerUtils;
import com.veniosg.dir.util.Notifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * To use, call the copyTo and moveTo static methods with the appropriate parameters.
 * <br/><br/><br/>
 * Internal usage instructions: <br/>
 * <ol>
 * <li>Pass the files to be copied/moved as a list of FileHolders on EXTRA_FILES.</li>
 * <li>Pass the path to copy/move to as the data string of the intent.</li>
 * <li>Choose between copy or move by using ACTION_COPY or ACTION_MOVE respectively.</li>
 * </ol>
 *
 * @author George Venios.
 */
public class CopyService extends IntentService {
    private static final int COPY_BUFFER_SIZE = 32 * 1024;

    private static final String ACTION_COPY = "com.veniosg.dir.action.COPY";
    private static final String ACTION_MOVE = "com.veniosg.dir.action.MOVE";
    private static final String EXTRA_FILES = "com.veniosg.dir.action.FILES";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CopyService() {
        super(CopyService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<FileHolder> files = intent.getParcelableArrayListExtra(EXTRA_FILES);
        File to = new File(intent.getData().getPath());
        long remSpace;

        if (ACTION_COPY.equals(intent.getAction())) {
            remSpace = spaceRemainingAfterCopy(files, to);
            if (remSpace > 0) {
                // Do copy.
                copy(files, to);
            }
        } else if (ACTION_MOVE.equals(intent.getAction())) {
            remSpace = spaceRemainingAfterMove(files, to);
            if (remSpace > 0) {
                // Do move.
                move(files, to);
            }
        } else {
            return;
        }

        if (remSpace > 0) {
            FileListFragment.refresh(this, to);
        } else {
            Notifier.showNotEnoughSpaceNotification(-remSpace, files, to.getPath(), this);
        }
    }

    private void copy(List<FileHolder> files, File to) {
        int fileCount = FileUtils.getFileCount(files);
        int filesCopied = 0;

        for (FileHolder fh : files) {
            File f = FileUtils.createUniqueCopyName(this, to, fh.getName());
            filesCopied = copyCore(filesCopied, fileCount, fh.getFile(), f, files.hashCode());

            if(fh.getFile().isDirectory()) {
                MediaScannerUtils.informFolderAdded(getApplicationContext(), f);
            } else {
                MediaScannerUtils.informFileAdded(getApplicationContext(), f);
            }
        }

        // Show copy result.
        Notifier.showCopyDoneNotification(filesCopied == fileCount, files.hashCode(), to.getPath(), this);
    }

    private boolean move(List<FileHolder> files, File to) {
        boolean res = true;
        int fileIndex = 0;
        boolean fileMoved;

        File from;
        File toFile;
        for (FileHolder fh : files) {
            Notifier.showMoveProgressNotification(fileIndex++, files, to.getPath(), this);

            from = fh.getFile().getAbsoluteFile();
            toFile = new File(to, fh.getName());

            List<String> paths = new ArrayList<String>();
            if (from.isDirectory()) {
                MediaScannerUtils.getPathsOfFolder(paths, from);
            }

            // Move
            fileMoved = fh.getFile().renameTo(toFile);

            // Inform media scanner
            if (fileMoved) {
                if (toFile.isDirectory()) {
                    MediaScannerUtils.informPathsDeleted(getApplicationContext(), paths);
                    MediaScannerUtils.informFolderAdded(getApplicationContext(), toFile);
                } else {
                    MediaScannerUtils.informFileDeleted(getApplicationContext(), from);
                    MediaScannerUtils.informFileAdded(getApplicationContext(), toFile);
                }
            }

            res &= fileMoved;
        }

        // Show copy result.
        Notifier.showMoveDoneNotification(res, files, to.getPath(), this);

        return res;
    }

    /**
     * Copy a file.
     *
     * @param filesCopied Initial value of how many files have been copied.
     * @param oldFile File to copy.
     * @param newFile The file to be created.
     * @param notId The id for the progress notification.
     * @return The new filesCopied count.
     */
    private int internalCopyFile(int filesCopied, int fileCount, File oldFile, File newFile, int notId) {
        Notifier.showCopyProgressNotification(filesCopied, fileCount, notId, newFile, this);

        try {
            FileInputStream input = new FileInputStream(oldFile);
            FileOutputStream output = new FileOutputStream(newFile);

            byte[] buffer = new byte[COPY_BUFFER_SIZE];

            while (true) {
                int bytes = input.read(buffer);

                if (bytes <= 0) {
                    break;
                }

                output.write(buffer, 0, bytes);
            }

            output.close();
            input.close();

        } catch (Exception e) {
            return filesCopied;
        }
        return filesCopied+1;
    }

    /**
     * Recursively copy a folder.
     *
     * @param filesCopied Initial value of how many files have been copied.
     * @param oldFile Folder to copy.
     * @param newFile The dir to be created.
     * @param notId The id for the progress notification.
     * @return The new filesCopied count.
     */
    private int copyCore(int filesCopied, int fileCount, File oldFile, File newFile,
                         int notId) {
        if (oldFile.isDirectory()) {
            // if directory not exists, create it
            if (!newFile.exists()) {
                newFile.mkdir();
            }

            // list all the directory contents
            String files[] = oldFile.list();

            for (String file : files) {
                // construct the src and dest file structure
                File srcFile = new File(oldFile, file);
                File destFile = new File(newFile, file);
                // recursive copy
                filesCopied = copyCore(filesCopied, fileCount, srcFile, destFile, notId);
            }
        } else {
            filesCopied = internalCopyFile(filesCopied, fileCount, oldFile, newFile, notId);
        }

        return filesCopied;
    }

    public static void copyTo(Context c, List<FileHolder> mClipboard, File copyTo) {
        Intent i = new Intent(ACTION_COPY);
        i.setClassName(c, CopyService.class.getName());
        i.setData(Uri.fromFile(copyTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, mClipboard instanceof ArrayList
                ? (ArrayList<FileHolder>) mClipboard
                : new ArrayList<FileHolder>(mClipboard));
        c.startService(i);
    }

    public static void moveTo(Context c, List<FileHolder> mClipboard, File moveTo) {
        Intent i = new Intent(ACTION_MOVE);
        i.setClassName(c, CopyService.class.getName());
        i.setData(Uri.fromFile(moveTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, mClipboard instanceof ArrayList
                ? (ArrayList<FileHolder>) mClipboard
                : new ArrayList<FileHolder>(mClipboard));
        c.startService(i);
    }

    private static long spaceRemainingAfterCopy(List<FileHolder> of, File on) {
        long needed = 0;

        for (FileHolder f : of) {
            needed += (f.getFile().isDirectory() ? FileUtils.folderSize(f.getFile()) : f.getFile().length());
        }

        return on.getUsableSpace() - needed;
    }

    private static long spaceRemainingAfterMove(List<FileHolder> of, File on) {
        long needed = 0;
        // We know all clipboard files to be on the same directory.
        boolean onSameStorage = onSameStorage(of.get(0).getFile(), on);

        for (FileHolder f : of) {
            if (!onSameStorage) {
                needed += (f.getFile().isDirectory() ? FileUtils.folderSize(f.getFile()) : f.getFile().length());
            }
        }

        return on.getUsableSpace() - needed;
    }

    /**
     * Bad but good enough for the common case. Someone, please write something better :)
     * @return Whether these two files are on the same storage card/disk.
     * May return false positives but never false negatives.
     */
    private static boolean onSameStorage(File file1, File file2) {
        StatFs fs1 = new StatFs(file1.getAbsolutePath());
        StatFs fs2 = new StatFs(file2.getAbsolutePath());
        return fs1.getAvailableBlocks() == fs2.getAvailableBlocks()
                && fs1.getBlockCount() == fs2.getBlockCount()
                && fs1.getFreeBlocks() == fs2.getFreeBlocks()
                && fs1.getBlockSize() == fs2.getBlockSize();
    }
}
