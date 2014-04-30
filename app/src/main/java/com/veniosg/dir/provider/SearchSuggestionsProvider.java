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
 *
 * @author George Venios
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
                        .getMimeTypes(), getContext()
        );

        MatrixCursor cursor = new MatrixCursor(new String[]{
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                BaseColumns._ID});
        for (FileHolder fh : results)
            cursor.newRow().add(Utils.getIconResourceForFile(
                    ((FileManagerApplication) getContext().getApplicationContext())
                            .getMimeTypes(),
                    fh.getMimeType(),
                    fh.getFile()
            ))
                    .add(fh.getName())
                    .add(fh.getFile().getPath())
                    .add(fh.getFile().getAbsolutePath())
                    .add(fh.getFile().getAbsolutePath().hashCode());
        return cursor;
    }
}