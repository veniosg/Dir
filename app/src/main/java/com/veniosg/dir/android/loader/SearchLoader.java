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

package com.veniosg.dir.android.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.veniosg.dir.android.FileManagerApplication;
import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.android.util.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author George Venios
 */
// TODO DELETE
public class SearchLoader extends AsyncTaskLoader<List<FileHolder>> {
    private List<FileHolder> mData;
    private File mRoot;
    private String mQuery;
    private Context mContext;

    public SearchLoader(Context context, File root, String query) {
        super(context);

        mRoot = root;
        mQuery = query;
        mContext = context;
    }

    @Override
    public List<FileHolder> loadInBackground() {
        return Collections.emptyList();/*Utils.searchIn(mRoot, Utils.newFilter(mQuery),
                ((FileManagerApplication) getContext().getApplicationContext())
                        .getMimeTypes(),
                mContext, 100);*/
    }

    @Override
    public void deliverResult(List<FileHolder> data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            releaseResources(data);
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        List<FileHolder> oldData = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is. Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        if (mData != null) {
            releaseResources(mData);
            mData = null;
        }
    }

    @Override
    public void onCanceled(List<FileHolder> data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }

    private void releaseResources(List<FileHolder> oldData) {
    }
}
