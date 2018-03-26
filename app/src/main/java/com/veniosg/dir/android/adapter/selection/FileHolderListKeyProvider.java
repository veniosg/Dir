/*
 * Copyright (C) 2018 George Venios
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
package com.veniosg.dir.android.adapter.selection;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.veniosg.dir.android.adapter.FileHolderListAdapter;

import androidx.recyclerview.selection.ItemKeyProvider;

public final class FileHolderListKeyProvider extends ItemKeyProvider<Long> {
    private FileHolderListAdapter adapter;

    public FileHolderListKeyProvider(FileHolderListAdapter adapter) {
        super(SCOPE_MAPPED);
        this.adapter = adapter;
    }

    @Override
    @NonNull
    public Long getKey(int position) {
        return adapter.getItemId(position);
    }

    @Override
    public int getPosition(@NonNull Long key) {
        Integer position = adapter.getPosition(key);
        return position == null ? RecyclerView.NO_POSITION : position;
    }
}
