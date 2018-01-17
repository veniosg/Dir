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

package com.veniosg.dir.mvvm.model.storage.operation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;

import com.veniosg.dir.android.fragment.FileListFragment;
import com.veniosg.dir.android.util.MediaScannerUtils;
import com.veniosg.dir.android.util.Notifier;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.mvvm.model.storage.operation.argument.ExtractArguments;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.veniosg.dir.android.util.Logger.log;
import static com.veniosg.dir.android.util.Notifier.clearNotification;
import static com.veniosg.dir.android.util.Notifier.showExtractProgressNotification;
import static com.veniosg.dir.android.util.Utils.getLastPathSegment;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.createDirectory;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.createFile;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.outputStreamFor;
import static com.veniosg.dir.mvvm.model.storage.DocumentFileUtils.safAwareDelete;

public class ExtractOperation extends FileOperation<ExtractArguments> {
    private static final int BUFFER_SIZE = 1024;

    private final Context context;

    public ExtractOperation(Context context) {
        this.context = context;
    }

    @Override
    public boolean operate(ExtractArguments args) {
        return new NormalExtractor().extract(args);
    }

    @Override
    public boolean operateSaf(ExtractArguments args) {
        return new SafExtractor().extract(args);
    }

    @Override
    public void onStartOperation(ExtractArguments args) {
    }

    @Override
    public void onResult(boolean success, ExtractArguments args) {
        File to = args.getTarget();
        if (!success) safAwareDelete(context, to);

        MediaScannerUtils.informFileAdded(context, to);
        Notifier.showExtractDoneNotification(success, id, to, context);
        FileListFragment.refresh(context, to.getParentFile());
    }

    @Override
    public void onAccessDenied() {
    }

    @Override
    public void onRequestingAccess() {
        clearNotification(id, context);
    }

    @Override
    public boolean needsWriteAccess() {
        return true;
    }

    private abstract class Extractor {
        boolean extract(ExtractArguments args) {
            List<FileHolder> zipHolders = args.getZipFiles();
            File dstDirectory = args.getTarget();
            List<ZipFile> zipFiles;
            try {
                zipFiles = fileHoldersToZipFiles(zipHolders);
            } catch (IOException e) {
                log(e);
                return false;
            }
            int fileCount = entriesIn(zipFiles);
            int extractedCount = 0;

            for (ZipFile zipFile : zipFiles) {
                for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();

                    showExtractProgressNotification(extractedCount, fileCount,
                            getLastPathSegment(entry.getName()),
                            getLastPathSegment(zipFile.getName()),
                            id, context);

                    boolean extractSuccessful = extractEntry(zipFile, entry, dstDirectory);
                    if (!extractSuccessful) return false;
                    extractedCount++;
                }
            }

            return true;
        }

        private boolean extractEntry(ZipFile zipFile, ZipEntry zipEntry, File outputDir) {
            if (zipEntry.isDirectory()) {
                return createDir(new File(outputDir, zipEntry.getName()));
            }
            File outputFile = new File(outputDir, zipEntry.getName());
            if (!outputFile.getParentFile().exists()) {
                boolean parentCreated = createDir(outputFile.getParentFile());
                if (!parentCreated) return false;
            }

            try (
                    BufferedInputStream inputStream =
                            new BufferedInputStream(zipFile.getInputStream(zipEntry));
                    BufferedOutputStream outputStream =
                            new BufferedOutputStream(outputStream(outputFile))
            ) {
                int len;
                byte buf[] = new byte[BUFFER_SIZE];
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                //noinspection ResultOfMethodCallIgnored
                outputFile.setLastModified(zipEntry.getTime());
            } catch (IOException e) {
                log(e);
                return false;
            }

            return true;
        }

        private List<ZipFile> fileHoldersToZipFiles(List<FileHolder> files) throws IOException {
            List<ZipFile> zips = new ArrayList<>(files.size());

            for (FileHolder fh : files) {
                zips.add(new ZipFile(fh.getFile()));
            }

            return zips;
        }

        private int entriesIn(List<ZipFile> zipFiles) {
            int count = 0;
            for (ZipFile z : zipFiles) count += z.size();
            return count;
        }

        abstract boolean createDir(File dir);

        @NonNull
        abstract OutputStream outputStream(File outputFile) throws FileNotFoundException;
    }

    private class NormalExtractor extends Extractor {
        @Override
        public boolean createDir(File dir) {
            return dir.exists() || dir.mkdirs();
        }

        @Override
        @NonNull
        public OutputStream outputStream(File outputFile) throws FileNotFoundException {
            return new FileOutputStream(outputFile);
        }
    }

    private class SafExtractor extends Extractor {
        @Override
        boolean createDir(File dir) {
            return dir.exists() || createDirectory(context, dir) != null;
        }

        @NonNull
        @Override
        OutputStream outputStream(File outputFile) throws FileNotFoundException, NullPointerException {
            DocumentFile toSaf = createFile(context, outputFile, "application/zip");
            return outputStreamFor(toSaf, context);
        }
    }
}
