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

package com.veniosg.dir.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.veniosg.dir.android.fragment.FileListFragment;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.android.util.FileUtils;
import com.veniosg.dir.android.util.Logger;
import com.veniosg.dir.android.util.MediaScannerUtils;
import com.veniosg.dir.android.util.Notifier;
import com.veniosg.dir.android.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author George Venios
 */
public class ZipService extends IntentService {
    private static final int BUFFER_SIZE = 1024;

    private static final String ACTION_COMPRESS = "com.veniosg.dir.action.COMPRESS";
    private static final String ACTION_EXTRACT = "com.veniosg.dir.action.EXTRACT";
    private static final String EXTRA_FILES = "com.veniosg.dir.action.FILES";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public ZipService() {
        super(ZipService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<FileHolder> files = intent.getParcelableArrayListExtra(EXTRA_FILES);
        File to = new File(intent.getData().getPath());

        if (ACTION_COMPRESS.equals(intent.getAction())) {
            try {
                compress(files, to);
            } catch (Exception e) {
                // Cleanup
                to.delete();

                Logger.log(e);
                Notifier.showCompressDoneNotification(false, files.hashCode(), to, this);
            }
        } else if (ACTION_EXTRACT.equals(intent.getAction())) {
            try {
                extract(files, to);
            } catch (Exception e) {
                // Cleanup
                FileUtils.deleteDirectory(to);

                Logger.log(e);
                Notifier.showExtractDoneNotification(false, files.hashCode(), to, this);
            }
        }
    }

    private void extract(List<FileHolder> files, File to) throws IOException {
        List<ZipFile> zipFiles = fileHoldersToZipFiles(files);
        int fileCount = countFilesInZip(zipFiles);
        int extractedCount = 0;

        for (ZipFile zipFile : zipFiles) {
            for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                Notifier.showExtractProgressNotification(extractedCount, fileCount,
                        Utils.getLastPathSegment(entry.getName()),
                        Utils.getLastPathSegment(zipFile.getName()),
                        files.hashCode(), this);

                extractCore(zipFile, entry, to);
                extractedCount++;
            }
        }

        MediaScannerUtils.informFolderAdded(this, to);
        Notifier.showExtractDoneNotification(true, files.hashCode(), to, this);
        FileListFragment.refresh(this, to.getParentFile());
    }

    private void extractCore(ZipFile zipfile, ZipEntry entry,
                             File outputDir) throws IOException {
        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }
        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            int len;
            byte buf[] = new byte[BUFFER_SIZE];
            while ((len = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    /**
     * @param list The files to compress.
     * @param to The zip file to create.
     */
    private void compress(List<FileHolder> list, File to) throws IOException, NullPointerException {
        int fileCount = FileUtils.getFileCount(list);
        int filesCompressed = 0;
        ZipOutputStream zipStream = new ZipOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(to)));

        for (FileHolder file : list) {
            filesCompressed = compressCore(list.hashCode(), zipStream, file.getFile(),
                    null, filesCompressed, fileCount, to);
        }

        zipStream.flush();
        zipStream.close();

        MediaScannerUtils.informFileAdded(getApplicationContext(), to);
        Notifier.showCompressDoneNotification(true, list.hashCode(), to, this);
        FileListFragment.refresh(this, to.getParentFile());
    }

    /**
     * Recursively compress a File.
     * @return How many files where compressed.
     */
    private int compressCore(int notId, ZipOutputStream zipStream, File toCompress, String internalPath,
                             int filesCompressed, final int fileCount, File zipFile) throws IOException {
        Notifier.showCompressProgressNotification(
                filesCompressed, fileCount, notId, zipFile, toCompress, this);
        if (internalPath == null)
            internalPath = "";

        if (toCompress.isFile()) {
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            FileInputStream in = new FileInputStream(toCompress);

            // Create internal zip file entry.
            if (internalPath.length() > 0)
                zipStream.putNextEntry(new ZipEntry(internalPath + "/" + toCompress.getName()));
            else {
                zipStream.putNextEntry(new ZipEntry(toCompress.getName()));
            }

            // Compress
            while ((len = in.read(buf)) > 0) {
                zipStream.write(buf, 0, len);
            }

            filesCompressed++;
            zipStream.closeEntry();
            in.close();
        } else {
            if (toCompress.list().length == 0) {
                zipStream.putNextEntry(new ZipEntry(internalPath + "/" + toCompress.getName() + "/"));
                zipStream.closeEntry();
            } else {
                for (File child : toCompress.listFiles()) {
                    filesCompressed = compressCore(notId, zipStream, child,
                            internalPath + "/" + toCompress.getName(),
                            filesCompressed, fileCount, zipFile);
                }
            }
        }

        return filesCompressed;
    }

    private int countFilesInZip(List<ZipFile> zipFiles) {
        int count = 0;

        for (ZipFile z : zipFiles) {
            count += z.size();
        }

        return count;
    }

    private static void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    private static List<ZipFile> fileHoldersToZipFiles(List<FileHolder> files) throws IOException {
        List<ZipFile> zips = new ArrayList<ZipFile>(files.size());

        for (FileHolder fh : files) {
            zips.add(new ZipFile(fh.getFile()));
        }

        return zips;
    }

    public static void extractTo(Context c, List<FileHolder> tbe, File extractTo) {
        Intent i = new Intent(ACTION_EXTRACT);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(extractTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, tbe instanceof ArrayList
                ? (ArrayList<FileHolder>) tbe
                : new ArrayList<FileHolder>(tbe));
        c.startService(i);
    }

    public static void compressTo(Context c, List<FileHolder> tbc, File compressTo) {
        Intent i = new Intent(ACTION_COMPRESS);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(compressTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, tbc instanceof ArrayList
                ? (ArrayList<FileHolder>) tbc
                : new ArrayList<FileHolder>(tbc));
        c.startService(i);
    }

    public static void extractTo(final Context c, final FileHolder tbe, File extractTo) {
        final Intent i = new Intent(ACTION_EXTRACT);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(extractTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, new ArrayList<FileHolder>(){{add(tbe);}});
        c.startService(i);
    }

    public static void compressTo(Context c, final FileHolder tbc, File compressTo) {
        Intent i = new Intent(ACTION_COMPRESS);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(compressTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, new ArrayList<FileHolder>(){{add(tbc);}});
        c.startService(i);
    }
}
