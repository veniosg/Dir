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

package com.veniosg.dir.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;

import com.veniosg.dir.FileManagerApplication;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.util.Utils;
import com.veniosg.dir.util.Utils;

import java.io.File;
import java.util.List;

/**
 * Synchronous query happens once query() is called.
 * This is non-persistent. All calls for CRUD are being ignored.
 */
public class SearchSuggestionsProvider extends ContentProvider {
    public static final String SEARCH_SUGGEST_MIMETYPE = "vnd.android.cursor.item/vnd.veniosg.search_suggestion";
    public static final String PROVIDER_NAME = "com.veniosg.dir.search.suggest";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);

    @Override
    /**
     * Always clears all suggestions. Parameters other than uri are ignored.
     */
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return SEARCH_SUGGEST_MIMETYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }

    @Override
    /**
     * Actual search happens here.
     */
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String query = uri.getLastPathSegment();
        File root = Environment.getExternalStorageDirectory();
        List<FileHolder> results = Utils.searchIn(root, query,
                ((FileManagerApplication) getContext().getApplicationContext())
                        .getMimeTypes(), getContext());

        MatrixCursor cursor = new MatrixCursor(new String[]{
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                BaseColumns._ID});
        for (FileHolder fh : results)
            cursor.newRow()
                    // TODO reenable this sometime later
//                    .add(Utils.getIconResourceForFile(
//                        ((FileManagerApplication) getContext().getApplicationContext())
//                                .getMimeTypes(),
//                        fh.getMimeType(),
//                        fh.getFile()))
                    .add(android.R.color.transparent)
                    .add(fh.getName())
                    .add(fh.getFile().getPath())
                    .add(fh.getFile().getAbsolutePath())
                    .add(fh.getFile().getAbsolutePath().hashCode());
        return cursor;
    }
}