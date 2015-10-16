/*
 * Copyright (C) 2012 OpenIntents.org
 * Copyright (C) 2014-2015 George Venios
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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.io.File;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.DIRECTORY_MOVIES;
import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.text.TextUtils.isEmpty;
import static java.lang.String.format;

public class BookmarkProvider extends ContentProvider implements BaseColumns {
    private static final String TB_NAME = "bookmarks";
    public static final String NAME = "name";
    public static final String PATH = "path";
    public static final String CHECKED = "checked"; // Only because of multiple choice delete dialog
    public static final String PROVIDER_NAME = "com.veniosg.dir.bookmarks";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);
    public static final String BOOKMARK_MIMETYPE = "vnd.android.cursor.item/vnd.veniosg.dir.bookmark";
    public static final String BOOKMARKS_MIMETYPE = "vnd.android.cursor.dir/vnd.veniosg.dir.bookmark";

    private static final int BOOKMARKS = 1;
    private static final int BOOKMARK_ID = 2;
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, null, BOOKMARKS);
        uriMatcher.addURI(PROVIDER_NAME, "#", BOOKMARK_ID);
    }

    private SQLiteDatabase db = null;

    private static final String DATABASE_CREATE = format(
            "CREATE TABLE %s (%s integer primary key autoincrement, %s text not null, %s text not null, %s integer default 0);",
            TB_NAME, _ID, NAME, PATH, CHECKED);

    private static final String DATABASE_NAME = "com.veniosg.dir.filemanager";
    private static final int DATABASE_VERSION = 3;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                db.execSQL(DATABASE_CREATE);
                db.insert(TB_NAME, null,
                        contentValuesFor(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)));
                db.insert(TB_NAME, null,
                        contentValuesFor(getExternalStoragePublicDirectory(DIRECTORY_MUSIC)));
                db.insert(TB_NAME, null,
                        contentValuesFor(getExternalStoragePublicDirectory(DIRECTORY_DCIM)));
                db.insert(TB_NAME, null,
                        contentValuesFor(getExternalStoragePublicDirectory(DIRECTORY_MOVIES)));
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }

        private ContentValues contentValuesFor(File file) {
            ContentValues values = new ContentValues();
            values.put(BookmarkProvider.NAME, file.getName());
            values.put(BookmarkProvider.PATH, file.getPath());
            return values;
        }

        /**
         * When changing database version, you MUST change this method.
         * Currently, it would delete all of the user's bookmarks.
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
            onCreate(db);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case BOOKMARKS:
                count = db.delete(TB_NAME, selection, selectionArgs);
                break;
            case BOOKMARK_ID:
                String id = uri.getPathSegments().get(0);
                String selectionTrain = isEmpty(selection) ? "" : " AND (" + selection + ')';
                count = db.delete(TB_NAME, _ID + " = " + id + selectionTrain, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOKMARKS:
                return BOOKMARKS_MIMETYPE;
            case BOOKMARK_ID:
                return BOOKMARK_MIMETYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long rowID = db.insert(TB_NAME, "", values);
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(_uri, null);
            }
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        return dbHelper.getWritableDatabase() != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(TB_NAME);
        if (uriMatcher.match(uri) == BOOKMARK_ID) {
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(0));
        }

        if (isEmpty(sortOrder)) {
            sortOrder = _ID;
        }

        Cursor c = sqlBuilder.query(db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        if (getContext() != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        switch (uriMatcher.match(uri)) {
            case BOOKMARKS:
                count = db.update(TB_NAME, values, selection, selectionArgs);
                break;
            case BOOKMARK_ID:
                String selectionTrain = isEmpty(selection) ? "" : " AND (" + selection + ')';
                count = db.update(TB_NAME, values,
                        _ID + " = " + uri.getPathSegments().get(0) + selectionTrain,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
