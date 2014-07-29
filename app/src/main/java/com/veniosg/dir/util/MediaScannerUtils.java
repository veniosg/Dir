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

package com.veniosg.dir.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

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

        ArrayList<String> filePaths = new ArrayList<String>();
        informFolderAddedCore(filePaths, parentFile);

        MediaScannerConnection.scanFile(c.getApplicationContext(), filePaths.toArray(new String[filePaths.size()]), null,
                sLogScannerListener);
    }

    /**
     * Fills "paths" with the paths of all files contained in "from", recursively.
     * @param paths An initialized list instance.
     * @param from The root folder.
     */
    public static void getPathsOfFolder(List<String> paths, File from) {
        if (from == null)
            return;

        informFolderAddedCore(paths, from);
    }

    private static void informFolderAddedCore(List<String> pathList, File folder) {
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                informFolderAddedCore(pathList, f);
            } else {
                pathList.add(f.getAbsolutePath());
            }
        }
        pathList.add(folder.getAbsolutePath());
    }
	
	public static void informFileDeleted(Context c, File f) {
        Uri fileUri = getImageContentUri(c, f);
        c.getContentResolver().delete(fileUri, null, null);

//        informFileAdded(c, f);
	}

    public static void informFolderDeleted(Context c, File parentFile) {
        informFolderAdded(c, parentFile);
    }

    public static void informPathsDeleted(Context c, List<String> paths) {
        MediaScannerConnection.scanFile(c.getApplicationContext(), paths.toArray(new String[paths.size()]), null,
                sLogScannerListener);
    }


    private static Uri getImageContentUri(Context context, File file) {
        String filePath = file.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}