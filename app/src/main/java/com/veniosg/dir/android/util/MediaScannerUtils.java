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

package com.veniosg.dir.android.util;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class MediaScannerUtils {
    private static final MediaScannerConnection.OnScanCompletedListener sLogScannerListener =
            new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {
                    Logger.logV(Logger.TAG_MEDIASCANNER, "Scanner connected");
                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Logger.logV(Logger.TAG_MEDIASCANNER, "Path: " + path + "\tUri: " + uri + " - scanned");
                }
            };

    private MediaScannerUtils() {}

	/**
	 * Request a MediaScanner scan for a single file.
	 */
	public static void informFileAdded(Context c, File f) {
		if (f == null)
			return;

        MediaScannerConnection.scanFile(c.getApplicationContext(), new String[]{f.getAbsolutePath()}, null,
                sLogScannerListener);
	}

    public static void informFolderAdded(Context c, File parentFile) {
        if (parentFile == null)
            return;

        ArrayList<String> filePaths = new ArrayList<>();
        getPathsUnder(filePaths, parentFile);

        MediaScannerConnection.scanFile(c.getApplicationContext(), filePaths.toArray(new String[filePaths.size()]), null,
                sLogScannerListener);
    }

    /**
     * Fills "paths" with the paths of all files contained in "from", recursively.
     * @param paths An initialized list instance.
     * @param from The root folder.
     */
    public static void getPathsOfFolder(@NonNull List<String> paths, @Nullable File from) {
        if (from == null)
            return;

        getPathsUnder(paths, from);
    }

    private static void getPathsUnder(@NonNull List<String> pathList, @NonNull File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    getPathsUnder(pathList, f);
                } else {
                    pathList.add(f.getAbsolutePath());
                }
            }
        }
        pathList.add(folder.getAbsolutePath());
    }

    public static void informFolderDeleted(Context c, File parentFile) {
        List<String> paths = new ArrayList<>();
        getPathsOfFolder(paths, parentFile);
        informPathsDeleted(c, paths);
    }

    public static void informPathsDeleted(Context c, List<String> paths) {
        DeleteTaskParams params = new DeleteTaskParams();
        params.context = c.getApplicationContext();
        params.paths = paths;

        new DeleteFromMediaStoreAsyncTask().execute(params);
    }

    private static Uri getFileContentUri(Context context, File file) {
        String filePath = file.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                new String[] { MediaStore.Files.FileColumns._ID },
                MediaStore.Files.FileColumns.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
            cursor.close();
            return MediaStore.Files.getContentUri("external", id);
        }

        return null;
    }

    private static class DeleteFromMediaStoreAsyncTask extends AsyncTask<DeleteTaskParams, Void, Void> {
        @Override
        protected Void doInBackground(DeleteTaskParams... params) {
            Context context = params[0].context.getApplicationContext();
            File file = params[0].file;
            List<String> paths = params[0].paths;

            if (paths == null) {
                safeDelete(context, file);
            } else {
                for (String path : paths) {
                    safeDelete(context, new File(path));
                }
            }

            return null;
        }

        private void safeDelete(Context context, File file) {
            Uri uri = getFileContentUri(context, file);
            if (uri != null) {
                context.getContentResolver().delete(uri, null, null);
            } else {
                Logger.logV(Logger.TAG_MEDIASCANNER, "Error in removing file at " + file.getAbsolutePath() + " from MediaStore");
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Logger.logV(Logger.TAG_MEDIASCANNER, "Async removal of references from MediaStore complete");
        }
    }

    private static class DeleteTaskParams {
        Context context;
        File file;
        List<String> paths;
    }
}